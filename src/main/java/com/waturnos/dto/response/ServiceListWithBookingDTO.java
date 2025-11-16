package com.waturnos.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class ServiceListWithBookingDTO {
	private String name; 
    private Long id;     
    private List<BookingExtendedDTO> list;
}
