package com.waturnos.controller.stateless;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.controller.ApiResponse;
import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.mapper.BookingMapper;
import com.waturnos.service.BookingService;

/**
 * The Class BookingControllerStateless.
 * Endpoints públicos sin autenticación para operaciones específicas de booking.
 */
@RestController
@RequestMapping("/public/bookings")
public class BookingControllerStateless {

	/** The service. */
	private final BookingService service;

	/** The mapper. */
	private final BookingMapper mapper;

	/**
	 * Instantiates a new booking controller stateless.
	 *
	 * @param service the service
	 * @param mapper the mapper
	 */
	public BookingControllerStateless(BookingService service, BookingMapper mapper) {
		this.service = service;
		this.mapper = mapper;
	}

	/**
	 * Reserve booking after cancel.
	 * Reserva un turno y lo marca con estado RESERVED_AFTER_CANCEL.
	 *
	 * @param bookingId the booking id
	 * @param clientId the client id
	 * @return the response entity
	 */
	@PostMapping("/reserve")
	public ResponseEntity<ApiResponse<BookingDTO>> reserveBookingAfterCancel(
			@RequestParam Long bookingId,
			@RequestParam Long clientId) {

		Booking booking = service.reserveBookingAfterCancel(bookingId, clientId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking reserved after cancel", mapper.toDto(booking)));
	}
}
