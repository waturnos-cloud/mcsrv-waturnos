package com.waturnos.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProviderOrganizationDTO {
	private Long id;
	private Long providerId;
	private Long organizationId;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String creator;
	private String modificator;
}