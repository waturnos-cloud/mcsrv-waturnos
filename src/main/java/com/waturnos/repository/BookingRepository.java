package com.waturnos.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.Booking;
import com.waturnos.entity.extended.BookingReminder;
import com.waturnos.entity.extended.BookingSummaryDetail;

/**
 * The Interface BookingRepository.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

	/**
	 * Find by service id.
	 *
	 * @param serviceId the service id
	 * @return the list
	 */
	List<Booking> findByServiceId(Long serviceId);

	/**
	 * Delete all by service id.
	 *
	 * @param serviceId the service id
	 */
	@Modifying
	@Query("DELETE FROM Booking b WHERE b.service.id = :serviceId")
	void deleteAllByServiceId(@Param("serviceId") Long serviceId);

	/**
	 * Find by start time between.
	 *
	 * @param startOfDay the start of day
	 * @param endOfDay   the end of day
	 * @return the list
	 */
	List<Booking> findByStartTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

	/**
	 * Find by provider and start time between.
	 *
	 * @param providerId the provider id
	 * @param start      the start
	 * @param end        the end
	 * @return the list
	 */
	@Query("SELECT b.id AS id, " + 
		       "b.service.name AS serviceName, " +
		       "b.service.capacity AS serviceCapacity, " +
		       "b.startTime AS startTime, " + 
		       "b.endTime AS endTime, " + 
		       "b.service.id AS serviceId, " + 
		       "b.status AS status, " + 
		       "b.notes AS notes, " + 
		       "b.freeSlots AS freeSlots, " + 
		       "b.cancelReason AS cancelReason " + 
		       "FROM Booking b " + 
		       "WHERE b.service.user.id = :providerId " + 
		       "AND b.startTime BETWEEN :start AND :end " + 
		       "ORDER BY b.service.name ASC, b.startTime ASC")
		List<BookingSummaryDetail> findByProviderAndStartTimeBetween(@Param("providerId") Long providerId,
		        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	/**
	 * Consulta SQL Nativa para obtener el conteo de reservas agrupadas por fecha
	 * (CAST del start_time a DATE) y estado, filtrando por el Provider ID dentro de
	 * un rango de fechas. * @param startDate Fecha de inicio del rango (inclusivo).
	 *
	 * @param startDate  the start date
	 * @param endDate    Fecha de fin del rango (exclusivo, debe ser
	 *                   toDate.plusDays(1)).
	 * @param providerId ID del Proveedor.
	 * @return List<Object[]> donde cada Object[] contiene [Fecha, Estado, Conteo].
	 */
	@Query(value = """
			   SELECT
			       CAST(b.start_time AS DATE) AS booking_date,
			       b.status,
			       COUNT(b.id) AS count
			   FROM
			       booking b
			   JOIN
			       service s ON b.service_id = s.id
			   WHERE
			       s.user_id = :providerId
			       -- Filtro por fecha, usando el timestamp
			       AND b.start_time >= CAST(:startDate AS timestamp)
			       AND b.start_time < CAST(:endDate AS timestamp)
			   GROUP BY
			       CAST(b.start_time AS DATE), b.status
			ORDER BY
			       booking_date ASC,  -- Ordena por día ascendente (el día más antiguo primero)
			       b.status
			   """, nativeQuery = true)
	List<Object[]> countBookingsByDayAndStatus(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("providerId") Long providerId);

	/**
	 * Consulta que retorna las reservas confirmadas para el día de mañana. La
	 * conversión de fechas se realiza a nivel de base de datos usando NOW().
	 *
	 * @return the list
	 */
	@Query(value = """
            SELECT
                c.full_name AS fullName,
                c.email AS email,
                b.start_time AS startTime,
                s.name AS serviceName
            FROM
                booking b
            JOIN
                booking_client bc ON b.id = bc.booking_id  -- Nuevo JOIN a la tabla N:N
            JOIN
                client c ON bc.client_id = c.id          -- JOIN al cliente a través de bc
            JOIN
                service s ON b.service_id = s.id
            WHERE
                (b.status = 'RESERVED' or b.status = 'PARTIALLY_RESERVED' ) 
                AND b.start_time >= (CURRENT_DATE + INTERVAL '1 day')
                AND b.start_time < (CURRENT_DATE + INTERVAL '2 days')
            ORDER BY
                b.start_time
        """, nativeQuery = true)
List<BookingReminder> findBookingsForTomorrow();

	/**
	 * Find reserved with client and service between.
	 *
	 * @param start     the start
	 * @param end       the end
	 * @param serviceId the service id
	 * @return the list
	 */
	@Query("""
	        SELECT b FROM Booking b
	        WHERE b.startTime >= :start
	          AND b.endTime <= :end
	          AND b.bookingClients IS NOT EMPTY 
	          AND b.status NOT IN ('CANCELLED', 'COMPLETED') 
	          AND b.service.id = :serviceId
	        """)
	List<Booking> findReservedWithClientAndServiceBetween(@Param("start") LocalDateTime start,
	        @Param("end") LocalDateTime end, @Param("serviceId") Long serviceId);

	/**
	 * Delete bookings between dates.
	 *
	 * @param start     the start
	 * @param end       the end
	 * @param serviceId the service id
	 */
	@Modifying
	@Query("""
			    DELETE FROM Booking b
			    WHERE b.startTime >= :start
			      AND b.endTime <= :end
			      AND b.service.id = :serviceId
			""")
	void deleteBookingsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("serviceId") Long serviceId);

	/**
	 * Find by provider and range.
	 *
	 * @param providerId the provider id
	 * @param start the start
	 * @param end the end
	 * @return the list
	 */
	@Query("""
			    SELECT b
			    FROM Booking b
			    JOIN b.service s
			    WHERE s.user.id = :providerId
			      AND b.startTime >= :start
			      AND b.startTime < :end
			""")
	List<Booking> findByProviderAndRange(@Param("providerId") Long providerId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	/**
	 * Find by provider service and range.
	 *
	 * @param providerId the provider id
	 * @param serviceId the service id
	 * @param start the start
	 * @param end the end
	 * @return the list
	 */
	@Query("""
			    SELECT b
			    FROM Booking b
			    JOIN b.service s
			    WHERE s.user.id = :providerId
			      AND s.id = :serviceId
			      AND b.startTime >= :start
			      AND b.startTime < :end
			""")
	List<Booking> findByProviderServiceAndRange(@Param("providerId") Long providerId,
			@Param("serviceId") Long serviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
