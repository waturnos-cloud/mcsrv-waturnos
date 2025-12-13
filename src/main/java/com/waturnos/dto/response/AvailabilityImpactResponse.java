package com.waturnos.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar el impacto de cambios en availability sobre bookings existentes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityImpactResponse {
	
	private Integer affectedCount;
	private List<AffectedBookingDTO> affectedBookings;
	
}
