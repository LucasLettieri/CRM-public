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

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.tokenEsValido(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

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

 
        SecurityContextHolder.getContext().setAuthentication(autenticacion);

        filterChain.doFilter(request, response);
    }
}
