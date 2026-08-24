package com.crmVs.crm_vs.model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import lombok.Data;
@Entity
@Data
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String rol; // "vendedor", "supervisor", "gerente"

    // Relación con el jefe directo (el árbol jerárquico)
    @ManyToOne
    @JoinColumn(name = "jefe_id")
    private User jefe;

    // Relación con el tenant al que pertenece
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}
