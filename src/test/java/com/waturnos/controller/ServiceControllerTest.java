package com.waturnos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.dto.beans.UnavailabilityDTO;
import com.waturnos.dto.request.CreateService;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.mapper.AvailabilityMapper;
import com.waturnos.mapper.ServiceMapper;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.ServiceEntityService;

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ServiceController Tests")
class ServiceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ServiceEntityService serviceEntityService;

	@MockBean
	private ServiceMapper serviceMapper;

	@MockBean
	private AvailabilityMapper availabilityMapper;

	@MockBean
	private JwtAuthFilter jwtAuthFilter;

	private ServiceEntity testServiceEntity;
	private ServiceDTO testServiceDTO;

	@BeforeEach
	void setUp() {
		testServiceEntity = new ServiceEntity();
		testServiceEntity.setId(1L);
		testServiceEntity.setName("Test Service");
		testServiceEntity.setDescription("Test Description");
		testServiceEntity.setDurationMinutes(60);

		testServiceDTO = new ServiceDTO();
		testServiceDTO.setId(1L);
		testServiceDTO.setName("Test Service");
		testServiceDTO.setDescription("Test Description");
		testServiceDTO.setDurationMinutes(60);
		
		// Agregar User y Location mock para evitar NullPointerException
		com.waturnos.dto.beans.UserDTO userDTO = new com.waturnos.dto.beans.UserDTO();
		userDTO.setId(1L);
		testServiceDTO.setUser(userDTO);
		
		com.waturnos.dto.beans.LocationDTO locationDTO = new com.waturnos.dto.beans.LocationDTO();
		locationDTO.setId(1L);
		testServiceDTO.setLocation(locationDTO);
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Crear servicio exitosamente")
	void testCreateService_Success() throws Exception {
		// Arrange
		CreateService createRequest = new CreateService();
		createRequest.setServiceDto(testServiceDTO);
		createRequest.setWorkInHollidays(false);

		when(serviceMapper.toEntity(any(ServiceDTO.class))).thenReturn(testServiceEntity);
		when(availabilityMapper.toEntityList(anyList())).thenReturn(List.of());
		when(serviceEntityService.create(any(ServiceEntity.class), anyList(), anyLong(), anyLong(), anyBoolean()))
				.thenReturn(testServiceEntity);
		when(serviceMapper.toDTO(any(ServiceEntity.class))).thenReturn(testServiceDTO);

		// Act & Assert
		mockMvc.perform(post("/services")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Service created"))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener servicios por usuario exitosamente")
	void testGetServicesByUserId_Success() throws Exception {
		// Arrange
		List<ServiceEntity> services = List.of(testServiceEntity);
		when(serviceEntityService.findByUser(anyLong())).thenReturn(services);
		when(serviceMapper.toDTO(any(ServiceEntity.class))).thenReturn(testServiceDTO);

		// Act & Assert
		mockMvc.perform(get("/services/user/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("Test Service"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener servicios por ubicación exitosamente")
	void testGetServicesByLocation_Success() throws Exception {
		// Arrange
		List<ServiceEntity> services = List.of(testServiceEntity);
		when(serviceEntityService.findByLocation(anyLong())).thenReturn(services);
		when(serviceMapper.toDTO(any(ServiceEntity.class))).thenReturn(testServiceDTO);

		// Act & Assert
		mockMvc.perform(get("/services/location/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("Test Service"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener servicio por ID exitosamente")
	void testGetServiceById_Success() throws Exception {
		// Arrange
		when(serviceEntityService.findById(anyLong())).thenReturn(testServiceEntity);
		when(serviceMapper.toDTO(any(ServiceEntity.class))).thenReturn(testServiceDTO);

		// Act & Assert
		mockMvc.perform(get("/services/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Service"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Actualizar servicio exitosamente")
	void testUpdateService_Success() throws Exception {
		// Arrange
		when(serviceMapper.toEntity(any(ServiceDTO.class), anyBoolean())).thenReturn(testServiceEntity);
		when(serviceEntityService.update(any(ServiceEntity.class))).thenReturn(testServiceEntity);
		when(serviceMapper.toDTO(any(ServiceEntity.class))).thenReturn(testServiceDTO);

		// Act & Assert
		mockMvc.perform(put("/services")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testServiceDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Service updated"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Eliminar servicio exitosamente")
	void testDeleteService_Success() throws Exception {
		// Arrange
		doNothing().when(serviceEntityService).delete(anyLong());

		// Act & Assert
		mockMvc.perform(delete("/services/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("User deleted"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Bloquear calendario exitosamente")
	void testLockCalendar_Success() throws Exception {
		// Arrange
		UnavailabilityDTO unavailabilityDTO = new UnavailabilityDTO();
		unavailabilityDTO.setServiceId(1L);
		unavailabilityDTO.setStartTime(LocalDateTime.now());
		unavailabilityDTO.setEndTime(LocalDateTime.now().plusHours(2));

		doNothing().when(serviceEntityService).lockCalendar(any(LocalDateTime.class), any(LocalDateTime.class), anyLong());

		// Act & Assert
		mockMvc.perform(post("/services/calendar/lock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(unavailabilityDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Lock calendar"));
	}
}
