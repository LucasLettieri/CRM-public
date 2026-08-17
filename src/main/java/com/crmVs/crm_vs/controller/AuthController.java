package com.crmVs.crm_vs.controller;

import com.crmVs.crm_vs.dto.LoginResponseDTO;
import com.crmVs.crm_vs.dto.UserResponseDTO;
import com.crmVs.crm_vs.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<LoginResponseDTO> adminLogin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request.getEmail(), request.getPassword()));
    }

    @Data
    static class LoginRequest {
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    static class TokenResponse {
        private final String token;
        private final UserResponseDTO usuario;

        public TokenResponse(String token, UserResponseDTO usuario) {
            this.token = token;
            this.usuario = usuario;
        }
    }
}