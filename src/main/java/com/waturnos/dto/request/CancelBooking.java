package com.waturnos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CancelBooking {
	@NotNull(message = "Booking ID is required")
	private Long id;
	
	private String reason;
	
	@NotNull(message = "Client ID is required")
	private Long clientId; // ID del cliente específico a cancelar (obligatorio)
}
