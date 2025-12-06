package com.waturnos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para autenticación con Google OAuth.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {
	
	/** El token ID de Google (JWT) */
	private String idToken;
	
	/** La organización a la que se quiere vincular el cliente (opcional) */
	private Long organizationId;
}
