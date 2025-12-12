package com.waturnos.dto.response;

import com.waturnos.enums.PaymentProviderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de proveedor de pago vinculado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProviderResponse {
	
	private PaymentProviderType type;
	private String accessToken;
	private String publicKey;
	private String accountId;
	private String webhookUrl;
	private Boolean sandboxMode;
	private Boolean isConfigured;
	
}
