package com.waturnos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for client login and registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientLoginRequest {
	
	/** The organization id. */
	private Long organizationId;
	
	/** The email. */
	private String email;
	
	/** The code. */
	private String code;
	
	/** The phone. */
	private String phone;
	
	/** The full name (only for registration). */
	private String fullName;
	
	/** The document number / DNI (only for registration). */
	private String dni;
}
