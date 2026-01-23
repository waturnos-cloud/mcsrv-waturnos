-- Migration: Create payments table
-- Date: 2026-01-23
-- Description: Tabla para registrar pagos de bookings realizados por MercadoPago u otros métodos

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    merchant_order_id VARCHAR(255),
    payment_type VARCHAR(100),
    status_detail VARCHAR(100),
    currency VARCHAR(10),
    payer_email VARCHAR(255),
    description VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    approved_at TIMESTAMP,
    
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_client_id ON payments(client_id);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Comentarios
COMMENT ON TABLE payments IS 'Registro de pagos realizados por clientes para bookings';
COMMENT ON COLUMN payments.booking_id IS 'ID del booking al que corresponde el pago';
COMMENT ON COLUMN payments.client_id IS 'ID del cliente que realizó el pago';
COMMENT ON COLUMN payments.payment_method IS 'Proveedor de pago: MERCADO_PAGO';
COMMENT ON COLUMN payments.amount IS 'Monto pagado';
COMMENT ON COLUMN payments.status IS 'Estado del pago: PENDING, APPROVED, REJECTED, CANCELLED, REFUNDED, CHARGED_BACK, IN_PROCESS';
COMMENT ON COLUMN payments.transaction_id IS 'ID de transacción del proveedor de pago (payment_id de MercadoPago)';
COMMENT ON COLUMN payments.merchant_order_id IS 'ID de orden del merchant (merchant_order_id de MercadoPago)';
COMMENT ON COLUMN payments.payment_type IS 'Tipo específico de pago (credit_card, debit_card, account_money, etc)';
COMMENT ON COLUMN payments.status_detail IS 'Detalle del estado del pago';
COMMENT ON COLUMN payments.currency IS 'Moneda del pago (ARS, USD, etc)';
COMMENT ON COLUMN payments.payer_email IS 'Email del pagador';
COMMENT ON COLUMN payments.description IS 'Descripción del pago';
COMMENT ON COLUMN payments.metadata IS 'Datos adicionales en formato JSON';
COMMENT ON COLUMN payments.created_at IS 'Fecha de creación del registro';
COMMENT ON COLUMN payments.updated_at IS 'Fecha de última actualización';
COMMENT ON COLUMN payments.approved_at IS 'Fecha de aprobación del pago';
