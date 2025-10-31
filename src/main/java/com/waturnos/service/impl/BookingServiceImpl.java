package com.waturnos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.enums.BookingStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
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
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	public List<Booking> findByOrganization(Long organizationId) {
		return bookingRepository.findByOrganizationId(organizationId);
	}

	/**
	 * Find by status.
	 *
	 * @param status the status
	 * @return the list
	 */
	@Override
	public List<Booking> findByStatus(BookingStatus status) {
		return bookingRepository.findByStatus(status);
	}

	/**
	 * Creates the.
	 *
	 * @param booking the booking
	 * @return the booking
	 */
	@Override
	public Booking create(Booking booking) {
		return bookingRepository.save(booking);
	}

	/**
	 * Update status.
	 *
	 * @param id     the id
	 * @param status the status
	 * @return the booking
	 */
	@Override
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
	 * @param status   the status
	 * @param clientId the client id
	 * @return the booking
	 */
	@Override
	public Booking assignBookingToClient(Long id, Long clientId) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		Client client = clientRepository.findById(id)
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
	 * @param id the id
	 * @param reason the reason
	 * @return the booking
	 */
	@Override
	public Booking cancelBooking(Long id, String reason) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		if (!booking.getStatus().equals(BookingStatus.RESERVED) &&
				!booking.getStatus().equals(BookingStatus.CONFIRMED))
				 {
			throw new EntityNotFoundException("Not valid status");
		}
		
		// TODO modificator? otro servicio para client y admin/manager?
		
		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancelReason(reason);
		
		return booking;

	}

	@Override
	public List<Booking> findAll() {
		return bookingRepository.findAll();
	}
}
