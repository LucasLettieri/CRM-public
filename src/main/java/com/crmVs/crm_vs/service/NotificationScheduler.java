package com.crmVs.crm_vs.service;

import com.crmVs.crm_vs.model.Lead;
import com.crmVs.crm_vs.model.User;
import com.crmVs.crm_vs.repository.LeadRepository;
import com.crmVs.crm_vs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;


@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {

        List<User> allUsers = userRepository.findAll();
        LocalDate hoy = LocalDate.now();

        for (User user : allUsers) {

            List<Lead> leadsHoy = leadRepository
                    .findByVendedorIdAndTenantIdAndVolverAContactar(
                            user.getId(),
                            user.getTenant().getId(),
                            hoy
                    );

            List<Lead> leadsVencidos = leadRepository
                    .findByVendedorIdAndTenantIdAndVolverAContactarLessThan(
                            user.getId(),
                            user.getTenant().getId(),
                            hoy
                    );

            if (leadsHoy.isEmpty() && leadsVencidos.isEmpty()) continue;

            mailService.sendDailyReminder(
                    user.getEmail(),
                    user.getNombre(),
                    leadsHoy.size(),
                    leadsVencidos.size()
            );
        }
    }
}