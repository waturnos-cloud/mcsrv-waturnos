package com.waturnos.controller.stateless;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.controller.ApiResponse;
import com.waturnos.service.ClientService;

import lombok.RequiredArgsConstructor;

/**
 * The Class ClientControllerStateless.
 * Public endpoints that don't require authentication.
 */
@RestController
@RequestMapping("/public/clients")
@RequiredArgsConstructor
public class ClientControllerStateless {

	/** The service. */
	private final ClientService service;

	/**
	 * Validate if a client exists and is linked to an organization.
	 * Used after login to determine if client needs registration or linking.
	 * This is a public endpoint - no authentication required.
	 *
	 * @param contact the contact (email or phone)
	 * @param organizationId the organization id
	 * @return the validation response
	 */
	@GetMapping("/validate")
	public ResponseEntity<ApiResponse<com.waturnos.dto.response.ClientValidationDTO>> validateClient(
			@RequestParam String contact,
			@RequestParam Long organizationId) {
		
		com.waturnos.dto.response.ClientValidationDTO validation = service.validateClient(contact, organizationId);
		return ResponseEntity.ok(new ApiResponse<>(true, "Client validation completed", validation));
	}
}
