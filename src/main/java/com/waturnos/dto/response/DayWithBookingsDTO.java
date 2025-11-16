package com.waturnos.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class DayWithBookingsDTO {
	private String date;
	private List<BookingExtendedDTO> list;
}