package com.waturnos.dto.beans;

import com.waturnos.enums.BookingStatus;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class BookingDTO {
	private Long id;
	private OffsetDateTime startTime;
	private OffsetDateTime endTime;
	private BookingStatus status;
	private String notes;
	private Long organizationId;
	private Long clientId;
	private Long providerId;
	private Long serviceId;
}
