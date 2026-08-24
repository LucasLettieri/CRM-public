package com.crmVs.crm_vs.dto;

import com.crmVs.crm_vs.model.LeadState;
import com.crmVs.crm_vs.model.RazonNoApto;
import com.crmVs.crm_vs.model.Source;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeadResponseDTO {
    private Long id;
    private String nombre;
    private String telefono;
    private String email;
    private String documento;
    private String cuil;
    private String nota;
    private Source origen;
    private LeadState estado;
    private LocalDate primerContacto;
    private LocalDate ultimoContacto;
    private LocalDate volverAContactar;
    private LocalDate fechaCarga;
    private String vendedorNombre;
    private Double costo;
    private Double ganancia;
    private RazonNoApto razonNoApto;
    private LocalDate fechaConfirmacion;
    private LocalDate proximoRecordatorio;
}