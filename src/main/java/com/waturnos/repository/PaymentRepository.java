package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.Payment;

/**
 * Repositorio para gestionar pagos.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	/**
	 * Busca todos los pagos de un booking.
	 *
	 * @param bookingId el ID del booking
	 * @return lista de pagos
	 */
	List<Payment> findByBookingId(Long bookingId);

	/**
	 * Busca un pago por transaction ID.
	 *
	 * @param transactionId el ID de transacción
	 * @return el pago si existe
	 */
	Optional<Payment> findByTransactionId(String transactionId);

	/**
	 * Busca todos los pagos de un cliente.
	 *
	 * @param clientId el ID del cliente
	 * @return lista de pagos
	 */
	List<Payment> findByClientId(Long clientId);
}
