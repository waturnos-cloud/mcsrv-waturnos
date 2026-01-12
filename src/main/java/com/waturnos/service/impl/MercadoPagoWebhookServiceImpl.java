package com.waturnos.service.impl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.service.MercadoPagoWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de webhooks de MercadoPago.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookServiceImpl implements MercadoPagoWebhookService {
	
	private final BookingRepository bookingRepository;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	@Value("${mercadopago.access-token}")
	private String accessToken;
	
	@Value("${mercadopago.webhook-secret}")
	private String webhookSecret;
	/**
	 * Valida la firma del webhook de MercadoPago.
	 *
	 * @param xSignature el header x-signature
	 * @param xRequestId el header x-request-id
	 * @param dataId el ID del recurso (payment ID)
	 * @return true si la firma es válida
	 */
	public boolean validateWebhookSignature(String xSignature, String xRequestId, String dataId) {
		try {
			// Construir el manifest según la documentación de MercadoPago
			// Format: id:<dataId>;request-id:<xRequestId>
			String manifest = "id:" + dataId + ";request-id:" + xRequestId;
			
			// Calcular HMAC SHA-256
			Mac hmac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(
				webhookSecret.getBytes(StandardCharsets.UTF_8), 
				"HmacSHA256"
			);
			hmac.init(secretKey);
			
			byte[] hash = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
			
			// Convertir a hexadecimal
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			
			String calculatedSignature = hexString.toString();
			
			// Extraer la firma real del header (puede tener formato "v1=<signature>,ts=<timestamp>")
			String actualSignature = extractSignatureFromHeader(xSignature);
			
			boolean isValid = actualSignature != null && actualSignature.equals(calculatedSignature);
			
			if (!isValid) {
				log.warn("Invalid webhook signature. Expected: {}, Received: {}", calculatedSignature, actualSignature);
			}
			
			return isValid;
			
		} catch (Exception e) {
			log.error("Error validating webhook signature", e);
			return false;
		}
	}
	
	/**
	 * Extrae la firma del header x-signature.
	 * Formato esperado: "v1=<signature>,ts=<timestamp>"
	 */
	private String extractSignatureFromHeader(String xSignature) {
		if (xSignature == null || xSignature.isEmpty()) {
			return null;
		}
		
		// Si tiene formato "v1=...,ts=..."
		if (xSignature.contains("v1=")) {
			String[] parts = xSignature.split(",");
			for (String part : parts) {
				if (part.trim().startsWith("v1=")) {
					return part.trim().substring(3);
				}
			}
		}
		
		// Si es solo el hash
		return xSignature;
	}
	
	
	private static final String MERCADOPAGO_API_URL = "https://api.mercadopago.com/v1/payments/";
	
	@Override
	@Transactional
	public void processPaymentNotification(String paymentId) {
		try {
			log.info("Processing payment notification for payment ID: {}", paymentId);
			
			// 1. Consultar el pago en MercadoPago para verificar su estado
			JsonNode paymentData = getPaymentFromMercadoPago(paymentId);
			
			if (paymentData == null) {
				log.warn("Could not retrieve payment data from MercadoPago for ID: {}", paymentId);
				return;
			}
			
			// 2. Extraer información del pago
			String status = paymentData.path("status").asText();
			String externalReference = paymentData.path("external_reference").asText(); // Booking ID
			
			log.info("Payment {} status: {}, external_reference: {}", paymentId, status, externalReference);
			
			if (externalReference == null || externalReference.isEmpty()) {
				log.warn("Payment {} has no external_reference (booking ID)", paymentId);
				return;
			}
			
			// 3. Buscar la reserva por ID
			Long bookingId;
			try {
				bookingId = Long.parseLong(externalReference);
			} catch (NumberFormatException e) {
				log.error("Invalid booking ID in external_reference: {}", externalReference);
				return;
			}
			
			Booking booking = bookingRepository.findById(bookingId).orElse(null);
			if (booking == null) {
				log.warn("Booking not found for ID: {}", bookingId);
				return;
			}
			
			// 4. Actualizar el estado de la reserva según el estado del pago
			BookingStatus newStatus = mapPaymentStatusToBookingStatus(status);
			
			if (newStatus != null && booking.getStatus() != newStatus) {
				booking.setStatus(newStatus);
				bookingRepository.save(booking);
				log.info("Updated booking {} status to {}", bookingId, newStatus);
				
				// TODO: Enviar notificación al cliente sobre el estado del pago
			}
			
		} catch (Exception e) {
			log.error("Error processing payment notification for ID: {}", paymentId, e);
		}
	}
	
	/**
	 * Consulta el pago en la API de MercadoPago.
	 *
	 * @param paymentId el ID del pago
	 * @return los datos del pago en formato JSON
	 */
	private JsonNode getPaymentFromMercadoPago(String paymentId) {
		try {
			String url = MERCADOPAGO_API_URL + paymentId;
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken);
			headers.set("Content-Type", "application/json");
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			
			ResponseEntity<String> response = restTemplate.exchange(
					url, 
					HttpMethod.GET, 
					entity, 
					String.class);
			
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return objectMapper.readTree(response.getBody());
			}
			
		} catch (Exception e) {
			log.error("Error calling MercadoPago API for payment {}", paymentId, e);
		}
		
		return null;
	}
	
	/**
	 * Mapea el estado de pago de MercadoPago al estado de reserva.
	 *
	 * @param mpStatus el estado en MercadoPago
	 * @return el estado de booking correspondiente
	 */
	private BookingStatus mapPaymentStatusToBookingStatus(String mpStatus) {
		return switch (mpStatus) {
			case "approved" -> BookingStatus.CONFIRMED;
			case "pending", "in_process" -> BookingStatus.PENDING;
			case "rejected", "cancelled" -> BookingStatus.CANCELLED;
			case "refunded", "charged_back" -> BookingStatus.CANCELLED;
			default -> null;
		};
	}
}
