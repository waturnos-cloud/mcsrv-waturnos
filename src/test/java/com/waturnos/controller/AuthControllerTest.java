package com.waturnos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.request.LoginRequest;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.security.JwtUtil;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JwtUtil jwtUtil;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private MessageSource messageSource;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;

	private User testUser;
	private Organization testOrganization;

	@BeforeEach
	void setUp() {
		testOrganization = new Organization();
		testOrganization.setId(1L);
		testOrganization.setName("Test Org");
		testOrganization.setStatus(OrganizationStatus.ACTIVE);

		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@example.com");
		testUser.setPassword("password");
		testUser.setOrganization(testOrganization);
	}

	@Test
	@DisplayName("Login exitoso con credenciales válidas - PROVIDER")
	void testLoginSuccess_Provider() throws Exception {
		// Arrange
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setEmail("test@example.com");
		loginRequest.setPassword("password");

		UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
				.username("test@example.com")
				.password("password")
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROVIDER")))
				.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
		when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

		// Act & Assert
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("mock-jwt-token"))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.role").value("PROVIDER"))
				.andExpect(jsonPath("$.organizationId").value(1))
				.andExpect(jsonPath("$.providerId").value(1));
	}

	@Test
	@DisplayName("Login exitoso con credenciales válidas - ADMIN")
	void testLoginSuccess_Admin() throws Exception {
		// Arrange
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setEmail("admin@example.com");
		loginRequest.setPassword("password");

		UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
				.username("admin@example.com")
				.password("password")
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));
		when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

		// Act & Assert
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("mock-jwt-token"))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("Login fallido con credenciales inválidas")
	void testLoginFailure_InvalidCredentials() throws Exception {
		// Arrange
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setEmail("test@example.com");
		loginRequest.setPassword("wrongpassword");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));
		when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Invalid username or password");

		// Act & Assert
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.errorCode").exists())
				.andExpect(jsonPath("$.message").value("Invalid username or password"));
	}

	@Test
	@DisplayName("Login fallido con organización inactiva")
	void testLoginFailure_InactiveOrganization() throws Exception {
		// Arrange
		testOrganization.setStatus(OrganizationStatus.INACTIVE);

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setEmail("test@example.com");
		loginRequest.setPassword("password");

		UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
				.username("test@example.com")
				.password("password")
				.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER")))
				.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

		// Act & Assert
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isUnauthorized());
	}
}
