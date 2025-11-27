package com.waturnos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.beans.WaitlistEntryDTO;
import com.waturnos.dto.request.CreateWaitlistRequest;
import com.waturnos.service.WaitlistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller para gestión de listas de espera (waitlist)
 */
@RestController
@RequestMapping("/waitlist")
@RequiredArgsConstructor
public class WaitlistController {
    
    private final WaitlistService waitlistService;
    
    /**
     * Crea una nueva entrada en la lista de espera
     * 
     * POST /waitlist
     * 
     * @param request datos de la entrada a crear
     * @return ApiResponse con la entrada creada
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WaitlistEntryDTO>> createWaitlistEntry(
            @Valid @RequestBody CreateWaitlistRequest request) {
        
        WaitlistEntryDTO entry = waitlistService.createEntry(request);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Entrada creada en lista de espera", entry)
        );
    }
    
    /**
     * Obtiene las listas de espera activas de un cliente
     * 
     * GET /waitlist/my/{clientId}
     * 
     * @param clientId ID del cliente
     * @param organizationId ID de la organización (opcional)
     * @return ApiResponse con lista de entradas
     */
    @GetMapping("/my/{clientId}")
    public ResponseEntity<ApiResponse<List<WaitlistEntryDTO>>> getMyWaitlist(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long organizationId) {
        
        List<WaitlistEntryDTO> entries = waitlistService.getMyWaitlist(clientId, organizationId);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Lista de espera obtenida", entries)
        );
    }
    
    /**
     * Cancela (sale de) una entrada en la lista de espera
     * 
     * DELETE /waitlist/{id}
     * 
     * @param id ID de la entrada
     * @param clientId ID del cliente (para validación)
     * @return ApiResponse indicando éxito
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> leaveWaitlist(
            @PathVariable Long id,
            @RequestParam Long clientId) {
        
        waitlistService.cancelEntry(id, clientId);
        
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Saliste de la lista de espera", null)
        );
    }
}
