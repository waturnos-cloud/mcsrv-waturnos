package com.waturnos.service.impl;

import com.waturnos.dto.request.CreateRecurrenceRequest;
import com.waturnos.dto.response.CheckRecurrenceResponse;
import com.waturnos.dto.response.RecurrenceDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingClient;
import com.waturnos.entity.Client;
import com.waturnos.entity.Recurrence;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.User;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.RecurrenceType;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.RecurrenceRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.RecurrenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurrenceServiceImpl implements RecurrenceService {
    
    private final RecurrenceRepository recurrenceRepository;
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public CheckRecurrenceResponse checkRecurrence(Long bookingId) {
        log.info("Verificando si booking {} puede ser recurrente", bookingId);
        
        // Obtener el booking
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking no encontrado: " + bookingId));
        
        // Verificar que el booking tenga un cliente asignado
        if (booking.getBookingClients() == null || booking.getBookingClients().isEmpty()) {
            return CheckRecurrenceResponse.builder()
                .canBeRecurrent(false)
                .message("El turno no tiene cliente asignado")
                .build();
        }
        
        BookingClient bookingClient = booking.getBookingClients().iterator().next();
        Client client = bookingClient.getClient();
        ServiceEntity service = booking.getService();
        
        // Obtener día de la semana y hora del turno
        DayOfWeek dayOfWeek = booking.getStartTime().getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue(); // 1=Lunes, 7=Domingo
        LocalTime timeSlot = booking.getStartTime().toLocalTime();
        
        log.info("Buscando turnos futuros para día {} a las {}", dayOfWeekValue, timeSlot);
        
        // Buscar todos los turnos futuros del mismo servicio, día y hora
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = booking.getStartTime().toLocalDate().plusWeeks(1); // Empezar desde la próxima semana
        LocalDate endDate = startDate.plusMonths(6); // Buscar hasta 6 meses adelante
        
        List<Booking> futureBookings = bookingRepository.findByServiceIdAndDateRange(
            service.getId(),
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        // Filtrar por día de la semana y hora exacta
        List<Booking> matchingSlots = futureBookings.stream()
            .filter(b -> b.getStartTime().getDayOfWeek().getValue() == dayOfWeekValue)
            .filter(b -> b.getStartTime().toLocalTime().equals(timeSlot))
            .collect(Collectors.toList());
        
        // Separar en disponibles y ocupados
        List<Booking> available = matchingSlots.stream()
            .filter(b -> b.getStatus() == BookingStatus.FREE)
            .collect(Collectors.toList());
        
        List<Booking> conflicting = matchingSlots.stream()
            .filter(b -> b.getStatus() != BookingStatus.FREE && b.getStatus() != BookingStatus.CANCELLED)
            .collect(Collectors.toList());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> availableDates = available.stream()
            .map(b -> b.getStartTime().toLocalDate().format(formatter))
            .collect(Collectors.toList());
        
        List<String> conflictingDates = conflicting.stream()
            .map(b -> b.getStartTime().toLocalDate().format(formatter))
            .collect(Collectors.toList());
        
        boolean canBeRecurrent = conflicting.isEmpty();
        String message;
        
        if (canBeRecurrent) {
            message = String.format("Todos los %d turnos futuros están disponibles. Se puede crear la recurrencia.", 
                available.size());
        } else {
            message = String.format("Hay %d turnos ocupados de %d totales. No se puede crear la recurrencia.", 
                conflicting.size(), matchingSlots.size());
        }
        
        return CheckRecurrenceResponse.builder()
            .canBeRecurrent(canBeRecurrent)
            .totalFutureSlots(matchingSlots.size())
            .availableSlots(available.size())
            .conflictingSlots(conflicting.size())
            .availableDates(availableDates)
            .conflictingDates(conflictingDates)
            .message(message)
            .build();
    }
    
    @Override
    @Transactional
    public RecurrenceDTO createRecurrence(CreateRecurrenceRequest request, Long userId) {
        log.info("Creando recurrencia para booking {}", request.getBookingId());
        
        // Obtener el booking
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking no encontrado: " + request.getBookingId()));
        
        // Verificar que tenga cliente asignado
        if (booking.getBookingClients() == null || booking.getBookingClients().isEmpty()) {
            throw new RuntimeException("El booking no tiene cliente asignado");
        }
        
        BookingClient bookingClient = booking.getBookingClients().iterator().next();
        Client client = bookingClient.getClient();
        ServiceEntity service = booking.getService();
        
        // Obtener día y hora
        DayOfWeek dayOfWeek = booking.getStartTime().getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue();
        LocalTime timeSlot = booking.getStartTime().toLocalTime();
        
        // Verificar que no exista ya una recurrencia activa para este cliente/servicio/día/hora
        recurrenceRepository.findActiveByClientServiceDayTime(
            client.getId(),
            service.getId(),
            service.getUser().getId(),
            dayOfWeekValue,
            timeSlot
        ).ifPresent(r -> {
            throw new RuntimeException("Ya existe una recurrencia activa para este horario");
        });
        
        // Obtener el usuario que crea (puede ser null si es un cliente)
        User createdByUser = null;
        if (userId != null) {
            createdByUser = userRepository.findById(userId)
                .orElse(null); // Si no se encuentra, dejarlo null
        }
        
        // Crear la recurrencia
        Recurrence recurrence = Recurrence.builder()
            .client(client)
            .service(service)
            .provider(service.getUser())
            .dayOfWeek(dayOfWeekValue)
            .timeSlot(timeSlot)
            .recurrenceType(request.getRecurrenceType())
            .occurrenceCount(request.getOccurrenceCount())
            .endDate(request.getEndDate())
            .active(true)
            .createdAt(LocalDateTime.now())
            .createdBy(createdByUser)
            .build();
        
        recurrence = recurrenceRepository.save(recurrence);
        log.info("Recurrencia creada con ID: {}", recurrence.getId());
        
        // Asignar el booking actual a la recurrencia
        booking.setRecurrence(recurrence);
        bookingRepository.save(booking);
        
        // Asignar todos los turnos futuros disponibles
        assignFutureBookings(recurrence, client, service);
        
        return convertToDTO(recurrence);
    }
    
    private void assignFutureBookings(Recurrence recurrence, Client client, ServiceEntity service) {
        log.info("Asignando turnos futuros para recurrencia {}", recurrence.getId());
        
        LocalDate startDate = LocalDate.now().plusWeeks(1);
        LocalDate endDate = calculateEndDate(recurrence, startDate);
        
        log.info("Buscando turnos desde {} hasta {}", startDate, endDate);
        
        // Obtener todos los bookings futuros del servicio
        List<Booking> futureBookings = bookingRepository.findByServiceIdAndDateRange(
            service.getId(),
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        // Filtrar por día de la semana y hora exacta
        List<Booking> matchingSlots = futureBookings.stream()
            .filter(b -> b.getStartTime().getDayOfWeek().getValue() == recurrence.getDayOfWeek())
            .filter(b -> b.getStartTime().toLocalTime().equals(recurrence.getTimeSlot()))
            .filter(b -> b.getStatus() == BookingStatus.FREE)
            .sorted((b1, b2) -> b1.getStartTime().compareTo(b2.getStartTime()))
            .collect(Collectors.toList());
        
        // Si es COUNT, limitar la cantidad
        if (recurrence.getRecurrenceType() == RecurrenceType.COUNT && recurrence.getOccurrenceCount() != null) {
            int remaining = recurrence.getOccurrenceCount() - 1; // -1 porque ya se asignó el turno inicial
            matchingSlots = matchingSlots.stream()
                .limit(remaining)
                .collect(Collectors.toList());
        }
        
        log.info("Asignando {} turnos futuros", matchingSlots.size());
        
        // Asignar cada turno al cliente
        for (Booking booking : matchingSlots) {
            BookingClient bc = BookingClient.builder()
                .booking(booking)
                .client(client)
                .build();
            
            booking.addBookingClient(bc);
            booking.setRecurrence(recurrence);
            booking.setUpdatedAt(LocalDateTime.now());
        }
        
        bookingRepository.saveAll(matchingSlots);
        log.info("Turnos futuros asignados correctamente");
    }
    
    private LocalDate calculateEndDate(Recurrence recurrence, LocalDate startDate) {
        switch (recurrence.getRecurrenceType()) {
            case END_DATE:
                return recurrence.getEndDate() != null ? recurrence.getEndDate() : startDate.plusYears(1);
            case COUNT:
                // Calcular fecha aproximada multiplicando cantidad por 7 días
                int weeks = recurrence.getOccurrenceCount() != null ? recurrence.getOccurrenceCount() : 52;
                return startDate.plusWeeks(weeks);
            case FOREVER:
            default:
                return startDate.plusYears(1); // Buscar 1 año adelante para FOREVER
        }
    }
    
    @Override
    @Transactional
    public void cancelRecurrence(Long recurrenceId) {
        log.info("Cancelando recurrencia {}", recurrenceId);
        
        Recurrence recurrence = recurrenceRepository.findById(recurrenceId)
            .orElseThrow(() -> new RuntimeException("Recurrencia no encontrada: " + recurrenceId));
        
        recurrence.setActive(false);
        recurrenceRepository.save(recurrence);
        
        log.info("Recurrencia {} desactivada", recurrenceId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Recurrence> getAllActiveRecurrences() {
        return recurrenceRepository.findAllActiveRecurrences();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RecurrenceDTO> getRecurrencesByClient(Long clientId) {
        List<Recurrence> recurrences = recurrenceRepository.findByClientIdAndActiveTrue(clientId);
        return recurrences.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private RecurrenceDTO convertToDTO(Recurrence recurrence) {
        return RecurrenceDTO.builder()
            .id(recurrence.getId())
            .clientId(recurrence.getClient().getId())
            .clientName(recurrence.getClient().getFullName())
            .serviceId(recurrence.getService().getId())
            .serviceName(recurrence.getService().getName())
            .providerId(recurrence.getProvider().getId())
            .providerName(recurrence.getProvider().getFullName())
            .dayOfWeek(recurrence.getDayOfWeek())
            .timeSlot(recurrence.getTimeSlot())
            .recurrenceType(recurrence.getRecurrenceType())
            .occurrenceCount(recurrence.getOccurrenceCount())
            .endDate(recurrence.getEndDate())
            .active(recurrence.getActive())
            .createdAt(recurrence.getCreatedAt().toString())
            .build();
    }
}
