package com.waturnos.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class ServiceWithBookingsDTO {
	private Long serviceId;
	private String serviceName;
	private String serviceDescription;
	private Double servicePrice;
	private List<BookingSimpleDTO> bookings;
}