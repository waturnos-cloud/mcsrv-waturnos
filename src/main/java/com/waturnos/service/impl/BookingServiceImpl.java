package com.waturnos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.audit.AuditContext;
import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingClient;
import com.waturnos.entity.Client;
import com.waturnos.entity.ClientOrganization;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.extended.BookingSummaryDetail;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.mapper.ServiceBookingMapper;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientOrganizationRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.WaitlistService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
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
	
	/** The client organization repository. */
	private final ClientOrganizationRepository clientOrganizationRepository;
	
	/** The notification factory. */
	private final NotificationFactory notificationFactory;

	/** The Constant DATE_FORMATTER. */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** The mapper. */
    private final ServiceBookingMapper mapper; 
    
	/** The message source. */
	private final MessageSource messageSource;
	
	/** The waitlist service. */
	private final WaitlistService waitlistService;
	
	/** The date forma email. */
	@Value("${app.datetime.email-format}")
	private String dateFormaEmail;	/** The url home. */
	@Value("${app.notification.HOME}")
	private String urlHome;
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
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER, UserRole.CLIENT  })
	@AuditAspect("BOOKING_UPDATE_STATUS")
	public Booking updateStatus(Long id, BookingStatus status) {
		Booking existing = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));
		if (existing.getService() != null && existing.getService().getUser() != null && existing.getService().getUser().getOrganization() != null) {
			AuditContext.setOrganization(existing.getService().getUser().getOrganization());
			AuditContext.setProvider(existing.getService().getUser());
			AuditContext.setService(existing.getService());
		}
		existing.setStatus(status);
		return bookingRepository.save(existing);
	}

	/**
	 * AssignBookingToClient (Inscribe a un cliente en un booking multi-plaza).
	 *
	 * @param bookingId the id del Booking
	 * @param clientId the client id
	 * @return the updated Booking
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER, UserRole.CLIENT })
	@Transactional(readOnly = false)
	@AuditAspect("BOOKING_ASSIGN_CLIENT")
	public Booking assignBookingToClient(Long bookingId, Long clientId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ServiceException(ErrorCode.BOOKING_NOT_FOUND, "Booking not found"));

		if (booking.getService() != null && booking.getService().getUser() != null && booking.getService().getUser().getOrganization() != null) {
			AuditContext.setOrganization(booking.getService().getUser().getOrganization());
			AuditContext.setProvider(booking.getService().getUser());
			AuditContext.setService(booking.getService());
		}

		ClientOrganization clientOrganization = clientOrganizationRepository
				.findByClientIdAndOrganizationId(clientId, booking.getService().getUser().getOrganization().getId())
				.orElseThrow(() -> new ServiceException(ErrorCode.CLIENT_NOT_EXISTS_IN_ORGANIZATION, "Booking not found"));

		securityAccessEntity.controlValidAccessOrganization(clientOrganization.getOrganization().getId());

		Client client = clientRepository.findById(clientId)
				.orElseThrow(() -> new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found"));
		
		AuditContext.setObject(client.getFullName());

		if (booking.getFreeSlots() <= 0) {
			throw new ServiceException(ErrorCode.BOOKING_FULL, "Booking is full, no free slots available");
		}

		if (booking.getBookingClients().stream()
					.anyMatch(bc -> bc.getClient().getId().equals(clientId))) {
			throw new ServiceException(ErrorCode.BOOKING_ALREADY_RESERVED_BYCLIENT, "Client is already registered for this booking.");
		}

		// Agregar el cliente al booking (mantener lógica original HEAD)
		BookingClient bookingClient = BookingClient.builder()
				.booking(booking)
				.client(client)
				.build();
		booking.addBookingClient(bookingClient);

		// Actualizar timestamp y disparar notificación
		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		notificationFactory.sendAsync(buildRequest(booking, client));

		// Guardar booking
		Booking savedBooking = bookingRepository.save(booking);

		// Verificar si cumple una waitlist (solo si el servicio tiene waitList activo)
		ServiceEntity service = savedBooking.getService();
		if (service != null && Boolean.TRUE.equals(service.getWaitList())) {
			waitlistService.fulfillWaitlist(savedBooking, clientId);
		}

		return savedBooking;
	}
	
	
	/**
	 * Builds the request.
	 *
	 * @param booking     the booking
	 * @param client the client
	 * @param serviceName the service name
	 * @return the notification request
	 */
	private NotificationRequest buildRequest(Booking booking, Client client) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", client.getFullName());
		properties.put("SERVICENAME", booking.getService().getName());
		properties.put("DATEBOOKING", DateUtils.format(booking.getStartTime(), dateFormaEmail));
		properties.put("URLHOME", urlHome);

		return NotificationRequest.builder().email(client.getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.assign.booking", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.BOOKING_ASSIGN).properties(properties).build();
	}
	
	/**
	 * Cancel booking.
	 *
	 * @param id the id
	 * @param reason the reason
	 * @return the booking
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER, UserRole.CLIENT })
	@AuditAspect("BOOKING_CANCEL")
	@Transactional
	public Booking cancelBooking(Long id, String reason) {
		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		if (booking.getService() != null && booking.getService().getUser() != null && booking.getService().getUser().getOrganization() != null) {
			AuditContext.setOrganization(booking.getService().getUser().getOrganization());
			AuditContext.setProvider(booking.getService().getUser());
			AuditContext.setService(booking.getService());
		}

		if (!booking.getStatus().equals(BookingStatus.RESERVED)) {
			throw new EntityNotFoundException("Not valid status");
		}

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancelReason(reason);

		Booking savedBooking = bookingRepository.save(booking);

		// Notificar waitlist solo si el servicio tiene habilitada la lista de espera
		ServiceEntity service = savedBooking.getService();
		if (service != null && Boolean.TRUE.equals(service.getWaitList())) {
			waitlistService.notifyNextInLine(savedBooking);
		}

		return savedBooking;
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

		// 4) Dentro de cada día: agrupar por servicio (usando ID para evitar lazy loading en hashCode)
		groupedByDay.forEach((day, dayList) -> {

			Map<Long, List<Booking>> byServiceId = dayList.stream()
					.collect(Collectors.groupingBy(b -> b.getService().getId()));

			List<ServiceWithBookingsDTO> dtoList = byServiceId.entrySet().stream()
					.map(e -> {
						ServiceEntity service = e.getValue().get(0).getService();
						return mapper.toServiceGroup(service, e.getValue());
					}).toList();

			response.put(day, dtoList);
		});

		return response;
	}

	/**
	 * Find booking details by id.
	 *
	 * @param bookingId the booking id
	 * @return the booking details DTO
	 */
	@Override
	public com.waturnos.dto.response.BookingDetailsDTO findBookingDetailsById(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

		com.waturnos.dto.response.BookingDetailsDTO detailsDTO = new com.waturnos.dto.response.BookingDetailsDTO();
		
		// Mapear datos básicos del booking
		detailsDTO.setId(booking.getId());
		detailsDTO.setStartTime(booking.getStartTime());
		detailsDTO.setEndTime(booking.getEndTime());
		detailsDTO.setStatus(booking.getStatus());
		detailsDTO.setNotes(booking.getNotes());
		detailsDTO.setServiceId(booking.getService().getId());
		detailsDTO.setFreeSlots(booking.getFreeSlots());
		
		// Mapear los clientes vinculados
		List<com.waturnos.dto.beans.ClientDTO> clientDTOs = booking.getBookingClients().stream()
				.map(bc -> {
					com.waturnos.dto.beans.ClientDTO clientDTO = new com.waturnos.dto.beans.ClientDTO();
					clientDTO.setId(bc.getClient().getId());
					clientDTO.setFullName(bc.getClient().getFullName());
					clientDTO.setDni(bc.getClient().getDni());
					clientDTO.setEmail(bc.getClient().getEmail());
					clientDTO.setPhone(bc.getClient().getPhone());
					return clientDTO;
				})
				.collect(Collectors.toList());
		
		detailsDTO.setClients(clientDTOs);
		
		return detailsDTO;
	}
}
