package com.waturnos.service;

/**
 * Servicio para procesar notificaciones de MercadoPago.
 */
public interface MercadoPagoWebhookService {
	
	/**
	 * Procesa una notificación de pago de MercadoPago.
	 * Consulta el estado del pago y actualiza la reserva correspondiente.
	 *
	 * @param paymentId el ID del pago en MercadoPago
	 */
	void processPaymentNotification(String paymentId);
}
