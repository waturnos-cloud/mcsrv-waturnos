package com.waturnos.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.request.AddPaymentRequest;
import com.waturnos.dto.response.PaymentProviderResponse;
import com.waturnos.enums.PaymentProviderType;
import com.waturnos.repository.UserPropsRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.service.PaymentProviderService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de proveedores de pago.
 * Utiliza users_props para almacenar la configuración sin necesidad de una entidad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProviderServiceImpl implements PaymentProviderService {
	
	private final UserPropsRepository userPropsRepository;
	private final UserRepository userRepository;
	private final SecurityAccessEntity securityAccessEntity;
	private final ObjectMapper objectMapper;
	
	private static final String PAYMENT_PREFIX = "payment.";
	
	@Override
	@Transactional
	public void addPaymentProvider(Long userId, AddPaymentRequest request) {
		// Verificar que el usuario existe
		if (!userRepository.existsById(userId)) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		
		// Verificar acceso (solo el mismo usuario o admin puede modificar)
		securityAccessEntity.controlAccessToUserId(userId);
		
		String keyPrefix = PAYMENT_PREFIX + request.getType().name().toLowerCase();
		
		// Construir JSON con los datos del proveedor
		Map<String, String> paymentData = new HashMap<>();
		if (request.getAccessToken() != null) {
			paymentData.put("accessToken", request.getAccessToken());
		}
		if (request.getPublicKey() != null) {
			paymentData.put("publicKey", request.getPublicKey());
		}
		if (request.getAccountId() != null) {
			paymentData.put("accountId", request.getAccountId());
		}
		if (request.getWebhookUrl() != null) {
			paymentData.put("webhookUrl", request.getWebhookUrl());
		}
		if (request.getSandboxMode() != null) {
			paymentData.put("sandboxMode", request.getSandboxMode().toString());
		}
		
		try {
			String jsonValue = objectMapper.writeValueAsString(paymentData);
			
			// Verificar si ya existe la configuración
			if (userPropsRepository.existsUserProp(userId, keyPrefix)) {
				// Actualizar
				userPropsRepository.updateUserProp(userId, keyPrefix, jsonValue);
				log.info("Updated payment provider {} for user {}", request.getType(), userId);
			} else {
				// Insertar
				userPropsRepository.insertUserProp(userId, keyPrefix, jsonValue);
				log.info("Added payment provider {} for user {}", request.getType(), userId);
			}
			
		} catch (JsonProcessingException e) {
			log.error("Error serializing payment data", e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Error processing payment data");
		}
	}
	
	@Override
	public PaymentProviderResponse getPaymentProvider(Long userId, PaymentProviderType type) {
		// Verificar acceso
//		securityAccessEntity.controlAccessToUserId(userId);
		
		String keyPrefix = PAYMENT_PREFIX + type.name().toLowerCase();
		
		Optional<String> valueOpt = userPropsRepository.findUserPropValue(userId, keyPrefix);
		
		if (valueOpt.isEmpty()) {
			return PaymentProviderResponse.builder()
					.type(type)
					.isConfigured(false)
					.build();
		}
		
		try {
			String jsonValue = valueOpt.get();
			@SuppressWarnings("unchecked")
			Map<String, String> paymentData = objectMapper.readValue(jsonValue, Map.class);
			
			return PaymentProviderResponse.builder()
					.type(type)
					.accessToken(paymentData.get("accessToken"))
					.publicKey(paymentData.get("publicKey"))
					.accountId(paymentData.get("accountId"))
					.webhookUrl(paymentData.get("webhookUrl"))
					.sandboxMode(paymentData.get("sandboxMode") != null ? Boolean.valueOf(paymentData.get("sandboxMode")) : null)
					.isConfigured(true)
					.build();
			
		} catch (JsonProcessingException e) {
			log.error("Error deserializing payment data", e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Error reading payment data");
		}
	}
	
	@Override
	public List<PaymentProviderResponse> getAllPaymentProviders(Long userId) {
		// Verificar acceso hay que arreglar esto para el cliente no anda
//		securityAccessEntity.controlAccessToUserId(userId);
		
		List<PaymentProviderResponse> providers = new ArrayList<>();
		
		// Iterar sobre todos los tipos de proveedores de pago
		for (PaymentProviderType type : PaymentProviderType.values()) {
			PaymentProviderResponse provider = getPaymentProvider(userId, type);
			
			// Solo agregar si está configurado
			if (provider.getIsConfigured()) {
				providers.add(provider);
			}
		}
		
		return providers;
	}
	
	@Override
	@Transactional
	public void removePaymentProvider(Long userId, PaymentProviderType type) {
		// Verificar acceso
		securityAccessEntity.controlAccessToUserId(userId);
		
		String keyPrefix = PAYMENT_PREFIX + type.name().toLowerCase();
		
		int deleted = userPropsRepository.deleteUserProp(userId, keyPrefix);
		
		if (deleted > 0) {
			log.info("Removed payment provider {} for user {}", type, userId);
		}
	}
}
