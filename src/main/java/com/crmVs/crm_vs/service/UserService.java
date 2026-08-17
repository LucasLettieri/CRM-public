package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.AdminUserResponseDTO;
import com.crmVs.crm_vs.dto.UserResponseDTO;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.TenantRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder; // ← nuevo


    public User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public List<UserResponseDTO> buscarSubordinados(){
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        return userRepository.findAllSubordinadosIds(usuarioLogueado.getUserId())
                .stream()
                .filter(id -> !id.equals(usuarioLogueado.getUserId()))
                .map(id -> buscarPorId(id))
                .map(this::convertirADTO)
                .toList();
    }
    public List<UserResponseDTO> buscarSubordinadosDirectos() {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();
        return userRepository
                .findByJefeIdAndTenantId(usuarioLogueado.getUserId(), usuarioLogueado.getTenantId())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList())
                ;
    }

    public List<UserResponseDTO> buscarSubordinadosDirectosDe(Long userId) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();
        List<Long> misSubordinados = userRepository.findAllSubordinadosIds(
                usuarioLogueado.getUserId()
        );

        if (!misSubordinados.contains(userId)) {
            throw new ForbiddenException("No tenés acceso a ese usuario.");
        }
        return userRepository
                .findByJefeIdAndTenantId(userId, usuarioLogueado.getTenantId())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private UserResponseDTO convertirADTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                user.getRol()
        );
    }
}