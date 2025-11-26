package com.waturnos.controller.stateless;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.service.OrganizationService;

import lombok.RequiredArgsConstructor;

/**
 * The Class OrganizationControllerStateless.
 */
@RestController
@RequestMapping("/public/organizations")

/**
 * Instantiates a new organization controller stateless.
 *
 * @param service            the service
 * @param organizationMapper the organization mapper
 * @param userMapper         the user mapper
 */
@RequiredArgsConstructor
public class OrganizationControllerStateless {

	/** The service. */
	private final OrganizationService service;

	/** The mapper. */
	private final OrganizationMapper organizationMapper;

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	@GetMapping
	public ResponseEntity<List<OrganizationDTO>> getAll() {
		return ResponseEntity.ok(service.findAll().stream().map(o -> organizationMapper.toDto(o, false)).toList());
	}


		/**
	 * Gets the by id.
	 *
	 * @param id the id
	 * @return the by id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id) {
		return service.findById(id).map(o -> organizationMapper.toDto(o, true)).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
