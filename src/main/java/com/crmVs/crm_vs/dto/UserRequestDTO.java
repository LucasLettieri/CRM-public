package com.crmVs.crm_vs.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "vendedor|supervisor|gerente",
            message = "El rol debe ser: vendedor, supervisor o gerente")
    private String rol;

    @NotNull(message = "El tenantId es obligatorio")
    private Long tenantId;

    private Long jefeId;
}
