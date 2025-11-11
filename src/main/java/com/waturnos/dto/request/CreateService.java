package com.waturnos.dto.request;

import java.util.List;

import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.dto.beans.ServiceDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Instantiates a new login request.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateService {
	private ServiceDTO serviceDto;
	private List<AvailabilityDTO> listAvailability;
	private boolean workInHollidays;

}
