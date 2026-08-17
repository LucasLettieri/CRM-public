package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.InteractionRequestDTO;
import com.crmVs.crm_vs.dto.InteractionResponseDTO;
import com.crmVs.crm_vs.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/{leadId}/interacciones")
    public ResponseEntity<InteractionResponseDTO> registrar(
            @PathVariable Long leadId,
            @RequestBody InteractionRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionService.registrar(leadId, request));
    }

    @GetMapping("/{leadId}/interacciones")
    public ResponseEntity<List<InteractionResponseDTO>> listar(
            @PathVariable Long leadId) {

        return ResponseEntity.ok(interactionService.listarPorLead(leadId));
    }
}