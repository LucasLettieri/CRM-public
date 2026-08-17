package com.crmVs.crm_vs.Specification;

import com.crmVs.crm_vs.dto.LeadFiltroDTO;
import org.springframework.data.domain.Sort;

public final class LeadSort {

    private LeadSort() {
    }

    public static Sort crear(LeadFiltroDTO filtro) {
        if (filtro.getOrdenarPor() == null) {
            return Sort.by(Sort.Direction.DESC, "fechaCarga");
        }

        Sort.Direction direccion = filtro.getDireccion() != null
                ? filtro.getDireccion()
                : Sort.Direction.DESC;

        return switch (filtro.getOrdenarPor()) {
            case FECHA_CARGA -> Sort.by(direccion, "fechaCarga");
            case GANANCIA -> Sort.by(direccion, "ganancia");
            case COSTO -> Sort.by(direccion, "costo");
            case VOLVER_A_CONTACTAR -> Sort.by(direccion, "volverAContactar");
        };
    }
}
