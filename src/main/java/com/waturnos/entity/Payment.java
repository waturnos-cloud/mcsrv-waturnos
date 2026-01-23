package com.waturnos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.waturnos.enums.PaymentProviderType;
import com.waturnos.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un pago realizado por un cliente.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Booking asociado al pago
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	/**
	 * Cliente que realizó el pago
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	/**
	 * Método de pago utilizado
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 50)
	private PaymentProviderType paymentMethod;

	/**
	 * Monto pagado
	 */
	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	/**
	 * Estado del pago
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private PaymentStatus status;

	/**
	 * ID de transacción del proveedor de pago (ej: payment_id de MercadoPago)
	 */
	@Column(name = "transaction_id", length = 255)
	private String transactionId;

	/**
	 * ID de orden del proveedor (ej: merchant_order_id de MercadoPago)
	 */
	@Column(name = "merchant_order_id", length = 255)
	private String merchantOrderId;

	/**
	 * Tipo de pago (ej: credit_card, debit_card, account_money)
	 */
	@Column(name = "payment_type", length = 100)
	private String paymentType;

	/**
	 * Detalle del estado (ej: accredited, pending_contingency)
	 */
	@Column(name = "status_detail", length = 100)
	private String statusDetail;

	/**
	 * Moneda del pago (ej: ARS, USD)
	 */
	@Column(name = "currency", length = 10)
	private String currency;

	/**
	 * Email del pagador
	 */
	@Column(name = "payer_email", length = 255)
	private String payerEmail;

	/**
	 * Descripción del pago
	 */
	@Column(name = "description", length = 500)
	private String description;

	/**
	 * Datos adicionales del pago en formato JSON
	 */
	@Column(name = "metadata", columnDefinition = "TEXT")
	private String metadata;

	/**
	 * Fecha de creación del pago
	 */
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	/**
	 * Fecha de última actualización
	 */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * Fecha de aprobación del pago
	 */
	@Column(name = "approved_at")
	private LocalDateTime approvedAt;
}
