package com.waturnos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.utils.DateUtils;

/**
 * The Class BookingServiceImpl.
 */
@Service
public class BookingServiceImpl implements BookingService {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/** The client repository. */
	private final ClientRepository clientRepository;

	/**
	 * Instantiates a new booking service impl.
	 *
	 * @param bookingRepository the booking repository
	 * @param clientRepository  the client repository
	 */
	public BookingServiceImpl(BookingRepository bookingRepository, ClientRepository clientRepository) {
		this.bookingRepository = bookingRepository;
		this.clientRepository = clientRepository;
	}

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

		return bookingRepository.save(booking);

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

		return bookingRepository.save(booking);


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

	
	public List<Booking> findBookingsForTodayByProvider(Long providerId) {
	    LocalDate today = LocalDate.now();
	    LocalDateTime startOfDay = today.atStartOfDay();
	    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
	    return bookingRepository.findByProviderAndStartTimeBetween(providerId, startOfDay, endOfDay);
	}
}
