package com.waturnos.dto.beans;

import java.time.LocalDateTime;

import com.waturnos.enums.BookingStatus;

import lombok.Data;

@Data
public class BookingDTO {
	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BookingStatus status;
	private String notes;
	private Long clientId;
	private Long serviceId;
}
