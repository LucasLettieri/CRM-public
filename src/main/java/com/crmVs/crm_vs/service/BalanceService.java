package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.BalanceResponseDTO;
import com.crmVs.crm_vs.model.Lead;
import com.crmVs.crm_vs.model.LeadState;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.UnauthorizedException;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    private LocalDate inicioMesActual() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public BalanceResponseDTO getMiBalance() {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        LocalDate inicio = inicioMesActual();

        List<Lead> leads = leadRepository
                .findByVendedorIdAndTenantIdAndFechaCargaGreaterThanEqual(
                        usuario.getUserId(), usuario.getTenantId(), inicio);

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(
                        usuario.getUserId(), usuario.getTenantId(), LeadState.GANADO, inicio);

        return construirBalance(leads, leadsGanadosPeriodo);
    }

    public BalanceResponseDTO getBalanceEquipo() {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        List<Long> ids = userRepository.findAllSubordinadosIds(usuario.getUserId());
        LocalDate inicio = inicioMesActual();

        List<Lead> leads = leadRepository
                .findByVendedorIdInAndTenantIdAndFechaCargaGreaterThanEqual(
                        ids, usuario.getTenantId(), inicio);

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(
                        ids, usuario.getTenantId(), LeadState.GANADO, inicio);

        return construirBalance(leads, leadsGanadosPeriodo);
    }

    public BalanceResponseDTO getBalanceEquipoDeSubordinado(Long subordinadoId) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        List<Long> idsPermitidos = userRepository.findAllSubordinadosIds(usuario.getUserId());

        if (!idsPermitidos.contains(subordinadoId)) {
            throw new ForbiddenException("No tenés permiso para ver el balance de este equipo");
        }

        List<Long> ids = userRepository.findAllSubordinadosIds(subordinadoId);
        LocalDate inicio = inicioMesActual();

        List<Lead> leads = leadRepository
                .findByVendedorIdInAndTenantIdAndFechaCargaGreaterThanEqual(
                        ids, usuario.getTenantId(), inicio);

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(
                        ids, usuario.getTenantId(), LeadState.GANADO, inicio);

        return construirBalance(leads, leadsGanadosPeriodo);
    }

    public BalanceResponseDTO getBalanceSubordinado(Long subordinadoId) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        List<Long> idsPermitidos = userRepository.findAllSubordinadosIds(usuario.getUserId());

        if (!idsPermitidos.contains(subordinadoId)) {
            throw new UnauthorizedException("No tenés permiso para ver el balance de este usuario");
        }

        LocalDate inicio = inicioMesActual();

        // Nota: dejo el mismo comportamiento que ya tenías (usuario.getUserId()), sin tocar el bug aparte
        List<Lead> leads = leadRepository
                .findByVendedorIdAndTenantIdAndFechaCargaGreaterThanEqual(
                        subordinadoId, usuario.getTenantId(), inicio);

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdAndTenantIdAndEstadoAndFechaConversionGreaterThanEqual(
                        subordinadoId, usuario.getTenantId(), LeadState.GANADO, inicio);

        return construirBalance(leads, leadsGanadosPeriodo);
    }

    private BalanceResponseDTO construirBalance(List<Lead> leads, List<Lead> leadsGanadosPeriodo) {
        double costoTotal = leads.stream()
                .mapToDouble(l -> l.getCosto() != null ? l.getCosto() : 0.0)
                .sum();

        double gananciaTotal = leadsGanadosPeriodo.stream()
                .mapToDouble(l -> l.getGanancia() != null ? l.getGanancia() : 0.0)
                .sum();

        long leadsConCosto = leads.stream()
                .filter(l -> l.getCosto() != null && l.getCosto() > 0)
                .count();

        long leadsConGanancia = leadsGanadosPeriodo.stream()
                .filter(l -> l.getGanancia() != null && l.getGanancia() > 0)
                .count();

        double gananciaPromedio = leadsConGanancia == 0 ? 0
                : gananciaTotal / leadsConGanancia;

        return BalanceResponseDTO.builder()
                .costoTotal(costoTotal)
                .gananciaTotal(gananciaTotal)
                .gananciaPromedioPorLead(gananciaPromedio)
                .leadsConCosto(leadsConCosto)
                .leadsConGanancia(leadsConGanancia)
                .build();
    }
}