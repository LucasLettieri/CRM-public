package com.crmVs.crm_vs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TenantResponseDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
    private LocalDateTime fechaAlta;
}