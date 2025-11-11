package com.waturnos.service.process.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.process.BatchProcessor;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;


/**
 * The Class UserProcessImpl.
 */
@Service

/**
 * Instantiates a new user process impl.
 *
 * @param userRepository the user repository
 * @param passwordEncoder the password encoder
 * @param providerRepository the provider repository
 * @param organizationRepository the organization repository
 * @param providerOrganizationRepository the provider organization repository
 */
@RequiredArgsConstructor

public class BatchProcessorImpl  implements BatchProcessor{
	
	
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
		List<Booking> bookingsToDelete = bookingRepository.findByServiceId(serviceId);

		bookingsToDelete.stream().filter(booking -> booking.getClient() != null) // Solo si hay cliente
				.forEach(booking -> {
					notificationFactory.send(buildRequest(booking, serviceName));
				});
		bookingRepository.deleteAllByServiceId(serviceId);
		if(deleteService) {
			serviceRepository.deleteById(serviceId);
		}	
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
	private NotificationRequest buildRequest(Booking booking, String serviceName) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", booking.getClient().getFullName());
		properties.put("SERVICENAME", serviceName);
		properties.put("DATEBOOKING", DateUtils.format(booking.getStartTime(), dateFormaEmail));
		properties.put("URLHOME", urlHome);

		return NotificationRequest.builder().email(booking.getClient().getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.cancel.booking.by.provider", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.CANCELBOOKING_BY_PROVIDER).properties(properties).build();
	}

}
