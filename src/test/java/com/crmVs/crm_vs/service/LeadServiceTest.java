package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.LeadRequestDTO;
import com.crmVs.crm_vs.dto.LeadResponseDTO;
import com.crmVs.crm_vs.model.*;
import com.crmVs.crm_vs.model.exception.BadRequestException;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.InteractionRepository;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InteractionRepository interactionRepository;

    @InjectMocks
    private LeadService leadService;

    private Tenant tenant;
    private User vendedor;
    private UsuarioLogueado usuarioLogueado;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setNombre("Sucursal Norte");

        vendedor = new User();
        vendedor.setId(3L);
        vendedor.setNombre("Lucas");
        vendedor.setEmail("lucas@empresa.com");
        vendedor.setRol("vendedor");
        vendedor.setTenant(tenant);

        usuarioLogueado = new UsuarioLogueado(3L, 1L, "vendedor", "lucas@empresa.com");
    }


    @Test
    void crearLead_estadoSiempreEsNuevo() {
        LeadRequestDTO request = new LeadRequestDTO();
        request.setNombre("María López");
        request.setTelefono("1145678900");
        request.setOrigen(Source.REDES);

        when(userRepository.findById(3L)).thenReturn(Optional.of(vendedor));

        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            LeadResponseDTO resultado = leadService.crearLead(request);

            assertThat(resultado.getEstado()).isEqualTo(LeadState.NUEVO);
        }
    }

    @Test
    void crearLead_tenantSeHeredaDelVendedor() {
        LeadRequestDTO request = new LeadRequestDTO();
        request.setNombre("Carlos Ruiz");
        request.setTelefono("1167891234");
        request.setOrigen(Source.CALLE);

        when(userRepository.findById(3L)).thenReturn(Optional.of(vendedor));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            LeadResponseDTO resultado = leadService.crearLead(request);

            assertThat(resultado).isNotNull();
            verify(leadRepository).save(argThat(lead ->
                    lead.getTenant().getId().equals(1L)
            ));
        }
    }

    @Test
    void crearLead_vendedorNoExiste_lanzaNotFoundException() {
        LeadRequestDTO request = new LeadRequestDTO();
        request.setNombre("Ana García");
        request.setTelefono("1198765432");
        request.setOrigen(Source.REFERIDO);

        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);


            assertThatThrownBy(() -> leadService.crearLead(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Vendedor no encontrado");
        }
    }


    @Test
    void cambiarEstado_noApto_sinRazon_lanzaBadRequestException() {

        Lead lead = new Lead();
        lead.setId(1L);
        lead.setEstado(LeadState.NUEVO);
        lead.setTenant(tenant);
        lead.setVendedor(vendedor);

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(userRepository.findAllSubordinadosIds(3L)).thenReturn(List.of(3L));
        when(userRepository.findById(3L)).thenReturn(Optional.of(vendedor));

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            assertThatThrownBy(() ->
                    leadService.cambiarEstado(1L, "NO_APTO", null, null)
            )
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Debés especificar la razón por la que el lead no es apto");
        }
    }

    @Test
    void cambiarEstado_leadDeOtroTenant_lanzaForbiddenException() {
        Tenant otroTenant = new Tenant();
        otroTenant.setId(99L);

        Lead lead = new Lead();
        lead.setId(1L);
        lead.setEstado(LeadState.NUEVO);
        lead.setTenant(otroTenant); 
        lead.setVendedor(vendedor);

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            assertThatThrownBy(() ->
                    leadService.cambiarEstado(1L, "EN_SEGUIMIENTO", null, null)
            )
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Test
    void cambiarEstado_leadDeOtroVendedor_lanzaForbiddenException() {
        User otroVendedor = new User();
        otroVendedor.setId(99L);

        Lead lead = new Lead();
        lead.setId(1L);
        lead.setEstado(LeadState.NUEVO);
        lead.setTenant(tenant);
        lead.setVendedor(otroVendedor); 

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(userRepository.findAllSubordinadosIds(3L)).thenReturn(List.of(3L));

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            assertThatThrownBy(() ->
                    leadService.cambiarEstado(1L, "EN_SEGUIMIENTO", null, null)
            )
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Test
    void cambiarEstado_exitoso_registraInteraccion() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setEstado(LeadState.NUEVO);
        lead.setTenant(tenant);
        lead.setVendedor(vendedor);

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(userRepository.findAllSubordinadosIds(3L)).thenReturn(List.of(3L));
        when(userRepository.findById(3L)).thenReturn(Optional.of(vendedor));
        when(leadRepository.save(any())).thenReturn(lead);
        when(interactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            leadService.cambiarEstado(1L, "EN_SEGUIMIENTO", null, null);

            verify(interactionRepository).save(argThat(interaction ->
                    interaction.getTipo().equals("cambio_estado")
            ));
        }
    }


    @Test
    void editarLead_camposNullNoSobreescriben() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setNombre("Nombre Original");
        lead.setTelefono("1145678900");
        lead.setEstado(LeadState.NUEVO);
        lead.setTenant(tenant);
        lead.setVendedor(vendedor);

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(userRepository.findAllSubordinadosIds(3L)).thenReturn(List.of(3L));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        com.crmVs.crm_vs.dto.LeadUpdateDTO request = new com.crmVs.crm_vs.dto.LeadUpdateDTO();
        request.setNota("Nueva nota");

        try (MockedStatic<UsuarioLogueado> mocked = mockStatic(UsuarioLogueado.class)) {
            mocked.when(UsuarioLogueado::obtener).thenReturn(usuarioLogueado);

            LeadResponseDTO resultado = leadService.editarLead(1L, request);

            assertThat(resultado.getNombre()).isEqualTo("Nombre Original");
        }
    }
}
