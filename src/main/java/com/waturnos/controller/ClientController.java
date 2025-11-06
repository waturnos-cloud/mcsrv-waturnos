package com.waturnos.controller;

import java.util.List;

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
	 * @param name the name
	 * @return the response entity
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<ClientDTO>>> search(@RequestParam(required = false) String email,
			@RequestParam(required = false) String phone, @RequestParam(required = false) String name) {

		List<Client> clients = service.search(email, phone, name);
		return ResponseEntity.ok(new ApiResponse<>(true, "Clients found", mapper.toDtoList(clients)));
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
}
