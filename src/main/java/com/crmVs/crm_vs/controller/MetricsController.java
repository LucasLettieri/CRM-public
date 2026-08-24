package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.MetricsResponseDTO;
import com.crmVs.crm_vs.model.Periodo;
import com.crmVs.crm_vs.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/metricas")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    // Cualquier rol puede ver sus propias métricas
    // tiene que traer las del mes
    @GetMapping("/mis-metricas")
    public ResponseEntity<MetricsResponseDTO> getMisMetricas(
            @RequestParam(defaultValue = "MES") Periodo periodo,
            @RequestParam(required = false) LocalDate referencia) {
        return ResponseEntity.ok(
                referencia != null
                        ? metricsService.getMisMetricas(periodo, referencia)
                        : metricsService.getMisMetricas(periodo)
        );
    }

    // Solo supervisores y gerentes pueden ver métricas del equipo
    @GetMapping("/equipo")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<MetricsResponseDTO> getMetricasEquipo(
            @RequestParam(defaultValue = "MES") Periodo periodo,
            @RequestParam(required = false) LocalDate referencia) {
        return ResponseEntity.ok(
                referencia != null
                        ? metricsService.getMetricasEquipo(periodo, referencia)
                        : metricsService.getMetricasEquipo(periodo)
        );
    }

    @GetMapping("/equipo/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<MetricsResponseDTO> getMetricasEquipoDeSubordinado(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "MES") Periodo periodo,
            @RequestParam(required = false) LocalDate referencia) {
        return ResponseEntity.ok(
                referencia != null
                        ? metricsService.getMetricasEquipoDeSubordinado(userId, periodo,referencia)
                        : metricsService.getMetricasEquipoDeSubordinado(userId, periodo)
        );
    }

    @GetMapping("/subordinado/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','GERENTE')")
    public ResponseEntity<MetricsResponseDTO> getMetricasSubordinado(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "MES") Periodo periodo,
            @RequestParam(required = false) LocalDate referencia) {
        return ResponseEntity.ok(
                referencia != null
                        ? metricsService.getMetricasSubordinado(userId,periodo, referencia)
                        : metricsService.getMetricasSubordinado(userId,periodo)
        );
    }
}