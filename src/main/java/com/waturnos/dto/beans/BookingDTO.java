package com.waturnos.dto.beans;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.enums.BookingStatus;

import lombok.Data;

@Data
public class BookingDTO {
	protected Long id;
	protected LocalDateTime startTime;
	protected LocalDateTime endTime;
	protected BookingStatus status;
	protected String notes;
	protected Long serviceId;
	protected String serviceName;
	protected Integer serviceDurationMinutes;
	protected Long userId;
	protected String userFullName;
	protected Integer freeSlots;
	protected Long recurrenceId;
	protected Boolean isRecurrent;
	protected String recurrencePattern; // Ej: "MIÉRCOLES 20:00"
	protected Boolean isOverbooking;
	protected List<BookingPropsDTO> bookingProps;
}
