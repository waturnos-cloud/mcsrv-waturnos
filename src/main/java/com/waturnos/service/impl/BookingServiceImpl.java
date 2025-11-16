package com.waturnos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.extended.BookingSummaryDetail;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.mapper.ServiceBookingMapper;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;

/**
 * The Class BookingServiceImpl.
 */
@Service

/**
 * Instantiates a new booking service impl.
 *
 * @param bookingRepository    the booking repository
 * @param clientRepository     the client repository
 * @param securityAccessEntity the security access entity
 */
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/** The client repository. */
	private final ClientRepository clientRepository;

	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;

	/** The Constant DATE_FORMATTER. */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ServiceBookingMapper mapper; 
	/**
	 * Creates the.
	 *
	 * @param list the list
	 * @return the list
	 */
	@Override
	public List<Booking> create(List<Booking> list) {
		return bookingRepository.saveAll(list);
	}

	/**
	 * Update status.
	 *
	 * @param id     the id
	 * @param status the status
	 * @return the booking
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER })
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
	 * @param clientId the client id
	 * @return the booking
	 */
	@Override
	public Booking assignBookingToClient(Long id, Long clientId) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		Client client = clientRepository.findById(clientId)
				.orElseThrow(() -> new EntityNotFoundException("Client not found"));

		if (!booking.getStatus().equals(BookingStatus.FREE)) {
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
	 * @param id     the id
	 * @param reason the reason
	 * @return the booking
	 */
	@Override
	public Booking cancelBooking(Long id, String reason) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		if (!booking.getStatus().equals(BookingStatus.RESERVED)) {
			throw new EntityNotFoundException("Not valid status");
		}

		// TODO modificator? otro servicio para client y admin/manager?

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancelReason(reason);

		return bookingRepository.save(booking);

	}

	/**
	 * Find by service id.
	 *
	 * @param serviceId the service id
	 * @return the list
	 */
	@Override
	public List<Booking> findByServiceId(Long serviceId) {
		return bookingRepository.findByServiceId(serviceId);
	}

	/**
	 * Find bookings for today.
	 *
	 * @return the list
	 */
	@Override
	public List<Booking> findBookingsForToday() {

		OffsetDateTime now = OffsetDateTime.now();
		LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		return bookingRepository.findByStartTimeBetween(startOfDay, endOfDay);

	}

	/**
	 * Find bookings for today by provider.
	 *
	 * @param providerId the provider id
	 * @return the map
	 */
	public Map<Long, List<BookingSummaryDetail>> findBookingsForTodayByProvider(Long providerId) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
		List<BookingSummaryDetail> bookings = bookingRepository.findByProviderAndStartTimeBetween(providerId,
				startOfDay, endOfDay);

		return bookings.stream().collect(Collectors.groupingBy(BookingSummaryDetail::getServiceId));

	}

	/**
	 * Count bookings by date range and provider.
	 *
	 * @param fromDate   the from date
	 * @param toDate     the to date
	 * @param providerId the provider id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public List<CountBookingDTO> countBookingsByDateRangeAndProvider(LocalDate fromDate, LocalDate toDate,
			Long providerId) {

		securityAccessEntity.controlValidAccessOrganization(providerId);

		LocalDate exclusiveEndDate = toDate.plusDays(1);

		List<Object[]> rawCounts = bookingRepository.countBookingsByDayAndStatus(fromDate, exclusiveEndDate,
				providerId);

		return rawCountsToDTO(rawCounts);
	}

	/**
	 * Convierte el resultado crudo del DAO (Fecha, Estado, Conteo) en una lista de
	 * CountBookingDTO, agrupando los estados por fecha.
	 *
	 * @param rawCounts the raw counts
	 * @return the list
	 */
	private List<CountBookingDTO> rawCountsToDTO(List<Object[]> rawCounts) {
		Map<String, CountBookingDTO> countsByDate = new LinkedHashMap<>();

		for (Object[] row : rawCounts) {

			java.sql.Date sqlDate = (java.sql.Date) row[0];
			LocalDate localDate = sqlDate.toLocalDate();
			String dateKey = localDate.format(DATE_FORMATTER);

			CountBookingDTO dto = countsByDate.getOrDefault(dateKey, CountBookingDTO.builder().date(dateKey).build());

			BookingStatus status = BookingStatus.valueOf((String) row[1]);
			Long countLong = (Long) row[2];
			int count = countLong.intValue();
			switch (status) {
			case NO_SHOW:
				dto.setCountNoShow(count);
				break;
			case RESERVED:
				dto.setCountReserved(count);
				break;
			case CANCELLED:
				dto.setCountReserved(count);
				break;
			case COMPLETED:
				dto.setCountCompleted(count);
				break;
			case FREE:
				dto.setCountFree(count);
				break;
			default:
				break;
			}

			countsByDate.put(dateKey, dto);
		}

		return new ArrayList<>(countsByDate.values());
	}

	/**
	 * Find by range.
	 *
	 * @param providerId   the provider id
	 * @param start        the start
	 * @param end          the end
	 * @param serviceIdOpt the service id opt
	 * @return the map
	 */
	public Map<LocalDate, List<ServiceWithBookingsDTO>> findByRange(Long providerId, LocalDate start, LocalDate end,
			Long serviceIdOpt) {

		LocalDateTime startDT = start.atStartOfDay();
		LocalDateTime endDT = end.plusDays(1).atStartOfDay();

		List<Booking> bookings;

		// 1) SI VIENE serviceId → QUERY DIRECTA
		if (serviceIdOpt != null) {
			bookings = bookingRepository.findByProviderServiceAndRange(providerId, serviceIdOpt, startDT, endDT);
		}
		// 2) SI NO → TODAS
		else {
			bookings = bookingRepository.findByProviderAndRange(providerId, startDT, endDT);
		}

		// 3) Agrupar por día
		Map<LocalDate, List<Booking>> groupedByDay = bookings.stream()
				.collect(Collectors.groupingBy(b -> b.getStartTime().toLocalDate()));

		Map<LocalDate, List<ServiceWithBookingsDTO>> response = new TreeMap<>();

		// 4) Dentro de cada día: agrupar por servicio
		groupedByDay.forEach((day, dayList) -> {

			Map<ServiceEntity, List<Booking>> byService = dayList.stream()
					.collect(Collectors.groupingBy(Booking::getService));

			List<ServiceWithBookingsDTO> dtoList = byService.entrySet().stream()
					.map(e -> mapper.toServiceGroup(e.getKey(), e.getValue())).toList();

			response.put(day, dtoList);
		});

		return response;
	}
}
