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
	
	@Value("${mercadopago.sandbox-mode:true}")
	private Boolean sandboxMode;
	
	@Value("${mercadopago.access-token}")
	private String envAccessToken;
	
	@Value("${mercadopago.public-key}")
	private String envPublicKey;
	
	@Value("${spring.profiles.active:dev}")
	private String activeProfile;
	
	private static final String MERCADOPAGO_API_URL = "https://api.mercadopago.com/checkout/preferences";
	
	@Override
	public PaymentPreferenceResponse createPreference(CreatePreferenceRequest request) {
		log.info("🔵 ========== INICIANDO CREACIÓN DE PREFERENCIA DE MERCADOPAGO ==========");
		log.info("💳 Booking ID: {}", request.getBookingId());
		log.info("💳 Amount: {}", request.getAmount());
		log.info("💳 Description: {}", request.getDescription());
		log.info("💳 CONFIG - frontendBaseUrl: '{}'", frontendBaseUrl);
		log.info("💳 CONFIG - webhookUrl: '{}'", webhookUrl);
		log.info("💳 CONFIG - sandboxMode (from application.yml): {}", sandboxMode);
		
		// Validar configuración
		if (frontendBaseUrl == null || frontendBaseUrl.trim().isEmpty()) {
			throw new IllegalStateException("mercadopago.frontend.base.url no está configurado");
		}
		if (frontendBaseUrl.contains("localhost") || frontendBaseUrl.contains("127.0.0.1")) {
			log.error("⚠️ ERROR: frontendBaseUrl contiene localhost: '{}'. Esto causará errores en MercadoPago.", frontendBaseUrl);
			throw new IllegalStateException("mercadopago.frontend.base.url no puede contener localhost en producción. Valor actual: " + frontendBaseUrl);
		}
		
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
		
		// EN DESARROLLO: Usar credenciales de variables de entorno para evitar mezclar prod/test
		String effectiveAccessToken;
		String effectivePublicKey;
		
		if ("dev".equals(activeProfile)) {
			log.info("💳 🔧 MODO DESARROLLO: Usando credenciales de variables de entorno");
			effectiveAccessToken = envAccessToken;
			effectivePublicKey = envPublicKey;
		} else {
			log.info("💳 🏭 MODO PRODUCCIÓN/DEMO: Usando credenciales de OAuth (DB)");
			effectiveAccessToken = paymentConfig.getAccessToken();
			effectivePublicKey = paymentConfig.getPublicKey();
		}
		
		log.info("💳 PAYMENT PROVIDER CONFIG:");
		log.info("💳   - Access Token (first 20 chars): {}...", effectiveAccessToken != null ? effectiveAccessToken.substring(0, Math.min(20, effectiveAccessToken.length())) : "null");
		log.info("💳   - Public Key: {}", effectivePublicKey);
		log.info("💳   - Account ID: {}", paymentConfig.getAccountId());
		log.info("💳   - Sandbox Mode (from DB): {} ⚠️ (IGNORADO en dev)", paymentConfig.getSandboxMode());
		log.info("💳   - Sandbox Mode (from CONFIG): {} ✅ (ESTE SE USA)", sandboxMode);
		log.info("💳   - Is Configured: {}", paymentConfig.getIsConfigured());
		log.info("💳   - Active Profile: {}", activeProfile);
		
		// SIEMPRE usar configuración, IGNORAR DB
		// El valor de DB puede estar incorrecto del OAuth anterior
		Boolean effectiveSandboxMode = sandboxMode;
		log.info("💳 ⭐ EFFECTIVE SANDBOX MODE (usando CONFIG, ignorando DB): {}", effectiveSandboxMode);
		
		// 4. Construir el cuerpo de la preferencia
		Map<String, Object> preferenceData = buildPreferenceData(request, booking);
		
		log.info("💳 MercadoPago Request Body: {}", preferenceData);
		
		// 5. Llamar a la API de MercadoPago
		try {
			log.info("💳 ========== PREPARANDO REQUEST A MERCADOPAGO API ==========");
			log.info("💳 API URL: {}", MERCADOPAGO_API_URL);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(effectiveAccessToken);
			// Agregar X-Idempotency-Key para evitar duplicados
			headers.set("X-Idempotency-Key", "booking-" + request.getBookingId() + "-" + System.currentTimeMillis());
			
			log.info("💳 Request Headers:");
			log.info("💳   - Content-Type: {}", headers.getContentType());
			log.info("💳   - Authorization: Bearer {}...", effectiveAccessToken.substring(0, Math.min(20, effectiveAccessToken.length())));
			log.info("💳   - X-Idempotency-Key: {}", headers.get("X-Idempotency-Key"));
			
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
				log.error("💳 ❌ Respuesta vacía de MercadoPago");
				throw new RuntimeException("Respuesta vacía de MercadoPago");
			}
			
			log.info("💳 ✅ Preferencia creada exitosamente");
			log.info("💳 Response - Preference ID: {}", responseBody.get("id"));
			log.info("💳 Response - Init Point: {}", responseBody.get("init_point"));
			log.info("💳 Response - Sandbox Init Point: {}", responseBody.get("sandbox_init_point"));
			log.info("💳 Response Status: {}", response.getStatusCode());
			log.info("🔵 ========== FIN CREACIÓN DE PREFERENCIA ==========");
			
			return PaymentPreferenceResponse.builder()
					.preferenceId((String) responseBody.get("id"))
					.initPoint((String) responseBody.get("init_point"))
					.sandboxInitPoint((String) responseBody.get("sandbox_init_point"))
					.build();
			
		} catch (Exception e) {
			log.error("💳 ❌ ERROR al crear preferencia en MercadoPago", e);
			log.error("💳 Error message: {}", e.getMessage());
			if (e.getCause() != null) {
				log.error("💳 Error cause: {}", e.getCause().getMessage());
			}
			log.info("🔵 ========== FIN CREACIÓN DE PREFERENCIA (CON ERROR) ==========");
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
		String successUrl = frontendBaseUrl + "/payment/success";
		String failureUrl = frontendBaseUrl + "/payment/failure";
		String pendingUrl = frontendBaseUrl + "/payment/pending";
		
		log.info("💳 MercadoPago - Constructed URLs:");
		log.info("💳   - success: '{}'", successUrl);
		log.info("💳   - failure: '{}'", failureUrl);
		log.info("💳   - pending: '{}'", pendingUrl);
		
		backUrls.put("success", successUrl);
		backUrls.put("failure", failureUrl);
		backUrls.put("pending", pendingUrl);
		preference.put("back_urls", backUrls);
		
		log.info("💳 MercadoPago Preference - backUrls map: {}", backUrls);
		
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
