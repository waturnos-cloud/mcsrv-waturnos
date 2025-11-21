package com.waturnos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.request.AssignBooking;
import com.waturnos.dto.request.CancelBooking;
import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.extended.BookingSummaryDetail;
import com.waturnos.enums.BookingStatus;
import com.waturnos.mapper.BookingMapper;
import com.waturnos.security.JwtAuthFilter;
import com.waturnos.service.BookingService;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingController Tests")
class BookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private BookingService bookingService;

	@MockitoBean
	private BookingMapper bookingMapper;

	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;

	private Booking testBooking;
	private BookingDTO testBookingDTO;
	private ServiceEntity testService;

	@BeforeEach
	void setUp() {
		testService = new ServiceEntity();
		testService.setId(1L);
		testService.setName("Test Service");
		testService.setCapacity(5);

		testBooking = Booking.builder()
				.id(1L)
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusHours(1))
				.status(BookingStatus.FREE)
				.freeSlots(5)
				.service(testService)
				.build();

		testBookingDTO = new BookingDTO();
		testBookingDTO.setId(1L);
		testBookingDTO.setServiceId(1L);
		testBookingDTO.setStartTime(LocalDateTime.now());
		testBookingDTO.setEndTime(LocalDateTime.now().plusHours(1));
		testBookingDTO.setStatus(BookingStatus.FREE);
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener bookings por service ID exitosamente")
	void testGetByServiceId_Success() throws Exception {
		// Arrange
		List<Booking> bookings = List.of(testBooking);
		List<BookingDTO> bookingDTOs = List.of(testBookingDTO);

		when(bookingService.findByServiceId(1L)).thenReturn(bookings);
		when(bookingMapper.toDto(any(Booking.class))).thenReturn(testBookingDTO);

		// Act & Assert
		mockMvc.perform(get("/bookings/service/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].serviceId").value(1));
	}

	@Test
	@WithMockUser(roles = "CLIENT")
	@DisplayName("Crear bookings exitosamente")
	void testCreateBookings_Success() throws Exception {
		// Arrange
		List<BookingDTO> bookingDTOs = List.of(testBookingDTO);
		List<Booking> bookings = List.of(testBooking);

		when(bookingMapper.toEntityList(anyList())).thenReturn(bookings);
		when(bookingService.create(anyList())).thenReturn(bookings);
		when(bookingMapper.toDtoList(anyList())).thenReturn(bookingDTOs);

		// Act & Assert
		mockMvc.perform(post("/bookings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bookingDTOs)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Bookings created"))
				.andExpect(jsonPath("$.data[0].id").value(1));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Asignar booking a cliente exitosamente")
	void testAssignBooking_Success() throws Exception {
		// Arrange
		AssignBooking assignBooking = new AssignBooking();
		assignBooking.setId(1L);
		assignBooking.setClientId(100L);

		testBooking.setStatus(BookingStatus.RESERVED);

		when(bookingService.assignBookingToClient(1L, 100L)).thenReturn(testBooking);
		when(bookingMapper.toDto(any(Booking.class))).thenReturn(testBookingDTO);

		// Act & Assert
		mockMvc.perform(post("/bookings/assign")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(assignBooking)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Booking assigned"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Cancelar booking exitosamente")
	void testCancelBooking_Success() throws Exception {
		// Arrange
		CancelBooking cancelBooking = new CancelBooking();
		cancelBooking.setId(1L);
		cancelBooking.setReason("Cliente canceló");

		testBooking.setStatus(BookingStatus.CANCELLED);

		when(bookingService.cancelBooking(1L, "Cliente canceló")).thenReturn(testBooking);
		when(bookingMapper.toDto(any(Booking.class))).thenReturn(testBookingDTO);

		// Act & Assert
		mockMvc.perform(post("/bookings/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cancelBooking)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Booking canceled"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener bookings de hoy exitosamente")
	void testGetTodayBookings_Success() throws Exception {
		// Arrange
		Map<Long, List<BookingSummaryDetail>> groupedMap = new HashMap<>();
		when(bookingService.findBookingsForTodayByProvider(1L)).thenReturn(groupedMap);

		// Act & Assert
		mockMvc.perform(get("/bookings/today")
				.param("providerId", "1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Bookings for today"));
	}

	@Test
	@WithMockUser(roles = "PROVIDER")
	@DisplayName("Obtener conteo de bookings por estado")
	void testGetCountBookings_Success() throws Exception {
		// Arrange
		List<CountBookingDTO> counts = new ArrayList<>();
		CountBookingDTO count1 = CountBookingDTO.builder()
				.date(LocalDate.now().toString())
				.countFree(5)
				.countReserved(3)
				.countCompleted(2)
				.countCancelled(1)
				.countNoShow(0)
				.build();
		counts.add(count1);

		when(bookingService.countBookingsByDateRangeAndProvider(
				any(LocalDate.class), any(LocalDate.class), anyLong()))
				.thenReturn(counts);

		// Act & Assert
		mockMvc.perform(get("/bookings/count")
				.param("fromDate", LocalDate.now().toString())
				.param("providerId", "1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].date").value(LocalDate.now().toString()))
				.andExpect(jsonPath("$[0].countFree").value(5));
	}
}
