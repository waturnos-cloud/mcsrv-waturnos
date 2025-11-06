package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.UserService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
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
 * @param userRepository the user repository
 * @param passwordEncoder the password encoder
 * @param userProcess the user process
 */
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	/** The user repository. */
	private final UserRepository userRepository;
	
	/** The user process. */
	private final UserProcess userProcess;
	
	private final SecurityAccessEntity securityAccessEntity;
	

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public List<User> findManagersByOrganization(Long organizationId) {
		return this.findUsersByOrganizationPrivate(organizationId, UserRole.MANAGER);
	}
	
	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public List<User> findProvidersByOrganization(Long organizationId) {
		return this.findUsersByOrganizationPrivate(organizationId, UserRole.PROVIDER);
	}
	
	/**
	 * Find users by organization private.
	 *
	 * @param organizationId the organization id
	 * @param userRole the user role
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
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public User createManager(Long organizationId, User manager) {
		return userProcess.createManager(Organization.builder()
				.id(organizationId)
				.build(), manager);
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param user the user
	 * @return the user
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public User updateManager(User user) {
		return userProcess.updateUser(user);
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	@Transactional
	public void deleteManager(Long id) {
		Optional<User> userDB = userRepository.findById(id);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		if(!UserRole.MANAGER.equals(userDB.get().getRole())){
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Not modified user that not MANAGER.");
		}
		if(SessionUtil.getCurrentUser().getId().equals(id)){
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot remove yourself as manager");
		}
		
		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());
		
		userRepository.deleteById(id);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public User createProvider(Long organizationId, User provider) {
		return userProcess.createManager(Organization.builder()
				.id(organizationId)
				.build(), provider);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public void deleteProvider(Long providerId, Long organizationId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER})
	public User updateProvider(User provider) {
		return userProcess.updateUser(provider);
	}
}
