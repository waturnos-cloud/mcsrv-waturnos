package com.waturnos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.mapper.ClientMapper;
import com.waturnos.service.ClientService;

/**
 * The Class ClientController.
 */
@RestController
@RequestMapping("/clients")
public class ClientController {

	/** The service. */
	private final ClientService service;

	/** The mapper. */
	private final ClientMapper mapper;

	/**
	 * Instantiates a new client controller.
	 *
	 * @param s the s
	 * @param m the m
	 */
	public ClientController(ClientService s, ClientMapper m) {
		this.service = s;
		this.mapper = m;
	}

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<ClientDTO>>> getAll() {
		List<Client> clients = service.findAll();
		return ResponseEntity.ok(new ApiResponse<>(true, "Clients retrieved", mapper.toDtoList(clients)));
	}
	
	/**
	 * Gets the all by provider.
	 *
	 * @param providerId the provider id
	 * @return the all by provider
	 */
	@GetMapping("/provider/{providerId}")
	public ResponseEntity<ApiResponse<List<ClientDTO>>> getByProvider(@PathVariable Long providerId) {
	    List<Client> clients = service.findByProviderId(providerId);
	    return ResponseEntity.ok(new ApiResponse<>(true, "Clients by provider", mapper.toDtoList(clients)));
	}

	/**
	 * Gets the by id.
	 *
	 * @param id the id
	 * @return the by id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDTO>> getById(@PathVariable Long id) {
		Client client = service.findById(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client found", mapper.toDto(client)));
	}

	/**
	 * Search.
	 *
	 * @param email the email
	 * @param phone the phone
	 * @param name  the name
	 * @return the response entity
	 */
	@GetMapping("/findBy")
	public ResponseEntity<ApiResponse<ClientDTO>> findBy(@RequestParam(required = false) String email,
			@RequestParam(required = false) String phone, 
			@RequestParam(required = false) String dni) {

		Optional<Client> clientOptional = service.findByEmailOrPhoneOrDni(email, phone, dni);

	    if (clientOptional.isPresent()) {
	        ClientDTO clientDTO = mapper.toDto(clientOptional.get());
	        return ResponseEntity.ok(new ApiResponse<>(true, "Client found successfully.", clientDTO));
	    } else {
	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .body(new ApiResponse<>(false, "Client not found with the provided criteria.", null));
	    }	
	}

	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<ClientDTO>> create(@RequestBody ClientDTO dto) {
		Client created = service.create(mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Client created", mapper.toDto(created)));
	}
	
	/**
	 * Creates the.
	 *
	 * @param dto the dto
	 * @return the response entity
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> assignClientToOrganization(@PathVariable Long clientId,
			@PathVariable Long organizationId) {
		service.assignClientToOrganization(clientId, organizationId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client vinculated", null));
	}

	/**
	 * Update.
	 *
	 * @param id  the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDTO>> update(@PathVariable Long id, @RequestBody ClientDTO dto) {
		Client updated = service.update(id, mapper.toEntity(dto));
		return ResponseEntity.ok(new ApiResponse<>(true, "Client updated", mapper.toDto(updated)));
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the response entity
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client deleted", null));
	}

	/**
	 * Count clients.
	 *
	 * @return the response entity
	 */
	@GetMapping("/count")
	public ResponseEntity<ApiResponse<Long>> countClients() {
		long count = service.countAll();
		return ResponseEntity.ok(new ApiResponse<>(true, "Total clients", count));
	}
}
