package com.waturnos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.ServiceEntity;

/**
 * Servicio dedicado para generación asíncrona de bookings.
 */
public interface BookingGeneratorService {
    
    /**
     * Genera bookings de forma asíncrona para un servicio.
     * Este método se ejecuta en background y no bloquea la respuesta HTTP.
     *
     * @param service el servicio para el cual generar bookings
     * @param availabilities las disponibilidades del servicio
     * @param unavailabilities fechas no disponibles (feriados, etc)
     */
    void generateBookingsAsync(ServiceEntity service, List<AvailabilityEntity> availabilities,
                               Set<LocalDate> unavailabilities);
}
