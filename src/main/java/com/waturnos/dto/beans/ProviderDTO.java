package com.waturnos.dto.beans;

import java.util.List;

import lombok.Data;

@Data
public class ProviderDTO {
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String bio;
	private Boolean active;
	private String password;
	private List<OrganizationDTO> organizations;
}
