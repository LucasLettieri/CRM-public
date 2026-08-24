package com.crmVs.crm_vs.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta = LocalDateTime.now();


}
