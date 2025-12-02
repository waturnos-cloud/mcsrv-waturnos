package com.waturnos.config;

import java.time.LocalDate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.waturnos.enums.ScheduleType;
import com.waturnos.schedule.impl.ScheduledTasksServiceImpl;
import com.waturnos.service.SyncTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifica en el arranque si las tareas programadas ya se ejecutaron hoy.
 * Si no, las dispara manualmente para no perder la ventana.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupTasksChecker {

    private final SyncTaskService syncTaskService;
    private final ScheduledTasksServiceImpl scheduledTasksService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        LocalDate today = LocalDate.now();
        try {
            if (!syncTaskService.wasExecutedOn(ScheduleType.ADD_NEW_BOOKINGS, today)) {
                log.info("ADD_NEW_BOOKINGS no se ejecutó hoy. Ejecutando now...");
                // La implementación ya procesa todos los servicios y registra ejecución
                scheduledTasksService.addBookingNextDay();
            }
        } catch (Exception e) {
            log.error("Fallo verificando/ejecutando ADD_NEW_BOOKINGS al iniciar", e);
        }

        try {
            if (!syncTaskService.wasExecutedOn(ScheduleType.REMEMBER_BOOKING_TO_USERS, today)) {
                log.info("REMEMBER_BOOKING_TO_USERS no se ejecutó hoy. Ejecutando now...");
                // Delegamos el registro de ejecución al servicio interno
                scheduledTasksService.rememberBookingToUsers();
            }
        } catch (Exception e) {
            log.error("Fallo verificando/ejecutando REMEMBER_BOOKING_TO_USERS al iniciar", e);
        }
    }
}
