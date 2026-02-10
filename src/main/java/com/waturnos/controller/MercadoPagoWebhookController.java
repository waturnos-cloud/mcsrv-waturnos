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
	    log.error("RAW BODY: [{}]", rawBody);
	    // Caso 1 — Webhook NUEVO con body firmado
	    if (rawBody != null && !rawBody.isBlank()) {
	        JsonNode node = objectMapper.readTree(rawBody);
	        paymentId = node.path("data").path("id").asText(null);

	        boolean valid = webhookService.validateAndProcessWebhook(xSignature, xRequestId, paymentId);
	        if (!valid) return ResponseEntity.status(401).build();
	    }
	    // Caso 2 — Webhook viejo (sin firma)
	    else if (id != null) {
	        paymentId = id; // procesás pero NO validás firma
	        log.error("Legacy webhook without signature");
	    }

	    webhookService.processPaymentNotification(paymentId);
	    return ResponseEntity.ok().build();
	}
}
