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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.beans.UserDTO;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.mapper.UserMapper;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Tests")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private UserMapper userMapper;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	private User testUser;
	private UserDTO testUserDTO;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@example.com");
		testUser.setFullName("Test User");
		testUser.setRole(UserRole.PROVIDER);

		testUserDTO = new UserDTO();
		testUserDTO.setId(1L);
		testUserDTO.setEmail("test@example.com");
		testUserDTO.setFullName("Test User");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener proveedores por organización exitosamente")
	void testGetProvidersByOrganization_Success() throws Exception {
		// Arrange
		List<User> providers = List.of(testUser);
		when(userService.findProvidersByOrganization(anyLong())).thenReturn(providers);
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(get("/users/providers/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].fullName").value("Test User"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener managers por organización exitosamente")
	void testGetManagersByOrganization_Success() throws Exception {
		// Arrange
		testUser.setRole(UserRole.MANAGER);
		List<User> managers = List.of(testUser);
		when(userService.findManagersByOrganization(anyLong())).thenReturn(managers);
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(get("/users/managers/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].fullName").value("Test User"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener usuario por ID exitosamente")
	void testGetUserById_Success() throws Exception {
		// Arrange
		when(userService.findById(anyLong())).thenReturn(Optional.of(testUser));
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(get("/users/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.fullName").value("Test User"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Obtener usuario por ID - No encontrado")
	void testGetUserById_NotFound() throws Exception {
		// Arrange
		when(userService.findById(anyLong())).thenReturn(Optional.empty());

		// Act & Assert
		mockMvc.perform(get("/users/999")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Crear manager exitosamente")
	void testCreateManager_Success() throws Exception {
		// Arrange
		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
		when(userService.createManager(anyLong(), any(User.class))).thenReturn(testUser);
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(post("/users/managers/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testUserDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization add manager"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Crear provider exitosamente")
	void testCreateProvider_Success() throws Exception {
		// Arrange
		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
		when(userService.createProvider(anyLong(), any(User.class))).thenReturn(testUser);
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(post("/users/providers/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testUserDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Organization add provider"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Actualizar usuario exitosamente")
	void testUpdateUser_Success() throws Exception {
		// Arrange
		when(userMapper.toEntity(any(UserDTO.class))).thenReturn(testUser);
		when(userService.updateManager(any(User.class))).thenReturn(testUser);
		when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);

		// Act & Assert
		mockMvc.perform(put("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testUserDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User updated"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Eliminar manager exitosamente")
	void testDeleteManager_Success() throws Exception {
		// Arrange
		doNothing().when(userService).deleteManager(anyLong());

		// Act & Assert
		mockMvc.perform(delete("/users/managers/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User deleted"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("Eliminar provider exitosamente")
	void testDeleteProvider_Success() throws Exception {
		// Arrange
		doNothing().when(userService).deleteProvider(anyLong());

		// Act & Assert
		mockMvc.perform(delete("/users/providers/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User deleted"));
	}
}
