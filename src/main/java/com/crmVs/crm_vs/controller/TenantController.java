package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.model.Tenant;
import com.crmVs.crm_vs.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @PostMapping
    public ResponseEntity<Tenant> crearTenant(@RequestParam String nombre) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.crearTenant(nombre));
    }
    //En el futuro metodos para cambiar "Activo" "No activo" y demás validaciones

}