package com.waturnos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	
	private final ObjectMapper objectMapper;
	
	/**
	 * Endpoint para recibir notificaciones IPN de MercadoPago.
	 * MercadoPago llama a este endpoint cuando hay cambios en un pago.
	 *
	 * @param payload el cuerpo de la notificación
	 * @param topic el tipo de notificación (payment, merchant_order, etc.)
	 * @param xSignature header con la firma para validar autenticidad
	 * @param xRequestId header con el ID de request
	 * @return respuesta 200 OK para confirmar recepción
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping("/mercadopago")
	public ResponseEntity<String> handleWebhook(
	        @RequestHeader(value = "x-signature", required = false) String xSignature,
	        @RequestHeader(value = "x-request-id", required = false) String xRequestId,
	        @RequestBody(required = false) String rawBody,
	        @RequestParam(required = false) String id) throws JsonMappingException, JsonProcessingException {

	    String paymentId = null;
	    log.info("🔔 Webhook received - xSignature: {}, xRequestId: {}, queryId: {}", xSignature, xRequestId, id);
	    log.info("🔔 RAW BODY: [{}]", rawBody);
	    
	    // Caso 1 — Webhook con body
	    if (rawBody != null && !rawBody.isBlank()) {
	        JsonNode node = objectMapper.readTree(rawBody);
	        
	        // Formato nuevo: {"action":"payment.created","data":{"id":"123"}}
	        paymentId = node.path("data").path("id").asText(null);
	        
	        // Formato legacy: {"resource":"123","topic":"payment"}
	        if (paymentId == null || paymentId.isBlank()) {
	            paymentId = node.path("resource").asText(null);
	            log.info("🔔 Payment ID extracted from 'resource' field (legacy format): {}", paymentId);
	        } else {
	            log.info("🔔 Payment ID extracted from 'data.id' field (new format): {}", paymentId);
	        }

	        // Validar firma si está presente
	        if (xSignature != null && !xSignature.isBlank()) {
	            boolean valid = webhookService.validateAndProcessWebhook(xSignature, xRequestId, paymentId);
	            if (!valid) {
	                log.warn("🔔 ⚠️ Webhook signature validation failed");
	                return ResponseEntity.status(401).build();
	            }
	        } else {
	            log.info("🔔 No signature provided, skipping validation");
	        }
	    }
	    // Caso 2 — Webhook con query param (muy legacy)
	    else if (id != null) {
	        paymentId = id;
	        log.info("🔔 Payment ID from query param (legacy): {}", paymentId);
	    }

	    // Validar que tenemos un payment ID antes de procesar
	    if (paymentId == null || paymentId.isBlank() || "null".equals(paymentId)) {
	        log.error("🔔 ❌ No payment ID found in webhook request. Cannot process.");
	        return ResponseEntity.badRequest().body("Missing payment ID");
	    }

	    log.info("🔔 Processing payment notification for ID: {}", paymentId);
	    webhookService.processPaymentNotification(paymentId);
	    return ResponseEntity.ok().build();
	}
}
