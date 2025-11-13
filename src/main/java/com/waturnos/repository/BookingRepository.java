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

public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByClientId(Long clientId);

	List<Booking> findByServiceId(Long serviceId);

	@Modifying
	@Query("DELETE FROM Booking b WHERE b.service.id = :serviceId")
	void deleteAllByServiceId(@Param("serviceId") Long serviceId);

	List<Booking> findByStartTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

	@Query("SELECT b FROM Booking b WHERE b.service.user.id = :providerId AND b.startTime BETWEEN :start AND :end")
	List<Booking> findByProviderAndStartTimeBetween(
	        @Param("providerId") Long providerId,
	        @Param("start") LocalDateTime start,
	        @Param("end") LocalDateTime end);
	
	
	/**
     * Consulta SQL Nativa para obtener el conteo de reservas agrupadas por fecha 
     * (CAST del start_time a DATE) y estado, filtrando por el Provider ID 
     * dentro de un rango de fechas.
     * * @param startDate Fecha de inicio del rango (inclusivo).
     * @param endDate Fecha de fin del rango (exclusivo, debe ser toDate.plusDays(1)).
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
    List<Object[]> countBookingsByDayAndStatus(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        @Param("providerId") Long providerId);
    
    /**
     * Consulta que retorna las reservas confirmadas para el día de mañana.
     * La conversión de fechas se realiza a nivel de base de datos usando NOW().
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
            client c ON b.client_id = c.id
        JOIN
            service s ON b.service_id = s.id
        WHERE
            b.status = 'RESERVED'
            AND b.start_time >= (CURRENT_DATE + INTERVAL '1 day')
            AND b.start_time < (CURRENT_DATE + INTERVAL '2 days')
        ORDER BY
            b.start_time
    """, nativeQuery = true) 
    List<BookingReminder> findBookingsForTomorrow();    
}
