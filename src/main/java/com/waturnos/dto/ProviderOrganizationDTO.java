package com.waturnos.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProviderOrganizationDTO {
	private Long id;
	private Long providerId;
	private Long organizationId;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String creator;
	private String modificator;
}