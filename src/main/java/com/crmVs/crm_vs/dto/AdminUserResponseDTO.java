package com.crmVs.crm_vs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private Long tenantId;
    private String nombre;
    private String email;
    private String rol;
    private String jefeNombre;
    private String tenantNombre;
}
