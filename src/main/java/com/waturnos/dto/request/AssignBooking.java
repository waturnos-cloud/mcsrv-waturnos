package com.waturnos.dto.request;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AssignBooking {
	private Long id;
	private Long clientId;
	private Map<String, String> bookingProps;

}
