package com.waturnos.service;

import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import java.util.List;

/**
 * The Interface BookingService.
 */
public interface BookingService {
	
	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	List<Booking> findByOrganization(Long organizationId);

	/**
	 * Find by status.
	 *
	 * @param status the status
	 * @return the list
	 */
	List<Booking> findByStatus(BookingStatus status);

	/**
	 * Creates the.
	 *
	 * @param booking the booking
	 * @return the booking
	 */
	Booking create(Booking booking);

	/**
	 * Update status.
	 *
	 * @param id the id
	 * @param status the status
	 * @return the booking
	 */
	Booking updateStatus(Long id, BookingStatus status);


	/**
	 * Assign.
	 *
	 * @param id the id
	 * @param clientId the client id
	 * @return the booking
	 */
	Booking assignBookingToClient(Long id, Long clientId);

	/**
	 * Cancel.
	 *
	 * @param id the id
	 * @param reason the reason
	 * @return the booking
	 */
	Booking cancelBooking(Long id, String reason);

	List<Booking> findAll();
}
