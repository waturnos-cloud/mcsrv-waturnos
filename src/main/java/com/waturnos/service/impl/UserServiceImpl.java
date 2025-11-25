package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Organization;
import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.UserService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.BatchProcessor;
import com.waturnos.service.process.UserProcess;
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
	
	private final BatchProcessor batchProcessor;

	/**
	 * Find all.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.SELLER })
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
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.SELLER })
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
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.SELLER })
	@AuditAspect(eventCode = "USER_CREATE_MANAGER", behavior = "Creación de manager")
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
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@AuditAspect(eventCode = "USER_UPDATE_MANAGER", behavior = "Actualización de usuario manager")
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
	}

	/**
	 * Creates the provider.
	 *
	 * @param organizationId the organization id
	 * @param provider       the provider
	 * @return the user
	 */
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.SELLER })
	@AuditAspect(eventCode = "USER_CREATE_PROVIDER", behavior = "Creación de provider")
	public User createProvider(Long organizationId, User provider) {
		return userProcess.createProvider(Organization.builder().id(organizationId).build(), provider);
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

		batchProcessor.deleteProviderAsync(providerId);
	}

	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@AuditAspect(eventCode = "USER_UPDATE_PROVIDER", behavior = "Actualización de usuario provider")
	public User updateProvider(User provider) {
		return userProcess.updateUser(provider);
	}
}
