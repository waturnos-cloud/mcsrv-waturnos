package com.waturnos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.request.AssignBooking;
import com.waturnos.dto.request.CancelBooking;
import com.waturnos.dto.request.CreateRecurrenceRequest;
import com.waturnos.dto.request.OverBookingDTO;
import com.waturnos.dto.request.ReassignBooking;
import com.waturnos.dto.response.BookingDetailsDTO;
import com.waturnos.dto.response.BookingExtendedDTO;
import com.waturnos.dto.response.CheckRecurrenceResponse;
import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.dto.response.RecurrenceDTO;
import com.waturnos.dto.response.ServiceListWithBookingDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.User;
import com.waturnos.entity.extended.BookingSummaryDetail;
import com.waturnos.enums.BookingStatus;
import com.waturnos.mapper.BookingMapper;
import com.waturnos.security.ClientPrincipal;
import com.waturnos.service.BookingService;
import com.waturnos.service.PaymentProviderService;
import com.waturnos.service.RecurrenceService;

/**
 * The Class BookingController.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

	/** The service. */
	private final BookingService service;
	
	/** The recurrence service. */
	private final RecurrenceService recurrenceService;

	/** The mapper. */
	private final BookingMapper mapper;
	
	/** The payment provider service. */
	private final PaymentProviderService paymentProviderService;

	/**
	 * Instantiates a new booking controller.
	 *
	 * @param s the s
	 * @param m the m
	 * @param rs the recurrence service
	 * @param pps the payment provider service
	 */
	public BookingController(BookingService s, BookingMapper m, RecurrenceService rs, PaymentProviderService pps) {
		this.service = s;
		this.mapper = m;
		this.recurrenceService = rs;
		this.paymentProviderService = pps;
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
		
		// Guardar booking props si existen
		if (dto.getBookingProps() != null && !dto.getBookingProps().isEmpty()) {
			service.saveBookingProps(dto.getId(), dto.getBookingProps());
		}
		
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
	 * Reassign booking.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping("/reassign")
	public ResponseEntity<ApiResponse<BookingDTO>> reassignBooking(@RequestBody ReassignBooking dto) {

		Booking newBooking = service.reassignBooking(dto.getActualBookingId(), dto.getNewBookingId(), dto.getClientId());
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking reassigned successfully", mapper.toDto(newBooking)));
	}

	/**
	 * Update booking status.
	 *
	 * @param bookingId the booking id
	 * @param status the new status
	 * @return the response entity
	 */
	@PutMapping("/{bookingId}/status")
	public ResponseEntity<ApiResponse<BookingDTO>> updateBookingStatus(
			@PathVariable Long bookingId, 
			@RequestParam BookingStatus status) {
		
		Booking updated = service.updateStatus(bookingId, status);
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking status updated", mapper.toDto(updated)));
	}

	/**
	 * Completed booking.
	 *
	 * @param bookingId the booking id
	 * @return the response entity
	 */
	@PutMapping("completed/{bookingId}")
	public ResponseEntity<ApiResponse<BookingDTO>> completedBooking (
			@PathVariable Long bookingId) {
		
		Booking updated = service.completedBookingToClient(bookingId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking status updated", mapper.toDto(updated)));
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
	 * Gets the booking details by id.
	 *
	 * @param bookingId the booking id
	 * @return the booking details
	 */
	@GetMapping("/{bookingId}")
	public ResponseEntity<ApiResponse<BookingDetailsDTO>> getBookingDetails(@PathVariable Long bookingId) {
		BookingDetailsDTO bookingDetails = service.findBookingDetailsById(bookingId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Booking details retrieved", bookingDetails));
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
	    
	    // Agregar payment providers del provider a cada servicio
	    var paymentProviders = paymentProviderService.getAllPaymentProviders(providerId);
	    result.values().forEach(serviceList -> 
	        serviceList.forEach(serviceDto -> 
	            serviceDto.setPaymentProviders(paymentProviders)
	        )
	    );

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

	/**
	 * Gets grouped availability by service type/category for a specific date.
	 * This endpoint aggregates availability across all services of the same type,
	 * useful when the client doesn't care which specific service (e.g., which court)
	 * is available, only that there is availability.
	 *
	 * @param categoryId the category/type id
	 * @param date the date in YYYY-MM-DD format
	 * @param providerId the provider id
	 * @return grouped availability slots
	 */
	@GetMapping("/availability-by-type")
	public ResponseEntity<ApiResponse<List<com.waturnos.dto.response.GroupedAvailabilityDTO>>> 
			getGroupedAvailabilityByType(
			@RequestParam Long categoryId,
			@RequestParam String date,
			@RequestParam Long providerId) {

		LocalDate requestedDate = LocalDate.parse(date);
		List<com.waturnos.dto.response.GroupedAvailabilityDTO> availability = 
				service.findGroupedAvailabilityByType(categoryId, requestedDate, providerId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Grouped availability retrieved", availability));
	}
	
	// ========== RECURRENCE ENDPOINTS ==========
	
	/**
	 * Check if a booking can be recurrent by analyzing future slots
	 * @param bookingId the booking id
	 * @return check recurrence response
	 */
	@GetMapping("/{bookingId}/check-recurrence")
	public ResponseEntity<ApiResponse<CheckRecurrenceResponse>> checkRecurrence(
			@PathVariable Long bookingId) {
		CheckRecurrenceResponse response = recurrenceService.checkRecurrence(bookingId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Recurrence check completed", response));
	}
	
	/**
	 * Create a recurrence for a booking
	 * @param request the recurrence request
	 * @param authentication the authentication
	 * @return the created recurrence
	 */
	@PostMapping("/set-recurrence")
	public ResponseEntity<ApiResponse<RecurrenceDTO>> setRecurrence(
			@RequestBody CreateRecurrenceRequest request,
			Authentication authentication) {
		Object principal = authentication.getPrincipal();
		Long userId = null;
		
		if (principal instanceof User) {
			userId = ((User) principal).getId();
		} else if (principal instanceof ClientPrincipal) {
			// Los clientes no tienen userId, usar null (el servicio lo manejará)
			userId = null;
		}
		
		RecurrenceDTO recurrence = recurrenceService.createRecurrence(request, userId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Recurrencia creada exitosamente", recurrence));
	}
	
	/**
	 * Cancel a recurrence
	 * @param recurrenceId the recurrence id
	 * @return response
	 */
	@DeleteMapping("/recurrence/{recurrenceId}")
	public ResponseEntity<ApiResponse<Void>> cancelRecurrence(
			@PathVariable Long recurrenceId) {
		recurrenceService.cancelRecurrence(recurrenceId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Recurrencia cancelada", null));
	}
	
	/**
	 * Get recurrences by client
	 * @param clientId the client id
	 * @return list of recurrences
	 */
	@GetMapping("/recurrences/client/{clientId}")
	public ResponseEntity<ApiResponse<List<RecurrenceDTO>>> getRecurrencesByClient(
			@PathVariable Long clientId) {
		List<RecurrenceDTO> recurrences = recurrenceService.getRecurrencesByClient(clientId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Recurrencias obtenidas", recurrences));
	}

	/**
	 * Create overbooking - creates a booking with status RESERVED and assigns it to a client.
	 * The endTime is calculated automatically based on startTime + service duration.
	 * Validates that the client belongs to the organization and the service is from the same organization.
	 *
	 * @param dto the overbooking DTO containing booking data and client ID
	 * @return the created booking
	 */
	@PostMapping("/overbooking")
	public ResponseEntity<ApiResponse<BookingDTO>> createOverBooking(@RequestBody OverBookingDTO dto) {
		// Convertir BookingDTO a Booking entity
		Booking booking = mapper.toEntity(dto.getBooking());
		
		// Crear el overbooking
		Booking created = service.createOverBooking(booking, dto.getClientId());
		
		return ResponseEntity.ok(new ApiResponse<>(true, "Overbooking created successfully", mapper.toDto(created)));
	}

}
