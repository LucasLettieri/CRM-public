package com.crmVs.crm_vs.repository;

import com.crmVs.crm_vs.model.AfiliacionPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AfiliacionPendienteRepository extends JpaRepository<AfiliacionPendiente, Long> {

    Optional<AfiliacionPendiente> findByLeadId(Long leadId);

    List<AfiliacionPendiente> findByProximoRecordatorioLessThanEqual(LocalDate fecha);

    List<AfiliacionPendiente> findByLead_VendedorIdAndLead_TenantId(Long vendedorId, Long tenantId);
}
