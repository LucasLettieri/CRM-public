package com.crmVs.crm_vs.service.DemoSeeder;

import com.crmVs.crm_vs.model.*;
import com.crmVs.crm_vs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DemoSeeder {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final LeadRepository leadRepository;
    private final AfiliacionPendienteRepository afiliacionPendienteRepository;
    private final InteractionRepository interactionRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    private static final List<String> NOMBRES = List.of(
            "Martina", "Rodrigo", "Sofía", "Diego", "Valeria", "Emiliano", "Julián", "Camila",
            "Lucía", "Mateo", "Agustina", "Federico", "Bianca", "Nicolás", "Antonella", "Tomás",
            "Micaela", "Gonzalo", "Florencia", "Ignacio", "Paula", "Ezequiel", "Carolina", "Bruno"
    );

    private static final List<String> APELLIDOS = List.of(
            "Fernández", "Álvarez", "Ramírez", "Benítez", "Ortega", "Suárez", "Castro", "Morales",
            "Torres", "Gómez", "Díaz", "Romero", "Sosa", "Molina", "Herrera", "Acosta",
            "Aguirre", "Vega", "Cabrera", "Núñez", "Flores", "Rojas", "Medina", "Silva"
    );

    // Pool con pesos: más leads tempranos en el embudo, menos cerrados, como en la realidad
    private static final List<LeadState> POOL_ESTADOS = List.of(
            LeadState.NUEVO, LeadState.NUEVO, LeadState.NUEVO, LeadState.NUEVO,
            LeadState.EN_SEGUIMIENTO, LeadState.EN_SEGUIMIENTO, LeadState.EN_SEGUIMIENTO, LeadState.EN_SEGUIMIENTO,
            LeadState.APTO, LeadState.APTO, LeadState.APTO,
            LeadState.NO_APTO, LeadState.NO_APTO, LeadState.NO_APTO,
            LeadState.EN_TRAMITE, LeadState.EN_TRAMITE,
            LeadState.PENDIENTE, LeadState.PENDIENTE,
            LeadState.GANADO, LeadState.GANADO,
            LeadState.NO_INTERESADO
    );

    private static final List<String> TIPOS_INTERACCION = List.of("llamada", "whatsapp", "nota");

    // ===== Usuarios: se crean una sola vez, nunca se resetean =====

    @Transactional
    public void seedUsuariosSiHaceFalta() {
        if (userRepository.count() > 0) return;

        Tenant gerenciaNorte = new Tenant();
        gerenciaNorte.setNombre("Gerencia Norte");
        gerenciaNorte = tenantRepository.save(gerenciaNorte);

        User gerente1 = crearUsuario("gerente1@demo.com", "Gerente 1", "gerente", null, gerenciaNorte);
        userRepository.save(gerente1);

        User supervisor1 = crearUsuario("supervisor1@demo.com", "Supervisor 1", "supervisor", gerente1, gerenciaNorte);
        User supervisor2 = crearUsuario("supervisor2@demo.com", "Supervisor 2", "supervisor", gerente1, gerenciaNorte);
        userRepository.save(supervisor1);
        userRepository.save(supervisor2);

        User vendedor1 = crearUsuario("vendedor1@demo.com", "Vendedor 1", "vendedor", supervisor1, gerenciaNorte);
        User vendedor2 = crearUsuario("vendedor2@demo.com", "Vendedor 2", "vendedor", supervisor1, gerenciaNorte);
        User vendedor3 = crearUsuario("vendedor3@demo.com", "Vendedor 3", "vendedor", supervisor2, gerenciaNorte);
        User vendedor4 = crearUsuario("vendedor4@demo.com", "Vendedor 4", "vendedor", supervisor2, gerenciaNorte);
        userRepository.saveAll(List.of(vendedor1, vendedor2, vendedor3, vendedor4));

        Tenant gerenciaSur = new Tenant();
        gerenciaSur.setNombre("Gerencia Sur");
        gerenciaSur = tenantRepository.save(gerenciaSur);

        User gerenteSur = crearUsuario("gerente-sur@demo.com", "Gerente Sur", "gerente", null, gerenciaSur);
        userRepository.save(gerenteSur);

        User vendedorSur1 = crearUsuario("vendedor-sur1@demo.com", "Vendedor Sur 1", "vendedor", gerenteSur, gerenciaSur);
        User vendedorSur2 = crearUsuario("vendedor-sur2@demo.com", "Vendedor Sur 2", "vendedor", gerenteSur, gerenciaSur);
        userRepository.saveAll(List.of(vendedorSur1, vendedorSur2));
    }

    private User crearUsuario(String email, String nombre, String rol, User jefe, Tenant tenant) {
        User u = new User();
        u.setEmail(email);
        u.setNombre(nombre);
        u.setPasswordHash(passwordEncoder.encode("demo123"));
        u.setRol(rol);
        u.setJefe(jefe);
        u.setTenant(tenant);
        return u;
    }

    // ===== Leads: se borran y recrean en cada reset, para TODOS los usuarios =====

    @Transactional
    public void resetearLeadsDemo() {
        afiliacionPendienteRepository.deleteAll();
        interactionRepository.deleteAll();
        leadRepository.deleteAll();

        List<User> todosLosUsuarios = userRepository.findAll();
        if (todosLosUsuarios.size() < 10) return; // usuarios todavía no sembrados

        for (User usuario : todosLosUsuarios) {
            int cantidadLeads = 5 + random.nextInt(6); // entre 5 y 10
            generarLeadsParaUsuario(usuario, usuario.getTenant(), cantidadLeads);
        }
    }

    private void generarLeadsParaUsuario(User usuario, Tenant tenant, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            String nombreCompleto = NOMBRES.get(random.nextInt(NOMBRES.size()))
                    + " " + APELLIDOS.get(random.nextInt(APELLIDOS.size()));

            LeadState estado = POOL_ESTADOS.get(random.nextInt(POOL_ESTADOS.size()));
            Source source = Source.values()[random.nextInt(Source.values().length)];
            LocalDate fechaCarga = LocalDate.now().minusDays(random.nextInt(60));

            String documento = random.nextDouble() < 0.7
                    ? String.valueOf(20000000 + random.nextInt(19999999))
                    : null;
            String cuil = (documento != null && random.nextDouble() < 0.5)
                    ? (random.nextBoolean() ? "20-" : "27-") + documento + "-" + random.nextInt(10)
                    : null;

            Lead lead = new Lead();
            lead.setNombre(nombreCompleto);
            lead.setTelefono("11" + (20000000 + random.nextInt(9999999)));
            lead.setDocumento(documento);
            lead.setCuil(cuil);
            lead.setSource(source);
            lead.setEstado(estado);
            lead.setVendedor(usuario);
            lead.setTenant(tenant);
            lead.setFechaCarga(fechaCarga);
            lead.setUltimoContacto(fechaAleatoriaEntre(fechaCarga, LocalDate.now()));
            lead.setCosto(0.0);
            lead.setGanancia(0.0);

            if (estado == LeadState.NO_APTO) {
                lead.setRazonNoApto(RazonNoApto.values()[random.nextInt(RazonNoApto.values().length)]);
            }

            if (estado == LeadState.GANADO) {
                lead.setCosto(3000.0 + random.nextInt(5000));
                lead.setGanancia(20000.0 + random.nextInt(40000));
                LocalDate fechaConversion = fechaAleatoriaEntre(fechaCarga, LocalDate.now());
                lead.setFechaConversion(fechaConversion);
            }

            lead = leadRepository.save(lead);

            if (estado != LeadState.NUEVO) {
                int cantidadInteracciones = 1 + random.nextInt(3);
                for (int j = 0; j < cantidadInteracciones; j++) {
                    LocalDate fechaInteraccion = fechaAleatoriaEntre(fechaCarga, LocalDate.now());
                    crearInteraccion(lead, usuario, tenant,
                            TIPOS_INTERACCION.get(random.nextInt(TIPOS_INTERACCION.size())),
                            detalleParaEstado(estado), fechaInteraccion);
                }
            }

            if (estado == LeadState.PENDIENTE) {
                AfiliacionPendiente afiliacion = new AfiliacionPendiente();
                afiliacion.setLead(lead);
                afiliacion.setFechaConfirmacion(LocalDate.now().plusMonths(1 + random.nextInt(6)).withDayOfMonth(1));
                afiliacion.setProximoRecordatorio(LocalDate.now().withDayOfMonth(1).plusMonths(1));
                afiliacionPendienteRepository.save(afiliacion);
            }
        }
    }

    private LocalDate fechaAleatoriaEntre(LocalDate desde, LocalDate hasta) {
        long dias = ChronoUnit.DAYS.between(desde, hasta);
        if (dias <= 0) return desde;
        return desde.plusDays(random.nextInt((int) dias + 1));
    }

    private void crearInteraccion(Lead lead, User usuario, Tenant tenant, String tipo, String detalle, LocalDate fecha) {
        Interaction interaccion = new Interaction();
        interaccion.setLead(lead);
        interaccion.setUsuario(usuario);
        interaccion.setTenant(tenant);
        interaccion.setTipo(tipo);
        interaccion.setDetalle(detalle);
        interaccion.setFecha(fecha.atStartOfDay());
        interactionRepository.save(interaccion);
    }

    private String detalleParaEstado(LeadState estado) {
        return switch (estado) {
            case EN_SEGUIMIENTO -> "Contacto realizado, en seguimiento";
            case APTO -> "Cumple requisitos, avanzando en el proceso";
            case NO_APTO -> "No cumple con los requisitos actuales";
            case EN_TRAMITE -> "Documentación en revisión";
            case PENDIENTE -> "Afiliación provisoria en curso";
            case GANADO -> "Afiliación confirmada";
            case NO_INTERESADO -> "Cliente decidió no continuar";
            default -> "Seguimiento del lead";
        };
    }
}