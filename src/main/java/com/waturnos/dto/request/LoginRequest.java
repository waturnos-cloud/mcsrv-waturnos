package com.waturnos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Instantiates a new login request.
 */
@NoArgsConstructor

/**
 * Instantiates a new login request.
 *
 * @param email the email
 * @param password the password
 */
@AllArgsConstructor

/**
 * To string.
 *
 * @return the java.lang. string
 */
@Builder

/**
 * To string.
 *
 * @return the java.lang. string
 */
@Data
public class LoginRequest {
	
	/** The email. */
	private String email;
	
	/** The password. */
	private String password;
}
