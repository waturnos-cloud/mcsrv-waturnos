package com.waturnos.service.impl;

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
