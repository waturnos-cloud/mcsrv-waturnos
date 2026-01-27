package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	 * Busca un pago por transaction ID con lock pesimista para evitar race conditions.
	 * El lock PESSIMISTIC_WRITE asegura que solo un thread puede leer y crear el payment.
	 *
	 * @param transactionId el ID de transacción
	 * @return el pago si existe
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
	Optional<Payment> findByTransactionIdWithLock(@Param("transactionId") String transactionId);

	/**
	 * Busca todos los pagos de un cliente.
	 *
	 * @param clientId el ID del cliente
	 * @return lista de pagos
	 */
	List<Payment> findByClientId(Long clientId);
}
