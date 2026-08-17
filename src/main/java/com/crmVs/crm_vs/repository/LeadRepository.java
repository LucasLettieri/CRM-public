package com.crmVs.crm_vs.repository;

import com.crmVs.crm_vs.model.Lead;
import com.crmVs.crm_vs.model.LeadState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {
    List<Lead> findByVendedorId(Long vendedorId);

    List<Lead> findByTenantId(Long tenantId);

    List<Lead> findByVendedorIdAndTenantId(Long vendedorId, Long tenantId);

    // Busca leads de múltiples vendedores a la vez
    List<Lead> findByVendedorIdInAndTenantId(List<Long> vendedorIds, Long tenantId);

    List<Lead> findByVendedorIdAndTenantIdAndVolverAContactar(
            Long vendedorId,
            Long tenantId,
            LocalDate volverAContactar
    );
    List<Lead> findByVendedorIdAndTenantIdAndFechaCargaBetween(
            Long vendedorId, Long tenantId, LocalDate inicio, LocalDate fin);

    List<Lead> findByVendedorIdInAndTenantIdAndFechaCargaBetween(
            List<Long> vendedorIds, Long tenantId, LocalDate inicio, LocalDate fin);

    //Vencidos
    List<Lead> findByVendedorIdAndTenantIdAndVolverAContactarLessThan(
            Long vendedorId,
            Long tenantId,
            LocalDate fecha
    );

    //Nuevas metricas
    List<Lead> findByVendedorIdAndTenantIdAndEstadoAndFechaConversionBetween(
            Long vendedorId, Long tenantId, LeadState estado, LocalDate inicio, LocalDate fin);

    List<Lead> findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionBetween(
            List<Long> vendedorIds, Long tenantId, LeadState estado, LocalDate inicio, LocalDate fin);

    //Balance
    List<Lead> findByVendedorIdAndTenantIdAndFechaCargaGreaterThanEqual(Long userId, Long tenantId, LocalDate inicio);

    List<Lead> findByVendedorIdAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(Long userId, Long tenantId, LeadState leadState, LocalDate inicio);

    List<Lead> findByVendedorIdInAndTenantIdAndFechaCargaGreaterThanEqual(List<Long> ids, Long tenantId, LocalDate inicio);

    List<Lead> findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(List<Long> ids, Long tenantId, LeadState leadState, LocalDate inicio);
}

