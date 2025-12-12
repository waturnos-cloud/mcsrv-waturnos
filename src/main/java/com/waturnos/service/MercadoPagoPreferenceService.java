package com.waturnos.service;

import com.waturnos.dto.request.CreatePreferenceRequest;
import com.waturnos.dto.response.PaymentPreferenceResponse;

/**
 * Servicio para gestionar las preferencias de pago de MercadoPago.
 */
public interface MercadoPagoPreferenceService {
	
	/**
	 * Crea una preferencia de pago en MercadoPago.
	 * 
	 * La preferencia se crea usando el access token del provider que ofrece el servicio.
	 * El external_reference se establece con el ID del booking para rastrearlo en el webhook.
	 *
	 * @param request los datos para crear la preferencia
	 * @return la respuesta con el ID de preferencia y URL de checkout
	 * @throws IllegalStateException si el provider no tiene configurado MercadoPago
	 * @throws IllegalArgumentException si el booking no existe o no pertenece a un servicio válido
	 */
	PaymentPreferenceResponse createPreference(CreatePreferenceRequest request);
}
