package com.waturnos.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un booking afectado por cambios en availability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectedBookingDTO {
	
	private Long bookingId;
	private String clientFullName;
	private String clientPhone;
	private String clientEmail;
	private LocalDateTime startTime;
	
}
