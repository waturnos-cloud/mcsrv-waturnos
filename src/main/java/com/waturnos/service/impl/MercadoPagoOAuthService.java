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
			log.info("🔵 ========== INICIANDO OAUTH FLOW DE MERCADOPAGO ==========");
			log.info("🔐 User ID: {}", userId);
			log.info("🔐 Authorization Code: {}...", code.substring(0, Math.min(20, code.length())));
			
			// Usar redirect URI personalizada o la configurada
			String effectiveRedirectUri = customRedirectUri != null ? customRedirectUri : redirectUri;
			log.info("🔐 Redirect URI: {}", effectiveRedirectUri);
			log.info("🔐 App ID: {}", appId);
			
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
			String refreshToken = jsonResponse.path("refresh_token").asText();
			long expiresIn = jsonResponse.path("expires_in").asLong();
			boolean liveMode = jsonResponse.path("live_mode").asBoolean(false);
			
			log.info("🔐 OAuth Response:");
			log.info("🔐   - Access Token: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));
			log.info("🔐   - Public Key: {}", publicKey);
			log.info("🔐   - User ID (MP): {}", userId_mp);
			log.info("🔐   - Live Mode (from OAuth): {}", liveMode);
			log.info("🔐   - Expires In: {} seconds", expiresIn);
			
			if (accessToken.isEmpty() || publicKey.isEmpty()) {
				log.error("🔐 ❌ Invalid OAuth response - missing accessToken or publicKey");
				throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Invalid OAuth response from MercadoPago");
			}
			
			log.info("🔐 ✅ OAuth successful - User: {}, MP User ID: {}", userId, userId_mp);
			
			// Guardar credenciales usando el servicio de payment providers
			log.warn("⚠️ ⚠️ ⚠️ IMPORTANTE: OAuth está guardando sandboxMode=FALSE (hardcoded) ⚠️ ⚠️ ⚠️");
			log.warn("⚠️ Esto puede causar que las credenciales sandbox se usen en producción!");
			log.warn("⚠️ liveMode del OAuth response: {}", liveMode);
			
			// FIXME: Esto debería determinarse dinámicamente basado en el tipo de credenciales
			// Las credenciales de prueba empiezan con TEST, las de producción con APP_USR
			boolean isSandbox = publicKey.startsWith("TEST-") || publicKey.startsWith("APP_USR-");
			log.info("🔐 Detectado tipo de credencial - isSandbox: {} (basado en publicKey: {})", isSandbox, publicKey.substring(0, Math.min(15, publicKey.length())));
			
			AddPaymentRequest paymentRequest = AddPaymentRequest.builder()
					.type(PaymentProviderType.MERCADO_PAGO)
					.accessToken(accessToken)
					.publicKey(publicKey)
					.accountId(userId_mp)
					.sandboxMode(!liveMode) // Usar el liveMode del OAuth response
					.build();
			
			log.info("🔐 Guardando Payment Provider con:");
			log.info("🔐   - sandboxMode: {}", paymentRequest.getSandboxMode());
			log.info("🔐   - liveMode (OAuth): {}", liveMode);
			
			paymentProviderService.addPaymentProvider(userId, paymentRequest);
			
			log.info("🔐 ✅ MercadoPago credentials saved successfully for user {}", userId);
			log.info("🔵 ========== FIN OAUTH FLOW ==========");
			
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in OAuth flow for user {}", userId, e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "OAuth flow failed: " + e.getMessage());
		}
	}
}
