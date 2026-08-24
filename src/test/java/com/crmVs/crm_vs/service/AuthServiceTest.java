package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.dto.LoginResponseDTO;
import com.crmVs.crm_vs.model.Tenant;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.UnauthorizedException;
import com.crmVs.crm_vs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User usuario;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
    
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@crm.com");
        ReflectionTestUtils.setField(authService, "adminPassword", "claveSegura123");

        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setNombre("Sucursal Norte");

        usuario = new User();
        usuario.setId(1L);
        usuario.setNombre("Lucas");
        usuario.setEmail("lucas@empresa.com");
        usuario.setPasswordHash("$2a$10$hashBcrypt");
        usuario.setRol("vendedor");
        usuario.setTenant(tenant);
    }

    @Test
    void login_credencialesCorrectas_devuelveToken() {
        when(userRepository.findByEmail("lucas@empresa.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "$2a$10$hashBcrypt"))
                .thenReturn(true);
        when(jwtService.generarToken(usuario))
                .thenReturn("token.jwt.generado");

        LoginResponseDTO resultado = authService.login("lucas@empresa.com", "1234");

        assertThat(resultado.getToken()).isEqualTo("token.jwt.generado");
        assertThat(resultado.getUsuario().getEmail()).isEqualTo("lucas@empresa.com");
    }



    @Test
    void login_emailInexistente_lanzaUnauthorizedException() {
        when(userRepository.findByEmail("noexiste@empresa.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login("noexiste@empresa.com", "1234")
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void login_passwordIncorrecta_lanzaUnauthorizedException() {
        when(userRepository.findByEmail("lucas@empresa.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashBcrypt"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authService.login("lucas@empresa.com", "wrongPassword")
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void login_emailIncorrecto_noDevuelveInfoSiPasswordEsCorrecta() {

        when(userRepository.findByEmail("noexiste@empresa.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login("noexiste@empresa.com", "1234")
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas"); //mismo mensaje que password incorrecta
    }

    @Test
    void login_siempreLlamaAlRepositorioPorEmail() {
        when(userRepository.findByEmail("lucas@empresa.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login("lucas@empresa.com", "1234")
        ).isInstanceOf(UnauthorizedException.class);

        verify(userRepository, times(1)).findByEmail("lucas@empresa.com");
    }

    @Test
    void login_passwordNuncaSeVerificaSiEmailNoExiste() {
        when(userRepository.findByEmail("noexiste@empresa.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login("noexiste@empresa.com", "1234")
        ).isInstanceOf(UnauthorizedException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void adminLogin_credencialesCorrectas_devuelveToken() {
        when(jwtService.generarTokenAdmin("admin@crm.com"))
                .thenReturn("admin.token.generado");

        LoginResponseDTO resultado = authService.adminLogin("admin@crm.com", "claveSegura123");

        assertThat(resultado.getToken()).isEqualTo("admin.token.generado");
        assertThat(resultado.getUsuario().getEmail()).isEqualTo("admin@crm.com");
        assertThat(resultado.getUsuario().getRol()).isEqualTo("SUPERADMIN");
    }

    @Test
    void adminLogin_emailIncorrecto_lanzaUnauthorizedException() {
        assertThatThrownBy(() ->
                authService.adminLogin("otro@crm.com", "claveSegura123")
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void adminLogin_passwordIncorrecta_lanzaUnauthorizedException() {
        assertThatThrownBy(() ->
                authService.adminLogin("admin@crm.com", "wrongPassword")
        )
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void adminLogin_nuncaConsultaLaBaseDeDatos() {
        when(jwtService.generarTokenAdmin(any()))
                .thenReturn("token");

        authService.adminLogin("admin@crm.com", "claveSegura123");

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void adminLogin_credencialesCorrectas_llamaAlGeneradorDeTokenAdmin() {
        when(jwtService.generarTokenAdmin("admin@crm.com"))
                .thenReturn("token");

        authService.adminLogin("admin@crm.com", "claveSegura123");

        verify(jwtService).generarTokenAdmin("admin@crm.com");
        verify(jwtService, never()).generarToken(any());
    }
}
