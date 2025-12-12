package com.waturnos.dto.request;

import com.waturnos.enums.PaymentProviderType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para vincular un proveedor de pago a un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPaymentRequest {
	
	@NotNull(message = "Payment provider type is required")
	private PaymentProviderType type;
	
	/** Access Token de MercadoPago (privado - para operaciones del servidor) */
	private String accessToken;
	
	/** Public Key de MercadoPago (se puede exponer al frontend) */
	private String publicKey;
	
	/** ID de la cuenta de MercadoPago (opcional) */
	private String accountId;
	
	/** Webhook URL para notificaciones de MercadoPago (opcional) */
	private String webhookUrl;
	
	/** Indicar si se debe usar modo sandbox/producción */
	private Boolean sandboxMode;
}
