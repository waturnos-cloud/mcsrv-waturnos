package com.waturnos.service;

import com.waturnos.entity.AccessToken;
import java.util.Optional;

/**
 * The Interface AccessTokenService.
 */
public interface AccessTokenService {
	
	/**
	 * Generate token.
	 *
	 * @param email the email
	 * @param phone the phone
	 */
	void generateToken(String email, String phone);

	/**
	 * Validate token.
	 *
	 * @param email the email
	 * @param phone the phone
	 * @param code the code
	 * @return the optional
	 */
	Optional<AccessToken> validateToken(String email, String phone, String code);

	/**
	 * Delete token.
	 *
	 * @param id the id
	 */
	void deleteToken(Long id);

	/**
	 * Delete expired tokens.
	 */
	void deleteExpiredTokens();
}
