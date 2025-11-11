package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.request.AssignBooking;
import com.waturnos.dto.request.CancelBooking;
import com.waturnos.entity.Booking;
import com.waturnos.mapper.BookingMapper;
import com.waturnos.service.BookingService;

/**
 * The Class BookingController.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {
	
	/** The service. */
	private final BookingService service;
	
	/** The mapper. */
	private final BookingMapper mapper;

	/**
	 * Instantiates a new booking controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public BookingController(BookingService s, BookingMapper m) {
		this.service = s;
		this.mapper = m;
	}


	/**
	 * Gets the by service id.
	 *
	 * @param serviceId the service id
	 * @return the by service id
	 */
	@GetMapping("/service/{serviceId}")
	public ResponseEntity<List<BookingDTO>> getByServiceId(@PathVariable Long serviceId) {
		return ResponseEntity.ok(service.findByServiceId(serviceId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<List<BookingDTO>>> create(@RequestBody List<BookingDTO> dtos) {
		List<Booking> bookings = service.create(mapper.toEntityList(dtos));
		return ResponseEntity.ok(new ApiResponse<>(true, "Bookings created", mapper.toDtoList(bookings)));
	}


	
	/**
	 * Update status.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<BookingDTO>> assingBooking(@RequestBody AssignBooking dto) {
		
		Booking updated = service.assignBookingToClient(dto.getId(), dto.getClientId());
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking assigned", mapper.toDto(updated)));
	}
	
	/**
	 * Update status.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping("/cancel")
	public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@RequestBody CancelBooking dto) {
		
		Booking canceled = service.cancelBooking(dto.getId(), dto.getReason());
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking canceled", mapper.toDto(canceled)));
	}
	
	/**
	 * Gets the today bookings.
	 *
	 * @return the today bookings
	 */
	@GetMapping("/today")
	public ResponseEntity<ApiResponse<List<BookingDTO>>> getTodayBookings(
	        @RequestParam(name = "providerId", required = false) Long providerId) {

	    List<Booking> today;
	    if (providerId != null) {
	        today = service.findBookingsForTodayByProvider(providerId);
	    } else {
	        today = service.findBookingsForToday();
	    }

	    return ResponseEntity.ok(
	        new ApiResponse<>(true, "Bookings for today", mapper.toDtoList(today))
	    );
	}

}
