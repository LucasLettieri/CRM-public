package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.config.UsuarioLogueado;
import com.crmVs.crm_vs.dto.MetricsResponseDTO;
import com.crmVs.crm_vs.model.*;
import com.crmVs.crm_vs.model.exception.ForbiddenException;
import com.crmVs.crm_vs.model.exception.UnauthorizedException;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    private LocalDate calcularInicio(Periodo periodo, LocalDate referencia) {
        return switch (periodo) {
            case SEMANA -> calcularInicioSemana(referencia);
            case MES -> referencia.withDayOfMonth(1);
            case CUATRIMESTRE -> calcularInicioCuatrimestre(referencia);
            case HISTORICO -> LocalDate.of(2000, 1, 1);
        };
    }

    private LocalDate calcularFin(Periodo periodo, LocalDate referencia) {
        return switch (periodo) {
            case MES -> referencia.withDayOfMonth(referencia.lengthOfMonth());
            case CUATRIMESTRE -> calcularFinCuatrimestre(referencia);
            case SEMANA -> calcularFinSemana(referencia);
            case HISTORICO -> referencia;
        };
    }

    private LocalDate calcularInicioCuatrimestre(LocalDate referencia) {
        int mes = referencia.getMonthValue();
        int mesInicio = switch ((mes - 1) / 4) {
            case 0 -> 1;
            case 1 -> 5;
            default -> 9;
        };
        return LocalDate.of(referencia.getYear(), mesInicio, 1);
    }

    private LocalDate calcularFinCuatrimestre(LocalDate referencia) {
        LocalDate inicio = calcularInicioCuatrimestre(referencia);
        LocalDate ultimoMes = inicio.plusMonths(3);
        return ultimoMes.withDayOfMonth(ultimoMes.lengthOfMonth());
    }

    private LocalDate calcularInicioSemana(LocalDate referencia) {
        return referencia.with(java.time.DayOfWeek.MONDAY);
    }

    private LocalDate calcularFinSemana(LocalDate referencia) {
        return referencia.with(java.time.DayOfWeek.SUNDAY);
    }

    public MetricsResponseDTO getMisMetricas(Periodo periodo, LocalDate referencia) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        LocalDate inicio = calcularInicio(periodo, referencia);
        LocalDate fin = calcularFin(periodo, referencia);


        List<Lead> leads = leadRepository
                .findByVendedorIdAndTenantIdAndFechaCargaBetween(
                        usuario.getUserId(), usuario.getTenantId(), inicio, fin);

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdAndTenantIdAndEstadoAndFechaConversionBetween(
                        usuario.getUserId(), usuario.getTenantId(), LeadState.GANADO, inicio, fin);

        return construirMetricas(leads, leadsGanadosPeriodo);
    }

    public MetricsResponseDTO getMetricasEquipo(Periodo periodo, LocalDate referencia) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();
        Long tenantId = usuario.getTenantId();

        List<Long> ids = userRepository.findAllSubordinadosIds(usuario.getUserId());

        LocalDate inicio = calcularInicio(periodo, referencia);
        LocalDate fin = calcularFin(periodo, referencia);


        List<Lead> leads = leadRepository
                .findByVendedorIdInAndTenantIdAndFechaCargaBetween(
                        ids,
                        tenantId,
                        inicio,
                        fin
                );
        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionBetween(
                        ids, usuario.getTenantId(), LeadState.GANADO, inicio, fin);

        return construirMetricas(leads, leadsGanadosPeriodo);
    }

    public MetricsResponseDTO getMetricasEquipoDeSubordinado(Long subordinadoId, Periodo periodo, LocalDate referencia) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        List<Long> idsPermitidos = userRepository
                .findAllSubordinadosIds(usuario.getUserId());

        if (!idsPermitidos.contains(subordinadoId)) {
            throw new ForbiddenException(
                    "No tenés permiso para ver las métricas de este equipo"
            );
        }

        // Bajamos desde el subordinado, no desde el usuario logueado
        List<Long> idsEquipo = userRepository
                .findAllSubordinadosIds(subordinadoId);

        LocalDate inicio = calcularInicio(periodo, referencia);
        LocalDate fin = calcularFin(periodo, referencia);

        List<Lead> leads = leadRepository
                .findByVendedorIdInAndTenantIdAndFechaCargaBetween(
                        idsEquipo,
                        usuario.getTenantId(),
                        inicio,
                        fin
                );
        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdInAndTenantIdAndEstadoAndFechaConversionBetween(
                        idsEquipo,
                        usuario.getTenantId(),
                        LeadState.GANADO,
                        inicio,
                        fin
                );

        return construirMetricas(leads, leadsGanadosPeriodo);
    }

    public MetricsResponseDTO getMetricasSubordinado(Long subordinadoId, Periodo periodo, LocalDate referencia) {
        UsuarioLogueado usuario = UsuarioLogueado.obtener();

        // Verificar que el subordinado realmente está en su cadena jerárquica
        List<Long> idsPermitidos = userRepository.findAllSubordinadosIds(usuario.getUserId());
        if (!idsPermitidos.contains(subordinadoId)) {
            throw new UnauthorizedException("No tenés permiso para ver las métricas de este usuario");
        }

        LocalDate inicio = calcularInicio(periodo, referencia);
        LocalDate fin = calcularFin(periodo, referencia);

        List<Lead> leads = leadRepository
                .findByVendedorIdAndTenantIdAndFechaCargaBetween(
                        subordinadoId,
                        usuario.getTenantId(),
                        inicio,
                        fin
                );

        List<Lead> leadsGanadosPeriodo = leadRepository
                .findByVendedorIdAndTenantIdAndEstadoAndFechaConversionBetween(
                        usuario.getUserId(),
                        usuario.getTenantId(),
                        LeadState.GANADO,
                        inicio,
                        fin
                );
        return construirMetricas(leads, leadsGanadosPeriodo);
    }

    //Overloads
    public MetricsResponseDTO getMisMetricas(Periodo periodo) {
        return getMisMetricas(periodo, LocalDate.now());
    }

    public MetricsResponseDTO getMetricasEquipo(Periodo periodo) {
        return getMetricasEquipo(periodo, LocalDate.now());
    }

    public MetricsResponseDTO getMetricasEquipoDeSubordinado(Long subordinadoId, Periodo periodo) {
        return getMetricasEquipoDeSubordinado(subordinadoId, periodo, LocalDate.now());
    }

    public MetricsResponseDTO getMetricasSubordinado(Long subordinadoId, Periodo periodo) {
        return getMetricasSubordinado(subordinadoId, periodo, LocalDate.now());
    }


    // Método privado que construye las métricas a partir de una lista de leads
    // Lo separamos así porque getMisMetricas y getMetricasEquipo hacen lo mismo
    // con distintas listas, y no queremos repetir código
    private MetricsResponseDTO construirMetricas(List<Lead> leads, List<Lead> leadsGanadosPeriodo) {

        long total = leads.size();

        long ganados = leads.stream()
                .filter(l -> l.getEstado() == LeadState.GANADO)
                .count();

        long aptos = leads.stream()
                .filter(l -> l.getEstado() == LeadState.APTO
                        || l.getEstado() == LeadState.GANADO)
                .count();

        LocalDate hoy = LocalDate.now();
        long leadsCargadosHoy = leads.stream()
                .filter(l -> hoy.equals(l.getFechaCarga()))
                .count();

        double tasaTotal = total == 0
                ? 0
                : (double) ganados / total * 100;

        double tasaAptos = aptos == 0
                ? 0
                : (double) ganados / aptos * 100;


        return MetricsResponseDTO.builder()
                .totalLeads(total)
                .leadsHoy(leadsCargadosHoy)
                .porEstado(agruparPorEstado(leads))
                .porOrigen(agruparPorOrigen(leads))
                .tasaConversionTotal(tasaTotal)
                .tasaConversionAptos(tasaAptos)
                .razonesNoApto(agruparRazonesNoApto(leads))
                .conversionesPeriodo(leadsGanadosPeriodo.size())
                .build();
    }
    // Agrupa los leads por estado y cuenta cuántos hay de cada uno
    private Map<String, Long> agruparPorEstado(List<Lead> leads) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (LeadState estado : LeadState.values()) {
            long cantidad = leads.stream()
                    .filter(l -> l.getEstado() == estado)
                    .count();
            mapa.put(estado.name(), cantidad);
        }
        return mapa;
    }

    // Agrupa los leads por origen y cuenta cuántos hay de cada uno
    // Solo incluye los orígenes que tienen al menos 1 lead
    private Map<String, Long> agruparPorOrigen(List<Lead> leads) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Source origen : Source.values()) {
            long cantidad = leads.stream()
                    .filter(l -> l.getSource() == origen)
                    .count();
            if (cantidad > 0) {
                mapa.put(origen.name(), cantidad);
            }
        }
        return mapa;
    }

    private Map<String, Long> agruparRazonesNoApto(List<Lead> leads) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (RazonNoApto razon : RazonNoApto.values()) {
            long cantidad = leads.stream()
                    .filter(l -> l.getRazonNoApto() == razon)
                    .count();
            if (cantidad > 0) {
                mapa.put(razon.name(), cantidad);
            }
        }
        return mapa;
    }
}
    ///////////////                 Plata en general, ganda-invertida-total             ////////////////
    /////////////// Falta tasa de conversión total y tasa de conversion "en condiciones ///////////////
//todo por mes/semana, hay que hacer metodos generales y pasarles por parametro la cantidad de tiempo con el cual calcular

