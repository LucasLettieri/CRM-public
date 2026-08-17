package com.crmVs.crm_vs.service.DemoSeeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoStartupRunner implements CommandLineRunner {

    private final DemoSeeder demoSeeder;

    @Override
    public void run(String... args) {
        demoSeeder.seedUsuariosSiHaceFalta();
        demoSeeder.resetearLeadsDemo();
    }
}
