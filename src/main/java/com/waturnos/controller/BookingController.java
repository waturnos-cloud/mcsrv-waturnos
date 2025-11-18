package com.waturnos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.waturnos.dto.response.BookingExtendedDTO;
import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.dto.response.ServiceListWithBookingDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.extended.BookingSummaryDetail;
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
	 * @param dtos the dtos
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
	 * @param providerId the provider id
	 * @return the today bookings
	 */
	@GetMapping("/today")
	public ResponseEntity<ApiResponse<List<?>>> getTodayBookings(
			@RequestParam(name = "providerId", required = true) Long providerId) {

		Map<Long, List<BookingSummaryDetail>> groupedMap = service.findBookingsForTodayByProvider(providerId);

		List<ServiceListWithBookingDTO> result = groupedMap.entrySet().stream().map(entry -> {
			List<BookingSummaryDetail> bookingsForService = entry.getValue();

			List<BookingExtendedDTO> extendedBookings = mapper.toExtendedDTOList(bookingsForService);

			BookingSummaryDetail firstBooking = bookingsForService.get(0);

			ServiceListWithBookingDTO serviceGroup = new ServiceListWithBookingDTO();
			serviceGroup.setId(entry.getKey());
			serviceGroup.setName(firstBooking.getServiceName());
			serviceGroup.setList(extendedBookings);
			serviceGroup.setCapacity(firstBooking.getServiceCapacity());
			return serviceGroup;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(new ApiResponse<>(true, "Bookings for today", result));
	}

	/**
	 * Gets the bookings by date range.
	 *
	 * @param providerId the provider id
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return the bookings by date range
	 */
	@GetMapping("/range")
	public ResponseEntity<?> getBookingsByRange(
	        @RequestParam Long providerId,
	        @RequestParam String startDate,
	        @RequestParam String endDate,
	        @RequestParam(required = false) Long serviceId) {

	    LocalDate start = LocalDate.parse(startDate);
	    LocalDate end = LocalDate.parse(endDate);

	    Map<LocalDate, List<ServiceWithBookingsDTO>> result =
	            service.findByRange(providerId, start, end, serviceId);

	    return ResponseEntity.ok(new ApiResponse<>(true, "Bookings grouped by day", result));
	}
	/**
	 * Obtiene el conteo de reservas por estado (CANCELLED, RESERVED, COMPLETED,
	 * PENDING) para un día o rango de días específico y un Provider ID.
	 *
	 * @param fromDate the from date
	 * @param toDate the to date
	 * @param providerId the provider id
	 * @return the count bookings
	 */
	@GetMapping("/count")
	public ResponseEntity<List<CountBookingDTO>> getCountBookings(
			@RequestParam(name = "fromDate", required = true) LocalDate fromDate,
			@RequestParam(name = "toDate", required = false) LocalDate toDate,
			@RequestParam(name = "providerId", required = true) Long providerId) {

		List<CountBookingDTO> counts = service.countBookingsByDateRangeAndProvider(fromDate,
				toDate != null ? toDate : fromDate, providerId);

		return ResponseEntity.ok(counts);
	}

}
