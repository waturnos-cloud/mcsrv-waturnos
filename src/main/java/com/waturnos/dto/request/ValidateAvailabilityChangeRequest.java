package com.waturnos.dto.request;

import java.util.List;

import com.waturnos.dto.beans.AvailabilityDTO;

import lombok.Data;

/**
 * Request para validar el impacto de cambios en availability.
 */
@Data
public class ValidateAvailabilityChangeRequest {
	
	private Long serviceId;
	private List<AvailabilityDTO> newAvailability;
	
}
