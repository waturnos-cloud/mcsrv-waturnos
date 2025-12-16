package com.waturnos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.extended.BookingSummaryDetail;
import com.waturnos.enums.BookingStatus;

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
	 * @return the list
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

	/**
	 * Find bookings for today by provider.
	 *
	 * @param providerId the provider id
	 * @return the list
	 */
	Map<Long, List<BookingSummaryDetail>> findBookingsForTodayByProvider(Long providerId);

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



	/**
	 * Find by provider between dates.
	 *
	 * @param providerId the provider id
	 * @param start the start
	 * @param end the end
	 * @return the map
	 */

	Map<LocalDate, List<ServiceWithBookingsDTO>> findByRange(Long providerId, LocalDate start, LocalDate end,
			Long serviceId);

	/**
	 * Find booking details by id.
	 *
	 * @param bookingId the booking id
	 * @return the booking details DTO
	 */
	com.waturnos.dto.response.BookingDetailsDTO findBookingDetailsById(Long bookingId);

	/**
	 * Find the maximum (latest) booking date for a given service.
	 *
	 * @param serviceId the service id
	 * @return the latest LocalDate with bookings, or null if none exist
	 */
	LocalDate findMaxBookingDateByServiceId(Long serviceId);

	/**
	 * Find grouped availability by service type (category) for a specific date.
	 * This groups all services of the same type and shows aggregated availability.
	 *
	 * @param categoryId the category/type id
	 * @param date the date to check availability
	 * @param providerId the provider id
	 * @return list of grouped availability slots
	 */
	List<com.waturnos.dto.response.GroupedAvailabilityDTO> findGroupedAvailabilityByType(
			Long categoryId, LocalDate date, Long providerId);

	/**
	 * Create overbooking - creates a booking with status RESERVED and assigns it to a client.
	 * Validates that the client belongs to the organization and the service is from the same organization.
	 *
	 * @param booking the booking to create
	 * @param clientId the client id to assign
	 * @return the created booking
	 */
	Booking createOverBooking(Booking booking, Long clientId);

	/**
	 * Reassign booking - cancels the actual booking and assigns the client to a new booking.
	 *
	 * @param actualBookingId the actual booking id to cancel
	 * @param newBookingId the new booking id to assign
	 * @param clientId the client id to assign to the new booking
	 * @return the new booking with the client assigned
	 */
	Booking reassignBooking(Long actualBookingId, Long newBookingId, Long clientId);
	
	/**
	 * Assign.
	 *
	 * @param id       the id
	 * @return the booking
	 */
	Booking completedBookingToClient(Long id);

	/**
	 * Reserve booking after cancel.
	 * Reserva un turno y lo marca con estado RESERVED_AFTER_CANCEL.
	 *
	 * @param bookingId the booking id
	 * @param clientId the client id
	 * @return the booking reserved
	 */
	Booking reserveBookingAfterCancel(Long bookingId, Long clientId);

}
