package com.crmVs.crm_vs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
}
