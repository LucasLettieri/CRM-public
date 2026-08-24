package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.Specification.LeadSort;
import com.crmVs.crm_vs.Specification.LeadSpecification;
import com.crmVs.crm_vs.dto.*;
import com.crmVs.crm_vs.model.exception.*;
import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.model.*;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.AfiliacionPendienteRepository;
import com.crmVs.crm_vs.repository.InteractionRepository;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadService {                  

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;  
    private final InteractionRepository interactionRepository;
    private final AfiliacionPendienteRepository afiliacionPendienteRepository;

    public LeadResponseDTO crearLead(LeadRequestDTO request) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        User vendedor = userRepository.findById(usuarioLogueado.getUserId())  
                .orElseThrow(() -> new NotFoundException("Vendedor no encontrado"));

        Lead lead = new Lead();
        lead.setNombre(request.getNombre());
        lead.setTelefono(request.getTelefono());
        lead.setEmail(request.getEmail());
        lead.setDocumento(request.getDocumento());
        lead.setCuil(request.getCuil());
        lead.setNota(request.getNota());
        lead.setSource(request.getOrigen());
        lead.setVolverAContactar(request.getVolverAContactar());
        lead.setVendedor(vendedor);
        lead.setTenant(vendedor.getTenant());
        lead.setCosto(request.getCosto() != null ? request.getCosto() : 0.0);
        lead.setGanancia(request.getGanancia() != null ? request.getGanancia() : 0.0);
        lead.setRazonNoApto(request.getRazonNoApto());

        return convertirADTO(leadRepository.save(lead));
    }

    public List<LeadResponseDTO> listarMisLeads(
            LeadFiltroDTO filtro
    ) {

        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        Specification<Lead> spec = LeadSpecification.crear(
                filtro,
                usuario.getTenantId(),
                List.of(usuario.getUserId())
        );
        Sort sort = LeadSort.crear(filtro);

        return leadRepository.findAll(spec, sort)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<LeadResponseDTO> listarLeadsEquipo(LeadFiltroDTO filtro) {

        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuario.getUserId());

        Specification<Lead> spec = LeadSpecification.crear(
                filtro,
                usuario.getTenantId(),
                idsPermitidos
        );

        Sort sort = LeadSort.crear(filtro);

        return leadRepository.findAll(spec, sort)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<LeadResponseDTO> listarLeadsSubordinado(LeadFiltroDTO filtro, Long leadId) {

        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        User subordinado = userRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuario.getUserId());

        if (!idsPermitidos.contains(subordinado.getId())) {
            throw new ForbiddenException(
                    "No tenés permiso para ver los leads de este usuario"
            );
        }
        Specification<Lead> spec = LeadSpecification.crear(
                filtro,
                usuario.getTenantId(),
                List.of(leadId)
        );
        Sort sort = LeadSort.crear(filtro);

        return leadRepository.findAll(spec, sort)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<LeadResponseDTO> listarLeadsEquipoDeSubordinado(LeadFiltroDTO filtro,Long subordinadoId) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuario.getUserId());

        if (!idsPermitidos.contains(subordinadoId)) {
            throw new ForbiddenException(
                    "No tenés permiso para ver el equipo de este usuario"
            );
        }

        List<Long> idsEquipo = userRepository
                .findAllSubordinadosIds(subordinadoId);

        Specification<Lead> spec = LeadSpecification.crear(
                filtro,
                usuario.getTenantId(),
                idsEquipo
        );
        Sort sort = LeadSort.crear(filtro);

        return leadRepository.findAll(spec, sort)
                .stream()
                .map(this::convertirADTO)
                .toList();

    }

    public LeadResponseDTO cambiarEstado(Long leadId, String nuevoEstado, RazonNoApto razonNoApto, LocalDate fechaConfirmacion) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        if (lead.getEstado().name().equals(nuevoEstado) && lead.getEstado() != LeadState.NO_APTO) {
            throw new BadRequestException("El lead ya se encuentra en estado " + nuevoEstado);
        }

        if (!lead.getTenant().getId().equals(usuarioLogueado.getTenantId())) {
            throw new ForbiddenException("No tenés permiso para modificar este lead");
        }

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuarioLogueado.getUserId());

        if (!idsPermitidos.contains(lead.getVendedor().getId())) {
            throw new ForbiddenException("No tenés permiso para modificar este lead");
        }

        User usuario = userRepository.findById(usuarioLogueado.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        LeadState estadoNuevo = LeadState.valueOf(nuevoEstado);
        String estadoAnterior = lead.getEstado().name();

        if (estadoNuevo == LeadState.NO_APTO && razonNoApto == null) {
            throw new BadRequestException(
                    "Debés especificar la razón por la que el lead no es apto"
            );
        }

        if (estadoNuevo == LeadState.PENDIENTE && fechaConfirmacion == null) {
            throw new BadRequestException(
                    "Debés especificar la fecha de confirmación para un lead pendiente"
            );
        }


        lead.setEstado(estadoNuevo);

        if (!nuevoEstado.equals("NO_APTO")) {
            lead.setRazonNoApto(null);
        }
        if (razonNoApto != null) {
            lead.setRazonNoApto(razonNoApto);
        }

        if (estadoNuevo == LeadState.GANADO) {
            lead.setFechaConversion(LocalDate.now());
        } else {
            lead.setFechaConversion(null);
        }

        if (estadoNuevo == LeadState.PENDIENTE || estadoAnterior.equals(LeadState.PENDIENTE.name())) {
            manejarAfiliacionPendiente(lead, estadoNuevo, fechaConfirmacion);
        }

        leadRepository.save(lead);

        Interaction interaction = new Interaction();
        interaction.setTipo("cambio_estado");
        interaction.setDetalle("Estado cambiado de " + estadoAnterior + " a " + nuevoEstado);
        interaction.setLead(lead);
        interaction.setUsuario(usuario);
        interaction.setTenant(lead.getTenant());
        interactionRepository.save(interaction);

        return convertirADTO(lead);
    }

    private void manejarAfiliacionPendiente(Lead lead, LeadState estadoNuevo, LocalDate fechaConfirmacion) {
        if (estadoNuevo == LeadState.PENDIENTE) {
            AfiliacionPendiente afiliacion = new AfiliacionPendiente();
            afiliacion.setLead(lead);
            afiliacion.setFechaConfirmacion(fechaConfirmacion.withDayOfMonth(1));
            afiliacion.setProximoRecordatorio(calcularProximoRecordatorio(LocalDate.now()));
            afiliacionPendienteRepository.save(afiliacion);
        } else {
            afiliacionPendienteRepository.findByLeadId(lead.getId())
                    .ifPresent(afiliacionPendienteRepository::delete);
        }
    }

    private LocalDate calcularProximoRecordatorio(LocalDate desde) {
        return desde.withDayOfMonth(1).plusMonths(1);
    }

    public LeadResponseDTO convertirADTO(Lead lead) {
        LeadResponseDTO dto = new LeadResponseDTO();
        dto.setId(lead.getId());
        dto.setNombre(lead.getNombre());
        dto.setTelefono(lead.getTelefono());
        dto.setDocumento(lead.getDocumento());
        dto.setCuil(lead.getCuil());
        dto.setEmail(lead.getEmail());
        dto.setNota(lead.getNota());
        dto.setOrigen(lead.getSource());
        dto.setEstado(lead.getEstado());
        dto.setUltimoContacto(lead.getUltimoContacto());
        dto.setVolverAContactar(lead.getVolverAContactar());
        dto.setFechaCarga(lead.getFechaCarga());
        dto.setVendedorNombre(lead.getVendedor().getNombre());
        dto.setCosto(lead.getCosto());
        dto.setGanancia(lead.getGanancia());
        dto.setRazonNoApto(lead.getRazonNoApto());
        if (lead.getEstado() == LeadState.PENDIENTE) {
            afiliacionPendienteRepository.findByLeadId(lead.getId())
                    .ifPresent(afiliacion -> {
                        dto.setFechaConfirmacion(afiliacion.getFechaConfirmacion());
                        dto.setProximoRecordatorio(afiliacion.getProximoRecordatorio());
                    });
        }
        return dto;
    }




    public List<LeadResponseDTO> leadsAContactarHoy() {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();
        return leadRepository
                .findByVendedorIdAndTenantIdAndVolverAContactar(
                        usuarioLogueado.getUserId(),
                        usuarioLogueado.getTenantId(),
                        LocalDate.now()
                )
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public LeadResponseDTO setVolverAContactar(Long leadId, LocalDate date) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        if (!lead.getVendedor().getId().equals(usuarioLogueado.getUserId())) {
            throw new ForbiddenException("El Lead no pertenece al vendedor con el email: " +
                    usuarioLogueado.getEmail());
        }
        lead.setVolverAContactar(date);
        leadRepository.save(lead);
        return convertirADTO(lead);

    }

    public void borrarVolverAContactar(Long leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        lead.setVolverAContactar(null);
        leadRepository.save(lead);
    }

    public LeadResponseDTO editarLead(Long leadId, LeadUpdateDTO request) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        if (!lead.getTenant().getId().equals(usuarioLogueado.getTenantId())) {
            throw new ForbiddenException("No tenés permiso para editar este lead");
        }

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuarioLogueado.getUserId());
        if (!idsPermitidos.contains(lead.getVendedor().getId())) {
            throw new ForbiddenException("No tenés permiso para editar este lead");
        }

        //sobreescribe los campos que llegaron con valor
        if (request.getNombre() != null) lead.setNombre(request.getNombre());
        if (request.getTelefono() != null) lead.setTelefono(request.getTelefono());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getDocumento() != null) lead.setDocumento(request.getDocumento());
        if (request.getCuil() != null) lead.setCuil(request.getCuil());
        if (request.getNota() != null) lead.setNota(request.getNota());;
        if (request.getCosto() != null) lead.setCosto(request.getCosto());
        if (request.getGanancia() != null) lead.setGanancia(request.getGanancia());
        if (request.getRazonNoApto() != null) lead.setRazonNoApto(request.getRazonNoApto());


        return convertirADTO(leadRepository.save(lead));
    }


    public LeadResponseDTO verLead(Long leadId) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        if (!lead.getTenant().getId().equals(usuarioLogueado.getTenantId())) {
            throw new ForbiddenException("No tenés permiso para ver este lead");
        }

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuarioLogueado.getUserId());

        if (!idsPermitidos.contains(lead.getVendedor().getId())) {
            throw new ForbiddenException("No tenés permiso para ver este lead");
        }

        return convertirADTO(lead);
    }
}
