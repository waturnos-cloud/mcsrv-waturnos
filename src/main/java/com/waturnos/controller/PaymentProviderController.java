package com.waturnos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.request.AddPaymentRequest;
import com.waturnos.dto.request.MercadoPagoOAuthRequest;
import com.waturnos.dto.response.PaymentProviderResponse;
import com.waturnos.enums.PaymentProviderType;
import com.waturnos.service.PaymentProviderService;
import com.waturnos.service.impl.MercadoPagoOAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador para gestionar proveedores de pago vinculados a usuarios.
 */
@RestController
@RequestMapping("/users/{userId}/payment-providers")
@RequiredArgsConstructor
public class PaymentProviderController {
	
	private final PaymentProviderService paymentProviderService;
	private final MercadoPagoOAuthService mercadoPagoOAuthService;
	
	/**
	 * Vincula o actualiza un proveedor de pago para un usuario.
	 *
	 * @param userId el ID del usuario
	 * @param request los datos del proveedor de pago
	 * @return respuesta de éxito
	 */
	@PostMapping
	public ResponseEntity<String> addPaymentProvider(
			@PathVariable Long userId,
			@Valid @RequestBody AddPaymentRequest request) {
		paymentProviderService.addPaymentProvider(userId, request);
		return ResponseEntity.ok("Payment provider configured successfully");
	}
	
	/**
	 * Obtiene la configuración de un proveedor de pago.
	 *
	 * @param userId el ID del usuario
	 * @param type el tipo de proveedor
	 * @return la configuración del proveedor
	 */
	@GetMapping("/{type}")
	public ResponseEntity<PaymentProviderResponse> getPaymentProvider(
			@PathVariable Long userId,
			@PathVariable PaymentProviderType type) {
		PaymentProviderResponse response = paymentProviderService.getPaymentProvider(userId, type);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * Elimina un proveedor de pago vinculado.
	 *
	 * @param userId el ID del usuario
	 * @param type el tipo de proveedor
	 * @return respuesta de éxito
	 */
	@DeleteMapping("/{type}")
	public ResponseEntity<String> removePaymentProvider(
			@PathVariable Long userId,
			@PathVariable PaymentProviderType type) {
		paymentProviderService.removePaymentProvider(userId, type);
		return ResponseEntity.ok("Payment provider removed successfully");
	}
	
	/**
	 * Endpoint OAuth para vincular MercadoPago.
	 * Recibe el código de autorización y lo intercambia por access_token.
	 *
	 * @param userId el ID del usuario
	 * @param request el código de autorización de MercadoPago
	 * @return respuesta de éxito
	 */
	@PostMapping("/mercadopago/oauth")
	public ResponseEntity<String> mercadoPagoOAuth(
			@PathVariable Long userId,
			@Valid @RequestBody MercadoPagoOAuthRequest request) {
		mercadoPagoOAuthService.exchangeCodeForToken(userId, request.getCode(), request.getRedirectUri());
		return ResponseEntity.ok("MercadoPago account linked successfully");
	}
}
