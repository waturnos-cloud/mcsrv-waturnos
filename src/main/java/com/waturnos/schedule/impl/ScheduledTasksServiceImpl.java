package com.waturnos.schedule.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.waturnos.entity.extended.BookingReminder;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.schedule.ScheduledTasks;
import com.waturnos.service.impl.ServiceEntityServiceImpl;
import com.waturnos.service.SyncTaskService;
import com.waturnos.enums.ScheduleType;
import com.waturnos.enums.ExecutionStatus;
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
	@Override
    @Scheduled(cron = "${app.scheduling.add-free-bookings-cron}")
	public void addBookingNextDay() {
		java.time.LocalDate today = java.time.LocalDate.now();
		// Evitar doble ejecución en el mismo día si ya fue registrada
		if (syncTaskService.wasExecutedOn(ScheduleType.ADD_NEW_BOOKINGS, today)) {
			log.info("ADD_NEW_BOOKINGS ya se ejecutó hoy, se omite ejecución.");
			return;
		}
		// Extiende bookings: agrega 1 día más desde la última fecha de cada servicio
		List<ServiceEntity> services = serviceRepository.findAll();
		if (services.isEmpty()) {
			log.info("No hay servicios para extender bookings");
			syncTaskService.recordExecution(ScheduleType.ADD_NEW_BOOKINGS, java.time.LocalDate.now(),
					ExecutionStatus.SUCCESS, String.format("{\"successCount\":%d,\"errorCount\":%d}", 0, 0));
			return;
		}
		int success = 0;
		int error = 0;
		for (ServiceEntity service : services) {
			try {
				serviceEntityService.extendBookingsByOneDay(service);
				success++;
			} catch (Exception e) {
				log.error("Error extendiendo bookings para servicio {}", service.getId(), e);
				error++;
			}
		}
		String details = String.format("{\"successCount\":%d,\"errorCount\":%d}", success, error);
		syncTaskService.recordExecution(ScheduleType.ADD_NEW_BOOKINGS, java.time.LocalDate.now(), ExecutionStatus.SUCCESS, details);
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
