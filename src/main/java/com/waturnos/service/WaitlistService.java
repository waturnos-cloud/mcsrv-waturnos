package com.waturnos.service;

import java.util.List;

import com.waturnos.dto.beans.WaitlistEntryDTO;
import com.waturnos.dto.request.CreateWaitlistRequest;
import com.waturnos.entity.Booking;

public interface WaitlistService {
    
    /**
     * Crea una nueva entrada en la lista de espera
     * 
     * @param request datos de la entrada
     * @return DTO de la entrada creada
     */
    WaitlistEntryDTO createEntry(CreateWaitlistRequest request);
    
    /**
     * Obtiene la lista de espera de un cliente
     * 
     * @param clientId ID del cliente
     * @param organizationId ID de la organización (opcional)
     * @return lista de entradas
     */
    List<WaitlistEntryDTO> getMyWaitlist(Long clientId, Long organizationId);
    
    /**
     * Cancela una entrada de lista de espera
     * 
     * @param entryId ID de la entrada
     * @param clientId ID del cliente (para validación)
     */
    void cancelEntry(Long entryId, Long clientId);
    
    /**
     * Recalcula las posiciones de la cola para un servicio
     * 
     * @param serviceId ID del servicio
     */
    void recalculatePositions(Long serviceId);
    
    /**
     * Notifica al siguiente en la fila cuando se libera un turno
     * 
     * @param booking el turno que se liberó
     */
    void notifyNextInLine(Booking booking);
    
    /**
     * Marca una entrada de waitlist como FULFILLED cuando el cliente reserva
     * 
     * @param booking el turno que se reservó
     * @param clientId ID del cliente que reservó
     */
    void fulfillWaitlist(Booking booking, Long clientId);
}
