package com.crmVs.crm_vs.dto;

import com.crmVs.crm_vs.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
public class LeadFiltroDTO {
    //filtros
    private LeadState estado;

    private Source origen;

    private RazonNoApto razonNoApto;

    private String busqueda;

    private Periodo periodo;

    // orden

    private CampoOrdenLead ordenarPor;
    private Sort.Direction direccion;
    private FiltroContacto contacto;
}
