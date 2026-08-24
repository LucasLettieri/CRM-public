package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.dto.AdminUserResponseDTO;
import com.crmVs.crm_vs.dto.TenantResponseDTO;
import com.crmVs.crm_vs.dto.UserRequestDTO;
import com.crmVs.crm_vs.model.Tenant;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.BadRequestException;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.TenantRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Tenant crearTenant(String nombre) {
        Tenant tenant = new Tenant();
        tenant.setNombre(nombre);
        return tenantRepository.save(tenant);
    }

    public List<TenantResponseDTO> listarTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(t -> new TenantResponseDTO(
                        t.getId(),
                        t.getNombre(),
                        t.getActivo(),
                        t.getFechaAlta()
                ))
                .toList();
    }


    public User crearUsuario(Long tenantId, UserRequestDTO request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado"));

        User usuario = new User();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().trim().toLowerCase());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol().trim().toLowerCase());
        usuario.setTenant(tenant);

        if (request.getJefeId() != null) {
            User jefe = userRepository.findById(request.getJefeId())
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado"));

            if (!jefe.getTenant().getId().equals(tenantId)) {
                throw new RuntimeException(
                        "El jefe debe pertenecer al mismo tenant"
                );
            }

            usuario.setJefe(jefe);
        }

        return userRepository.save(usuario);
    }

    public List<User> listarUsuarios(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new RuntimeException("Tenant no encontrado");
        }
        return userRepository.findByTenantId(tenantId);
    }


    public TenantResponseDTO toggleTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant no encontrado"));
        tenant.setActivo(!tenant.getActivo());
        tenantRepository.save(tenant);
        return new TenantResponseDTO(
                tenant.getId(),
                tenant.getNombre(),
                tenant.getActivo(),
                tenant.getFechaAlta()
        );
    }

    public void resetearPassword(Long tenantId, Long usuarioId, String nuevaPassword) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!usuario.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException("El usuario no pertenece a este tenant");
        }

        if (nuevaPassword.length() < 6) {
            throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        userRepository.save(usuario);
    }

    public AdminUserResponseDTO cambiarEmail(Long tenantId, Long usuarioId, String nuevoEmail) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!usuario.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException("El usuario no pertenece a este tenant");
        }

        if (userRepository.findByEmail(nuevoEmail).isPresent()) {
            throw new BadRequestException("El email ya está registrado");
        }

        usuario.setEmail(nuevoEmail);
        userRepository.save(usuario);
        return adminConvertirADTO(usuario);
    }
    private AdminUserResponseDTO adminConvertirADTO(User user) {
        return new AdminUserResponseDTO(
                user.getId(),
                user.getTenant().getId(),
                user.getNombre(),
                user.getEmail(),
                user.getRol(),
                user.getJefe().getNombre(),
                user.getTenant().getNombre()
        );
    }
}