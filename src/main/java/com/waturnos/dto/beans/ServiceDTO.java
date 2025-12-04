package com.waturnos.dto.beans;

import java.util.List;

import lombok.Data;

@Data
public class ServiceDTO {
	private Long id;
	private String name;
	private String description;
	private Integer durationMinutes;
	private Double price;
	private Integer advancePayment;
	private Integer futureDays;
	private UserDTO user;
	private LocationDTO location;
	private CategoryTreeDTO type;
	private Integer capacity;
	private boolean waitList;
	private Integer waitListTime;
	private Integer offsetMinutes;
	private List<AvailabilityDTO> listAvailability;
}
