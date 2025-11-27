package com.waturnos.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.WaitlistEntry;
import com.waturnos.enums.WaitlistStatus;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    
    /**
     * Busca las entradas de lista de espera de un cliente con ciertos estados
     */
    @Query("SELECT w FROM WaitlistEntry w " +
           "JOIN FETCH w.service s " +
           "JOIN FETCH w.user u " +
           "WHERE w.client.id = :clientId " +
           "AND w.status IN :statuses " +
           "ORDER BY w.createdAt DESC")
    List<WaitlistEntry> findByClientIdAndStatusIn(
        @Param("clientId") Long clientId,
        @Param("statuses") List<WaitlistStatus> statuses
    );
    
    /**
     * Busca las entradas de lista de espera de un cliente en una organización con ciertos estados
     */
    @Query("SELECT w FROM WaitlistEntry w " +
           "JOIN FETCH w.service s " +
           "JOIN FETCH w.user u " +
           "WHERE w.client.id = :clientId " +
           "AND w.organization.id = :organizationId " +
           "AND w.status IN :statuses " +
           "ORDER BY w.createdAt DESC")
    List<WaitlistEntry> findByClientIdAndOrganizationIdAndStatusIn(
        @Param("clientId") Long clientId,
        @Param("organizationId") Long organizationId,
        @Param("statuses") List<WaitlistStatus> statuses
    );
    
    /**
     * Verifica si existe una entrada para un cliente en un servicio y fecha específica con cierto estado
     */
    boolean existsByClientIdAndServiceIdAndDateAndStatus(
        Long clientId,
        Long serviceId,
        LocalDate date,
        WaitlistStatus status
    );
    
    /**
     * Cuenta las entradas en espera para un servicio (para calcular posición)
     */
    Integer countByServiceIdAndStatus(
        Long serviceId,
        WaitlistStatus status
    );
    
    /**
     * Busca entradas ordenadas por fecha de creación (para recalcular posiciones)
     */
    List<WaitlistEntry> findByServiceIdAndStatusOrderByCreatedAtAsc(
        Long serviceId,
        WaitlistStatus status
    );
    
    /**
     * Busca candidatos para notificar cuando se libera un turno
     * Prioridad: SPECIFIC primero, luego TIME_WINDOW, ordenados por position
     */
    @Query(value = """
        SELECT w.* FROM waitlist_entries w
        WHERE w.service_id = :serviceId
          AND w.status = 'WAITING'
          AND w.date = :date
          AND w.time_from <= :time
          AND w.time_to >= :time
          AND (
            (w.type = 'SPECIFIC' AND w.specific_booking_id = :bookingId)
            OR w.type = 'TIME_WINDOW'
          )
        ORDER BY 
          CASE WHEN w.type = 'SPECIFIC' THEN 0 ELSE 1 END,
          w.position ASC
        LIMIT 1
        """, nativeQuery = true)
    List<WaitlistEntry> findCandidatesForBooking(
        @Param("serviceId") Long serviceId,
        @Param("date") LocalDate date,
        @Param("time") LocalTime time,
        @Param("bookingId") Long bookingId
    );
    
    /**
     * Busca notificaciones vencidas (para el cron job)
     */
    @Query("SELECT w FROM WaitlistEntry w " +
           "WHERE w.status = 'NOTIFIED' " +
           "AND w.expiresAt < :now")
    List<WaitlistEntry> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Busca una entrada de un cliente para un servicio y fecha con cierto estado
     */
    Optional<WaitlistEntry> findByClientIdAndServiceIdAndStatusAndDate(
        Long clientId,
        Long serviceId,
        WaitlistStatus status,
        LocalDate date
    );
}
