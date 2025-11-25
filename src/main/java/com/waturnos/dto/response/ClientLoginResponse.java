package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for client login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientLoginResponse {
	
	/** The JWT token. */
	private String token;
	
	/** The client id. */
	private Long clientId;
	
	/** Optional message. */
	private String message;
	
	/**
	 * Constructor without message.
	 *
	 * @param token the token
	 * @param clientId the client id
	 */
	public ClientLoginResponse(String token, Long clientId) {
		this.token = token;
		this.clientId = clientId;
	}
}
