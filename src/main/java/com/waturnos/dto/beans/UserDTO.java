package com.waturnos.dto.beans;

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
	private String photoUrl;
	private String bio;
}
