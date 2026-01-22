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
import com.waturnos.repository.BookingPropsRepository;
import com.waturnos.service.BookingService;
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
	private final BookingPropsRepository bookingPropsRepository;
	private final BookingService bookingService;
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
			log.info("🔔 ========== INICIANDO PROCESAMIENTO DE WEBHOOK MERCADOPAGO ==========");
			log.info("🔔 Payment ID: {}", paymentId);
			
			// 1. Consultar el pago en MercadoPago para verificar su estado
			JsonNode paymentData = getPaymentFromMercadoPago(paymentId);
			
			if (paymentData == null) {
				log.warn("🔔 ⚠️ Could not retrieve payment data from MercadoPago for ID: {}", paymentId);
				log.info("🔔 ========== FIN PROCESAMIENTO (SIN DATA) ==========");
				return;
			}
			
			// 2. Extraer información del pago
			String status = paymentData.path("status").asText();
			String externalReference = paymentData.path("external_reference").asText(); // Booking ID
			String statusDetail = paymentData.path("status_detail").asText();
			
			log.info("🔔 Payment Data:");
			log.info("🔔   - Status: {}", status);
			log.info("🔔   - Status Detail: {}", statusDetail);
			log.info("🔔   - External Reference (Booking ID): {}", externalReference);
			
			if (externalReference == null || externalReference.isEmpty()) {
				log.warn("🔔 ⚠️ Payment {} has no external_reference (booking ID)", paymentId);
				log.info("🔔 ========== FIN PROCESAMIENTO (SIN EXTERNAL REF) ==========");
				return;
			}
			
			// 3. Buscar la reserva por ID
			Long bookingId;
			try {
				bookingId = Long.parseLong(externalReference);
				log.info("🔔 Booking ID parsed: {}", bookingId);
			} catch (NumberFormatException e) {
				log.error("🔔 ❌ Invalid booking ID in external_reference: {}", externalReference);
				log.info("🔔 ========== FIN PROCESAMIENTO (INVALID BOOKING ID) ==========");
				return;
			}
			
			log.info("🔔 Searching for booking with ID: {}", bookingId);
			Booking booking = bookingRepository.findById(bookingId).orElse(null);
			if (booking == null) {
				log.warn("🔔 ⚠️ Booking not found for ID: {}", bookingId);
				log.info("🔔 ========== FIN PROCESAMIENTO (BOOKING NOT FOUND) ==========");
				return;
			}
			
			log.info("🔔 Booking found - Current status: {}", booking.getStatus());
			log.info("🔔 Booking has {} client(s) assigned", booking.getBookingClients().size());
			
			// 4. Actualizar el estado de la reserva según el estado del pago
			BookingStatus newStatus = mapPaymentStatusToBookingStatus(status);
			log.info("🔔 Mapped status '{}' to BookingStatus: {}", status, newStatus);
			
			if (newStatus != null && booking.getStatus() != newStatus) {
				log.info("🔔 Updating booking {} status from {} to {}", bookingId, booking.getStatus(), newStatus);
				booking.setStatus(newStatus);
				bookingRepository.save(booking);
				log.info("🔔 ✅ Booking status updated successfully");
				
				// 5. Si el pago fue aprobado y aún no hay cliente asignado, 
				// buscar el clientId en las booking props y asignar
				if ("approved".equals(status) && booking.getBookingClients().isEmpty()) {
					log.info("🔔 Payment approved and no clients assigned yet. Looking for clientId in booking props...");
					try {
						// Buscar el clientId guardado en las props
						var clientIdProp = bookingPropsRepository.findByBookingIdAndPropKey(bookingId, "_clientId");
						if (clientIdProp.isPresent()) {
							Long clientIdToAssign = Long.parseLong(clientIdProp.get().getPropValue());
							log.info("🔔 Found clientId {} in booking props. Assigning to booking...", clientIdToAssign);
							bookingService.assignBookingToClient(bookingId, clientIdToAssign);
							log.info("🔔 ✅ Client assigned to booking via webhook");
						} else {
							log.warn("🔔 ⚠️ No _clientId found in booking props for booking {}", bookingId);
						}
					} catch (Exception e) {
						log.error("🔔 ❌ Error assigning client to booking {}", bookingId, e);
					}
				}
				
				// TODO: Enviar notificación al cliente sobre el estado del pago
			} else if (newStatus == null) {
				log.warn("🔔 ⚠️ No status mapping found for MercadoPago status: {}", status);
			} else {
				log.info("🔔 Booking status unchanged (already {})", booking.getStatus());
			}
			
			log.info("🔔 ========== FIN PROCESAMIENTO WEBHOOK (SUCCESS) ==========");
			
		} catch (Exception e) {
			log.error("🔔 ❌ Error processing payment notification for ID: {}", paymentId, e);
			log.info("🔔 ========== FIN PROCESAMIENTO WEBHOOK (ERROR) ==========");
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
			log.info("🔔 Fetching payment data from: {}", url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
			headers.set("Content-Type", "application/json");
			
			HttpEntity<String> entity = new HttpEntity<>(headers);
			
			log.info("🔔 Calling MercadoPago API...");
			ResponseEntity<String> response = restTemplate.exchange(
					url, 
					HttpMethod.GET, 
					entity, 
					String.class);
			
			log.info("🔔 MercadoPago API response status: {}", response.getStatusCode());
			
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				log.info("🔔 ✅ Payment data retrieved successfully");
				JsonNode paymentData = objectMapper.readTree(response.getBody());
				log.info("🔔 Payment JSON response: {}", paymentData.toPrettyString());
				return paymentData;
			} else {
				log.warn("🔔 ⚠️ Unexpected response from MercadoPago: {}", response.getStatusCode());
			}
			
		} catch (Exception e) {
			log.error("🔔 ❌ Error calling MercadoPago API for payment {}", paymentId, e);
			log.error("🔔 Error details: {}", e.getMessage());
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
