package com.waturnos.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingByDayDTO {
	private LocalDate date;
	private List<BookingExtendedDTO> bookings;
}