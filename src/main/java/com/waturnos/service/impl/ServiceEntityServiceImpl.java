package com.waturnos.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.waturnos.audit.AuditContext;
import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.dto.response.AffectedBookingDTO;
import com.waturnos.dto.response.AvailabilityImpactResponse;
import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingClient;
import com.waturnos.entity.Client;
import com.waturnos.entity.Recurrence;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.UnavailabilityEntity;
import com.waturnos.entity.User;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.RecurrenceType;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.AvailabilityRepository;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.RecurrenceRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingGeneratorService;
import com.waturnos.service.BookingService;
import com.waturnos.service.ServiceEntityService;
import com.waturnos.service.UnavailabilityService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.BatchProcessor;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ServiceEntityServiceImpl.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceEntityServiceImpl implements ServiceEntityService {

	/** The service repository. */
	private final ServiceRepository serviceRepository;

	/** The availability repository. */
	private final AvailabilityRepository availabilityRepository;

	/** The booking service. */
	private final BookingService bookingService;

	/** The location repository. */
	private final LocationRepository locationRepository;

	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;

	/** The user repository. */
	private final UserRepository userRepository;

	/** The unavailability service. */
	private final UnavailabilityService unavailabilityService;

	/** The batch processor. */
	private final BatchProcessor batchProcessor;

	/** The booking generator service. */
	private final BookingGeneratorService bookingGeneratorService;
	
	/** The recurrence repository. */
	private final RecurrenceRepository recurrenceRepository;
	
	/** The booking repository. */
	private final BookingRepository bookingRepository;
	
	/**
	 * Creates the.
	 *
	 * @param serviceEntity    the service entity
	 * @param listAvailability the list availability
	 * @param userId           the user id
	 * @param locationId       the location id
	 * @param workInHollidays  the work in hollidays
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER, UserRole.SELLER })
	@Transactional(readOnly = false)
	@AuditAspect("SERVICE_CREATE")
	public ServiceEntity create(ServiceEntity serviceEntity, List<AvailabilityEntity> listAvailability, Long userId,
			Long locationId, boolean workInHollidays) {

		Optional<User> userDB = userRepository.findById(userId);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}

		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());

		Optional<ServiceEntity> service = serviceRepository.findByNameAndUserId(serviceEntity.getName(), userId);
		if (service.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_ALREADY_EXIST_EXCEPTION, "Service already exists exception");
		}
		AuditContext.setOrganization(userDB.get().getOrganization());
		serviceEntity.setLocation(locationRepository.findById(locationId).get());
		serviceEntity.setUser(userDB.get());
		serviceEntity.setCreator(SessionUtil.getUserName());
		serviceEntity.setCreatedAt(LocalDateTime.now());
		ServiceEntity serviceEntityResponse = serviceRepository.save(serviceEntity);
		AuditContext.setService(serviceEntityResponse);
		AuditContext.setProvider(userDB.get());
		AuditContext.get().setObject(serviceEntity.getName());
		listAvailability.forEach(av -> {
			av.setServiceId(serviceEntity.getId());
			availabilityRepository.save(av);
		});

		// Generar bookings de forma asíncrona para no bloquear la respuesta
		bookingGeneratorService.generateBookingsAsync(serviceEntity, listAvailability, 
				workInHollidays ? unavailabilityService.getHolidays() : null);
		return serviceEntityResponse;
	}

	/**
	 * Generate bookings for a specific date based on service availabilities and unavailabilities.
	 * Intended for daily expansion at midnight.
	 *
	 * @param service the service
	 * @param date the date to generate bookings for
	 */
	public void generateBookingsForDate(ServiceEntity service, LocalDate date, Set<LocalDate> unavailabilities) {
		List<AvailabilityEntity> availabilities = availabilityRepository.findByServiceId(service.getId());
		List<Booking> bookings = new ArrayList<>();
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		if (unavailabilities == null || !unavailabilities.contains(date)) {
			availabilities.stream()
		    .filter(a -> a.getDayOfWeek() == dayOfWeek.getValue())
		    .forEach(a -> {
		        LocalTime currentTime = a.getStartTime();
		        int duration = service.getDurationMinutes();
		        int offset = (service.getOffsetMinutes() != null ? service.getOffsetMinutes() : 0);
		        int intervalMinutes = duration + offset;

		        // Convertimos el final a LocalDateTime para una comparación absoluta
		        LocalDateTime endDateTime = LocalDateTime.of(date, a.getEndTime());

		        while (true) {
		            LocalDateTime currentStart = LocalDateTime.of(date, currentTime);
		            LocalDateTime currentEnd = currentStart.plusMinutes(duration);

		            // 1. Validar que el turno no exceda la hora de fin de disponibilidad
		            // 2. Validar que no hayamos saltado al día siguiente (overflow de LocalTime)
		            if (currentEnd.isAfter(endDateTime) || currentStart.toLocalDate().isAfter(date)) {
		                break;
		            }

		            Booking booking = Booking.builder()
		                .startTime(currentStart)
		                .endTime(currentEnd)
		                .status(BookingStatus.FREE)
		                .service(service)
		                .freeSlots(service.getCapacity())
		                .createdAt(DateUtils.getCurrentDateTime())
		                .build();
		            
		            bookings.add(booking);

		            // Avanzar el tiempo
		            currentTime = currentTime.plusMinutes(intervalMinutes);
		            
		            // Si el nuevo currentTime es menor al anterior, significa que cruzamos la medianoche
		            if (currentTime.isBefore(currentStart.toLocalTime()) && intervalMinutes > 0) {
		                break; 
		            }
		        }
		    });
		}
		if (!bookings.isEmpty()) {
			bookingService.create(bookings);
			// Aplicar recurrencias automáticamente a los nuevos turnos creados
			applyRecurrencesToNewBookings(service, date, bookings);
		}
	}

	/**
	 * Apply recurrences to newly created bookings.
	 * When new bookings are generated, this method checks if there are active recurrences
	 * that should be applied based on day of week and time slot.
	 *
	 * @param service the service
	 * @param date the date of the new bookings
	 * @param newBookings the list of newly created bookings
	 */
	private void applyRecurrencesToNewBookings(ServiceEntity service, LocalDate date, List<Booking> newBookings) {
		if (newBookings == null || newBookings.isEmpty()) {
			return;
		}
		
		int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
		
		// Obtener todas las recurrencias activas para este servicio y día
		List<Recurrence> activeRecurrences = recurrenceRepository.findByServiceAndDayOfWeek(
			service.getId(), 
			dayOfWeek
		);
		
		if (activeRecurrences.isEmpty()) {
			log.debug("No hay recurrencias activas para servicio {} en día {}", service.getId(), dayOfWeek);
			return;
		}
		
		log.info("Aplicando {} recurrencias activas para servicio {} en fecha {}", 
			activeRecurrences.size(), service.getId(), date);
		
		int recurrencesApplied = 0;
		
		for (Recurrence recurrence : activeRecurrences) {
			// Verificar si la recurrencia aún está vigente para esta fecha
			if (!isRecurrenceValidForDate(recurrence, date)) {
				log.debug("Recurrencia {} no es válida para la fecha {}", recurrence.getId(), date);
				continue;
			}
			
			LocalTime targetTime = recurrence.getTimeSlot();
			
			// Buscar el booking que coincida con la hora de la recurrencia
			Optional<Booking> matchingBooking = newBookings.stream()
				.filter(b -> b.getStartTime().toLocalTime().equals(targetTime))
				.filter(b -> b.getStatus() == BookingStatus.FREE || b.getStatus() == BookingStatus.FREE_AFTER_CANCEL)
				.findFirst();
			
			if (matchingBooking.isPresent()) {
				Booking booking = matchingBooking.get();
				Client client = recurrence.getClient();
				
				// Asignar el turno al cliente
				BookingClient bc = BookingClient.builder()
					.booking(booking)
					.client(client)
					.build();
				
				booking.addBookingClient(bc);
				booking.setRecurrence(recurrence);
				booking.setUpdatedAt(LocalDateTime.now());
				
				bookingRepository.save(booking);
				
				recurrencesApplied++;
				log.info("Recurrencia {} aplicada automáticamente al turno {} para clientId {} en fecha {} a las {}", 
					recurrence.getId(), booking.getId(), client.getId(), date, targetTime);
			} else {
				log.debug("No se encontró turno disponible para recurrencia {} a las {} en fecha {}", 
					recurrence.getId(), targetTime, date);
			}
		}
		
		log.info("Total de recurrencias aplicadas: {} de {} para fecha {}", 
			recurrencesApplied, activeRecurrences.size(), date);
	}
	
	/**
	 * Checks if a recurrence is still valid for a given date based on its type and configuration.
	 *
	 * @param recurrence the recurrence
	 * @param date the date to check
	 * @return true if the recurrence is valid for the date
	 */
	private boolean isRecurrenceValidForDate(Recurrence recurrence, LocalDate date) {
		if (!recurrence.getActive()) {
			return false;
		}
		
		// FOREVER siempre es válido
		if (recurrence.getRecurrenceType() == RecurrenceType.FOREVER) {
			return true;
		}
		
		// END_DATE: verificar que la fecha no supere el límite
		if (recurrence.getRecurrenceType() == RecurrenceType.END_DATE) {
			return recurrence.getEndDate() != null && !date.isAfter(recurrence.getEndDate());
		}
		
		// COUNT: necesitaríamos contar cuántas veces se ha aplicado, por ahora lo consideramos válido
		// ya que el sistema de recurrencias maneja esto en otra parte
		if (recurrence.getRecurrenceType() == RecurrenceType.COUNT) {
			// Contar bookings existentes con esta recurrencia
			// Si ya alcanzó el límite, no aplicar
			if (recurrence.getOccurrenceCount() != null) {
				// Esto es una aproximación, el conteo real debería hacerse en el servicio de recurrencia
				// Por ahora, asumimos que es válido y el servicio de recurrencia lo manejará
				return true;
			}
		}
		
		return true;
	}

	/**
	 * Extend bookings by one day from the last existing booking date.
	 * If no bookings exist, generates for the configured forward days.
	 *
	 * @param service the service entity
	 */
	public void extendBookingsByOneDay(ServiceEntity service, Set<LocalDate> unavailabilities) {
		LocalDate lastBookingDate = bookingService.findMaxBookingDateByServiceId(service.getId());
		LocalDate dateToGenerate;
		if (lastBookingDate == null) {
			// No hay bookings previos, generar desde hoy + 1
			dateToGenerate = LocalDate.now().plusDays(1);
		} else {
			// Extender un día más desde la última fecha
			dateToGenerate = lastBookingDate.plusDays(1);
		}
		generateBookingsForDate(service, dateToGenerate, unavailabilities);
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER, UserRole.SELLER , UserRole.CLIENT })
	public ServiceEntity findById(Long id) {
		Optional<ServiceEntity> serviceEntity = serviceRepository.findById(id);
		if (!serviceEntity.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		return serviceEntity.get();
	}

	/**
	 * Find by user.
	 *
	 * @param userId the user id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER, UserRole.SELLER , UserRole.CLIENT})
	public List<ServiceEntity> findByUser(Long userId) {
		Optional<User> userDB = userRepository.findById(userId);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
//		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());
		return serviceRepository.findByUserId(userId);
	}

	/**
	 * Find by location.
	 *
	 * @param locationId the location id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER, UserRole.SELLER })
	public List<ServiceEntity> findByLocation(Long locationId) {
		return serviceRepository.findByLocationId(locationId);
	}

	/**
	 * Update.
	 *
	 * @param id      the id
	 * @param service the service
	 * @param newAvailability the new availability list
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@Transactional(readOnly = false)
	@AuditAspect("SERVICE_UPDATE")
	public ServiceEntity update(ServiceEntity service, List<AvailabilityDTO> newAvailability) {

		Optional<ServiceEntity> serviceDBExists = serviceRepository.findById(service.getId());
		if (!serviceDBExists.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		if (!serviceDBExists.get().getUser().getId().equals(service.getUser().getId())) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		ServiceEntity serviceDB = serviceDBExists.get();
		AuditContext.setOrganization(serviceDB.getUser().getOrganization());
		AuditContext.setService(serviceDB);
		AuditContext.setProvider(service.getUser());
		AuditContext.get().setObject(service.getName());
		
		// Verificar si hay cambios en availability y procesar bookings afectados de forma asíncrona
		List<AvailabilityEntity> currentAvailability = availabilityRepository.findByServiceId(service.getId());
		
		if (newAvailability != null && !newAvailability.isEmpty() && hasAvailabilityChanged(currentAvailability, newAvailability)) {
			log.info("Availability changed for service {}, triggering async processing of affected bookings", service.getId());
			
			// Capturar el SecurityContext antes de ejecutar async
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			
			// Procesar de forma asíncrona (no bloquea la respuesta)
			batchProcessor.processAffectedBookingsAsync(service.getId(), newAvailability, authentication);
			
			// Actualizar availability
			availabilityRepository.deleteByServiceId(service.getId());
			newAvailability.forEach(av -> {
				AvailabilityEntity entity = new AvailabilityEntity();
				entity.setServiceId(service.getId());
				entity.setDayOfWeek(av.getDayOfWeek());
				entity.setStartTime(av.getStartTime());
				entity.setEndTime(av.getEndTime());
				availabilityRepository.save(entity);
			});
		}
		
		serviceDB.setUpdatedAt(LocalDateTime.now());
		serviceDB.setModificator(SessionUtil.getUserName());
		serviceDB.setName(service.getName());
		serviceDB.setDescription(service.getDescription());
		serviceDB.setAdvancePayment(service.getAdvancePayment());
		serviceDB.setLocation(service.getLocation());
		serviceDB.setPrice(service.getPrice());

		return serviceRepository.save(serviceDB);
	}
	
	/**
	 * Verifica si hubo cambios en la configuración de availability.
	 */
	private boolean hasAvailabilityChanged(List<AvailabilityEntity> current, List<AvailabilityDTO> newList) {
		if (current.size() != newList.size()) {
			return true;
		}
		
		// Comparar cada elemento
		for (AvailabilityEntity currentAv : current) {
			boolean found = newList.stream().anyMatch(newAv ->
				newAv.getDayOfWeek() == currentAv.getDayOfWeek() &&
				newAv.getStartTime().equals(currentAv.getStartTime()) &&
				newAv.getEndTime().equals(currentAv.getEndTime())
			);
			
			if (!found) {
				return true; // Hubo un cambio
			}
		}
		
		return false;
	}

	/**
	 * Delete.
	 *
	 * @param serviceId the service id
	 */
	@Override
	@Transactional
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@AuditAspect("SERVICE_DELETE")
	public void delete(Long serviceId) {
		Optional<ServiceEntity> serviceDB = serviceRepository.findById(serviceId);
		if (!serviceDB.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		AuditContext.setOrganization(serviceDB.get().getUser().getOrganization());
		AuditContext.setService(serviceDB.get());
		AuditContext.setProvider(serviceDB.get().getUser());
		AuditContext.get().setObject(serviceDB.get().getName());
		
		// Marcar como borrado INMEDIATAMENTE para que no aparezca en listados
		serviceRepository.markAsDeleted(serviceId);
		
		// Proceso async se encarga del borrado físico
		batchProcessor.deleteServiceAsync(serviceDB.get().getId(), serviceDB.get().getName(), true);
	}

	/**
	 * Lock calendar.
	 *
	 * @param startDate the start date
	 * @param endDate   the end date
	 * @param serviceId the service id
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.PROVIDER })
	@AuditAspect("SERVICE_LOCK_CALENDAR")
	public void lockCalendar(LocalDateTime startDate, LocalDateTime endDate, Long serviceId) {
		Optional<ServiceEntity> serviceEntity = serviceRepository.findById(serviceId);
		if (!serviceEntity.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		AuditContext.setOrganization(serviceEntity.get().getUser().getOrganization());
		AuditContext.setService(serviceEntity.get());
		AuditContext.setProvider(serviceEntity.get().getUser());
		AuditContext.get().setObject(serviceEntity.get().getName());

		unavailabilityService.create(UnavailabilityEntity.builder().startDay(startDate.toLocalDate())
				.startTime(startDate.toLocalTime()).endDay(endDate.toLocalDate()).endTime(endDate.toLocalTime())
				.service(ServiceEntity.builder().id(serviceId).build()).build());
        
		batchProcessor.deleteBookings(startDate, endDate, serviceEntity.get());
	}

	/**
	 * Valida el impacto de cambios en availability sobre bookings existentes.
	 *
	 * @param serviceId el ID del servicio
	 * @param newAvailability la nueva configuración de availability
	 * @return el impacto con la lista de bookings afectados
	 */
	@Override
	@Transactional(readOnly = true)
	public AvailabilityImpactResponse validateAvailabilityChange(Long serviceId, List<AvailabilityDTO> newAvailability) {
		// Verificar que el servicio existe
		ServiceEntity service = serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Service not found"));
		
		// Obtener todos los bookings futuros con clientes asignados
		LocalDateTime now = LocalDateTime.now();
		List<Booking> futureBookings = bookingService.findByServiceId(serviceId).stream()
				.filter(b -> b.getStartTime().isAfter(now))
				.filter(b -> b.getStatus() == BookingStatus.RESERVED || 
						     b.getStatus() == BookingStatus.PARTIALLY_RESERVED)
				.collect(Collectors.toList());
		
		// Identificar bookings que quedarían fuera del nuevo availability
		List<AffectedBookingDTO> affectedBookings = new ArrayList<>();
		
		for (Booking booking : futureBookings) {
			if (!isBookingWithinAvailability(booking, newAvailability)) {
				// Este booking queda fuera del nuevo horario
				// Obtener info de cada cliente en este booking
				for (BookingClient bc : booking.getBookingClients()) {
					Client client = bc.getClient();
					
					affectedBookings.add(AffectedBookingDTO.builder()
							.bookingId(booking.getId())
							.clientFullName(client.getFullName())
							.clientPhone(client.getPhone())
							.clientEmail(client.getEmail())
							.startTime(booking.getStartTime())
							.build());
				}
			}
		}
		
		return AvailabilityImpactResponse.builder()
				.affectedCount(affectedBookings.size())
				.affectedBookings(affectedBookings)
				.build();
	}
	
	/**
	 * Verifica si un booking está dentro de los rangos de availability.
	 *
	 * @param booking el booking a verificar
	 * @param availabilities la lista de availability
	 * @return true si el booking está cubierto, false si queda fuera
	 */
	private boolean isBookingWithinAvailability(Booking booking, List<AvailabilityDTO> availabilities) {
		DayOfWeek bookingDay = booking.getStartTime().getDayOfWeek();
		LocalTime bookingStartTime = booking.getStartTime().toLocalTime();
		LocalTime bookingEndTime = booking.getEndTime().toLocalTime();
		
		// Buscar si existe un availability para ese día que cubra el horario
		return availabilities.stream()
				.filter(av -> av.getDayOfWeek() == bookingDay.getValue())
				.anyMatch(av -> {
					// El booking debe estar completamente dentro del rango
					return !bookingStartTime.isBefore(av.getStartTime()) && 
						   !bookingEndTime.isAfter(av.getEndTime());
				});
	}
}

