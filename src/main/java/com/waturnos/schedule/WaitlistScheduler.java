package com.waturnos.schedule;

import com.waturnos.entity.Booking;
import com.waturnos.entity.WaitlistEntry;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.WaitlistStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.WaitlistEntryRepository;
import com.waturnos.service.WaitlistService;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task para gestionar expiración automática de notificaciones de waitlist.
 * Ejecuta cada minuto para verificar si hay notificaciones que superaron su tiempo de expiración
 * configurado en service.waitListTime.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WaitlistScheduler {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final WaitlistService waitlistService;
    private final BookingRepository bookingRepository;

    /**
     * Ejecuta cada minuto para buscar y expirar notificaciones que superaron su tiempo límite.
     * El tiempo de expiración (waitListTime) está configurado por servicio, típicamente 15 minutos.
     * 
     * Flujo:
     * 1. Busca entradas NOTIFIED donde expiresAt < now()
     * 2. Marca cada entrada como EXPIRED
     * 3. Notifica al siguiente cliente en la cola para ese servicio/fecha
     */
    @Scheduled(cron = "0 * * * * *") // Cada minuto: segundo 0 de cada minuto
    @Transactional
    public void expireOldNotifications() {
        log.debug("Ejecutando tarea programada: expiración de notificaciones de waitlist");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<WaitlistEntry> expiredEntries = waitlistEntryRepository.findExpiredNotifications(now);
            
            if (expiredEntries.isEmpty()) {
                log.debug("No hay notificaciones expiradas en este momento");
                return;
            }
            
            log.info("Encontradas {} notificaciones expiradas, procesando...", expiredEntries.size());
            
            for (WaitlistEntry entry : expiredEntries) {
                try {
                    log.info("Expirando notificación de waitlist ID={} para cliente={}, servicio={}, fecha={}", 
                            entry.getId(), 
                            entry.getClient().getId(), 
                            entry.getService().getId(), 
                            entry.getDate());
                    
                    // Marcar como expirado
                    entry.setStatus(WaitlistStatus.EXPIRED);
                    waitlistEntryRepository.save(entry);
                    
                    // Si la entrada expirada estaba asociada a un booking específico, notificar al siguiente
                    if (entry.getSpecificBooking() != null) {
                        log.info("Notificando al siguiente en la cola para el booking ID={}", entry.getSpecificBooking().getId());
                        waitlistService.notifyNextInLine(entry.getSpecificBooking());
                    } else {
                        log.info("Entrada de waitlist tipo TIME_WINDOW expirada, no hay booking específico para notificar");
                        Booking booking = entry.getSpecificBooking();
                        booking.setStatus(BookingStatus.FREE_AFTER_CANCEL);
                        booking.setUpdatedAt(DateUtils.getCurrentDateTime());
                        bookingRepository.save(booking);
                    }
                    
                    log.info("Notificación expirada exitosamente");
                    
                } catch (Exception e) {
                    log.error("Error al procesar entrada expirada ID={}: {}", entry.getId(), e.getMessage(), e);
                    // Continuar con las demás entradas aunque una falle
                }
            }
            
            log.info("Proceso de expiración completado. Procesadas {} entradas", expiredEntries.size());
            
        } catch (Exception e) {
            log.error("Error general en tarea de expiración de waitlist: {}", e.getMessage(), e);
        }
    }
}
