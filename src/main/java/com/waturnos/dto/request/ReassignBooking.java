package com.waturnos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ReassignBooking.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReassignBooking {
	
	/** The actual booking id. */
	private Long actualBookingId;
	
	/** The new booking id. */
	private Long newBookingId;
	
	/** The client id. */
	private Long clientId;

}
