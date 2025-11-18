package com.waturnos.service;

import java.util.List;
import java.util.Optional;

import com.waturnos.entity.Client;

/**
 * The Interface ClientService.
 */
public interface ClientService {

	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	List<Client> findByOrganization(Long organizationId);

	/**
	 * Creates the.
	 *
	 * @param client the client
	 * @return the client
	 */
	Client create(Client client);

	/**
	 * Update.
	 *
	 * @param id     the id
	 * @param client the client
	 * @return the client
	 */
	Client update(Long id, Client client);

	/**
	 * Delete.
	 *
	 * @param id the id
	 */
	void delete(Long id);

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	List<Client> findAll();

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the client
	 */
	Client findById(Long id);

	/**
	 * Count all.
	 *
	 * @return the long
	 */
	long countAll();

	/**
	 * Find by provider id.
	 *
	 * @param providerId the provider id
	 * @return the list
	 */
	List<Client> findByProviderId(Long providerId);

	Optional<Client> findByEmailOrPhoneOrDni(String email, String phone, String dni);


}
