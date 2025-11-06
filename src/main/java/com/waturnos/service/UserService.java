package com.waturnos.service;

import java.util.List;
import java.util.Optional;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;

public interface UserService {
	
	Optional<User> findByEmail(String email);

	/**
	 * Creates the manager.
	 *
	 * @param organizationId the id
	 * @param entity the entity
	 * @return the user
	 */
	User createManager(Long organizationId, User entity);
	
	User updateManager(User user);

	void deleteManager(Long id);

	Optional<User> findById(Long id);

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	List<User> findManagersByOrganization(Long organizationId);
	
	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	List<User> findProvidersByOrganization(Long organizationId);

	/**
	 * Creates the manager.
	 *
	 * @param organizationId the id
	 * @param entity the entity
	 * @return the user
	 */
	User createProvider(Long organizationId, User provider);

	/**
	 * Delete.
	 *
	 * @param providerId the provider id
	 * @param organizationId the organization id
	 */
	void deleteProvider(Long providerId, Long organizationId);

	/**
	 * Update.
	 *
	 * @param provider the provider
	 * @return the provider
	 */
	User updateProvider(User provider);
}
