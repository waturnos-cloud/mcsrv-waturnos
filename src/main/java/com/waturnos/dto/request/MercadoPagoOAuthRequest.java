package com.waturnos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir el código de autorización OAuth de MercadoPago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoOAuthRequest {
	
	/** Código de autorización devuelto por MercadoPago */
	private String code;
	
	/** Redirect URI usado en el flujo OAuth (debe coincidir con el configurado) */
	private String redirectUri;
}
