package com.waturnos.service.process.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.waturnos.audit.AuditContext;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.UserProcess;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;
import com.waturnos.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


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

/** The Constant log. */
@Slf4j
public class UserProcessImpl  implements UserProcess{
	
	
	/** The user repository. */
	private final UserRepository userRepository;
	
	/** The password encoder. */
	private final PasswordEncoder passwordEncoder;
	
	/** The organization repository. */
	private final OrganizationRepository organizationRepository;
	
	/** The notification factory. */
	private final NotificationFactory notificationFactory;
	
	/** The message source. */
	private final MessageSource messageSource;
	
    @Value("${app.notification.WELCOME_USER}")
    private String baseUrlWelcomeUser;
    
	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;
	

	@Override
	public User createManager(Organization organization, User manager) {
		Organization organizationDB = organizationRepository.findById(organization.getId()).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));

		securityAccessEntity.controlValidAccessOrganization(organizationDB.getId());
		AuditContext.setOrganization(organizationDB);
		AuditContext.setObject(manager.getFullName());
		
		Optional<User> user = userRepository.findByEmail(manager.getEmail());
		if(user.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		
		return createUserPrivate(organizationDB, UserRole.MANAGER,
				manager);
	}

	
	
	/**
	 * Creates the user.
	 *
	 * @param organizationDB the organization DB
	 * @param role the role
	 * @param user the user
	 * @return the user
	 */
	private User createUserPrivate(Organization organizationDB, UserRole role, User user) {
		user.setOrganization(organizationDB);
		user.setRole(role);
		user.setCreatedAt(DateUtils.getCurrentDateTime());
		user.setCreator(SessionUtil.getUserName());
		String passwordUser = Utils.buildPassword(user.getFullName(), user.getPhone());
		log.error("Password inicial: "+ passwordUser);
		user.setPassword(passwordEncoder.encode(passwordUser));
		notificationFactory.sendAsync(buildRequest(user,passwordUser));
		return userRepository.save(user);

	}
	
	/**
	 * Builds the request.
	 *
	 * @param manager the manager
	 * @param temporalPasswordUser 
	 * @return the notification request
	 */
	private NotificationRequest buildRequest(User manager, String temporalPasswordUser) {
		Map<String, String> properties = new HashMap<>();
        properties.put("USERNAME", manager.getFullName());
        properties.put("TEMPORAL_PASSWORD",  temporalPasswordUser);
        properties.put("LINK",  baseUrlWelcomeUser);
		return NotificationRequest
				.builder().email(manager.getEmail()).language("ES")
				.subject(messageSource
				.getMessage("notification.subject.welcome_organization", null, LocaleContextHolder.getLocale()))
				.type(NotificationType.WELCOME_USER)
				.properties(properties).build();
	}

	@Override
	public User updateUser(User user) {
		User userDB = userRepository.findById(user.getId())
				.orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found"));
		securityAccessEntity.controlAccessToUserId(userDB.getId());
		if(UserRole.ADMIN != SessionUtil.getRoleUser()) {
			AuditContext.setOrganization(userDB.getOrganization());
		}	
		AuditContext.setObject(user.getFullName());
		if (StringUtils.hasLength(user.getPassword())) {
			userDB.setPassword(passwordEncoder.encode(user.getPassword()));
		}	
		userDB.setFullName(user.getFullName());
		userDB.setEmail(user.getEmail());
		userDB.setPhone(user.getPhone());
		userDB.setAvatar(user.getAvatar());
		userDB.setBio(user.getBio());
		userDB.setPhotoUrl(user.getPhotoUrl());
		userDB.setModificator(SessionUtil.getUserName());
		userDB.setUpdatedAt(DateUtils.getCurrentDateTime());
		return userRepository.save(userDB);
	}

	@Override
	public User createProvider(Organization organization, User provider) {
		Organization organizationDB = organizationRepository.findById(organization.getId()).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		
		securityAccessEntity.controlValidAccessOrganization(organizationDB.getId());
		AuditContext.setOrganization(organizationDB);
		AuditContext.setObject(provider.getFullName());
		Optional<User> existUser = userRepository.findByEmail(provider.getEmail());
		if(existUser.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		return createUserPrivate(organizationDB, UserRole.PROVIDER, User.builder()
				.email(provider.getEmail())
				.fullName(provider.getFullName())
				.bio(provider.getBio())
				.photoUrl(provider.getPhotoUrl())
				.phone(provider.getPhone())
				.build());
	}


}
