package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;

/**
 * The Class BookingServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/** The client repository. */
	private final ClientRepository clientRepository;
	
	/** The notification factory. */
	private final NotificationFactory notificationFactory;
	
	/** The message source. */
	private final MessageSource messageSource;
	
	/** The date forma email. */
	@Value("${app.datetime.email-format}")
	private String dateFormaEmail;
	
	@Value("${app.notification.HOME}")
	private String urlHome;
	
	/**
	 * Creates the.
	 *
	 * @param list the list
	 */
	@Override
	public List<Booking> create(List<Booking> list) {
		return bookingRepository.saveAll(list);
	}

	/**
	 * Update status.
	 *
	 * @param id     the id
	 * @param status the status
	 * @return the booking
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER })
	public Booking updateStatus(Long id, BookingStatus status) {
		Booking existing = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));
		existing.setStatus(status);
		return bookingRepository.save(existing);
	}

	/**
	 * Update.
	 *
	 * @param id       the id
	 * @param clientId the client id
	 * @return the booking
	 */
	@Override
	public Booking assignBookingToClient(Long id, Long clientId) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		Client client = clientRepository.findById(clientId)
				.orElseThrow(() -> new EntityNotFoundException("Client not found"));

		if (!booking.getStatus().equals(BookingStatus.PENDING)) {
			throw new EntityNotFoundException("Not valid status");
		}

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.RESERVED);
		booking.setClient(client);

		notificationFactory.send(buildRequest(client, booking));
		
		return bookingRepository.save(booking);

	}
	
	/**
	 * Builds the request.
	 *
	 * @param client the manager
	 * @param temporalPasswordUser 
	 * @return the notification request
	 */
	private NotificationRequest buildRequest(Client client, Booking booking) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", client.getFullName());
		properties.put("SERVICENAME", booking.getService().getName());
		properties.put("DATEBOOKING", DateUtils.format(booking.getStartTime(), dateFormaEmail));
		properties.put("URLHOME", urlHome);

		return NotificationRequest.builder().email(booking.getClient().getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.assign.booking", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.BOOKING_ASSIGN).properties(properties).build();
	}

	/**
	 * Cancel booking.
	 *
	 * @param id     the id
	 * @param reason the reason
	 * @return the booking
	 */
	@Override
	public Booking cancelBooking(Long id, String reason) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		if (!booking.getStatus().equals(BookingStatus.RESERVED)
				&& !booking.getStatus().equals(BookingStatus.CONFIRMED)) {
			throw new EntityNotFoundException("Not valid status");
		}

		// TODO modificator? otro servicio para client y admin/manager?

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancelReason(reason);

		return booking;

	}

	/**
	 * Find by service id.
	 *
	 * @param serviceId the service id
	 * @return the list
	 */
	@Override
	public List<Booking> findByServiceId(Long serviceId) {
		return bookingRepository.findByServiceId(serviceId);
	}

	/**
	 * Find bookings for today.
	 *
	 * @return the list
	 */
	@Override
	public List<Booking> findBookingsForToday() {
	    
	    OffsetDateTime now = OffsetDateTime.now();
	    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
	    LocalDateTime endOfDay = startOfDay.plusDays(1);

	    return bookingRepository.findByStartTimeBetween(startOfDay, endOfDay);
	    
	}

}
