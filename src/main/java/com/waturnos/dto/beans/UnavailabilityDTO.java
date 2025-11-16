package com.waturnos.dto.beans;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * The Class UnavailabilityDTO.
 */
@Data
public class UnavailabilityDTO {
	
	/** The start time. */
	private LocalDateTime startTime;
	/** The end time. */
	private LocalDateTime endTime;
	
	/** The service id. */
	private Long serviceId;
}
