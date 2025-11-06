package com.waturnos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.waturnos.entity.Location;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;

/**
 * The Interface OrganizationService.
 */
public interface OrganizationService {

	/**
	 * Find all.
	 * @param authentication 
	 *
	 * @return the list
	 */
	List<Organization> findAll(Authentication authentication);

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
	Organization create(Organization org, User manager);

	/**
	 * Update basic info.
	 *
	 * @param org the org
	 * @return the organization
	 */
	Organization updateBasicInfo(Organization org);
	
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
	 * Find all.
	 *
	 * @return the list
	 */
	List<Organization> findAll();

}
