package com.waturnos.service.process.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.dto.response.AffectedBookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingClient;
import com.waturnos.entity.Client;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.User;
import com.waturnos.enums.BookingStatus;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.BookingService;
import com.waturnos.service.process.BatchProcessor;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class UserProcessImpl.
 */
@Service

/**
 * Instantiates a new user process impl.
 *
 * @param userRepository                 the user repository
 * @param passwordEncoder                the password encoder
 * @param providerRepository             the provider repository
 * @param organizationRepository         the organization repository
 * @param providerOrganizationRepository the provider organization repository
 */
@RequiredArgsConstructor
@Slf4j
public class BatchProcessorImpl implements BatchProcessor {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/** The service repository. */
	private final ServiceRepository serviceRepository;

	/** The user repository. */
	private final UserRepository userRepository;

	/** The notification factory. */
	private final NotificationFactory notificationFactory;

	/** The message source. */
	private final MessageSource messageSource;
	
	/** The booking service. */
	@Autowired
	private BookingService bookingService;

	/** The date forma email. */
	@Value("${app.datetime.email-format}")
	private String dateFormaEmail;

	@Value("${app.notification.HOME}")
	private String urlHome;

	/**
	 * Delete bookings async.
	 *
	 * @param serviceId the service id
	 */
	@Override
	@Async
	@Transactional(readOnly = false)
	public void deleteServiceAsync(long serviceId, String serviceName, boolean deleteService) {
	    // Traer todos los bookings con sus clientes en UNA SOLA QUERY (evita N+1)
	    List<Booking> bookingsWithClients = bookingRepository.findByServiceIdWithClients(serviceId);

	    // Enviar notificaciones a los clientes afectados
	    bookingsWithClients.forEach(booking -> {
	        if (booking.getBookingClients() != null && !booking.getBookingClients().isEmpty()) {
	            booking.getBookingClients().stream()
	                .map(bookingClient -> bookingClient.getClient())
	                .forEach(client -> {
	                    notificationFactory.send(buildRequest(booking, client, serviceName));
	                });
	        }
	    });

	    // Borrar en batch: primero booking_clients, luego bookings, finalmente el servicio
	    bookingRepository.deleteAllBookingClientsByServiceId(serviceId);
	    bookingRepository.deleteAllByServiceId(serviceId);
	    
	    if (deleteService) {
	    	serviceRepository.deleteById(serviceId);
	    }
	    
	    log.info("Servicio {} eliminado - {} bookings procesados", serviceId, bookingsWithClients.size());
	}

	/**
	 * Delete provider async.
	 *
	 * @param providerId the provider id
	 */
	@Override
	@Async
	@Transactional(readOnly = false)
	public void deleteProviderAsync(long providerId) {
		List<ServiceEntity> servicesToDelete = serviceRepository.findByUserId(providerId);
		servicesToDelete.forEach(service -> {
			this.deleteServiceAsync(service.getId(), service.getName(), false);
		});
		serviceRepository.deleteAllByUserId(providerId);
		userRepository.deleteById(providerId);
		
	}

	/**
	 * Builds the request.
	 *
	 * @param booking     the booking
	 * @param serviceName the service name
	 * @return the notification request
	 */
	private NotificationRequest buildRequest(Booking booking, Client client, String serviceName) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", client.getFullName());
		properties.put("SERVICENAME", serviceName);
		properties.put("DATEBOOKING", DateUtils.format(booking.getStartTime(), dateFormaEmail));
		properties.put("URLHOME", urlHome);

		return NotificationRequest.builder().email(client.getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.cancel.booking.by.provider", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.CANCELBOOKING_BY_PROVIDER).properties(properties).build();
	}

	/**
	 * Delete bookings.
	 *
	 * @param startDate     the start date
	 * @param endDate       the end date
	 * @param serviceEntity the service entity
	 */
	@Override
	@Async
	@Transactional(readOnly = false)
	public void deleteBookings(LocalDateTime startDate, LocalDateTime endDate, ServiceEntity serviceEntity) {

		List<Booking> bookingList = bookingRepository.findReservedWithClientAndServiceBetween(startDate, endDate,
				serviceEntity.getId());

		bookingList.forEach(booking -> {
            
            if (booking.getBookingClients() != null && !booking.getBookingClients().isEmpty()) {
                
                booking.getBookingClients().stream()
                    .map(bookingClient -> bookingClient.getClient())
                    .forEach(client -> {
                        notificationFactory.send(buildRequest(booking, client, serviceEntity.getName()));
                    });
            }
		});
		bookingRepository.deleteBookingsBetweenDates(startDate, endDate, serviceEntity.getId());
	}
	
	/**
	 * Procesa de forma asíncrona los bookings afectados por cambios en availability.
	 *
	 * @param serviceId el ID del servicio
	 * @param newAvailability la nueva configuración de availability
	 * @param authentication el contexto de autenticación del usuario
	 */
	@Override
	@Async
	@Transactional(readOnly = false)
	public void processAffectedBookingsAsync(Long serviceId, List<AvailabilityDTO> newAvailability, Authentication authentication) {
		// Propagar el SecurityContext al hilo asíncrono
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		log.info("Processing affected bookings asynchronously for service {}", serviceId);
		
		try {
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
			
			if (!affectedBookings.isEmpty()) {
				log.info("Found {} affected bookings for service {}", affectedBookings.size(), serviceId);
				
				// Agrupar por bookingId para cancelar cada booking una vez
				Map<Long, List<AffectedBookingDTO>> bookingGroups = affectedBookings.stream()
						.collect(Collectors.groupingBy(AffectedBookingDTO::getBookingId));
				
				bookingGroups.forEach((bookingId, affectedClients) -> {
					try {
						// Cancelar el booking
						bookingService.cancelBooking(bookingId, "Cambio en horarios de disponibilidad del servicio");
						
						// Notificar a cada cliente afectado
						affectedClients.forEach(clientData -> {
							try {
								Map<String, String> properties = new HashMap<>();
								properties.put("USERNAME", clientData.getClientFullName());
								properties.put("DATEBOOKING", DateUtils.format(clientData.getStartTime(), dateFormaEmail));
								properties.put("URLHOME", urlHome);
								properties.put("REASON", "El provider ha modificado los horarios de atención");
								
								NotificationRequest notificationRequest = NotificationRequest.builder()
										.email(clientData.getClientEmail())
										.language("ES")
										.subject(messageSource.getMessage("notification.subject.cancel.booking.by.provider", 
												null, LocaleContextHolder.getLocale()))
										.type(NotificationType.CANCELBOOKING_BY_PROVIDER)
										.properties(properties)
										.build();
								
								notificationFactory.send(notificationRequest);
								
								log.info("Notified client {} about booking {} cancellation", 
										clientData.getClientEmail(), bookingId);
							} catch (Exception e) {
								log.error("Error notifying client {} about booking cancellation", 
										clientData.getClientEmail(), e);
							}
						});
						
					} catch (Exception e) {
						log.error("Error processing affected booking {}", bookingId, e);
					}
				});
				
				log.info("Finished processing {} affected bookings for service {}", 
						bookingGroups.size(), serviceId);
			} else {
				log.info("No affected bookings found for service {}", serviceId);
			}
			
		} catch (Exception e) {
			log.error("Error processing affected bookings for service {}", serviceId, e);
		}
	}
	
	/**
	 * Verifica si un booking está dentro de los rangos de availability.
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
