package com.crmVs.crm_vs.Specification;

import com.crmVs.crm_vs.dto.LeadFiltroDTO;
import com.crmVs.crm_vs.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class LeadSpecification {
    private LeadSpecification() {
    }

    public static Specification<Lead> crear(
            LeadFiltroDTO filtro,
            Long tenantId,
            List<Long> vendedoresIds
    ) {

        Specification<Lead> spec = Specification
                .where(perteneceAlTenant(tenantId))
                .and(perteneceAVendedores(vendedoresIds));

        Periodo periodo = filtro.getPeriodo() != null ? filtro.getPeriodo() : Periodo.MES;
        spec = spec.and(porPeriodo(periodo));

        if (filtro.getEstado() != null) {
            spec = spec.and(
                    estadoEs(filtro.getEstado())
            );
        }
        if (filtro.getOrigen() != null) {
            spec = spec.and(
                    origenEs(filtro.getOrigen())
            );
        }
        if (filtro.getEstado() == LeadState.NO_APTO
                && filtro.getRazonNoApto() != null) {

            spec = spec.and(
                    razonNoAptoEs(filtro.getRazonNoApto())
            );
        }
        if (filtro.getBusqueda() != null && !filtro.getBusqueda().isBlank()) {
            spec = spec.and(
                    busqueda(filtro.getBusqueda())
            );
        }

        if (filtro.getContacto() != null) {
            spec = spec.and(porContacto(filtro.getContacto()));
        }
        return spec;
    }

    private static Specification<Lead> perteneceAlTenant(Long tenantId) {
        return (root, query, cb) ->
                cb.equal(root.get("tenant").get("id"), tenantId);
    }

    private static Specification<Lead> perteneceAVendedores(
            List<Long> vendedoresIds
    ) {

        return (root, query, cb) ->
                root.get("vendedor").get("id").in(vendedoresIds);

    }

    private static Specification<Lead> estadoEs(LeadState estado) {

        return (root, query, cb) ->
                cb.equal(root.get("estado"), estado);

    }

    private static Specification<Lead> origenEs(Source source) {
        return (root, query, cb) ->
                cb.equal(root.get("source"), source);
    }

    private static Specification<Lead> razonNoAptoEs(RazonNoApto razon) {

        return (root, query, cb) ->
                cb.equal(root.get("razonNoApto"), razon);

    }

    private static Specification<Lead> busqueda(String texto) {

        texto = texto.trim();
        String patron = "%" + texto.toLowerCase() + "%";
        String finalTexto = texto;

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(root.get("telefono"), "%" + finalTexto + "%"),
                cb.like(cb.lower(root.get("email")), patron),
                cb.like(root.get("documento"), "%" + finalTexto + "%"),
                cb.like(root.get("cuil"), "%" + finalTexto + "%")
        );
    }

    private static Specification<Lead> porPeriodo(Periodo periodo) {
        return (root, query, cb) -> {
            LocalDate hoy = LocalDate.now();
            LocalDate desde = switch (periodo) {
                case SEMANA -> hoy.minusWeeks(1);
                case MES -> hoy.withDayOfMonth(1);
                case CUATRIMESTRE -> hoy.minusMonths(4);
                case HISTORICO -> null;
            };
            if (desde == null) return cb.conjunction(); // sin filtro de fecha
            return cb.greaterThanOrEqualTo(root.get("fechaCarga"), desde);
        };
    }

    private static Specification<Lead> porContacto(FiltroContacto contacto) {
        return (root, query, cb) -> {
            LocalDate hoy = LocalDate.now();
            return switch (contacto) {
                case HOY -> cb.equal(root.get("volverAContactar"), hoy);
                case VENCIDO -> cb.lessThan(root.get("volverAContactar"), hoy);
            };
        };
    }


}



