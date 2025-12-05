package com.waturnos.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
	 * @param paramSearch el parámetro de búsqueda (puede ser email, phone o dni)
	 * @return the response entity
	 */
	@GetMapping("/findBy")
	public ResponseEntity<ApiResponse<ClientDTO>> findBy(@RequestParam String paramSearch) {

		// Busca por email, phone o dni usando el mismo parámetro
		Optional<Client> clientOptional = service.findByEmailOrPhoneOrDni(paramSearch, paramSearch, paramSearch);

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
	 * Search clients.
	 *
	 * @param name the name
	 * @param email the email
	 * @param phone the phone
	 * @param dni the dni
	 * @param organizationId the organization id
	 * @return the response entity
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<ClientDTO>>> searchClients(@RequestParam(required = false) String name,
			@RequestParam(required = false) String email, @RequestParam(required = false) String phone,
			@RequestParam(required = false) String dni,
			@RequestParam(required = true) Long organizationId) {
		List<Client> clients = service.searchClients(name, email, phone, dni, organizationId);

        List<ClientDTO> clientDTOs = clients.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Clients retrieved successfully for organization ID: " + organizationId, 
                clientDTOs
        ));
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
	 * @param clientId the client id
	 * @param organizationId the organization id
	 * @return the response entity
	 */
	@PostMapping("{clientId}/{organizationId}")
	public ResponseEntity<ApiResponse<Void>> assignClientToOrganization(@PathVariable Long clientId,
			@PathVariable Long organizationId) {
		service.assignClientToOrganization(clientId, organizationId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client vinculated", null));
	}

	/**
	 * Notify a client. POST /clients/{clientId}/notify
	 */
	@PostMapping("{clientId}/notify")
	public ResponseEntity<ApiResponse<Void>> notifyClient(@PathVariable Long clientId,
			@RequestBody com.waturnos.dto.beans.ClientNotificationDTO dto) {
		service.notifyClient(clientId, dto);
		return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent", null));
	}

	/**
	 * Unassign client from organization.
	 * DELETE /clients/{clientId}/{organizationId}
	 */
	@DeleteMapping("{clientId}/{organizationId}")
	public ResponseEntity<ApiResponse<Void>> unassignClientFromOrganization(@PathVariable Long clientId,
			@PathVariable Long organizationId) {
		service.unassignClientFromOrganization(clientId, organizationId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client unvinculated", null));
	}
	
	/**
	 * Listar todos los clientes asociados a una organización específica.
	 * GET /api/organizations/{organizationId}/clients
	 *
	 * @param organizationId the organization id
	 * @return the clients by organization
	 */
    @GetMapping("/listByOrganization/{organizationId}")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getClientsByOrganization(
            @PathVariable Long organizationId) {

        List<Client> clients = service.findByOrganization(organizationId);

        List<ClientDTO> clientDTOs = clients.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Clients retrieved successfully for organization ID: " + organizationId, 
                clientDTOs
        ));
    }

	/**
	 * Update.
	 *
	 * @param id  the id
	 * @param dto the dto
	 * @return the response entity
	 */
	@PutMapping
	public ResponseEntity<ApiResponse<ClientDTO>> update(@RequestBody ClientDTO dto) {
		Client updated = service.update(mapper.toEntity(dto));
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

	/**
	 * Get upcoming bookings for a client.
	 * Returns all future bookings from now onwards, ordered by date ascending.
	 * Optionally filters by organization and date range.
	 *
	 * @param clientId the client id
	 * @param organizationId optional organization id filter
	 * @param fromDate optional start date filter (ISO format: 2025-11-27T14:00:00)
	 * @param toDate optional end date filter (ISO format: 2025-12-31T23:59:59)
	 * @return the response entity with list of upcoming bookings
	 */
	@GetMapping("/{clientId}/bookings/upcoming")
	public ResponseEntity<ApiResponse<List<com.waturnos.dto.response.ClientBookingDTO>>> getUpcomingBookings(
			@PathVariable Long clientId,
			@RequestParam(required = false) Long organizationId,
			@RequestParam(required = false) java.time.LocalDateTime fromDate,
			@RequestParam(required = false) java.time.LocalDateTime toDate) {
		List<com.waturnos.dto.response.ClientBookingDTO> bookings = service.getUpcomingBookings(clientId, organizationId, fromDate, toDate);
		return ResponseEntity.ok(new ApiResponse<>(true, "Upcoming bookings retrieved", bookings));
	}
}

