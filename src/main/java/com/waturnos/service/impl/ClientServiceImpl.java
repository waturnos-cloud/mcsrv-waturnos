package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.waturnos.entity.Client;
import com.waturnos.entity.ClientOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.ClientOrganizationRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.ClientService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;

/**
 * The Class ClientServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

	/** The client repository. */
	private final ClientRepository clientRepository;
	
	/** The client organization repository. */
	private final ClientOrganizationRepository clientOrganizationRepository;
	
	/** The organization repository. */
	private final OrganizationRepository organizationRepository;

	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	public List<Client> findByOrganization(Long organizationId) {
		Organization organization = organizationRepository.findById(organizationId)
	            .orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));

	    return organization.getClientOrganizations().stream()
	            .map(ClientOrganization::getClient)
	            .collect(Collectors.toList());
	}

	/**
	 * Creates the.
	 *
	 * @param client the client
	 * @return the client
	 */
	@Override
	public Client create(Client client) {
	
	    final String email = StringUtils.hasLength(client.getEmail()) ? client.getEmail().trim() : null;
	    final String dni = StringUtils.hasLength(client.getDni()) ? client.getDni().trim() : null;
	    final String phone = StringUtils.hasLength(client.getPhone()) ? client.getPhone().trim() : null;
	    
	    Optional<Client> clientDB = clientRepository.findExistingClientByUniqueFields(email, dni, phone);

	    if (clientDB.isPresent()) {
	        Client existingClient = clientDB.get();
	        String errorMessage = "Client already exists.";
	        
	        if (email != null && email.equals(existingClient.getEmail())) {
	            errorMessage = "Email already exists exception in client";
	        } 
	        else if (dni != null && dni.equals(existingClient.getDni())) {
	            errorMessage = "DNI already exists exception in client";
	        } 
	        else if (phone != null && phone.equals(existingClient.getPhone())) {
	            errorMessage = "Phone already exists exception in client";
	        }
	        throw new ServiceException(ErrorCode.CLIENT_EXISTS, errorMessage);
	    }
		
		return clientRepository.save(client);
	}

	/**
	 * Update.
	 *
	 * @param id     the id
	 * @param client the client
	 * @return the client
	 */
	@Override
	public Client update(Long id, Client client) {
		Client existing = clientRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Client not found"));
		client.setId(existing.getId());
		return clientRepository.save(client);
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 */
	@Override
	public void delete(Long id) {
		if (!clientRepository.existsById(id))
			throw new EntityNotFoundException("Client not found");
		clientRepository.deleteById(id);
	}

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	public List<Client> findAll() {
		return clientRepository.findAll();
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the client
	 */
	@Override
	public Client findById(Long id) {
		return clientRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
	}

	/**
	 * Find by email or phone or dni.
	 *
	 * @param email the email
	 * @param phone the phone
	 * @param dni the dni
	 * @return the optional
	 */
	@Override
	@RequireRole(value = {UserRole.ADMIN,UserRole.MANAGER, UserRole.PROVIDER})
	public Optional<Client> findByEmailOrPhoneOrDni(String email, String phone, String dni) {
		return clientRepository
				.findByEmailOrPhoneOrDni(email,phone,dni);
	}

	/**
	 * Count all.
	 *
	 * @return the long
	 */
	@Override
	public long countAll() {
	    return clientRepository.count();
	}
	
	
	/**
	 * Find by provider id.
	 *
	 * @param providerId the provider id
	 * @return the list
	 */
	public List<Client> findByProviderId(Long providerId) {
	    return clientRepository.findByProviderId(providerId);
	}

	/**
	 * Assign client to organization.
	 *
	 * @param clientId the client id
	 * @param organizationId the organization id
	 */
	@Override
	public void assignClientToOrganization(Long clientId, Long organizationId) {
		Optional<Client> clientDB = clientRepository.findById(clientId);
		if (!clientDB.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found");
		}
		Optional<Organization> organizationDB = organizationRepository.findById(organizationId);
		if (!organizationDB.isPresent()) {
			throw new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found");
		}	
		
		clientOrganizationRepository.save(ClientOrganization.builder()
				.client(clientDB.get())
				.organization(organizationDB.get())
				.build());
		
	}

}
