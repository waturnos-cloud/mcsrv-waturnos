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
	
	/**
	 * Valida la firma del webhook y procesa el pago si es válido.
	 * 
	 * @param xSignature header x-signature del webhook
	 * @param xRequestId header x-request-id del webhook
	 * @param paymentId ID del pago a procesar
	 * @return true si el webhook es válido y fue procesado, false si la firma es inválida
	 */
	boolean validateAndProcessWebhook(String xSignature, String xRequestId, String paymentId);
}
