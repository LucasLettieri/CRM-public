package com.crmVs.crm_vs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MailService {

    @Value("${crm.base-url}")
    private String crmBaseUrl;

    @Value("${resend.api-key}")
    private String resendApiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    private void enviarMail(String to, String subject, String texto) {
        restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", "onboarding@resend.dev",
                        "to", to,
                        "subject", subject,
                        "text", texto
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void sendDailyReminder(String toEmail, String sellerName, int leadsHoy, int leadsVencidos) {
        StringBuilder texto = new StringBuilder();
        texto.append("Buenos días ").append(sellerName).append("!\n\n");

        if (leadsHoy > 0) {
            texto.append("Tenés ").append(leadsHoy)
                    .append(leadsHoy == 1 ? " lead" : " leads")
                    .append(" para contactar hoy:\n")
                    .append(crmBaseUrl).append("/leads/hoy\n\n");
        }

        if (leadsVencidos > 0) {
            texto.append("Tenés ").append(leadsVencidos)
                    .append(leadsVencidos == 1 ? " lead" : " leads")
                    .append(" con contacto vencido:\n")
                    .append(crmBaseUrl).append("/leads/vencidos\n\n");
        }

        texto.append("Que tengas un excelente día!");

        enviarMail(toEmail, "📋 Recordatorio diario de leads", texto.toString());
    }

    public void sendRecordatorioPendientes(String toEmail, String sellerName, int cantidadPendientes) {
        StringBuilder texto = new StringBuilder();
        texto.append("Hola ").append(sellerName).append("!\n\n");
        texto.append("Tenés ").append(cantidadPendientes)
                .append(cantidadPendientes == 1 ? " afiliación pendiente" : " afiliaciones pendientes")
                .append(" que requieren seguimiento:\n\n")
                .append(crmBaseUrl).append("/pendientes").append("\n\n")
                .append("Recordá mantener el contacto regular con estos clientes hasta que se confirmen.\n\n")
                .append("Saludos!");

        enviarMail(toEmail, "⏳ Recordatorio de afiliaciones pendientes", texto.toString());
    }
}