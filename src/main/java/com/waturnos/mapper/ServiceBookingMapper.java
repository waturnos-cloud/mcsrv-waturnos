package com.waturnos.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.waturnos.dto.response.BookingClientSimpleDTO;
import com.waturnos.dto.response.BookingSimpleDTO;
import com.waturnos.dto.response.ServiceWithBookingsDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;

@Component
public class ServiceBookingMapper {

    public ServiceWithBookingsDTO toServiceGroup(ServiceEntity service, List<Booking> bookings) {

        ServiceWithBookingsDTO dto = new ServiceWithBookingsDTO();
        dto.setServiceId(service.getId());
        dto.setServiceName(service.getName());
        dto.setServiceDescription(service.getDescription());
        dto.setServicePrice(service.getPrice());
        dto.setAdvancePayment(service.getAdvancePayment());

        List<BookingSimpleDTO> simpleList = bookings.stream()
                .map(this::toSimple)
                .toList();

        dto.setBookings(simpleList);
        return dto;
    }

    private BookingSimpleDTO toSimple(Booking b) {
        BookingSimpleDTO dto = new BookingSimpleDTO();
        dto.setId(b.getId());
        dto.setStartTime(b.getStartTime());
        dto.setEndTime(b.getEndTime());
        dto.setStatus(b.getStatus());
        dto.setIsOverbooking(b.getIsOverbooking());
        
        // Map bookingClients with client information
        if (b.getBookingClients() != null) {
            List<BookingClientSimpleDTO> clientDTOs = b.getBookingClients().stream()
                .map(bc -> {
                    BookingClientSimpleDTO clientDTO = new BookingClientSimpleDTO();
                    if (bc.getClient() != null) {
                        clientDTO.setClientId(bc.getClient().getId());
                        clientDTO.setClientName(bc.getClient().getFullName());
                        clientDTO.setClientEmail(bc.getClient().getEmail());
                        clientDTO.setClientPhone(bc.getClient().getPhone());
                    }
                    return clientDTO;
                })
                .toList();
            dto.setBookingClients(clientDTOs);
        }
        
        return dto;
    }
}