package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.repository.ProviderRepository;
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
	
	/** The password encoder. */
	private final PasswordEncoder passwordEncoder;
	
	/** The user process. */
	private final UserProcess userProcess;
	
	private final SecurityAccessEntity securityAccessEntity;
	
	private final ProviderOrganizationRepository providerOrganizationRepository;
	
	private final ProviderRepository providerRepository;

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public List<User> findManagersByOrganization(Long organizationId) {
		if(!securityAccessEntity.hasValidAccessOrganization(organizationId)){
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot list users another organization");
		}
		if (SessionUtil.getCurrentUser().getRole() == UserRole.ADMIN) {
			return userRepository.findByOrganizationIdOrderByFullNameAsc(organizationId);
		}
		return userRepository.findByOrganizationIdAndRoleOrderByFullNameAsc(organizationId, SessionUtil.getCurrentUser().getRole());
	}
	
	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER})
	public List<User> findProvidersByOrganization(Long organizationId) {
		if(!securityAccessEntity.hasValidAccessOrganization(organizationId)){
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot list users another organization");
		}
		if (SessionUtil.getCurrentUser().getRole() == UserRole.ADMIN) {
			return userRepository.findByOrganizationIdOrderByFullNameAsc(organizationId);
		}
		return userRepository.findByOrganizationIdAndRoleOrderByFullNameAsc(organizationId, SessionUtil.getCurrentUser().getRole());
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
	 * @param user the user
	 * @return the user
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public User createManager(Long organizationId, User user) {
		return userProcess.createManager(Organization.builder()
				.id(organizationId)
				.build(), user, false);
	}

	/**
	 * Update.
	 *
	 * @param id the id
	 * @param user the user
	 * @return the user
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER})
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
		if(!securityAccessEntity.hasValidAccessOrganization(userDB.get().getOrganization().getId())) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot remove manager from another organization");
		}
		userRepository.deleteById(id);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public User createProvider(Long organizationId, User provider) {
		return userProcess.createManager(Organization.builder()
				.id(organizationId)
				.build(), provider, false);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	public void deleteProvider(Long providerId, Long organizationId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER})
	public User updateProvider(User provider) {
		return userProcess.upManager(Organization.builder()
				.id(organizationId)
				.build(), provider, false);
	}
}
