package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import com.waturnos.enums.BookingStatus;
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
	 * Gets the by organization.
	 *
	 * @param orgId the org id
	 * @return the by organization
	 */
	@GetMapping("/organization/{orgId}")
	public ResponseEntity<List<BookingDTO>> getByOrganization(@PathVariable Long orgId) {
		return ResponseEntity.ok(service.findByOrganization(orgId).stream().map(mapper::toDto).toList());
	}

	/**
	 * Gets the by status.
	 *
	 * @param status the status
	 * @return the by status
	 */
	@GetMapping("/status/{status}")
	public ResponseEntity<List<BookingDTO>> getByStatus(@PathVariable BookingStatus status) {
		return ResponseEntity.ok(service.findByStatus(status).stream().map(mapper::toDto).toList());
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<BookingDTO>> create(@RequestBody BookingDTO dto) {
		Booking created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking created", mapper.toDto(created)));
	}


	
	/**
	 * Update status.
	 *
	 * @param id the id
	 * @param status the status
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
	 * @param id the id
	 * @param status the status
	 * @return the response entity
	 */
	@PostMapping("/cancel")
	public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@RequestBody CancelBooking dto) {
		
		Booking canceled = service.cancelBooking(dto.getId(), dto.getReason());
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking canceled", mapper.toDto(canceled)));
	}
	
	
	
	
	/**
	 * Update status.
	 *
	 * @param id the id
	 * @param status the status
	 * @return the response entity
	 */
	@PatchMapping("/{id}/status")
	public ResponseEntity<ApiResponse<BookingDTO>> updateStatus(@PathVariable Long id,
			@RequestParam BookingStatus status) {
		Booking updated = service.updateStatus(id, status);
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking status updated", mapper.toDto(updated)));
	}
}
