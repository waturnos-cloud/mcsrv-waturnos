package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	private String token;
	private Long userId;
	private String role;
	private Long organizationId;
	private Long providerId;
	private String avatar;

	public LoginResponse(String token, Long userId, String role) {
		this.token = token;
		this.userId = userId;
		this.role = role;
	}
}