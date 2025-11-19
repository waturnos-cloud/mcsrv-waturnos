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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.beans.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.mapper.ClientMapper;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.ClientService;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ClientController Tests")
class ClientControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ClientService clientService;

	@MockBean
	private ClientMapper clientMapper;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	private Client testClient;
	private ClientDTO testClientDTO;

	@BeforeEach
	void setUp() {
		testClient = Client.builder()
				.id(1L)
				.fullName("Test Client")
				.email("client@test.com")
				.phone("123456789")
				.build();

		testClientDTO = new ClientDTO();
		testClientDTO.setId(1L);
		testClientDTO.setFullName("Test Client");
		testClientDTO.setEmail("client@test.com");
		testClientDTO.setPhone("123456789");
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener todos los clientes")
	void testGetAllClients_Success() throws Exception {
		// Arrange
		List<Client> clients = List.of(testClient);
		List<ClientDTO> clientDTOs = List.of(testClientDTO);
		
		when(clientService.findAll()).thenReturn(clients);
		when(clientMapper.toDtoList(clients)).thenReturn(clientDTOs);

		// Act & Assert
		mockMvc.perform(get("/clients")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Clients retrieved"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener cliente por ID")
	void testGetClientById_Success() throws Exception {
		// Arrange
		when(clientService.findById(1L)).thenReturn(testClient);
		when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

		// Act & Assert
		mockMvc.perform(get("/clients/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Buscar clientes por filtros")
	void testSearchClients_Success() throws Exception {
		// Arrange
		List<Client> clients = List.of(testClient);
		List<ClientDTO> clientDTOs = List.of(testClientDTO);
		
		when(clientService.search(any(), any(), any())).thenReturn(clients);
		when(clientMapper.toDtoList(clients)).thenReturn(clientDTOs);

		// Act & Assert
		mockMvc.perform(get("/clients/search")
				.param("name", "Test")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Crear cliente exitosamente")
	void testCreateClient_Success() throws Exception {
		// Arrange
		when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(testClient);
		when(clientService.create(any(Client.class))).thenReturn(testClient);
		when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

		// Act & Assert
		mockMvc.perform(post("/clients")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testClientDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Client created"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Actualizar cliente exitosamente")
	void testUpdateClient_Success() throws Exception {
		// Arrange
		when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(testClient);
		when(clientService.update(anyLong(), any(Client.class))).thenReturn(testClient);
		when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

		// Act & Assert
		mockMvc.perform(put("/clients/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testClientDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Client updated"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Eliminar cliente exitosamente")
	void testDeleteClient_Success() throws Exception {
		// Arrange
		doNothing().when(clientService).delete(1L);

		// Act & Assert
		mockMvc.perform(delete("/clients/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Client deleted"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Contar clientes exitosamente")
	void testCountClients_Success() throws Exception {
		// Arrange
		when(clientService.countAll()).thenReturn(10L);

		// Act & Assert
		mockMvc.perform(get("/clients/count")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").value(10));
	}
}
