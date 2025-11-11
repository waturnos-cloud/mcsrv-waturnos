package com.waturnos.service;

import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;

import java.time.LocalDate;
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

	/**
	 * Find bookings for today.
	 *
	 * @return the list
	 */
	List<Booking> findBookingsForToday();

	List<Booking> findBookingsForTodayByProvider(Long providerId);

	/**
	 * Count bookings by date range and provider.
	 *
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param providerId the provider id
	 * @return the list
	 */
	List<CountBookingDTO> countBookingsByDateRangeAndProvider( LocalDate fromDate, 
            LocalDate toDate, 
            Long providerId);

}
