package com.waturnos.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.waturnos.controller.exceptions.ErrorResponse;
import com.waturnos.dto.request.AccessTokenRequest;
import com.waturnos.dto.request.AccessTokenValidateRequest;
import com.waturnos.dto.request.ClientLoginRequest;
import com.waturnos.dto.request.LoginRequest;
import com.waturnos.dto.response.ClientLoginResponse;
import com.waturnos.dto.response.LoginResponse;
import com.waturnos.entity.Client;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.JwtUtil;
import com.waturnos.service.AccessTokenService;
import com.waturnos.service.ClientService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;

/**
 * The Class AuthController.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	/** The jwt util. */
	private final JwtUtil jwtUtil;
	
	/** The authentication manager. */
	private final AuthenticationManager authenticationManager;
	
	/** The message source. */
	private final MessageSource messageSource;
	
	/** The user repository. */
	private final UserRepository userRepository;
	
	/** The client service. */
	private final ClientService clientService;
    
    /** The access token service. */
    private final AccessTokenService accessTokenService;
	
	/**
	 * Solicita un código de acceso temporal (OTP) por email o teléfono.
	 *
	 * @param request the request
	 * @return the response entity
	 */
	@PostMapping("/access-token/request")
	public ResponseEntity<?> requestAccessToken(@RequestBody AccessTokenRequest request) {
		if (request.getEmail() == null && request.getPhone() == null) {
			return ResponseEntity.badRequest().body("Email or phone required");
		}
		accessTokenService.generateToken(request.getEmail(), request.getPhone());
		return ResponseEntity.ok("Código enviado");
	}

	/**
	 * Valida el código de acceso temporal y lo elimina si es válido.
	 *
	 * @param request the request
	 * @return the response entity
	 */
	@PostMapping("/access-token/validate")
	public ResponseEntity<?> validateAccessToken(@RequestBody AccessTokenValidateRequest request) {
		if ((request.getEmail() == null && request.getPhone() == null) || request.getCode() == null) {
			return ResponseEntity.badRequest().body("Email/phone and code required");
		}
		var tokenOpt = accessTokenService.validateToken(request.getEmail(), request.getPhone(), request.getCode());
		if (tokenOpt.isPresent()) {
			accessTokenService.deleteToken(tokenOpt.get().getId());
			return ResponseEntity.ok("Código válido");
		} else {
			return ResponseEntity.status(401).body("Código inválido o expirado");
		}
	}

	/**
	 * Login.
	 *
	 * @param request the request
	 * @param webRequest the web request
	 * @return the response entity
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, WebRequest webRequest) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String email = userDetails.getUsername();

			// Buscar el usuario real para obtener IDs
			User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

			String role = userDetails.getAuthorities().stream().findFirst()
					.map(auth -> auth.getAuthority().replace("ROLE_", "")).orElse("USER");

			String token = jwtUtil.generateToken(email, role);

			// Determinar organizationId y providerId según el rol
			Long organizationId = null;
			Long providerId = null;
			boolean checkStatus = true;
			switch (role) {
			case "ADMIN" -> {
				organizationId = null; // elige luego en el dashboard
				checkStatus = false;
				break;
			}
			case "MANAGER" -> {
				organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
				break;
			}
			case "PROVIDER" -> {
				organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
				providerId = user.getId();
				break;
			}
			}
			if (checkStatus && user.getOrganization().getStatus() != OrganizationStatus.ACTIVE) {
				throw new ServiceException(ErrorCode.ORGANIZATION_NOT_ACTIVE, "Organization not active");
			}
			// ✅ Respuesta extendida
			LoginResponse response = new LoginResponse(token, user.getId(), role, organizationId, providerId);

			return ResponseEntity.ok(response);

		} catch (AuthenticationException e) {
			ErrorResponse errorDetails = new ErrorResponse(ErrorCode.INVALID_USER_OR_PASSWORD.getCode(),
					messageSource.getMessage(ErrorCode.INVALID_USER_OR_PASSWORD.getMessageKey(), null,
							LocaleContextHolder.getLocale()),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
		} catch (ServiceException e) {
			ErrorResponse errorDetails = new ErrorResponse(e.getErrorCode().getCode(), e.getMessage(),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
		}
	}
	
	/**
	 * Client login endpoint.
	 * Authenticates and generates token. Returns clientId if exists, null if not.
	 *
	 * @param request the client login request
	 * @param webRequest the web request
	 * @return the response entity with token and optional client id
	 */
	@PostMapping("/client/login")
	public ResponseEntity<?> clientLogin(@RequestBody ClientLoginRequest request, WebRequest webRequest) {
		try {
			if ((request.getEmail() == null && request.getPhone() == null) || request.getCode() == null) {
				return ResponseEntity.badRequest().body("Email/phone and code required");
			}
			var tokenOpt = accessTokenService.validateToken(request.getEmail(), request.getPhone(), request.getCode());
			if (tokenOpt.isPresent()) {
				accessTokenService.deleteToken(tokenOpt.get().getId());
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido o expirado");
			}
			// Validar que venga organizationId
			if (request.getOrganizationId() == null) {
				ErrorResponse errorDetails = new ErrorResponse(
						ErrorCode.GLOBAL_ERROR.getCode(),
						"Organization ID is required",
						webRequest.getDescription(false));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
			}
			
			// Validar que venga al menos email o phone
			if (request.getEmail() == null && request.getPhone() == null) {
				ErrorResponse errorDetails = new ErrorResponse(
						ErrorCode.GLOBAL_ERROR.getCode(),
						"Email or phone is required",
						webRequest.getDescription(false));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
			}
			
			// Validar que la organización existe
			if (!clientService.organizationExists(request.getOrganizationId())) {
				ErrorResponse errorDetails = new ErrorResponse(
						ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION.getCode(),
						"Organization not found",
						webRequest.getDescription(false));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
			}
			
			// Buscar cliente existente (NO crear)
			Client client = clientService.findClientIfExists(
					request.getOrganizationId(),
					request.getEmail(),
					request.getPhone());
			
			// Generar token JWT con role CLIENT (usar identifier del request)
			String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();
			Long clientId = client != null ? client.getId() : null;
			String token = jwtUtil.generateClientToken(
					identifier,
					clientId,
					request.getOrganizationId());
			
			String message = client != null 
					? "Client logged in successfully" 
					: "Token generated, client not registered";
			
			ClientLoginResponse response = new ClientLoginResponse(
					token, 
					clientId, 
					message);
			
			return ResponseEntity.ok(response);
			
		} catch (ServiceException e) {
			ErrorResponse errorDetails = new ErrorResponse(
					e.getErrorCode().getCode(),
					e.getMessage(),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
		} catch (Exception e) {
			ErrorResponse errorDetails = new ErrorResponse(
					ErrorCode.GLOBAL_ERROR.getCode(),
					"An error occurred during client login: " + e.getMessage(),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
		}
	}
	
	/**
	 * Client registration endpoint.
	 * Creates a new client and links them to an organization.
	 *
	 * @param request the client login request
	 * @param webRequest the web request
	 * @return the response entity with token and client id
	 */
	@PostMapping("/client/register")
	public ResponseEntity<?> clientRegister(@RequestBody ClientLoginRequest request, WebRequest webRequest) {
		try {
			// Validar que venga organizationId
			if (request.getOrganizationId() == null) {
				ErrorResponse errorDetails = new ErrorResponse(
						ErrorCode.GLOBAL_ERROR.getCode(),
						"Organization ID is required",
						webRequest.getDescription(false));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
			}
			
			// Validar que venga al menos email o phone
			if (request.getEmail() == null && request.getPhone() == null) {
				ErrorResponse errorDetails = new ErrorResponse(
						ErrorCode.GLOBAL_ERROR.getCode(),
						"Email or phone is required",
						webRequest.getDescription(false));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
			}
			
			// Crear cliente nuevo
			Client client = clientService.registerClient(
					request.getOrganizationId(),
					request.getEmail(),
					request.getPhone(),
					request.getFullName(),
					request.getDni());
			
			// Generar token JWT con role CLIENT
			String identifier = client.getEmail() != null ? client.getEmail() : client.getPhone();
			String token = jwtUtil.generateClientToken(
					identifier,
					client.getId(),
					request.getOrganizationId());
			
			ClientLoginResponse response = new ClientLoginResponse(
					token, 
					client.getId(), 
					"Client registered successfully");
			
			return ResponseEntity.ok(response);
			
		} catch (ServiceException e) {
			ErrorResponse errorDetails = new ErrorResponse(
					e.getErrorCode().getCode(),
					e.getMessage(),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
		} catch (Exception e) {
			ErrorResponse errorDetails = new ErrorResponse(
					ErrorCode.GLOBAL_ERROR.getCode(),
					"An error occurred during client registration: " + e.getMessage(),
					webRequest.getDescription(false));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
		}
	}
}