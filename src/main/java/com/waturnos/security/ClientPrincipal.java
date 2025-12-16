package com.waturnos.security;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Principal object for client authentication.
 * Contains client information extracted from JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPrincipal {
	
	/** The client id. */
	private Long clientId;
	
	/** The identifier (email or phone). */
	private String identifier;
	
	/** The organization id. */
	private Long organizationId;
}
