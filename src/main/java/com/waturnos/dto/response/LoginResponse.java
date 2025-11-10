package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private String token;
	private Long userId;
	private Long organizationId;
	private String organizationName;
	private String role;
	private Boolean simpleOrganization;
}
