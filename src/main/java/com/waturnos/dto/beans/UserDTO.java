package com.waturnos.dto.beans;

import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String password;
}
