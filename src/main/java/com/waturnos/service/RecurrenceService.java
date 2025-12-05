package com.waturnos.service;

import com.waturnos.dto.request.CreateRecurrenceRequest;
import com.waturnos.dto.response.CheckRecurrenceResponse;
import com.waturnos.dto.response.RecurrenceDTO;
import com.waturnos.entity.Recurrence;

import java.util.List;

public interface RecurrenceService {
    
    /**
     * Verifica si un booking puede ser recurrente analizando conflictos futuros
     * @param bookingId ID del booking a verificar
     * @return Respuesta con disponibilidad y conflictos
     */
    CheckRecurrenceResponse checkRecurrence(Long bookingId);
    
    /**
     * Crea una recurrencia y asigna todos los turnos futuros disponibles
     * @param request Datos de la recurrencia
     * @param userId ID del usuario que crea la recurrencia
     * @return DTO de la recurrencia creada
     */
    RecurrenceDTO createRecurrence(CreateRecurrenceRequest request, Long userId);
    
    /**
     * Cancela una recurrencia (desactiva)
     * @param recurrenceId ID de la recurrencia a cancelar
     */
    void cancelRecurrence(Long recurrenceId);
    
    /**
     * Obtiene todas las recurrencias activas
     * @return Lista de recurrencias activas
     */
    List<Recurrence> getAllActiveRecurrences();
    
    /**
     * Busca recurrencias por cliente
     * @param clientId ID del cliente
     * @return Lista de DTOs de recurrencias
     */
    List<RecurrenceDTO> getRecurrencesByClient(Long clientId);
}
