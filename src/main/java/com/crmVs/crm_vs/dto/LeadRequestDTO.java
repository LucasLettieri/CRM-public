package com.crmVs.crm_vs.dto;

import com.crmVs.crm_vs.model.RazonNoApto;
import com.crmVs.crm_vs.model.Source;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeadRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s]{10,20}$",
            message = "El teléfono debe contener solo números y tener entre 10 y 20 caracteres")
    private String telefono;

    @Email(message = "El email no tiene un formato válido")
    private String email;

    private String nota;


    @Size(min = 8, max = 8, message = "El DNI debe tener una cantidad fija de 8 números")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener solo números")
    private String documento;

    @Pattern(
            regexp = "^\\d{2}-\\d{8}-\\d{1}$",
            message = "El formato del CUIL debe ser: XX-XXXXXXXX-X (ej. 20-12345678-1)"
    )
    private String cuil;

    @NotNull(message = "El origen es obligatorio")
    private Source origen;

    @PositiveOrZero(message = "El costo no puede ser menor a 0")
    private Double costo;

    @PositiveOrZero(message = "La ganancia no puede ser menor a 0")
    private Double ganancia;

    private RazonNoApto razonNoApto;
    private LocalDate volverAContactar;



}