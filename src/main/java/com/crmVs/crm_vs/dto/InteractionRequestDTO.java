package com.crmVs.crm_vs.dto;

import lombok.Data;

@Data
public class InteractionRequestDTO {
    private String tipo;    // "llamada", "whatsapp", "nota", "cambio_estado"
    private String detalle;
}