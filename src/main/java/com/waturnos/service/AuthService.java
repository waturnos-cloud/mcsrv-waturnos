package com.waturnos.service;

/**
 * The Interface BookingService.
 */
public interface AuthService {
	
	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	void createPasswordResetTokenAndSendEmail(String email, String userType);

	/**
	 * Reset password.
	 *
	 * @param token the token
	 * @param newPassword the new password
	 * @param userType the user type
	 */
	void resetPassword(String token, String newPassword, String userType);
}
