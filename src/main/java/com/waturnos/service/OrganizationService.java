package com.waturnos.service;

import java.util.List;
import java.util.Optional;

import com.waturnos.entity.Location;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;

/**
 * The Interface OrganizationService.
 */
public interface OrganizationService {

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	List<Organization> findAll();

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the optional
	 */
	Optional<Organization> findById(Long id);

	/**
	 * Creates the.
	 *
	 * @param org the org
	 * @param manager the manager
	 * @param b the b
	 * @return the organization
	 */
	Organization create(Organization org, User manager, boolean b);

	/**
	 * Update basic info.
	 *
	 * @param id the id
	 * @param org the org
	 * @return the organization
	 */
	Organization updateBasicInfo(Long id, Organization org);
	
	/**
	 * Update locations.
	 *
	 * @param id the id
	 * @param locations the locations
	 * @return the organization
	 */
	Organization updateLocations(Long id, List<Location> locations);

	/**
	 * Delete.
	 *
	 * @param id the id
	 */
	void delete(Long id);

	/**
	 * Activate or deactivate.
	 *
	 * @param id the id
	 * @param organizationStatus the organization status
	 * @return the organization
	 */
	Organization activateOrDeactivate(Long id, OrganizationStatus organizationStatus);

	/**
	 * Adds the manager.
	 *
	 * @param id the id
	 * @param entity the entity
	 * @return the user
	 */
	User addManager(Long id, User entity);
	
	/**
	 * Adds the manager.
	 *
	 * @param id the id
	 * @param entity the entity
	 * @return the user
	 */
	Provider addProvider(Long id, Provider entity);
}
