package com.waturnos.schedule.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.extended.BookingReminder;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.ExecutionStatus;
import com.waturnos.enums.ScheduleType;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.schedule.ScheduledTasks;
import com.waturnos.service.SyncTaskService;
import com.waturnos.service.impl.ServiceEntityServiceImpl;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksServiceImpl implements ScheduledTasks{
	
	/** The booking repository. */
	private final BookingRepository bookingRepository;
	
	/** The notification factory. */
	private final NotificationFactory notificationFactory;
	
	/** The date forma email. */
	@Value("${app.datetime.email-format}")
	private String dateFormaEmail;

	@Value("${app.notification.HOME}")
	private String urlHome;
	
	/** The message source. */
	private final MessageSource messageSource;

	/** Service repository to iterate services. */
	private final ServiceRepository serviceRepository;

	/** Service logic to generate bookings per day. */
	private final ServiceEntityServiceImpl serviceEntityService;

	/** Unavailability service for holidays. */
	private final com.waturnos.service.UnavailabilityService unavailabilityService;

	private final SyncTaskService syncTaskService;
	
	/**
	 * Remember booking to users.
	 */
	@Override
    @Scheduled(cron = "${app.scheduling.notify-clients-cron}")
	public void rememberBookingToUsers() {
		java.time.LocalDate today = java.time.LocalDate.now();
		// Evitar doble ejecución en el mismo día si ya fue registrada
		if (syncTaskService.wasExecutedOn(ScheduleType.REMEMBER_BOOKING_TO_USERS, today)) {
			log.info("REMEMBER_BOOKING_TO_USERS ya se ejecutó hoy, se omite ejecución.");
			return;
		}
		List<BookingReminder> reminders = bookingRepository.findBookingsForTomorrow();

        if (reminders.isEmpty()) {
           log.info("No hay reservas para notificar mañana.");
			syncTaskService.recordExecution(ScheduleType.REMEMBER_BOOKING_TO_USERS, java.time.LocalDate.now(),
					ExecutionStatus.SUCCESS, String.format("{\"successCount\":%d,\"errorCount\":%d}", 0, 0));
           return;
        }
		int success = 0;
		int error = 0;
		for (BookingReminder reminder : reminders) {
			try {
				notificationFactory.send(buildRequestReminderBooking(reminder));
				success++;
			} catch (Exception e) {
				log.error("Error enviando recordatorio a {}", reminder.getEmail(), e);
				error++;
			}
		}
		String details = String.format("{\"successCount\":%d,\"errorCount\":%d}", success, error);
		syncTaskService.recordExecution(ScheduleType.REMEMBER_BOOKING_TO_USERS, java.time.LocalDate.now(), ExecutionStatus.SUCCESS, details);
		
	}

	/**
	 * Extends bookings by one day from the last existing booking date.
	 * Ejecuta a medianoche: extiende la agenda de cada servicio agregando 1 día más.
	 */

	@Value("${app.scheduling.add-bookings-page-size:15}")
	private int addBookingsPageSize;

	@Override
	@Scheduled(cron = "${app.scheduling.add-free-bookings-cron}")
	public void addBookingNextDay() {
		java.time.LocalDate today = java.time.LocalDate.now();
		// Evitar doble ejecución en el mismo día si ya fue registrada
		if (syncTaskService.wasExecutedOn(ScheduleType.ADD_NEW_BOOKINGS, today)) {
			log.info("ADD_NEW_BOOKINGS ya se ejecutó hoy, se omite ejecución.");
			return;
		}

		int page = 0;
		int success = 0;
		int error = 0;
		boolean hasMore = true;
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, addBookingsPageSize, org.springframework.data.domain.Sort.by("id").ascending());
		java.util.Set<java.time.LocalDate> unavailabilities = unavailabilityService.getHolidays();

		do {
			org.springframework.data.domain.Page<ServiceEntity> servicePage = serviceRepository.findAllActive(pageable);
			List<ServiceEntity> services = servicePage.getContent();
			if (services.isEmpty() && page == 0) {
				log.info("No hay servicios para extender bookings");
				syncTaskService.recordExecution(ScheduleType.ADD_NEW_BOOKINGS, java.time.LocalDate.now(),
						ExecutionStatus.SUCCESS, String.format("{\"successCount\":%d,\"errorCount\":%d}", 0, 0));
				return;
			}
			for (ServiceEntity service : services) {
				try {
					serviceEntityService.extendBookingsByOneDay(service, unavailabilities);
					success++;
				} catch (Exception e) {
					log.error("Error extendiendo bookings para servicio {}", service.getId(), e);
					error++;
				}
			}
			hasMore = servicePage.hasNext();
			page++;
			pageable = org.springframework.data.domain.PageRequest.of(page, addBookingsPageSize, org.springframework.data.domain.Sort.by("id").ascending());
		} while (hasMore);

		String details = String.format("{\"successCount\":%d,\"errorCount\":%d}", success, error);
		syncTaskService.recordExecution(ScheduleType.ADD_NEW_BOOKINGS, java.time.LocalDate.now(), ExecutionStatus.SUCCESS, details);
	}
	
	/**
	 * Completes reserved bookings at end of day.
	 * Runs at 23:50 to convert RESERVED to COMPLETED and RESERVED_AFTER_CANCEL to COMPLETED_AFTER_CANCEL.
	 * Processes all bookings up to current time to handle any missed executions from previous days.
	 */
	@Override
	@Scheduled(cron = "${app.scheduling.complete-reserved-bookings-cron:0 50 23 * * *}")
	@Transactional
	public void completeReservedBookings() {
		java.time.LocalDate today = java.time.LocalDate.now();
		
		// Evitar doble ejecución en el mismo día si ya fue registrada
		if (syncTaskService.wasExecutedOn(ScheduleType.COMPLETE_RESERVED_BOOKINGS, today)) {
			log.info("COMPLETE_RESERVED_BOOKINGS ya se ejecutó hoy, se omite ejecución.");
			return;
		}
		
		// Procesar todos los bookings hasta el momento actual (23:50)
		// Esto garantiza que si falló la ejecución anterior, se procesen los pendientes
		LocalDateTime now = LocalDateTime.now();
		List<BookingStatus> statusesToComplete = List.of(BookingStatus.RESERVED, BookingStatus.RESERVED_AFTER_CANCEL);
		
		List<Booking> bookingsToComplete = bookingRepository.findByStartTimeBeforeAndStatusIn(now, statusesToComplete);
		
		if (bookingsToComplete.isEmpty()) {
			log.info("No hay turnos para completar hasta las {}", now);
			syncTaskService.recordExecution(ScheduleType.COMPLETE_RESERVED_BOOKINGS, today,
					ExecutionStatus.SUCCESS, String.format("{\"processedCount\":%d,\"reservedToCompleted\":%d,\"reservedAfterCancelToCompletedAfterCancel\":%d}", 0, 0, 0));
			return;
		}
		
		int reservedToCompleted = 0;
		int reservedAfterCancelToCompletedAfterCancel = 0;
		
		for (Booking booking : bookingsToComplete) {
			if (booking.getStatus() == BookingStatus.RESERVED) {
				booking.setStatus(BookingStatus.COMPLETED);
				booking.setUpdatedAt(LocalDateTime.now());
				reservedToCompleted++;
				log.debug("Turno {} cambiado de RESERVED a COMPLETED (startTime: {})", 
						booking.getId(), booking.getStartTime());
			} else if (booking.getStatus() == BookingStatus.RESERVED_AFTER_CANCEL) {
				booking.setStatus(BookingStatus.COMPLETED_AFTER_CANCEL);
				booking.setUpdatedAt(LocalDateTime.now());
				reservedAfterCancelToCompletedAfterCancel++;
				log.debug("Turno {} cambiado de RESERVED_AFTER_CANCEL a COMPLETED_AFTER_CANCEL (startTime: {})", 
						booking.getId(), booking.getStartTime());
			}
		}
		
		bookingRepository.saveAll(bookingsToComplete);
		
		log.info("Turnos completados exitosamente: {} RESERVED->COMPLETED, {} RESERVED_AFTER_CANCEL->COMPLETED_AFTER_CANCEL", 
				reservedToCompleted, reservedAfterCancelToCompletedAfterCancel);
		
		String details = String.format("{\"processedCount\":%d,\"reservedToCompleted\":%d,\"reservedAfterCancelToCompletedAfterCancel\":%d}", 
				bookingsToComplete.size(), reservedToCompleted, reservedAfterCancelToCompletedAfterCancel);
		syncTaskService.recordExecution(ScheduleType.COMPLETE_RESERVED_BOOKINGS, today, ExecutionStatus.SUCCESS, details);
	}
	

	/**
	 * Builds the request reminder booking.
	 *
	 * @param bookingReminder the booking reminder
	 * @return the notification request
	 */
	private NotificationRequest buildRequestReminderBooking(BookingReminder bookingReminder ) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", bookingReminder.getFullName());
		properties.put("SERVICENAME", bookingReminder.getServiceName());
		properties.put("DATEBOOKING", DateUtils.format(bookingReminder.getStartTime(), dateFormaEmail));
		properties.put("URLHOME", urlHome);

		return NotificationRequest.builder().email(bookingReminder.getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.reminder.booking", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.REMINDER_BOOKING).properties(properties).build();
	}

}
