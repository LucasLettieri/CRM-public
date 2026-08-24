package com.crmVs.crm_vs.service;
import com.crmVs.crm_vs.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(User usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("tenantId", usuario.getTenant().getId())
                .claim("rol", usuario.getRol().toUpperCase())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    public Long extraerUserId(String token) {
        return extraerClaims(token).get("userId", Long.class);
    }

    public Long extraerTenantId(String token) {
        return extraerClaims(token).get("tenantId", Long.class);
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean tokenEsValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generarTokenAdmin(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", 0L)
                .claim("tenantId", 0L)
                .claim("rol", "SUPERADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }
}
