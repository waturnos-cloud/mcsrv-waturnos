package com.waturnos.dto.beans;

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
}
