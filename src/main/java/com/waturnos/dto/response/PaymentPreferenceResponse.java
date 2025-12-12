package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de creación de preferencia de pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPreferenceResponse {
	
	/**
	 * ID de la preferencia creada en MercadoPago
	 */
	private String preferenceId;
	
	/**
	 * URL de checkout donde se redirige al cliente para pagar
	 */
	private String initPoint;
	
	/**
	 * URL de checkout para sandbox (modo prueba)
	 */
	private String sandboxInitPoint;
}
