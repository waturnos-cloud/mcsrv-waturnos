package com.waturnos.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	 * @param id el ID del recurso notificado
	 * @return respuesta 200 OK para confirmar recepción
	 */
	@PostMapping("/mercadopago")
	public ResponseEntity<String> handleMercadoPagoWebhook(
			@RequestBody(required = false) Map<String, Object> payload,
			@RequestParam(required = false) String topic,
			@RequestParam(required = false) String id) {
		
		try {
			log.info("Received MercadoPago webhook - Topic: {}, ID: {}, Payload: {}", topic, id, payload);
			
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
			
			if (paymentId == null || paymentId.isEmpty()) {
				log.warn("Received webhook without payment ID");
				return ResponseEntity.ok("OK");
			}
			
			// Procesar la notificación según el tipo
			if ("payment".equals(notificationType)) {
				webhookService.processPaymentNotification(paymentId);
			} else {
				log.info("Webhook type {} not processed", notificationType);
			}
			
			// Siempre retornar 200 OK para que MercadoPago no reintente
			return ResponseEntity.ok("OK");
			
		} catch (Exception e) {
			log.error("Error processing MercadoPago webhook", e);
			// Retornar 200 para evitar reintentos innecesarios
			return ResponseEntity.ok("ERROR");
		}
	}
	
	/**
	 * Endpoint para probar el webhook manualmente.
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
}
