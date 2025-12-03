package com.waturnos.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for grouped availability response.
 * Shows time slots without distinguishing specific services of the same type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedAvailabilityDTO {
	
	/** The time slot start. */
	private LocalDateTime startTime;
	
	/** The time slot end. */
	private LocalDateTime endTime;
	
	/** Total services of this type. */
	private Integer totalServices;
	
	/** How many are available in this slot. */
	private Integer availableCount;
	
	/** List of available booking IDs for this slot. */
	private List<Long> availableBookingIds;
	
	/** Is this time slot completely booked? */
	private Boolean isFullyBooked;
}
