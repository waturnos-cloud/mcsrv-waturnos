package com.waturnos.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Booking;
import com.waturnos.entity.Organization;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.UserService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.UserProcess;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

/**
 * The Class UserServiceImpl.
 */
@Service

/**
 * Instantiates a new user service impl.
 *
 * @param userRepository  the user repository
 * @param passwordEncoder the password encoder
 * @param userProcess     the user process
 */
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	/** The user repository. */
	private final UserRepository userRepository;

	/** The user process. */
	private final UserProcess userProcess;

	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;

	/** The service repository. */
	private final ServiceRepository serviceRepository;

	/** The booking repository. */
	private final BookingRepository bookingRepository;

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
	 * Find all.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	public List<User> findManagersByOrganization(Long organizationId) {
		return this.findUsersByOrganizationPrivate(organizationId, UserRole.MANAGER);
	}

	/**
	 * Find all.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	public List<User> findProvidersByOrganization(Long organizationId) {
		return this.findUsersByOrganizationPrivate(organizationId, UserRole.PROVIDER);
	}

	/**
	 * Find users by organization private.
	 *
	 * @param organizationId the organization id
	 * @param userRole       the user role
	 * @return the list
	 */
	private List<User> findUsersByOrganizationPrivate(Long organizationId, UserRole userRole) {
		securityAccessEntity.controlValidAccessOrganization(organizationId);

		return userRepository.findByOrganizationIdAndRoleOrderByFullNameAsc(organizationId, userRole);
	}

	/**
	 * Find by email.
	 *
	 * @param email the email
	 * @return the optional
	 */
	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the optional
	 */
	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	/**
	 * Creates the.
	 *
	 * @param manager the user
	 * @return the user
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	public User createManager(Long organizationId, User manager) {
		return userProcess.createManager(Organization.builder().id(organizationId).build(), manager);
	}

	/**
	 * Update.
	 *
	 * @param id   the id
	 * @param user the user
	 * @return the user
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	public User updateManager(User user) {
		return userProcess.updateUser(user);
	}

	public void deleteManager(Long managerId) {

		validateCommons(managerId, UserRole.MANAGER);

		userRepository.deleteById(managerId);
	}

	/**
	 * Validate commons.
	 *
	 * @param userId   the user id
	 * @param userRole the user role
	 */
	private void validateCommons(Long userId, UserRole userRole) {
		Optional<User> userDB = userRepository.findById(userId);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		if (!userRole.equals(userDB.get().getRole())) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR,
					String.format("Not modified user that not %1$s.", userRole.name()));
		}
		if (SessionUtil.getCurrentUser().getId().equals(userId)) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot remove yourself");
		}

		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());

		userRepository.deleteById(userId);

	}

	/**
	 * Creates the provider.
	 *
	 * @param organizationId the organization id
	 * @param provider       the provider
	 * @return the user
	 */
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	public User createProvider(Long organizationId, User provider) {
		return userProcess.createManager(Organization.builder().id(organizationId).build(), provider);
	}

	/**
	 * Delete provider.
	 *
	 * @param providerId the provider id
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER })
	@Transactional(readOnly = false)
	public void deleteProvider(Long providerId) {
		validateCommons(providerId, UserRole.PROVIDER);

		List<ServiceEntity> servicesToDelete = serviceRepository.findByUserId(providerId);

		servicesToDelete.forEach(service -> {
			List<Booking> bookingsToDelete = bookingRepository.findByServiceId(service.getId());

			bookingsToDelete.stream().filter(booking -> booking.getClient() != null) // Solo si hay cliente
					.forEach(booking -> {
						notificationFactory.send(buildRequest(booking, service.getName()));
					});
			bookingRepository.deleteAllByServiceId(service.getId());
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

	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public User updateProvider(User provider) {
		return userProcess.updateUser(provider);
	}
}
