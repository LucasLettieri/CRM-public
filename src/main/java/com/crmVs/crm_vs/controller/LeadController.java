package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.LeadFiltroDTO;
import com.crmVs.crm_vs.dto.LeadRequestDTO;
import com.crmVs.crm_vs.dto.LeadResponseDTO;
import com.crmVs.crm_vs.dto.LeadUpdateDTO;
import com.crmVs.crm_vs.model.RazonNoApto;
import com.crmVs.crm_vs.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<LeadResponseDTO> crearLead(@Valid @RequestBody LeadRequestDTO request) {
        LeadResponseDTO response = leadService.crearLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponseDTO>verLead(@PathVariable Long leadId){
        return ResponseEntity.ok(leadService.verLead(leadId));
    }

    @PatchMapping("/{leadId}/volverAContactar")
    public ResponseEntity<LeadResponseDTO> setVolverAContactar(@PathVariable Long leadId, @RequestBody LocalDate date){
        LeadResponseDTO response = leadService.setVolverAContactar(leadId,date);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{leadId}/volverAContactar")
    public ResponseEntity<Void> borrarVolverAContactar(@PathVariable Long leadId) {
        leadService.borrarVolverAContactar(leadId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mios")
    public List<LeadResponseDTO> listarMisLeads(@ModelAttribute LeadFiltroDTO filtro){
        return leadService.listarMisLeads(filtro);
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    @GetMapping("/equipo")
    public List<LeadResponseDTO> listarLeadsEquipo(@ModelAttribute LeadFiltroDTO filtro) {
        return leadService.listarLeadsEquipo(filtro);
    }
    //lista leads de UN subordinado
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    @GetMapping("/subordinado/{subordinadoId}")
    public List<LeadResponseDTO> listarLeadsSubordinado(@ModelAttribute LeadFiltroDTO filtro,
           @PathVariable Long subordinadoId) {
        return leadService.listarLeadsSubordinado(filtro,subordinadoId);
    }
    //lista leads de de todo el equipo debajo de subordinado
    @GetMapping("/equipo/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public List<LeadResponseDTO>listarLeadsEquipoDeSubordinado(@ModelAttribute LeadFiltroDTO filtro,
            @PathVariable Long userId) {
        return
                leadService.listarLeadsEquipoDeSubordinado(filtro, userId);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<LeadResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado,
            @RequestParam(required = false) RazonNoApto razonNoApto,
            @RequestParam(required = false) LocalDate fechaConfirmacion) {
        return ResponseEntity.ok(leadService.cambiarEstado(id, nuevoEstado, razonNoApto, fechaConfirmacion));
    }

    @GetMapping("/today")
    public ResponseEntity<List<LeadResponseDTO>> findLeadsDueToday() {
        return ResponseEntity.ok(leadService.leadsAContactarHoy());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> editarLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadUpdateDTO request) {
        return ResponseEntity.ok(leadService.editarLead(id, request));
    }

}
