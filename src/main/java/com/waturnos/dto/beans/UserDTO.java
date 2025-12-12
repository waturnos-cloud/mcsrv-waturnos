package com.waturnos.dto.beans;

import java.util.List;

import com.waturnos.dto.response.PaymentProviderResponse;
import com.waturnos.enums.UserRole;

import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String password;
	private UserRole role;
	private String avatar;
	private String photoUrl;
	private String bio;
	private List<PaymentProviderResponse> paymentProviders;
}
