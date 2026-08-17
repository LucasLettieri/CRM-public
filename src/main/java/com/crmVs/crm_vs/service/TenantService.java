package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.model.Tenant;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Tenant crearTenant(String nombre) {
        Tenant tenant = new Tenant();
        tenant.setNombre(nombre);
        return tenantRepository.save(tenant);
    }
}