package com.waturnos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.request.CreateOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.mapper.OrganizationMapper;
import com.waturnos.mapper.UserMapper;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.OrganizationService;

@WebMvcTest(OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrganizationController Tests")
class OrganizationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrganizationService organizationService;

	@MockBean
	private OrganizationMapper organizationMapper;

	@MockBean
	private UserMapper userMapper;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	private Organization testOrganization;
	private OrganizationDTO testOrganizationDTO;

	@BeforeEach
	void setUp() {
		testOrganization = new Organization();
		testOrganization.setId(1L);
		testOrganization.setName("Test Organization");
		testOrganization.setEmail("test@organization.com");
		testOrganization.setStatus(OrganizationStatus.ACTIVE);

		testOrganizationDTO = new OrganizationDTO();
		testOrganizationDTO.setId(1L);
		testOrganizationDTO.setName("Test Organization");
		testOrganizationDTO.setEmail("test@organization.com");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener todas las organizaciones exitosamente")
	void testGetAllOrganizations_Success() throws Exception {
		// Arrange
		List<Organization> organizations = List.of(testOrganization);
		when(organizationService.findAll(any(Authentication.class))).thenReturn(organizations);
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(get("/organizations")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("Test Organization"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener organización por ID exitosamente")
	void testGetOrganizationById_Success() throws Exception {
		// Arrange
		when(organizationService.findById(anyLong())).thenReturn(Optional.of(testOrganization));
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(get("/organizations/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Organization"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener organización por ID - No encontrada")
	void testGetOrganizationById_NotFound() throws Exception {
		// Arrange
		when(organizationService.findById(anyLong())).thenReturn(Optional.empty());

		// Act & Assert
		mockMvc.perform(get("/organizations/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Crear organización exitosamente")
	void testCreateOrganization_Success() throws Exception {
		// Arrange
		CreateOrganization createRequest = new CreateOrganization();
		createRequest.setOrganization(testOrganizationDTO);
		
		User manager = new User();
		manager.setId(1L);
		manager.setEmail("manager@test.com");

		when(organizationMapper.toEntity(any(OrganizationDTO.class))).thenReturn(testOrganization);
		when(userMapper.toEntity(any())).thenReturn(manager);
		when(organizationService.create(any(Organization.class), any(User.class))).thenReturn(testOrganization);
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(post("/organizations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization created"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Actualizar información básica exitosamente")
	void testUpdateBasicInfo_Success() throws Exception {
		// Arrange
		when(organizationMapper.toEntity(any(OrganizationDTO.class))).thenReturn(testOrganization);
		when(organizationService.updateBasicInfo(any(Organization.class))).thenReturn(testOrganization);
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(put("/organizations/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testOrganizationDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization updated"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Actualizar ubicaciones exitosamente")
	void testUpdateLocations_Success() throws Exception {
		// Arrange
		when(organizationMapper.mapLocationsToEntity(any())).thenReturn(List.of());
		when(organizationService.updateLocations(anyLong(), any())).thenReturn(testOrganization);
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(put("/organizations/updatelocations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testOrganizationDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization updated"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Activar/Desactivar organización exitosamente")
	void testActivateOrDeactivate_Success() throws Exception {
		// Arrange
		when(organizationService.activateOrDeactivate(anyLong(), any(OrganizationStatus.class)))
				.thenReturn(testOrganization);
		when(organizationMapper.toDto(any(Organization.class), any(Boolean.class))).thenReturn(testOrganizationDTO);

		// Act & Assert
		mockMvc.perform(put("/organizations/activate/1/ACTIVE")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization updated"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Eliminar organización exitosamente")
	void testDeleteOrganization_Success() throws Exception {
		// Arrange
		doNothing().when(organizationService).delete(anyLong());

		// Act & Assert
		mockMvc.perform(delete("/organizations/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization deleted"));
	}
}
