package com.crmVs.crm_vs.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Data
@AllArgsConstructor
public class UsuarioLogueado {

    private Long userId;
    private Long tenantId;
    private String rol;
    private String email;

    // Metodo estático que construye el objeto desde el SecurityContextHolder
    public static UsuarioLogueado obtener() {

        var auth =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication();

        return (UsuarioLogueado) auth.getPrincipal();
    }
}
