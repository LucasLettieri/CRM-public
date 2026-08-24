package com.crmVs.crm_vs.service.DemoSeeder;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoResetScheduler {

    private final DemoSeeder demoSeeder;

    @Scheduled(cron = "0 */15 * * * *")
    public void resetear() {
        demoSeeder.resetearLeadsDemo();
    }
}