package com.crmVs.crm_vs.config;

import com.crmVs.crm_vs.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Buscar el header Authorization en el pedido
        String authHeader = request.getHeader("Authorization");

        // 2. Si no hay header o no empieza con "Bearer ", dejá pasar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (sacar el "Bearer " del principio)
        String token = authHeader.substring(7);

        // 4. Verificar que el token es válido
        if (!jwtService.tokenEsValido(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 5. Extraer la información del usuario del token
        Long userId = jwtService.extraerUserId(token);
        Long tenantId = jwtService.extraerTenantId(token);
        String rol = jwtService.extraerRol(token);
        String email = jwtService.extraerEmail(token);

        UsuarioLogueado usuario =
                new UsuarioLogueado(
                        userId,
                        tenantId,
                        rol,
                        email
                );

        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + rol.toUpperCase()
                                )
                        )
                );

        // 7. Guardar la info del usuario en el contexto de seguridad
        // Esto hace que esté disponible en cualquier controller o service
        SecurityContextHolder.getContext().setAuthentication(autenticacion);

        // 8. Dejar pasar el pedido al controller
        filterChain.doFilter(request, response);
    }
}