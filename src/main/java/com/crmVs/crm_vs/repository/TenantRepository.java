package com.crmVs.crm_vs.repository;
import com.crmVs.crm_vs.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}