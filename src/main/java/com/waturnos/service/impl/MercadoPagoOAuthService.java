package com.waturnos.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.request.AddPaymentRequest;
import com.waturnos.enums.PaymentProviderType;
import com.waturnos.service.PaymentProviderService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para manejar el flujo OAuth de MercadoPago.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoOAuthService {
	
	private final PaymentProviderService paymentProviderService;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	@Value("${mercadopago.app-id}")
	private String appId;
	
	@Value("${mercadopago.app-secret}")
	private String appSecret;
	
	@Value("${mercadopago.oauth-redirect-uri}")
	private String redirectUri;
	
	private static final String OAUTH_TOKEN_URL = "https://api.mercadopago.com/oauth/token";
	
	/**
	 * Intercambia el código de autorización por un access token.
	 *
	 * @param userId el ID del usuario
	 * @param code el código de autorización de MercadoPago
	 * @param customRedirectUri redirect URI personalizada (opcional)
	 */
	public void exchangeCodeForToken(Long userId, String code, String customRedirectUri) {
		try {
			log.info("Exchanging OAuth code for access token - User: {}", userId);
			
			// Usar redirect URI personalizada o la configurada
			String effectiveRedirectUri = customRedirectUri != null ? customRedirectUri : redirectUri;
			
			// Preparar request para MercadoPago
			Map<String, String> requestBody = new HashMap<>();
			requestBody.put("grant_type", "authorization_code");
			requestBody.put("client_id", appId);
			requestBody.put("client_secret", appSecret);
			requestBody.put("code", code);
			requestBody.put("redirect_uri", effectiveRedirectUri);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
			
			// Llamar a MercadoPago OAuth API
			ResponseEntity<String> response = restTemplate.postForEntity(
					OAUTH_TOKEN_URL, 
					entity, 
					String.class);
			
			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("MercadoPago OAuth failed with status: {}", response.getStatusCode());
				throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Failed to exchange OAuth code");
			}
			
			// Parsear respuesta
			JsonNode jsonResponse = objectMapper.readTree(response.getBody());
			
			String accessToken = jsonResponse.path("access_token").asText();
			String publicKey = jsonResponse.path("public_key").asText();
			String userId_mp = jsonResponse.path("user_id").asText();
			
			if (accessToken.isEmpty() || publicKey.isEmpty()) {
				throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Invalid OAuth response from MercadoPago");
			}
			
			log.info("OAuth successful - User: {}, MP User ID: {}", userId, userId_mp);
			
			// Guardar credenciales usando el servicio de payment providers
			AddPaymentRequest paymentRequest = AddPaymentRequest.builder()
					.type(PaymentProviderType.MERCADO_PAGO)
					.accessToken(accessToken)
					.publicKey(publicKey)
					.accountId(userId_mp)
					.sandboxMode(false) // OAuth siempre es producción
					.build();
			
			paymentProviderService.addPaymentProvider(userId, paymentRequest);
			
			log.info("MercadoPago credentials saved successfully for user {}", userId);
			
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in OAuth flow for user {}", userId, e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "OAuth flow failed: " + e.getMessage());
		}
	}
}
