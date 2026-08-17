package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.InteractionRequestDTO;
import com.crmVs.crm_vs.dto.InteractionResponseDTO;
import com.crmVs.crm_vs.model.Interaction;
import com.crmVs.crm_vs.model.Lead;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.NotFoundException;
import com.crmVs.crm_vs.repository.InteractionRepository;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public InteractionResponseDTO registrar(Long leadId, InteractionRequestDTO request) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        User usuario = userRepository.findById(usuarioLogueado.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Interaction interaction = new Interaction();
        interaction.setTipo(request.getTipo());
        interaction.setDetalle(request.getDetalle());
        interaction.setLead(lead);
        interaction.setUsuario(usuario);
        interaction.setTenant(lead.getTenant());

        lead.setUltimoContacto(LocalDate.now());

        return convertirADTO(interactionRepository.save(interaction));
    }

    public List<InteractionResponseDTO> listarPorLead(Long leadId) {
        UsuarioLogueado usuarioLogueado = UsuarioLogueado.obtener();

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead no encontrado"));

        if (!lead.getTenant().getId().equals(usuarioLogueado.getTenantId())) {
            throw new ForbiddenException("No tenés permiso para ver las interacciones de este lead");
        }

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuarioLogueado.getUserId());

        if (!idsPermitidos.contains(lead.getVendedor().getId())) {
            throw new ForbiddenException("No tenés permiso para ver las interacciones de este lead");
        }

        return interactionRepository.findByLeadId(leadId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private InteractionResponseDTO convertirADTO(Interaction interaction) {
        InteractionResponseDTO dto = new InteractionResponseDTO();
        dto.setId(interaction.getId());
        dto.setTipo(interaction.getTipo());
        dto.setDetalle(interaction.getDetalle());
        dto.setFecha(interaction.getFecha());
        dto.setUsuarioNombre(interaction.getUsuario().getNombre());
        dto.setLeadId(interaction.getLead().getId());
        return dto;
    }
}