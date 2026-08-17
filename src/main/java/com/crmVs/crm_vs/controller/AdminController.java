package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.AdminUserResponseDTO;
import com.crmVs.crm_vs.dto.TenantResponseDTO;
import com.crmVs.crm_vs.dto.UserRequestDTO;
import com.crmVs.crm_vs.model.Tenant;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/tenants")
    public ResponseEntity<Tenant> crearTenant(@RequestParam String nombre) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.crearTenant(nombre));
    }

    @PostMapping("/tenants/{tenantId}/usuarios")
    public ResponseEntity<User> crearUsuario(
            @PathVariable Long tenantId,
            @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.crearUsuario(tenantId, request));
    }

    @GetMapping("/tenants/{tenantId}/usuarios")
    public ResponseEntity<List<User>> listarUsuarios(@PathVariable Long tenantId) {
        return ResponseEntity.ok(adminService.listarUsuarios(tenantId));
    }
    @GetMapping("/tenants")
    public ResponseEntity<List<TenantResponseDTO>> listarTenants() {
        return ResponseEntity.ok(adminService.listarTenants());
    }
    @PatchMapping("/tenants/{tenantId}/activo")
    public ResponseEntity<TenantResponseDTO> toggleTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(adminService.toggleTenant(tenantId));
    }

    @PatchMapping("/tenants/{tenantId}/usuarios/{usuarioId}/password")
    public ResponseEntity<Void> resetearPassword(
            @PathVariable Long tenantId,
            @PathVariable Long usuarioId,
            @RequestParam String nuevaPassword) {
        adminService.resetearPassword(tenantId, usuarioId, nuevaPassword);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tenants/{tenantId}/usuarios/{usuarioId}/email")
    public ResponseEntity<AdminUserResponseDTO> cambiarEmail(
            @PathVariable Long tenantId,
            @PathVariable Long usuarioId,
            @RequestParam String nuevoEmail) {
        return ResponseEntity.ok(adminService.cambiarEmail(tenantId, usuarioId, nuevoEmail));
    }
}