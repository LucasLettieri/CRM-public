package com.crmVs.crm_vs.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponseDTO {
    private double costoTotal;
    private double gananciaTotal;
    private double gananciaPromedioPorLead;
    private long leadsConCosto;       // cuántos leads tienen costo > 0
    private long leadsConGanancia;    // cuántos leads tienen ganancia > 0
}