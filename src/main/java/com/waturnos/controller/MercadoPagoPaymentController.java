package com.waturnos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.request.CreatePreferenceRequest;
import com.waturnos.dto.response.PaymentPreferenceResponse;
import com.waturnos.service.MercadoPagoPreferenceService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller para gestionar las preferencias de pago de MercadoPago.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Gestión de pagos")
public class MercadoPagoPaymentController {
	
	private final MercadoPagoPreferenceService preferenceService;
	
	/**
	 * Crea una preferencia de pago en MercadoPago para un booking.
	 * 
	 * Este endpoint:
	 * 1. Obtiene el booking por ID
	 * 2. Identifica el provider asociado al servicio
	 * 3. Lee el access_token de MercadoPago del provider desde users_props
	 * 4. Crea una preferencia en MercadoPago con el external_reference = bookingId
	 * 5. Devuelve el init_point (URL de checkout) para redirigir al cliente
	 *
	 * @param request datos de la preferencia (bookingId, amount, description)
	 * @return la respuesta con preferenceId e initPoint
	 */
	@PostMapping("/create-preference")
	public ResponseEntity<PaymentPreferenceResponse> createPreference(
			@Valid @RequestBody CreatePreferenceRequest request) {
		
		PaymentPreferenceResponse response = preferenceService.createPreference(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
