package com.waturnos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.waturnos.dto.beans.WaitlistEntryDTO;
import com.waturnos.dto.request.CreateWaitlistRequest;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.Organization;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.User;
import com.waturnos.entity.WaitlistEntry;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.WaitlistStatus;
import com.waturnos.enums.WaitlistType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.repository.WaitlistEntryRepository;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.impl.WaitlistServiceImpl;

/**
 * Test unitario simplificado para WaitlistService.
 * Valida los flujos principales del sistema de waitlist.
 */
@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistEntryRepository waitlistRepo;
    
    @Mock
    private ServiceRepository serviceRepo;
    
    @Mock
    private BookingRepository bookingRepo;
    
    @Mock
    private ClientRepository clientRepo;
    
    @Mock
    private UserRepository userRepo;
    
    @Mock
    private OrganizationRepository organizationRepo;
    
    @Mock
    private NotificationFactory notificationFactory;
    
    @InjectMocks
    private WaitlistServiceImpl waitlistService;
    
    private Client testClient;
    private ServiceEntity testService;
    private User testProvider;
    private Organization testOrg;
    private Booking testBooking;
    
    @BeforeEach
    void setUp() {
        // Setup test entities
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFullName("Juan Pérez");
        testClient.setEmail("juan@example.com");
        
        testProvider = new User();
        testProvider.setId(1L);
        testProvider.setFullName("Dr. García");
        
        testOrg = new Organization();
        testOrg.setId(1L);
        testOrg.setName("Clínica Test");
        
        testService = new ServiceEntity();
        testService.setId(1L);
        testService.setName("Consulta General");
        testService.setWaitList(true);
        testService.setWaitListTime(15);
        testService.setUser(testProvider);
        
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setService(testService);
        testBooking.setStartTime(LocalDateTime.of(2025, 12, 1, 10, 0));
        testBooking.setStatus(BookingStatus.FREE);
    }
    
    @Test
    void testCreateEntry_Success() {
        // Arrange
        CreateWaitlistRequest request = CreateWaitlistRequest.builder()
            .clientId(1L)
            .serviceId(1L)
            .providerId(1L)
            .organizationId(1L)
            .type(WaitlistType.SPECIFIC)
            .specificBookingId(1L)
            .date(LocalDate.of(2025, 12, 1))
            .timeFrom(LocalTime.of(10, 0))
            .timeTo(LocalTime.of(11, 0))
            .build();
        
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(testService));
        when(clientRepo.findById(1L)).thenReturn(Optional.of(testClient));
        when(userRepo.findById(1L)).thenReturn(Optional.of(testProvider));
        when(organizationRepo.findById(1L)).thenReturn(Optional.of(testOrg));
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(testBooking));
        
        when(waitlistRepo.existsByClientIdAndServiceIdAndDateAndStatus(
            anyLong(), anyLong(), any(LocalDate.class), any(WaitlistStatus.class)))
            .thenReturn(false);
        
        when(waitlistRepo.countByServiceIdAndStatus(anyLong(), any(WaitlistStatus.class)))
            .thenReturn(2);
        
        when(waitlistRepo.save(any(WaitlistEntry.class))).thenAnswer(invocation -> {
            WaitlistEntry entry = invocation.getArgument(0);
            entry.setId(1L);
            return entry;
        });
        
        // Act
        WaitlistEntryDTO result = waitlistService.createEntry(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClientId());
        assertEquals(1L, result.getServiceId());
        assertEquals(3, result.getPosition()); // 2 existentes + 1 = posición 3
        assertEquals(WaitlistStatus.WAITING, result.getStatus());
        assertEquals(WaitlistType.SPECIFIC, result.getType());
        
        verify(waitlistRepo, times(1)).save(any(WaitlistEntry.class));
    }
    
    @Test
    void testCreateEntry_ServiceWithoutWaitlistEnabled_ThrowsException() {
        // Arrange
        testService.setWaitList(false);
        
        CreateWaitlistRequest request = CreateWaitlistRequest.builder()
            .clientId(1L)
            .serviceId(1L)
            .providerId(1L)
            .organizationId(1L)
            .type(WaitlistType.SPECIFIC)
            .specificBookingId(1L)
            .date(LocalDate.of(2025, 12, 1))
            .timeFrom(LocalTime.of(10, 0))
            .timeTo(LocalTime.of(11, 0))
            .build();
        
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(testService));
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> waitlistService.createEntry(request));
        
        assertTrue(exception.getMessage().contains("no tiene habilitada la lista de espera"));
        verify(waitlistRepo, never()).save(any(WaitlistEntry.class));
    }
    
    @Test
    void testCreateEntry_DuplicateEntry_ThrowsException() {
        // Arrange
        CreateWaitlistRequest request = CreateWaitlistRequest.builder()
            .clientId(1L)
            .serviceId(1L)
            .providerId(1L)
            .organizationId(1L)
            .type(WaitlistType.SPECIFIC)
            .specificBookingId(1L)
            .date(LocalDate.of(2025, 12, 1))
            .timeFrom(LocalTime.of(10, 0))
            .timeTo(LocalTime.of(11, 0))
            .build();
        
        when(serviceRepo.findById(1L)).thenReturn(Optional.of(testService));
        when(clientRepo.findById(1L)).thenReturn(Optional.of(testClient));
        when(userRepo.findById(1L)).thenReturn(Optional.of(testProvider));
        when(organizationRepo.findById(1L)).thenReturn(Optional.of(testOrg));
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(testBooking));
        
        when(waitlistRepo.existsByClientIdAndServiceIdAndDateAndStatus(
            anyLong(), anyLong(), any(LocalDate.class), any(WaitlistStatus.class)))
            .thenReturn(true);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> waitlistService.createEntry(request));
        
        assertTrue(exception.getMessage().contains("Ya tienes una entrada activa"));
        verify(waitlistRepo, never()).save(any(WaitlistEntry.class));
    }
    
    @Test
    void testGetMyWaitlist_Success() {
        // Arrange
        WaitlistEntry entry1 = new WaitlistEntry();
        entry1.setId(1L);
        entry1.setClient(testClient);
        entry1.setService(testService);
        entry1.setUser(testProvider);
        entry1.setOrganization(testOrg);
        entry1.setType(WaitlistType.SPECIFIC);
        entry1.setDate(LocalDate.of(2025, 12, 1));
        entry1.setPosition(1);
        entry1.setStatus(WaitlistStatus.WAITING);
        entry1.setExpirationMinutes(15);
        entry1.setCreatedAt(LocalDateTime.now());
        entry1.setUpdatedAt(LocalDateTime.now());
        
        List<WaitlistEntry> entries = Arrays.asList(entry1);
        
        when(waitlistRepo.findByClientIdAndStatusIn(
            eq(1L), any()))
            .thenReturn(entries);
        
        // Act
        List<WaitlistEntryDTO> result = waitlistService.getMyWaitlist(1L, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(WaitlistStatus.WAITING, result.get(0).getStatus());
        assertEquals(1, result.get(0).getPosition());
    }
    
    @Test
    void testCancelEntry_Success() {
        // Arrange
        WaitlistEntry entry = new WaitlistEntry();
        entry.setId(1L);
        entry.setClient(testClient);
        entry.setService(testService);
        entry.setUser(testProvider);
        entry.setOrganization(testOrg);
        entry.setStatus(WaitlistStatus.WAITING);
        entry.setPosition(2);
        entry.setDate(LocalDate.of(2025, 12, 1));
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        
        when(waitlistRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(waitlistRepo.save(any(WaitlistEntry.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        waitlistService.cancelEntry(1L, 1L);
        
        // Assert
        verify(waitlistRepo, times(1)).save(argThat(e -> 
            e.getStatus() == WaitlistStatus.CANCELLED
        ));
    }
    
    @Test
    void testNotifyNextInLine_Success() {
        // Arrange
        WaitlistEntry winner = new WaitlistEntry();
        winner.setId(1L);
        winner.setClient(testClient);
        winner.setService(testService);
        winner.setUser(testProvider);
        winner.setOrganization(testOrg);
        winner.setStatus(WaitlistStatus.WAITING);
        winner.setPosition(1);
        winner.setExpirationMinutes(15);
        winner.setType(WaitlistType.SPECIFIC);
        winner.setSpecificBooking(testBooking);
        winner.setDate(LocalDate.of(2025, 12, 1));
        winner.setCreatedAt(LocalDateTime.now());
        winner.setUpdatedAt(LocalDateTime.now());
        
        when(waitlistRepo.findCandidatesForBooking(
            anyLong(), any(LocalDate.class), any(LocalTime.class), anyLong()))
            .thenReturn(Arrays.asList(winner));
        
        when(waitlistRepo.save(any(WaitlistEntry.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        waitlistService.notifyNextInLine(testBooking);
        
        // Assert
        verify(waitlistRepo, times(1)).save(argThat(e -> 
            e.getStatus() == WaitlistStatus.NOTIFIED &&
            e.getNotifiedAt() != null &&
            e.getExpiresAt() != null
        ));
        
        verify(notificationFactory, times(1)).sendAsync(any());
    }
    
    @Test
    void testFulfillWaitlist_SpecificType_Success() {
        // Arrange
        WaitlistEntry entry = new WaitlistEntry();
        entry.setId(1L);
        entry.setClient(testClient);
        entry.setService(testService);
        entry.setUser(testProvider);
        entry.setOrganization(testOrg);
        entry.setStatus(WaitlistStatus.NOTIFIED);
        entry.setType(WaitlistType.SPECIFIC);
        entry.setSpecificBooking(testBooking);
        entry.setDate(LocalDate.of(2025, 12, 1));
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        
        when(waitlistRepo.findByClientIdAndServiceIdAndStatusAndDate(
            eq(1L), eq(1L), eq(WaitlistStatus.NOTIFIED), any(LocalDate.class)))
            .thenReturn(Optional.of(entry));
        
        when(waitlistRepo.save(any(WaitlistEntry.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        waitlistService.fulfillWaitlist(testBooking, 1L);
        
        // Assert
        verify(waitlistRepo, times(1)).save(argThat(e -> 
            e.getStatus() == WaitlistStatus.FULFILLED
        ));
    }
}
