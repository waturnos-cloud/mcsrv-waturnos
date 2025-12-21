package com.waturnos.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${app.scheduling.run-tasks-on-startup:false}")
    private boolean runTasksOnStartup;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!runTasksOnStartup) {
            log.info("StartupTasksChecker desactivado (app.scheduling.run-tasks-on-startup=false). Las tareas se ejecutarán según su programación cron.");
            return;
        }
        
        log.info("StartupTasksChecker activado. Verificando tareas pendientes...");
        LocalDate today = LocalDate.now();
        
        try {
            if (!syncTaskService.wasExecutedOn(ScheduleType.ADD_NEW_BOOKINGS, today)) {
                log.info("ADD_NEW_BOOKINGS no se ejecutó hoy. Ejecutando now...");
                scheduledTasksService.addBookingNextDay();
            }
        } catch (Exception e) {
            log.error("Fallo verificando/ejecutando ADD_NEW_BOOKINGS al iniciar", e);
        }

        try {
            if (!syncTaskService.wasExecutedOn(ScheduleType.REMEMBER_BOOKING_TO_USERS, today)) {
                log.info("REMEMBER_BOOKING_TO_USERS no se ejecutó hoy. Ejecutando now...");
                scheduledTasksService.rememberBookingToUsers();
            }
        } catch (Exception e) {
            log.error("Fallo verificando/ejecutando REMEMBER_BOOKING_TO_USERS al iniciar", e);
        }
    }
}
