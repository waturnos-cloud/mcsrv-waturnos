package com.waturnos.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.enums.BookingStatus;
import com.waturnos.service.BookingGeneratorService;
import com.waturnos.service.BookingService;
import com.waturnos.utils.DateUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de generación asíncrona de bookings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingGeneratorServiceImpl implements BookingGeneratorService {

    private final BookingService bookingService;
    private final EntityManager entityManager;

    /**
     * Genera bookings de forma asíncrona procesando en chunks para optimizar memoria.
     * Este método se ejecuta en un hilo separado del pool async.
     * Nota: No lleva @Transactional aquí porque cada llamada a bookingService.create()
     * ya maneja su propia transacción, lo que permite que Hibernate use batch inserts correctamente.
     */
    @Override
    @Async("taskExecutor")
    public void generateBookingsAsync(ServiceEntity service, List<AvailabilityEntity> availabilities,
                                      Set<LocalDate> unavailabilities) {
        log.info("Iniciando generación asíncrona de bookings para servicio ID: {}, futureDays: {}", 
                service.getId(), service.getFutureDays());
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(service.getFutureDays());
        
        // Procesar en chunks de 30 días para reducir memoria
        final int DAYS_PER_CHUNK = 30;
        final int BATCH_SIZE = 250; // Reducido para turnos de 5 minutos
        
        long totalBookings = 0;
        
        for (LocalDate chunkStart = startDate; chunkStart.isBefore(endDate); chunkStart = chunkStart.plusDays(DAYS_PER_CHUNK)) {
            LocalDate chunkEnd = chunkStart.plusDays(DAYS_PER_CHUNK).isAfter(endDate) 
                    ? endDate 
                    : chunkStart.plusDays(DAYS_PER_CHUNK);
            
            List<Booking> bookings = new ArrayList<>();
            
            for (LocalDate date = chunkStart; !date.isAfter(chunkEnd); date = date.plusDays(1)) {
                final LocalDate currentDate = date;
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

                if (unavailabilities == null || !unavailabilities.contains(currentDate)) {
                    // Usar getReference para evitar cargar el servicio completo y sus relaciones lazy
                    ServiceEntity serviceRef = entityManager.getReference(ServiceEntity.class, service.getId());
                    
                    availabilities.stream()
                            .filter(a -> a.getDayOfWeek() == dayOfWeek.getValue())
                            .forEach(a -> {
                                LocalTime currentTime = a.getStartTime();
                                // Calcular el intervalo real entre turnos: duración + offset
                                int intervalMinutes = service.getDurationMinutes() + 
                                                    (service.getOffsetMinutes() != null ? service.getOffsetMinutes() : 0);
                                
                                while (!currentTime.plusMinutes(service.getDurationMinutes()).isAfter(a.getEndTime())) {
                                    Booking booking = new Booking();
                                    booking.setStartTime(LocalDateTime.of(currentDate, currentTime));
                                    booking.setEndTime(LocalDateTime.of(currentDate, 
                                            currentTime.plusMinutes(service.getDurationMinutes())));
                                    booking.setStatus(BookingStatus.FREE);
                                    booking.setService(serviceRef);
                                    booking.setFreeSlots(service.getCapacity());
                                    booking.setCreatedAt(DateUtils.getCurrentDateTime());
                                    bookings.add(booking);
                                    // Avanzar usando el intervalo (duración + offset)
                                    currentTime = currentTime.plusMinutes(intervalMinutes);

                                    // Flush inmediato al alcanzar el batch
                                    if (bookings.size() >= BATCH_SIZE) {
                                        bookingService.create(new ArrayList<>(bookings));
                                        bookings.clear();
                                    }
                                }
                            });
                }
            }

            // Guardar bookings restantes del chunk
            if (!bookings.isEmpty()) {
                bookingService.create(bookings);
                totalBookings += bookings.size();
                bookings.clear();
            }
            
            log.debug("Chunk procesado: {} a {} ({} bookings acumulados)", 
                    chunkStart, chunkEnd, totalBookings);
        }
        
        log.info("Generación de bookings completada para servicio ID: {} - Total: {} bookings", 
                service.getId(), totalBookings);
    }
}
