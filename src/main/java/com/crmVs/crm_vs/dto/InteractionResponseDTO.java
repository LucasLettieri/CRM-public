package com.crmVs.crm_vs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InteractionResponseDTO {
    private Long id;
    private String tipo;
    private String detalle;
    private LocalDateTime fecha;
    private String usuarioNombre;
    private Long leadId;
}