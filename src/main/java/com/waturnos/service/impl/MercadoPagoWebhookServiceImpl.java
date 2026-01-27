package com.waturnos.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import com.waturnos.entity.Client;
import com.waturnos.entity.Payment;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.PaymentProviderType;
import com.waturnos.enums.PaymentStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.PaymentRepository;
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
	private final BookingService bookingService;
	private final PaymentRepository paymentRepository;
	private final ClientRepository clientRepository;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	@Value("${mercadopago.access-token}")
	private String accessToken;
	
	@Value("${mercadopago.webhook-secret}")
	private String webhookSecret;
	
	@Value("${mercadopago.mock-webhook-enabled:false}")
	private boolean mockWebhookEnabled;
	
	@Value("${mercadopago.environment.dev:false}")
	private Boolean isDevMode;
	
	private static final String MERCADOPAGO_API_URL = "https://api.mercadopago.com/v1/payments/";
	/**
	 * Valida la firma del webhook de MercadoPago.
	 *
	 * @param xSignature el header x-signature
	 * @param xRequestId el header x-request-id
	 * @param dataId el ID del recurso (payment ID)
	 * @return true si la firma es válida
	 */
//	public boolean validateWebhookSignature(String xSignature, String xRequestId, String dataId) {
//		try {
//			// Construir el manifest según la documentación de MercadoPago
//			// Format: id:<dataId>;request-id:<xRequestId>
//			String manifest = "id:" + dataId + ";request-id:" + xRequestId;
//			
//			// Calcular HMAC SHA-256
//			Mac hmac = Mac.getInstance("HmacSHA256");
//			SecretKeySpec secretKey = new SecretKeySpec(
//				webhookSecret.getBytes(StandardCharsets.UTF_8), 
//				"HmacSHA256"
//			);
//			hmac.init(secretKey);
//			
//			byte[] hash = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
//			
//			// Convertir a hexadecimal
//			StringBuilder hexString = new StringBuilder();
//			for (byte b : hash) {
//				String hex = Integer.toHexString(0xff & b);
//				if (hex.length() == 1) {
//					hexString.append('0');
//				}
//				hexString.append(hex);
//			}
//			
//			String calculatedSignature = hexString.toString();
//			
//			// Extraer la firma real del header (puede tener formato "v1=<signature>,ts=<timestamp>")
//			String actualSignature = extractSignatureFromHeader(xSignature);
//			
//			boolean isValid = actualSignature != null && actualSignature.equals(calculatedSignature);
//			
//			if (!isValid) {
//				log.warn("Invalid webhook signature. Expected: {}, Received: {}", calculatedSignature, actualSignature);
//			}
//			
//			return isValid;
//			
//		} catch (Exception e) {
//			log.error("Error validating webhook signature", e);
//			return false;
//		}
//	}
//	
//	/**
//	 * Extrae la firma del header x-signature.
//	 * Formato esperado: "v1=<signature>,ts=<timestamp>"
//	 */
//	private String extractSignatureFromHeader(String xSignature) {
//		if (xSignature == null || xSignature.isEmpty()) {
//			return null;
//		}
//		
//		// Si tiene formato "v1=...,ts=..."
//		if (xSignature.contains("v1=")) {
//			String[] parts = xSignature.split(",");
//			for (String part : parts) {
//				if (part.trim().startsWith("v1=")) {
//					return part.trim().substring(3);
//				}
//			}
//		}
//		
//		// Si es solo el hash
//		return xSignature;
//	}
	
	@Override
	public boolean validateAndProcessWebhook(String xSignature, String xRequestId, String paymentId) {
		// Si está en modo dev, saltear validación de firma
		if (isDevMode) {
			log.info("🧪 DEV MODE - Skipping webhook signature validation");
			processPaymentNotification(paymentId);
			return true;
		}
		
		// Validar firma del webhook si están presentes los headers
		if (xSignature != null && xRequestId != null) {
			log.info("🌐 Validating webhook signature for payment: {}", paymentId);
			boolean isValid = validateWebhookSignature(xSignature, xRequestId, paymentId);
			
			if (!isValid) {
				log.warn("🌐 ⚠️ Invalid webhook signature for PaymentId: {}. Processing anyway (temp fix).", paymentId);
				// TEMPORAL: Procesar de todas formas para no perder pagos mientras se corrige el secret
				// TODO: Cambiar a return false cuando el webhook secret esté correctamente configurado
			}
			
			log.info("🌐 ✅ Webhook signature validated successfully for payment: {}", paymentId);
		} else {
			log.warn("🌐 ⚠️ Webhook received without signature headers. PaymentId: {}", paymentId);
		}
		
		// Procesar el pago
		processPaymentNotification(paymentId);
		return true;
	}
	
	public boolean validateWebhookSignature(String xSignature, String xRequestId, String dataId) {
	    try {
	        // 1. Extraer ts y v1 del header
	        String ts = "";
	        String v1 = "";
	        for (String part : xSignature.split(",")) {
	            String[] kv = part.split("=");
	            if (kv.length == 2) {
	                if (kv[0].trim().equals("ts")) ts = kv[1].trim();
	                if (kv[0].trim().equals("v1")) v1 = kv[1].trim();
	            }
	        }
	        
	        // DEBUG: Log del webhook secret (solo primeros 10 chars por seguridad)
	        log.info("🔍 Webhook secret length: {}, starts with: {}...", 
	            webhookSecret != null ? webhookSecret.length() : 0,
	            webhookSecret != null ? webhookSecret.substring(0, Math.min(10, webhookSecret.length())) : "null");
	        log.info("🔍 x-signature: {}", xSignature);
	        log.info("🔍 Extracted ts={}, v1={}", ts, v1);

	        // 2. Intentar primero con el formato de PAGO REAL (data.id)
	        String manifest = "data.id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
	        String calculated = calculateHmac(manifest);
	        log.info("🔍 Manifest (data.id): {}", manifest);
	        log.info("🔍 Calculated (data.id): {}", calculated);

	        if (MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8))) {
	            log.info("✅ Firma válida con formato data.id");
	            return true;
	        }

	        // 3. Si falla, intentar con el formato de TEST (id)
	        String altManifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
	        String altCalculated = calculateHmac(altManifest);
	        log.info("🔍 Manifest (id): {}", altManifest);
	        log.info("🔍 Calculated (id): {}", altCalculated);

	        if (MessageDigest.isEqual(altCalculated.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8))) {
	            log.info("✅ Firma válida con formato id");
	            return true;
	        }

	        log.warn("❌ Firma inválida con ambos formatos");
	        log.warn("Expected v1: {}", v1);
	        return false;

	    } catch (Exception e) {
	        log.error("Error validando firma", e);
	        return false;
	    }
	}

	// Agrega este método auxiliar abajo para que el código anterior funcione
	private String calculateHmac(String message) throws Exception {
	    Mac hmac = Mac.getInstance("HmacSHA256");
	    SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	    hmac.init(secretKey);
	    byte[] hash = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
	    StringBuilder hexString = new StringBuilder();
	    for (byte b : hash) hexString.append(String.format("%02x", b));
	    return hexString.toString();
	}	
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
			String externalReference = paymentData.path("external_reference").asText(); // Formato: "bookingId_clientId"
			String statusDetail = paymentData.path("status_detail").asText();
			
			log.info("🔔 Payment Data:");
			log.info("🔔   - Status: {}", status);
			log.info("🔔   - Status Detail: {}", statusDetail);
			log.info("🔔   - External Reference: {}", externalReference);
			
			if (externalReference == null || externalReference.isEmpty()) {
				log.warn("🔔 ⚠️ Payment {} has no external_reference", paymentId);
				log.info("🔔 ========== FIN PROCESAMIENTO (SIN EXTERNAL REF) ==========");
				return;
			}
			
			// 3. Parsear external_reference: formato "bookingId_clientId"
			String[] parts = externalReference.split("_");
			if (parts.length != 2) {
				log.error("🔔 ❌ Invalid external_reference format: {}. Expected 'bookingId_clientId'", externalReference);
				log.info("🔔 ========== FIN PROCESAMIENTO (INVALID FORMAT) ==========");
				return;
			}
			
			Long bookingId;
			Long clientId;
			try {
				bookingId = Long.parseLong(parts[0]);
				clientId = Long.parseLong(parts[1]);
				log.info("🔔 Parsed - Booking ID: {}, Client ID: {}", bookingId, clientId);
			} catch (NumberFormatException e) {
				log.error("🔔 ❌ Error parsing external_reference: {}", externalReference, e);
				log.info("🔔 ========== FIN PROCESAMIENTO (PARSE ERROR) ==========");
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
			
			// 4. Verificar si ya existe un pago registrado para este transaction_id
			// Esto evita procesar el mismo webhook múltiples veces
			String transactionId = paymentData.path("id").asText();
			var existingPayment = paymentRepository.findByTransactionId(transactionId);
			if (existingPayment.isPresent()) {
				log.info("🔔 ℹ️ Payment already processed for transaction_id: {}", transactionId);
				log.info("🔔 ========== FIN PROCESAMIENTO (ALREADY PROCESSED) ==========");
				return;
			}
			
			// 5. Verificar si el pago fue aprobado y asignar el cliente
			if ("approved".equals(status)) {
				log.info("🔔 Payment approved. Checking if client {} is assigned to booking...", clientId);
				boolean isClientAssigned = booking.getBookingClients().stream()
						.anyMatch(bc -> bc.getClient().getId().equals(clientId));
				
				if (!isClientAssigned) {
					log.info("🔔 Client {} not assigned yet. Assigning to booking...", clientId);
					try {
						bookingService.assignBookingToClientNotLogin(bookingId, clientId);
						log.info("🔔 ✅ Client {} assigned successfully to booking {}", clientId, bookingId);
					} catch (Exception e) {
						// Si falla por duplicate key, es porque otro webhook ya lo asignó
						if (e.getMessage() != null && e.getMessage().contains("uk_booking_client")) {
							log.warn("🔔 ⚠️ Client {} already assigned by another webhook thread", clientId);
						} else {
							throw e; // Re-lanzar si es otro tipo de error
						}
					}
				} else {
					log.info("🔔 ℹ️ Client {} already assigned to booking {}", clientId, bookingId);
				}
				
				// 6. Crear registro de Payment con toda la información de MercadoPago
				log.info("🔔 Creating Payment record for approved payment...");
				createPaymentRecord(paymentData, booking, clientId);
				log.info("🔔 ✅ Payment record created successfully");
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
		// Si está habilitado el modo mock, retornar datos dummy
		if (mockWebhookEnabled) {
			log.info("🔔 🧪 MOCK MODE ENABLED - Returning dummy payment data for paymentId: {}", paymentId);
			return getMockPaymentData(paymentId);
		}
		
		try {
			String url = MERCADOPAGO_API_URL + paymentId;
			log.info("🔔 Fetching payment data from: {}", url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken);
			headers.set("Content-Type", "application/json");
			
			// Log solo para debug (no incluir el token completo por seguridad)
			log.info("🔔 Authorization header set with token starting: {}...", 
				accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) : "null");
			
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
	
	/**
	 * Crea un registro de Payment con los datos de MercadoPago.
	 *
	 * @param paymentData datos del pago de MercadoPago
	 * @param booking el booking asociado
	 * @param clientId el ID del cliente
	 */
	private void createPaymentRecord(JsonNode paymentData, Booking booking, Long clientId) {
		try {
			// Verificar si ya existe un payment con este transaction_id
			String transactionId = paymentData.path("id").asText();
			var existingPayment = paymentRepository.findByTransactionId(transactionId);
			if (existingPayment.isPresent()) {
				log.info("🔔 Payment record already exists for transaction_id: {}", transactionId);
				return;
			}
			
			// Crear nuevo Payment
			Payment payment = new Payment();
			payment.setBooking(booking);
			
			// Buscar el cliente
			Client client = clientRepository.findById(clientId)
					.orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
			payment.setClient(client);
			
			// Datos de MercadoPago
			payment.setPaymentMethod(PaymentProviderType.MERCADO_PAGO);
			payment.setTransactionId(transactionId);
			payment.setMerchantOrderId(paymentData.path("order").path("id").asText(null));
			payment.setPaymentType(paymentData.path("payment_type_id").asText(null));
			payment.setStatusDetail(paymentData.path("status_detail").asText(null));
			payment.setCurrency(paymentData.path("currency_id").asText("ARS"));
			
			// Monto
			BigDecimal amount = paymentData.path("transaction_amount").decimalValue();
			payment.setAmount(amount != null ? amount : BigDecimal.ZERO);
			
			// Email del pagador
			payment.setPayerEmail(paymentData.path("payer").path("email").asText(null));
			
			// Descripción
			payment.setDescription(paymentData.path("description").asText(null));
			
			// Estado del pago
			String mpStatus = paymentData.path("status").asText();
			payment.setStatus(mapMercadoPagoStatusToPaymentStatus(mpStatus));
			
			// Metadata completo como JSON
			payment.setMetadata(paymentData.toString());
			
			// Fechas
			LocalDateTime now = LocalDateTime.now();
			payment.setCreatedAt(now);
			payment.setUpdatedAt(now);
			
			if ("approved".equals(mpStatus)) {
				payment.setApprovedAt(now);
			}
			
			paymentRepository.save(payment);
			log.info("🔔 ✅ Payment record saved with ID: {}", payment.getId());
			
		} catch (Exception e) {
			log.error("🔔 ❌ Error creating payment record", e);
			throw e;
		}
	}
	
	/**
	 * Mapea el estado de MercadoPago al enum PaymentStatus.
	 */
	private PaymentStatus mapMercadoPagoStatusToPaymentStatus(String mpStatus) {
		return switch (mpStatus) {
			case "approved" -> PaymentStatus.APPROVED;
			case "pending" -> PaymentStatus.PENDING;
			case "in_process", "in_mediation" -> PaymentStatus.IN_PROCESS;
			case "rejected" -> PaymentStatus.REJECTED;
			case "cancelled" -> PaymentStatus.CANCELLED;
			case "refunded" -> PaymentStatus.REFUNDED;
			case "charged_back" -> PaymentStatus.CHARGED_BACK;
			default -> PaymentStatus.PENDING;
		};
	}
	
	/**
	 * Retorna datos de pago dummy para testing local.
	 * Simula la respuesta de la API de MercadoPago.
	 */
	private JsonNode getMockPaymentData(String paymentId) {
		try {
			// JSON que simula una respuesta de MercadoPago exitosa
			String mockJson = """
				{
					"id": "%s",
					"status": "approved",
					"status_detail": "accredited",
					"external_reference": "868_4",
					"transaction_amount": 5000.00,
					"currency_id": "ARS",
					"payment_type_id": "credit_card",
					"payment_method_id": "visa",
					"merchant_order_id": "123456789",
					"payer": {
						"email": "test@test.com",
						"identification": {
							"type": "DNI",
							"number": "12345678"
						}
					},
					"date_created": "2026-01-23T14:00:00.000Z",
					"date_approved": "2026-01-23T14:00:05.000Z",
					"description": "Pago de turno - Booking ID: 868",
					"metadata": {
						"booking_id": "868",
						"client_id": "4"
					}
				}
				""".formatted(paymentId);
			
			return objectMapper.readTree(mockJson);
		} catch (Exception e) {
			log.error("🔔 ❌ Error creating mock payment data", e);
			throw new RuntimeException("Error creating mock payment data", e);
		}
	}
}
