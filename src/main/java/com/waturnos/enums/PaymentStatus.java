package com.waturnos.enums;

/**
 * Estados posibles de un pago.
 */
public enum PaymentStatus {
	/** Pago pendiente de procesamiento */
	PENDING,
	
	/** Pago aprobado y acreditado */
	APPROVED,
	
	/** Pago rechazado */
	REJECTED,
	
	/** Pago cancelado */
	CANCELLED,
	
	/** Pago reembolsado */
	REFUNDED,
	
	/** Pago con contracargo */
	CHARGED_BACK,
	
	/** Pago en proceso */
	IN_PROCESS
}
