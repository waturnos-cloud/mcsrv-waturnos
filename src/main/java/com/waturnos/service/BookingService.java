package com.waturnos.service;

import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import java.util.List;

/**
 * The Interface BookingService.
 */
public interface BookingService {

	/**
	 * Find by status.
	 *
	 * @param serviceId the service id
	 * @return the list
	 */
	List<Booking> findByServiceId(Long serviceId);

	/**
	 * Creates the.
	 *
	 * @param list the list
	 */
	List<Booking> create(List<Booking> list);

	/**
	 * Update status.
	 *
	 * @param id     the id
	 * @param status the status
	 * @return the booking
	 */
	Booking updateStatus(Long id, BookingStatus status);

	/**
	 * Assign.
	 *
	 * @param id       the id
	 * @param clientId the client id
	 * @return the booking
	 */
	Booking assignBookingToClient(Long id, Long clientId);

	/**
	 * Cancel.
	 *
	 * @param id     the id
	 * @param reason the reason
	 * @return the booking
	 */
	Booking cancelBooking(Long id, String reason);

}
