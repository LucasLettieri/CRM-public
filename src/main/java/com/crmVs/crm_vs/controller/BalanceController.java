package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.BalanceResponseDTO;
import com.crmVs.crm_vs.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/mio")
    public ResponseEntity<BalanceResponseDTO> getMiBalance() {
        return ResponseEntity.ok(balanceService.getMiBalance());
    }

    @GetMapping("/equipo")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<BalanceResponseDTO> getBalanceEquipo() {
        return ResponseEntity.ok(balanceService.getBalanceEquipo());
    }

    @GetMapping("/equipo/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<BalanceResponseDTO> getBalanceEquipoDeSubordinado(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                balanceService.getBalanceEquipoDeSubordinado(userId)
        );
    }

    @GetMapping("/subordinado/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<BalanceResponseDTO> getBalanceSubordinado(
            @PathVariable Long userId) {
        return ResponseEntity.ok(balanceService.getBalanceSubordinado(userId));
    }
}