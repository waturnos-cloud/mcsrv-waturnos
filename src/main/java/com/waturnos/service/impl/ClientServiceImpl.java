package com.waturnos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.waturnos.entity.Client;
import com.waturnos.repository.ClientRepository;
import com.waturnos.service.ClientService;
import com.waturnos.service.exceptions.EntityNotFoundException;

/**
 * The Class ClientServiceImpl.
 */
@Service
public class ClientServiceImpl implements ClientService {

	/** The client repository. */
	private final ClientRepository clientRepository;

	/**
	 * Instantiates a new client service impl.
	 *
	 * @param clientRepository the client repository
	 */
	public ClientServiceImpl(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	public List<Client> findByOrganization(Long organizationId) {
		return clientRepository.findByOrganizationId(organizationId);
	}

	/**
	 * Creates the.
	 *
	 * @param client the client
	 * @return the client
	 */
	@Override
	public Client create(Client client) {
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

	@Override
	public List<Client> findAll() {
		return clientRepository.findAll();
	}

	@Override
	public Client findById(Long id) {
		return clientRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
	}

	@Override
	public List<Client> search(String email, String phone, String name) {
		// Si todos los filtros son nulos, devuelve todos
		if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())
				&& (name == null || name.isBlank())) {
			return clientRepository.findAll();
		}

		// Busca por coincidencias parciales en cualquiera de los campos
		return clientRepository
				.findByEmailContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrFullNameContainingIgnoreCase(
						email != null ? email : "", phone != null ? phone : "", name != null ? name : "");
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

}
