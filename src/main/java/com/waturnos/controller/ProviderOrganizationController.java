package com.waturnos.controller;

import com.waturnos.dto.ProviderOrganizationDTO;
import com.waturnos.service.ProviderOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/provider-organizations")
@Tag(name = "Provider Organization", description = "Gestión de relación entre proveedores y organizaciones")
public class ProviderOrganizationController {

	private final ProviderOrganizationService service;

	public ProviderOrganizationController(ProviderOrganizationService service) {
		this.service = service;
	}

	@Operation(summary = "Crear relación proveedor-organización")
	@PostMapping
	public ResponseEntity<ProviderOrganizationDTO> create(@RequestBody ProviderOrganizationDTO dto) {
		return ResponseEntity.ok(service.create(dto));
	}

	@Operation(summary = "Listar todas las relaciones proveedor-organización")
	@GetMapping
	public ResponseEntity<List<ProviderOrganizationDTO>> findAll() {
		return ResponseEntity.ok(service.findAll());
	}

	@Operation(summary = "Buscar por ID")
	@GetMapping("/{id}")
	public ResponseEntity<ProviderOrganizationDTO> findById(@PathVariable Long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	@Operation(summary = "Buscar por proveedor")
	@GetMapping("/provider/{providerId}")
	public ResponseEntity<List<ProviderOrganizationDTO>> findByProvider(@PathVariable Long providerId) {
		return ResponseEntity.ok(service.findByProvider(providerId));
	}

	@Operation(summary = "Buscar por organización")
	@GetMapping("/organization/{organizationId}")
	public ResponseEntity<List<ProviderOrganizationDTO>> findByOrganization(@PathVariable Long organizationId) {
		return ResponseEntity.ok(service.findByOrganization(organizationId));
	}

	@Operation(summary = "Actualizar relación")
	@PutMapping("/{id}")
	public ResponseEntity<ProviderOrganizationDTO> update(@PathVariable Long id,
			@RequestBody ProviderOrganizationDTO dto) {
		return ResponseEntity.ok(service.update(id, dto));
	}

	@Operation(summary = "Eliminar relación")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}