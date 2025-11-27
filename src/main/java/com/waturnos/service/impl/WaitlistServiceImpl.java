package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.repository.WaitlistEntryRepository;
import com.waturnos.service.WaitlistService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistServiceImpl implements WaitlistService {
    
    private final WaitlistEntryRepository waitlistRepo;
    private final ServiceRepository serviceRepo;
    private final BookingRepository bookingRepo;
    private final ClientRepository clientRepo;
    private final UserRepository userRepo;
    private final OrganizationRepository organizationRepo;
    private final NotificationFactory notificationFactory;
    
    @Override
    @Transactional
    public WaitlistEntryDTO createEntry(CreateWaitlistRequest request) {
        log.info("Creando entrada en waitlist para cliente {} en servicio {}", 
                 request.getClientId(), request.getServiceId());
        
        // 1. Validar que el servicio existe
        ServiceEntity service = serviceRepo.findById(request.getServiceId())
            .orElseThrow(() -> new ServiceException(ErrorCode.SERVICE_NOT_FOUND, 
                                                    "Servicio no encontrado"));
        
        // 2. Validar que el servicio tiene waitList activo
        if (!Boolean.TRUE.equals(service.getWaitList())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, 
                                      "Este servicio no permite lista de espera");
        }
        
        // 3. Si es SPECIFIC, validar booking
        Booking specificBooking = null;
        if (request.getType() == WaitlistType.SPECIFIC) {
            if (request.getSpecificBookingId() == null) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, 
                    "specificBookingId es requerido para tipo SPECIFIC");
            }
            
            specificBooking = bookingRepo.findById(request.getSpecificBookingId())
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKING_NOT_FOUND, 
                                                       "Booking no encontrado"));
            
            if (specificBooking.getStatus() == BookingStatus.FREE) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, 
                                          "Este turno ya está disponible");
            }
        }
        
        // 4. Verificar si ya está en la cola
        boolean exists = waitlistRepo.existsByClientIdAndServiceIdAndDateAndStatus(
            request.getClientId(),
            request.getServiceId(),
            request.getDate(),
            WaitlistStatus.WAITING
        );
        
        if (exists) {
            throw new ServiceException(ErrorCode.CONFLICT, 
                                      "Ya estás en la lista de espera para este turno");
        }
        
        // 5. Obtener entidades relacionadas
        Client client = clientRepo.findById(request.getClientId())
            .orElseThrow(() -> new ServiceException(ErrorCode.CLIENT_NOT_FOUND, 
                                                    "Cliente no encontrado"));
        
        User provider = userRepo.findById(request.getProviderId())
            .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND, 
                                                    "Proveedor no encontrado"));
        
        Organization organization = organizationRepo.findById(request.getOrganizationId())
            .orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND, 
                                                    "Organización no encontrada"));
        
        // 6. Calcular posición
        Integer position = waitlistRepo.countByServiceIdAndStatus(
            request.getServiceId(),
            WaitlistStatus.WAITING
        ) + 1;
        
        // 7. Crear entry
        WaitlistEntry entry = WaitlistEntry.builder()
            .client(client)
            .service(service)
            .user(provider)
            .organization(organization)
            .type(request.getType())
            .specificBooking(specificBooking)
            .date(request.getDate())
            .timeFrom(request.getTimeFrom())
            .timeTo(request.getTimeTo())
            .position(position)
            .expirationMinutes(service.getWaitListTime())
            .status(WaitlistStatus.WAITING)
            .build();
        
        entry = waitlistRepo.save(entry);
        
        log.info("Entrada creada exitosamente con ID {} en posición {}", entry.getId(), position);
        
        return toDTO(entry);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WaitlistEntryDTO> getMyWaitlist(Long clientId, Long organizationId) {
        log.info("Obteniendo waitlist para cliente {} en org {}", clientId, organizationId);
        
        List<WaitlistEntry> entries;
        List<WaitlistStatus> activeStatuses = List.of(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED);
        
        if (organizationId != null) {
            entries = waitlistRepo.findByClientIdAndOrganizationIdAndStatusIn(
                clientId,
                organizationId,
                activeStatuses
            );
        } else {
            entries = waitlistRepo.findByClientIdAndStatusIn(
                clientId,
                activeStatuses
            );
        }
        
        return entries.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void cancelEntry(Long entryId, Long clientId) {
        log.info("Cancelando entrada {} para cliente {}", entryId, clientId);
        
        // 1. Buscar entry
        WaitlistEntry entry = waitlistRepo.findById(entryId)
            .orElseThrow(() -> new ServiceException(ErrorCode.WAITLIST_NOT_FOUND, 
                                                    "Entry no encontrada"));
        
        // 2. Validar que pertenece al cliente
        if (!entry.getClient().getId().equals(clientId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, 
                                      "No tienes permiso para cancelar esta entry");
        }
        
        // 3. Validar que no fue cancelada
        if (entry.getStatus() == WaitlistStatus.CANCELLED) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, 
                                      "Ya saliste de esta lista");
        }
        
        Long serviceId = entry.getService().getId();
        
        // 4. Marcar como cancelada
        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepo.save(entry);
        
        log.info("Entry {} cancelada exitosamente", entryId);
        
        // 5. Recalcular posiciones
        recalculatePositions(serviceId);
    }
    
    @Override
    @Transactional
    public void recalculatePositions(Long serviceId) {
        log.info("Recalculando posiciones para servicio {}", serviceId);
        
        List<WaitlistEntry> waitingEntries = waitlistRepo
            .findByServiceIdAndStatusOrderByCreatedAtAsc(
                serviceId,
                WaitlistStatus.WAITING
            );
        
        for (int i = 0; i < waitingEntries.size(); i++) {
            WaitlistEntry entry = waitingEntries.get(i);
            entry.setPosition(i + 1);
            waitlistRepo.save(entry);
        }
        
        log.info("Posiciones recalculadas: {} entradas actualizadas", waitingEntries.size());
    }
    
    @Override
    @Transactional
    public void notifyNextInLine(Booking booking) {
        log.info("Notificando siguiente en línea para booking {}", booking.getId());
        
        // 1. Buscar candidatos usando query nativa
        List<WaitlistEntry> candidates = waitlistRepo.findCandidatesForBooking(
            booking.getService().getId(),
            booking.getStartTime().toLocalDate(),
            booking.getStartTime().toLocalTime(),
            booking.getId()
        );
        
        if (candidates.isEmpty()) {
            log.info("No hay candidatos en espera para este turno");
            return;
        }
        
        WaitlistEntry winner = candidates.get(0);
        
        // 2. Calcular expiración
        LocalDateTime expiresAt = LocalDateTime.now()
            .plusMinutes(winner.getExpirationMinutes());
        
        // 3. Actualizar estado a NOTIFIED
        winner.setStatus(WaitlistStatus.NOTIFIED);
        winner.setNotifiedAt(LocalDateTime.now());
        winner.setExpiresAt(expiresAt);
        waitlistRepo.save(winner);
        
        log.info("Cliente {} notificado, tiene hasta {} para reservar", 
                 winner.getClient().getId(), expiresAt);
        
        // Enviar notificación
        sendWaitlistNotification(winner, booking);
    }
    
    @Override
    @Transactional
    public void fulfillWaitlist(Booking booking, Long clientId) {
        log.info("Verificando si cliente {} cumplió waitlist para booking {}", 
                 clientId, booking.getId());
        
        // Buscar si el cliente tenía una entry NOTIFIED para este servicio y fecha
        Optional<WaitlistEntry> entryOpt = waitlistRepo.findByClientIdAndServiceIdAndStatusAndDate(
            clientId,
            booking.getService().getId(),
            WaitlistStatus.NOTIFIED,
            booking.getStartTime().toLocalDate()
        );
        
        if (entryOpt.isEmpty()) {
            log.debug("Cliente {} no tenía waitlist activa para este booking", clientId);
            return;
        }
        
        WaitlistEntry entry = entryOpt.get();
        
        // Validar que el booking coincida con lo esperado
        boolean isMatch = false;
        
        if (entry.getType() == WaitlistType.SPECIFIC) {
            // Si es SPECIFIC, debe ser exactamente ese booking
            isMatch = entry.getSpecificBooking() != null && 
                     entry.getSpecificBooking().getId().equals(booking.getId());
        } else {
            // Si es TIME_WINDOW, debe estar dentro del rango de tiempo
            LocalTime bookingTime = booking.getStartTime().toLocalTime();
            isMatch = !bookingTime.isBefore(entry.getTimeFrom()) && 
                     !bookingTime.isAfter(entry.getTimeTo());
        }
        
        if (isMatch) {
            entry.setStatus(WaitlistStatus.FULFILLED);
            waitlistRepo.save(entry);
            log.info("Waitlist entry {} marcada como FULFILLED", entry.getId());
        } else {
            log.debug("Booking no coincide con las expectativas de la waitlist entry {}", 
                     entry.getId());
        }
    }
    
    /**
     * Convierte una entidad WaitlistEntry a DTO
     */
    private WaitlistEntryDTO toDTO(WaitlistEntry entry) {
        return WaitlistEntryDTO.builder()
            .id(entry.getId())
            .clientId(entry.getClient().getId())
            .serviceId(entry.getService().getId())
            .serviceName(entry.getService().getName())
            .providerId(entry.getUser().getId())
            .providerName(entry.getUser().getFullName())
            .organizationId(entry.getOrganization().getId())
            .type(entry.getType())
            .specificBookingId(entry.getSpecificBooking() != null ? 
                              entry.getSpecificBooking().getId() : null)
            .date(entry.getDate())
            .timeFrom(entry.getTimeFrom())
            .timeTo(entry.getTimeTo())
            .position(entry.getPosition())
            .status(entry.getStatus())
            .expirationMinutes(entry.getExpirationMinutes())
            .notifiedAt(entry.getNotifiedAt())
            .expiresAt(entry.getExpiresAt())
            .createdAt(entry.getCreatedAt())
            .updatedAt(entry.getUpdatedAt())
            .build();
    }
    
    /**
     * Envía notificación por email cuando un turno se libera para el cliente en waitlist.
     */
    private void sendWaitlistNotification(WaitlistEntry entry, Booking booking) {
        try {
            Client client = entry.getClient();
            ServiceEntity service = entry.getService();
            User provider = entry.getUser();
            Organization organization = entry.getOrganization();
            
            // Formatear fecha y hora del booking
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));
            String dateBooking = booking.getStartTime().format(formatter);
            
            // Construir propiedades del email
            Map<String, String> properties = new HashMap<>();
            properties.put("USERNAME", client.getFullName());
            properties.put("SERVICENAME", service.getName());
            properties.put("PROVIDERNAME", provider.getFullName());
            properties.put("DATEBOOKING", dateBooking);
            properties.put("EXPIRATION_MINUTES", String.valueOf(entry.getExpirationMinutes()));
            properties.put("ORGANIZATIONNAME", organization.getName());
            properties.put("BOOKING_URL", "https://app.waturnos.com/bookings/" + booking.getId());
            
            // Construir request de notificación
            NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.WAITLIST_AVAILABLE)
                .email(client.getEmail())
                .subject("¡Turno Disponible! - " + service.getName())
                .language("es") // TODO: Usar idioma del cliente cuando esté disponible
                .properties(properties)
                .build();
            
            // Enviar notificación de forma asíncrona
            notificationFactory.sendAsync(request);
            
            log.info("Notificación de waitlist enviada a {} ({})", client.getFullName(), client.getEmail());
            
        } catch (Exception e) {
            log.error("Error al enviar notificación de waitlist para entry {}: {}", 
                     entry.getId(), e.getMessage(), e);
            // No propagamos la excepción para no interrumpir el flujo principal
        }
    }
}
