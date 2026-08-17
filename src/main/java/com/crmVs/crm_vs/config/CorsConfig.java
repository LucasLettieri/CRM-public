package com.crmVs.crm_vs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (filtramos null por si CORS_ALLOWED_ORIGIN no está seteada)
        List<String> origins = new ArrayList<>();
        origins.add("http://localhost:5173");

        String corsEnvOrigin = System.getenv("CORS_ALLOWED_ORIGIN");
        if (corsEnvOrigin != null && !corsEnvOrigin.isBlank()) {
            origins.add(corsEnvOrigin);
        }

        config.setAllowedOrigins(origins);

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers permitidos en el pedido
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}