package com.crmVs.crm_vs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data

public class AfiliacionPendienteDTO {

    private LeadResponseDTO lead;

    private LocalDate fechaConfirmacion;

    private LocalDate proximoRecordatorio;


}
