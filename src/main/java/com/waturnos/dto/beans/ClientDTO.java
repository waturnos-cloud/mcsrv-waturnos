package com.waturnos.dto.beans;

import lombok.Data;

@Data
public class ClientDTO {
	private Long id;
	private String fullName;
	private String dni;
	private String email;
	private String phone;
}
