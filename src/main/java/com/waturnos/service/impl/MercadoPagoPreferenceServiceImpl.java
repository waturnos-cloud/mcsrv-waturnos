package com.waturnos.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.waturnos.dto.request.CreatePreferenceRequest;
import com.waturnos.dto.response.PaymentPreferenceResponse;
import com.waturnos.dto.response.PaymentProviderResponse;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.enums.PaymentProviderType;
import com.waturnos.repository.BookingRepository;
import com.waturnos.service.MercadoPagoPreferenceService;
import com.waturnos.service.PaymentProviderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio para gestionar preferencias de pago de MercadoPago.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoPreferenceServiceImpl implements MercadoPagoPreferenceService {
	
	private final BookingRepository bookingRepository;
	private final PaymentProviderService paymentProviderService;
	private final RestTemplate restTemplate;
	
	@Value("${mercadopago.webhook-url}")
	private String webhookUrl;
	
	@Value("${server.servlet.context-path:}")
	private String contextPath;
	
	@Value("${mercadopago.frontend.base.url}")
	private String frontendBaseUrl;
	
	private static final String MERCADOPAGO_API_URL = "https://api.mercadopago.com/checkout/preferences";
	
	@Override
	public PaymentPreferenceResponse createPreference(CreatePreferenceRequest request) {
		log.info("Creando preferencia de pago para booking ID: {}", request.getBookingId());
		
		// 1. Obtener el booking
		Booking booking = bookingRepository.findById(request.getBookingId())
				.orElseThrow(() -> new IllegalArgumentException("Booking no encontrado con ID: " + request.getBookingId()));
		
		// 2. Obtener el servicio asociado al booking
		ServiceEntity service = booking.getService();
		if (service == null || service.getUser() == null) {
			throw new IllegalArgumentException("El booking no tiene un servicio o usuario asociado válido");
		}
		
		Long providerId = service.getUser().getId();
		log.info("Provider ID obtenido del servicio: {}", providerId);
		
		// 3. Obtener la configuración de MercadoPago del provider
		PaymentProviderResponse paymentConfig = paymentProviderService.getPaymentProvider(
				providerId, 
				PaymentProviderType.MERCADO_PAGO
		);
		
		if (paymentConfig == null || !paymentConfig.getIsConfigured()) {
			throw new IllegalStateException("El provider no tiene configurado MercadoPago");
		}
		
		// 4. Construir el cuerpo de la preferencia
		Map<String, Object> preferenceData = buildPreferenceData(request, booking);
		
		// 5. Llamar a la API de MercadoPago
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(paymentConfig.getAccessToken());
			
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(preferenceData, headers);
			
			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
					MERCADOPAGO_API_URL,
					HttpMethod.POST,
					entity,
					Map.class
			);
			
			Map<String, Object> responseBody = response.getBody();
			if (responseBody == null) {
				throw new RuntimeException("Respuesta vacía de MercadoPago");
			}
			
			log.info("Preferencia creada exitosamente: {}", responseBody.get("id"));
			
			return PaymentPreferenceResponse.builder()
					.preferenceId((String) responseBody.get("id"))
					.initPoint((String) responseBody.get("init_point"))
					.sandboxInitPoint((String) responseBody.get("sandbox_init_point"))
					.build();
			
		} catch (Exception e) {
			log.error("Error al crear preferencia en MercadoPago", e);
			throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Construye el cuerpo de la petición para crear la preferencia en MercadoPago.
	 */
	private Map<String, Object> buildPreferenceData(CreatePreferenceRequest request, Booking booking) {
		Map<String, Object> preference = new HashMap<>();
		
		// External reference: permite rastrear el pago en el webhook
		preference.put("external_reference", request.getBookingId().toString());
		
		// Items (productos/servicios)
		Map<String, Object> item = new HashMap<>();
		item.put("title", request.getDescription() != null ? request.getDescription() : "Reserva de turno");
		item.put("quantity", 1);
		item.put("unit_price", request.getAmount());
		item.put("currency_id", "ARS");
		
		preference.put("items", List.of(item));
		
		// URLs de retorno
		Map<String, String> backUrls = new HashMap<>();
		backUrls.put("success", frontendBaseUrl + "/payment/success");
		backUrls.put("failure", frontendBaseUrl + "/payment/failure");
		backUrls.put("pending", frontendBaseUrl + "/payment/pending");
		preference.put("back_urls", backUrls);
		
		preference.put("auto_return", "approved");
		
		// URL de notificación (webhook)
		preference.put("notification_url", webhookUrl);
		
		// Metadata adicional (opcional)
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("booking_id", request.getBookingId());
		if (booking.getService() != null) {
			metadata.put("service_id", booking.getService().getId());
		}
		preference.put("metadata", metadata);
		
		return preference;
	}
}
