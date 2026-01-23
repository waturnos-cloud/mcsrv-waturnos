package com.waturnos.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar la creación de una preferencia de pago en MercadoPago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreferenceRequest {
	
	@NotNull(message = "El ID del booking es requerido")
	private Long bookingId;
	
	@NotNull(message = "El ID del cliente es requerido")
	private Long clientId;
	
	@NotNull(message = "El monto es requerido")
	@Positive(message = "El monto debe ser positivo")
	private BigDecimal amount;
	
	private String description;
}
