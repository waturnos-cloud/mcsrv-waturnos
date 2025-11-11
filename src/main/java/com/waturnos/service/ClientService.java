package com.waturnos.service;

import com.waturnos.entity.Client;
import java.util.List;

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
	 * Search.
	 *
	 * @param email the email
	 * @param phone the phone
	 * @param name  the name
	 * @return the list
	 */
	List<Client> search(String email, String phone, String name);

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


}
