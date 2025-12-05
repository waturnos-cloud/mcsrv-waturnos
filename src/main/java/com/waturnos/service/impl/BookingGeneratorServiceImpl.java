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
import com.waturnos.entity.BookingClient;
import com.waturnos.entity.Recurrence;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.RecurrenceType;
import com.waturnos.repository.BookingRepository;
import com.waturnos.service.BookingGeneratorService;
import com.waturnos.service.BookingService;
import com.waturnos.service.RecurrenceService;
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
    private final RecurrenceService recurrenceService;
    private final BookingRepository bookingRepository;

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
        
        // Aplicar recurrencias activas a los bookings recién generados
        applyRecurrencesToNewBookings(service.getId(), startDate, endDate);
    }
    
    /**
     * Aplica las recurrencias activas a los bookings recién generados
     */
    private void applyRecurrencesToNewBookings(Long serviceId, LocalDate startDate, LocalDate endDate) {
        log.info("Aplicando recurrencias activas a bookings del servicio {}", serviceId);
        
        try {
            List<Recurrence> activeRecurrences = recurrenceService.getAllActiveRecurrences();
            
            // Filtrar por servicio
            List<Recurrence> serviceRecurrences = activeRecurrences.stream()
                .filter(r -> r.getService().getId().equals(serviceId))
                .toList();
            
            if (serviceRecurrences.isEmpty()) {
                log.debug("No hay recurrencias activas para el servicio {}", serviceId);
                return;
            }
            
            log.info("Encontradas {} recurrencias activas para aplicar", serviceRecurrences.size());
            
            for (Recurrence recurrence : serviceRecurrences) {
                // Validar si la recurrencia aún es válida
                if (!isRecurrenceValid(recurrence, endDate)) {
                    log.debug("Recurrencia {} no es válida para este rango de fechas", recurrence.getId());
                    continue;
                }
                
                // Buscar bookings que coincidan con el patrón de recurrencia
                List<Booking> matchingBookings = bookingRepository.findByServiceIdAndDateRange(
                    serviceId,
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
                ).stream()
                    .filter(b -> b.getStartTime().getDayOfWeek().getValue() == recurrence.getDayOfWeek())
                    .filter(b -> b.getStartTime().toLocalTime().equals(recurrence.getTimeSlot()))
                    .filter(b -> b.getStatus() == BookingStatus.FREE)
                    .filter(b -> b.getRecurrence() == null) // Solo asignar si no tiene recurrencia
                    .toList();
                
                // Limitar si es COUNT
                List<Booking> bookingsToAssign = matchingBookings;
                if (recurrence.getRecurrenceType() == RecurrenceType.COUNT && 
                    recurrence.getOccurrenceCount() != null) {
                    
                    // Contar cuántos bookings ya tiene asignados esta recurrencia
                    long currentCount = bookingRepository.findByServiceId(serviceId).stream()
                        .filter(b -> b.getRecurrence() != null && b.getRecurrence().getId().equals(recurrence.getId()))
                        .count();
                    
                    int remaining = recurrence.getOccurrenceCount() - (int)currentCount;
                    if (remaining <= 0) {
                        log.debug("Recurrencia {} ya alcanzó su límite de ocurrencias", recurrence.getId());
                        continue;
                    }
                    
                    bookingsToAssign = matchingBookings.stream()
                        .limit(remaining)
                        .toList();
                }
                
                // Asignar cada booking al cliente
                log.info("Asignando {} bookings automáticamente para recurrencia {}", 
                    bookingsToAssign.size(), recurrence.getId());
                
                for (Booking booking : bookingsToAssign) {
                    BookingClient bc = BookingClient.builder()
                        .booking(booking)
                        .client(recurrence.getClient())
                        .build();
                    
                    booking.addBookingClient(bc);
                    booking.setRecurrence(recurrence);
                    booking.setUpdatedAt(DateUtils.getCurrentDateTime());
                }
                
                if (!bookingsToAssign.isEmpty()) {
                    bookingRepository.saveAll(bookingsToAssign);
                }
            }
            
            log.info("Recurrencias aplicadas exitosamente");
            
        } catch (Exception e) {
            log.error("Error aplicando recurrencias: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Valida si una recurrencia sigue siendo válida para el rango de fechas
     */
    private boolean isRecurrenceValid(Recurrence recurrence, LocalDate endDate) {
        switch (recurrence.getRecurrenceType()) {
            case END_DATE:
                return recurrence.getEndDate() == null || 
                       !recurrence.getEndDate().isBefore(LocalDate.now());
            case FOREVER:
                return true;
            case COUNT:
                // Siempre validar si es COUNT, se verifica el límite en el método principal
                return true;
            default:
                return false;
        }
    }
}
