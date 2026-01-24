package com.waturnos.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.service.MercadoPagoWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para recibir notificaciones de MercadoPago.
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {
	
	private final MercadoPagoWebhookService webhookService;
	
	/**
	 * Endpoint para recibir notificaciones IPN de MercadoPago.
	 * MercadoPago llama a este endpoint cuando hay cambios en un pago.
	 *
	 * @param payload el cuerpo de la notificación
	 * @param topic el tipo de notificación (payment, merchant_order, etc.)
	 * @param xSignature header con la firma para validar autenticidad
	 * @param xRequestId header con el ID de request
	 * @return respuesta 200 OK para confirmar recepción
	 */
	@PostMapping("/mercadopago")
	public ResponseEntity<String> handleMercadoPagoWebhook(
			@RequestBody(required = false) Map<String, Object> payload,
			@RequestParam(required = false) String topic,
			@RequestParam(required = false) String id,
			@RequestHeader(value = "x-signature", required = false) String xSignature,
			@RequestHeader(value = "x-request-id", required = false) String xRequestId) {
		
		try {
			log.info("🌐 ========== WEBHOOK MERCADOPAGO RECIBIDO ==========");
			log.info("🌐 Topic: {}", topic);
			log.info("🌐 ID: {}", id);
			log.info("🌐 Payload: {}", payload);
			log.info("🌐 Headers - x-signature: {}", xSignature != null ? "present" : "absent");
			log.info("🌐 Headers - x-request-id: {}", xRequestId);
			
			// MercadoPago puede enviar notificaciones de diferentes formas:
			// 1. Query params: ?topic=payment&id=123456
			// 2. Body JSON: {"action":"payment.updated","data":{"id":"123456"}}
			
			String paymentId = null;
			String notificationType = topic;
			
			// Extraer payment ID del payload si viene en el body
			if (payload != null && !payload.isEmpty()) {
				if (payload.containsKey("data")) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) payload.get("data");
					if (data != null && data.containsKey("id")) {
						paymentId = data.get("id").toString();
					}
				}
				if (payload.containsKey("type")) {
					notificationType = payload.get("type").toString();
				}
			}
			
			// Si viene por query param, usar ese ID
			if (id != null && !id.isEmpty()) {
				paymentId = id;
			}
			
			log.info("🌐 Extracted - Payment ID: {}, Notification Type: {}", paymentId, notificationType);
			
			if (paymentId == null || paymentId.isEmpty()) {
				log.warn("🌐 ⚠️ Received webhook without payment ID");
				log.info("🌐 ========== FIN WEBHOOK (SIN PAYMENT ID) ==========");
				return ResponseEntity.ok("OK");
			}
			
			// Procesar la notificación según el tipo
			log.info("🌐 Processing notification type: {}", notificationType);
			if ("payment".equals(notificationType)) {
				log.info("🌐 Calling webhookService.validateAndProcessWebhook({})...", paymentId);
				boolean processed = webhookService.validateAndProcessWebhook(xSignature, xRequestId, paymentId);
				
				if (!processed) {
					log.error("🌐 ❌ Webhook validation failed or processing error");
					log.info("🌐 ========== FIN WEBHOOK (VALIDATION FAILED) ==========");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
				}
			} else {
				log.info("🌐 ⚠️ Webhook type {} not processed", notificationType);
			}
			
			// Siempre retornar 200 OK para que MercadoPago no reintente
			log.info("🌐 ========== FIN WEBHOOK (SUCCESS) ==========");
			return ResponseEntity.ok("OK");
			
		} catch (Exception e) {
			log.error("🌐 ❌ Error processing MercadoPago webhook", e);
			log.error("🌐 Error details: {}", e.getMessage());
			log.info("🌐 ========== FIN WEBHOOK (ERROR) ==========");
			// Retornar 200 para evitar reintentos innecesarios
			return ResponseEntity.ok("ERROR");
		}
	}
	
	/**
	 * Endpoint para probar el webhook manualmente con paymentId.
	 * Solo para desarrollo/testing.
	 *
	 * @param paymentId el ID del pago a procesar
	 * @return resultado del procesamiento
	 */
	@PostMapping("/mercadopago/test")
	public ResponseEntity<String> testWebhook(@RequestParam String paymentId) {
		try {
			webhookService.processPaymentNotification(paymentId);
			return ResponseEntity.ok("Webhook test completed for payment: " + paymentId);
		} catch (Exception e) {
			log.error("Error in webhook test", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Endpoint para simular webhooks de MercadoPago sin validación de firma.
	 * Útil para testing con la herramienta de simulación de webhooks de MercadoPago.
	 * Solo para desarrollo/testing.
	 *
	 * @param payload el cuerpo de la notificación de MercadoPago
	 * @return respuesta 200 OK
	 */
	@PostMapping("/mercadopago/simulate")
	public ResponseEntity<String> simulateWebhook(@RequestBody Map<String, Object> payload) {
		try {
			log.info("🧪 ========== WEBHOOK SIMULADO (SIN VALIDACIÓN DE FIRMA) ==========");
			log.info("🧪 Payload recibido: {}", payload);
			
			// Extraer payment ID del payload
			String paymentId = null;
			if (payload.containsKey("data")) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) payload.get("data");
				if (data != null && data.containsKey("id")) {
					paymentId = data.get("id").toString();
				}
			}
			
			if (paymentId == null || paymentId.isEmpty()) {
				log.warn("🧪 ⚠️ Payload simulado sin payment ID");
				return ResponseEntity.ok("OK - No payment ID");
			}
			
			log.info("🧪 Processing simulated webhook for payment: {}", paymentId);
			
			// Procesar sin validar firma (solo para testing)
			webhookService.processPaymentNotification(paymentId);
			
			log.info("🧪 ========== FIN WEBHOOK SIMULADO ==========");
			return ResponseEntity.ok("OK");
			
		} catch (Exception e) {
			log.error("🧪 ❌ Error processing simulated webhook", e);
			return ResponseEntity.ok("ERROR - " + e.getMessage());
		}
	}
}
