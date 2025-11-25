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

	/**
	 * Assign client to organization.
	 *
	 * @param clientId the client id
	 * @param organizationId the organization id
	 */
	void assignClientToOrganization(Long clientId, Long organizationId);

	/**
	 * Unassign client from organization.
	 *
	 * @param clientId the client id
	 * @param organizationId the organization id
	 */
	void unassignClientFromOrganization(Long clientId, Long organizationId);

	/**
	 * Notify a client (send email/whatsapp according to config)
	 *
	 * @param clientId the client id
	 * @param dto notification data
	 */
	void notifyClient(Long clientId, com.waturnos.dto.beans.ClientNotificationDTO dto);

	/**
	 * Search clients.
	 *
	 * @param name the name
	 * @param email the email
	 * @param phone the phone
	 * @param dni the dni
	 * @param organizationId the organization id
	 * @return the list
	 */
	List<Client> searchClients(String name, String email, String phone, String dni, Long organizationId);

	/**
	 * Update.
	 *
	 * @param entity the entity
	 * @return the client
	 */
	Client update(Client entity);
	
	/**
	 * Find or create client for login.
	 * If client doesn't exist, creates a new one.
	 *
	 * @param organizationId the organization id
	 * @param email the email (optional)
	 * @param phone the phone (optional)
	 * @return the client
	 */
	/**
	 * Check if organization exists.
	 * 
	 * @param organizationId the organization id
	 * @return true if exists, false otherwise
	 */
	boolean organizationExists(Long organizationId);
	
	/**
	 * Find existing client if exists (does not create, does not throw exception).
	 * 
	 * @param organizationId the organization id
	 * @param email the client email (optional)
	 * @param phone the client phone (optional)
	 * @return the existing client or null if not found
	 */
	Client findClientIfExists(Long organizationId, String email, String phone);
	
	/**
	 * Register a new client and link to organization.
	 * 
	 * @param organizationId the organization id
	 * @param email the client email (optional)
	 * @param phone the client phone (optional)
	 * @param fullName the client full name (optional)
	 * @param dni the client document/dni (optional)
	 * @return the newly created client
	 * @throws ServiceException if client already exists or validation fails
	 */
	Client registerClient(Long organizationId, String email, String phone, String fullName, String dni);

}
