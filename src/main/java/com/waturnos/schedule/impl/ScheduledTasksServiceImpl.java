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
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.schedule.ScheduledTasks;
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
	
	/**
	 * Remember booking to users.
	 */
	@Override
    @Scheduled(cron = "${app.scheduling.notify-clients-cron}")
	public void rememberBookingToUsers() {
        List<BookingReminder> reminders = bookingRepository.findBookingsForTomorrow();

        if (reminders.isEmpty()) {
           log.info("No hay reservas para notificar mañana.");
            return;
        }
        reminders.forEach(reminder -> {
        	notificationFactory.send(buildRequestReminderBooking(reminder)) ;             
        });
		
	}

	/**
	 * Adds the booking next day.
	 */
	@Override
    @Scheduled(cron = "${app.scheduling.add-free-bookings-cron}")
	public void addBookingNextDay() {
		
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
