package com.waturnos.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.Recurrence;

public interface RecurrenceRepository extends JpaRepository<Recurrence, Long> {
    
    /**
     * Busca recurrencias activas por cliente
     */
    List<Recurrence> findByClientIdAndActiveTrue(Long clientId);
    
    /**
     * Busca recurrencias activas por servicio
     */
    List<Recurrence> findByServiceIdAndActiveTrue(Long serviceId);
    
    /**
     * Busca recurrencias activas por proveedor
     */
    List<Recurrence> findByProviderIdAndActiveTrue(Long providerId);
    
    /**
     * Busca una recurrencia específica activa por cliente, servicio, día y hora
     */
    @Query("SELECT r FROM Recurrence r WHERE r.client.id = :clientId " +
           "AND r.service.id = :serviceId AND r.provider.id = :providerId " +
           "AND r.dayOfWeek = :dayOfWeek AND r.timeSlot = :timeSlot AND r.active = true")
    Optional<Recurrence> findActiveByClientServiceDayTime(
        @Param("clientId") Long clientId,
        @Param("serviceId") Long serviceId,
        @Param("providerId") Long providerId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("timeSlot") LocalTime timeSlot
    );
    
    /**
     * Busca todas las recurrencias activas para aplicar al generar nuevos turnos
     */
    @Query("SELECT r FROM Recurrence r WHERE r.active = true " +
           "AND (r.recurrenceType = 'FOREVER' " +
           "OR (r.recurrenceType = 'END_DATE' AND r.endDate >= CURRENT_DATE) " +
           "OR r.recurrenceType = 'COUNT')")
    List<Recurrence> findAllActiveRecurrences();
    
    /**
     * Busca recurrencias por servicio y día de la semana
     */
    @Query("SELECT r FROM Recurrence r WHERE r.service.id = :serviceId " +
           "AND r.dayOfWeek = :dayOfWeek AND r.active = true")
    List<Recurrence> findByServiceAndDayOfWeek(
        @Param("serviceId") Long serviceId,
        @Param("dayOfWeek") Integer dayOfWeek
    );
}
