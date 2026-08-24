package com.crmVs.crm_vs.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class AfiliacionPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "lead_id", nullable = false, unique = true)
    private Lead lead;

    @Column(name = "fecha_confirmacion", nullable = false)
    private LocalDate fechaConfirmacion;

    @Column(name = "proximo_recordatorio")
    private LocalDate proximoRecordatorio;
}