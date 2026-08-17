package com.crmVs.crm_vs.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MetricsResponseDTO {
    private long totalLeads;
    private long leadsHoy;
    private Map<String, Long> porEstado;
    private Map<String, Long> porOrigen;
    private double tasaConversionTotal;
    private double tasaConversionAptos;
    private Map<String, Long> razonesNoApto;
    private long conversionesPeriodo;

}