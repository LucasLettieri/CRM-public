package com.crmVs.crm_vs.dto;

import com.crmVs.crm_vs.model.RazonNoApto;
import com.crmVs.crm_vs.model.Source;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class LeadUpdateDTO {

    private String nombre;

    @Pattern(regexp = "^[0-9+\\-\\s]{10,20}$",
            message = "El teléfono debe contener solo números y tener entre 10 y 20 caracteres")
    private String telefono;

    @Size(min = 8, max = 8, message = "El DNI debe tener una cantidad fija de 8 números")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener solo números")
    private String documento;

    @Pattern(
            regexp = "^\\d{2}-\\d{8}-\\d{1}$",
            message = "El formato debe ser exacto: XX-XXXXXXXX-X (ej. 20-12345678-1)"
    )
    private String cuil;

    @Email(message = "El email no tiene un formato válido")
    private String email;

    @PositiveOrZero(message = "El costo no puede ser menor a 0")
    private Double costo;

    @PositiveOrZero(message = "La ganancia no puede ser menor a 0")
    private Double ganancia;
    private RazonNoApto razonNoApto;
    private Source origen;
    private String nota;

}