package com.waturnos.dto.request;

import com.waturnos.dto.beans.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ForgotPassword.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ForgotPassword {
	private String email;
	private UserType userType;

}
