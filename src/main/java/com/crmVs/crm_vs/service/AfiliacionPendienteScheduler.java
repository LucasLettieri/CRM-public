package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.model.AfiliacionPendiente;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.repository.AfiliacionPendienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AfiliacionPendienteScheduler {

    private final AfiliacionPendienteRepository afiliacionPendienteRepository;
    private final MailService mailService;

    @Scheduled(cron = "0 0 8 * * *") // todos los días a las 8am
    public void enviarRecordatoriosPendientes() {
        LocalDate hoy = LocalDate.now();

        List<AfiliacionPendiente> pendientes = afiliacionPendienteRepository
                .findByProximoRecordatorioLessThanEqual(hoy);

        if (pendientes.isEmpty()) {
            return;
        }

        Map<User, List<AfiliacionPendiente>> porVendedor = pendientes.stream()
                .collect(Collectors.groupingBy(a -> a.getLead().getVendedor()));

        for (Map.Entry<User, List<AfiliacionPendiente>> entry : porVendedor.entrySet()) {
            User vendedor = entry.getKey();
            List<AfiliacionPendiente> afiliacionesDelVendedor = entry.getValue();

            mailService.sendRecordatorioPendientes(
                    vendedor.getEmail(),
                    vendedor.getNombre(),
                    afiliacionesDelVendedor.size()
            );
        }

        for (AfiliacionPendiente afiliacion : pendientes) {
            afiliacion.setProximoRecordatorio(afiliacion.getProximoRecordatorio().plusMonths(1));
        }
        afiliacionPendienteRepository.saveAll(pendientes);
    }
}