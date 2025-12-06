package com.waturnos.dto.request;

import com.waturnos.dto.beans.BookingDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un booking con sobrecupo (overbooking).
 * Incluye los datos del booking y el cliente que lo reserva.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverBookingDTO {
	
	/** El booking a crear */
	private BookingDTO booking;
	
	/** El ID del cliente que reserva */
	private Long clientId;
}
