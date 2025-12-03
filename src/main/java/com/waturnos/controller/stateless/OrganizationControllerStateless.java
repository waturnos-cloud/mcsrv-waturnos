package com.waturnos.controller.stateless;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	/**
	 * Obtiene organización por subdominio.
	 *
	 * @param subdomain subdominio
	 * @return organización en modo completo si existe
	 */
	@GetMapping("/bySubdomain")
	public ResponseEntity<OrganizationDTO> getBySubdomain(@RequestParam String subdomain) {
		return service.findBySubdomain(subdomain)
				.map(o -> organizationMapper.toDto(o, true))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Gets organizations by category.
	 *
	 * @param categoryId the category id
	 * @return the organizations by category
	 */
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<OrganizationDTO>> getByCategory(@PathVariable Long categoryId) {
		return ResponseEntity.ok(service.findByCategory(categoryId).stream()
				.map(o -> organizationMapper.toDto(o, false)).toList());
	}
}
