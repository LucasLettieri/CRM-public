package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.dto.LoginResponseDTO;
import com.crmVs.crm_vs.dto.UserResponseDTO;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.UnauthorizedException;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(String email, String password) {
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario);

        UserResponseDTO usuarioDTO = new UserResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().toUpperCase()
        );

        return new LoginResponseDTO(token, usuarioDTO);
    }
    @Value("${superadmin.email}")
    private String adminEmail;

    @Value("${superadmin.password}")
    private String adminPassword;

    public LoginResponseDTO adminLogin(String email, String password) {
        if (!adminEmail.equals(email) || !adminPassword.equals(password)) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
        String token = jwtService.generarTokenAdmin(email);
        UserResponseDTO usuarioDTO = new UserResponseDTO(0L, "Superadmin", email, "SUPERADMIN");
        return new LoginResponseDTO(token, usuarioDTO);
    }

}