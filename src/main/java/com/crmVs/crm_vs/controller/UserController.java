package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.UserResponseDTO;
import com.crmVs.crm_vs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/prueba")
    public void pruebaLocalDate(){
        System.out.println("Zona horaria JVM: " + java.util.TimeZone.getDefault().getID());
        System.out.println("LocalDate.now(): " + LocalDate.now());
        System.out.println("LocalDate.now(ZoneId.of(\"America/Argentina/Buenos_Aires\")): " + LocalDate.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")));
    }
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    @GetMapping("/subordinados")
    public ResponseEntity<List<UserResponseDTO>> buscarSubordinados() {
        return ResponseEntity.ok(userService.buscarSubordinados());
    }
    @GetMapping("/subordinados/directos")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<List<UserResponseDTO>> buscarSubordinadosDirectos() {
        return ResponseEntity.ok(userService.buscarSubordinadosDirectos());
    }

    @GetMapping("/subordinados/directos/{userId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
    public ResponseEntity<List<UserResponseDTO>> buscarSubordinadosDirectosDe(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.buscarSubordinadosDirectosDe(userId));
    }
}