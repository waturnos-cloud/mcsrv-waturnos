package com.waturnos.service;

import java.util.List;
import java.util.Optional;

import com.waturnos.entity.User;

/**
 * The Interface UserService.
 */
public interface UserService {
	
	/**
	 * Find by email.
	 *
	 * @param email the email
	 * @return the optional
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Creates the manager.
	 *
	 * @param organizationId the id
	 * @param entity the entity
	 * @return the user
	 */
	User createManager(Long organizationId, User entity);
	
	/**
	 * Update manager.
	 *
	 * @param user the user
	 * @return the user
	 */
	User updateManager(User user);

	/**
	 * Delete manager.
	 *
	 * @param id the id
	 */
	void deleteManager(Long id);

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the optional
	 */
	Optional<User> findById(Long id);

	/**
	 * Find all.
	 *
	 * @param organizationId the organization id
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
	 * @param provider the provider
	 * @return the user
	 */
	User createProvider(Long organizationId, User provider);

	/**
	 * Delete.
	 *
	 * @param providerId the provider id
	 */
	void deleteProvider(Long providerId);

	/**
	 * Update.
	 *
	 * @param provider the provider
	 * @return the provider
	 */
	User updateProvider(User provider);
	
}
