package com.waturnos.dto.beans;

import java.time.LocalTime;

import lombok.Data;

@Data
public class AvailabilityDTO {
	private Long id;
	private int dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;
}
