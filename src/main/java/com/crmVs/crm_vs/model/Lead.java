package com.crmVs.crm_vs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;
    private String email;
    private String documento;
    private String cuil;
    private String nota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadState estado = LeadState.NUEVO;

    @Column(name = "fecha_conversion")
    private LocalDate fechaConversion;

    @Column(name = "ultimo_contacto")
    private LocalDate ultimoContacto = LocalDate.now();

    @Column(name = "volver_a_contactar")
    private LocalDate volverAContactar;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDate fechaCarga = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private User vendedor;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "ganancia", nullable = false)
    private Double ganancia = 0.0;

    @Column(name = "costo", nullable = false)
    private Double costo = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "razon_no_apto")
    private RazonNoApto razonNoApto; // solo se llena cuando estado = NO_APTO

    // Setter personalizado con validación
    public void setVolverAContactar(LocalDate volverAContactar) {
        if (volverAContactar != null && volverAContactar.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No puedes agendar un contacto en el pasado");
        }
        this.volverAContactar = volverAContactar;
    }
} //Volver a contactar, costo y ganancia, fecha de carga