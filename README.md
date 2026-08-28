# CRM Multi-tenant con Jerarquías de Supervisión (Backend)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-blue)
![Estado](https://img.shields.io/badge/estado-en%20producción-success)

CRM multi-tenant para gestión de leads y seguimiento comercial, desarrollado en Spring Boot. **En producción y en uso activo por un equipo de ventas entero.**

> ⚠️ Este repositorio corresponde solo al **backend**. El frontend (React) vive en un repositorio aparte.

## El problema que resuelve

El sistema gestiona el ciclo de vida completo de un lead comercial, desde su carga hasta la conversión. Le recuerda al vendedor todos los leads que tiene que contactar en el día (configurado por el propio usuario) y categoriza cada estado posible antes de la conversión.

A esto se suma un sistema de métricas y balance económico (costos, ganancias, tasas de conversión) segmentado por jerarquía de usuarios (vendedor → supervisor → gerente), un historial de interacciones por lead, y notificaciones automáticas por email.

La particularidad de este CRM es que deja que los superiores, regidos por roles, se encarguen de la gestión de contactos de cada subordinado: pueden ver todo su historial de interacciones y cambios de estado, ver y/o setear una fecha para volver a contactar a un lead, y en general controlar el flujo de sus leads. Todo esto con fines de capacitación y optimización del flujo de trabajo de un equipo comercial.

De la misma forma se pueden observar las métricas: un supervisor ve las suyas, las de todo su equipo y las de un subordinado en particular; un gerente ve las de los equipos de sus supervisores y las de su gerencia completa (el conjunto de supervisores).

Para simplificar el entendimiento del modelo de negocio, este es el árbol jerárquico:

<img width="1024" height="559" alt="Jerarquía de roles: gerente, supervisores a cargo y vendedores por supervisor" src="https://github.com/user-attachments/assets/86fc6538-8fea-4905-8d5f-f7872e1757a8" />

Esto logra una aislación total de datos entre gerencias y, al mismo tiempo, permite el control del flujo de ventas en cada una de ellas, haciendo posible que cada equipo de la gerencia acceda a los datos que le pertenecen, facilitando la correcta auditoría y capacitación en cada nivel de la empresa.

En resumen, este sistema permite a los supervisores y gerentes tomar decisiones sobre las estrategias de venta de sus equipos. Al tener los datos de desempeño en tiempo real, el superior puede concentrarse en lo que realmente aporta valor: pipelines, decisiones sobre el presupuesto de los leads (bajarlo, subirlo), cuándo felicitar a un vendedor y cuándo tomar acción para hacer crecer los números.

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA / Hibernate
- **Base de datos:** PostgreSQL (Supabase), conexión directa vía JDBC
- **Auth:** JWT + `@PreAuthorize` a nivel de método
- **Validación:** Bean Validation (`spring-boot-starter-validation`)
- **Mail transaccional:** Resend (API HTTP)
- **Deploy:** Railway
- **Build:** Maven

## Arquitectura y decisiones técnicas

Algunos puntos del diseño que vale la pena destacar:

### Multi-tenancy con seguridad en el backend

La aplicación es multi-tenant (varias organizaciones sobre el mismo esquema), y la seguridad vive enteramente en el backend: cada request valida `tenantId` y rol vía JWT y `@PreAuthorize`. El controller delega en el service, que arma la consulta ya acotada por tenant y por los subordinados que el usuario logueado tiene permitido ver:

```java
@PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE')")
@GetMapping("/equipo")
public List<LeadResponseDTO> listarLeadsEquipo(@ModelAttribute LeadFiltroDTO filtro) {
    return leadService.listarLeadsEquipo(filtro);
}

public List<LeadResponseDTO> listarLeadsEquipo(LeadFiltroDTO filtro) {
    UsuarioLogueado usuario = UsuarioLogueado.obtener();
    List<Long> idsPermitidos = userRepository
            .findAllSubordinadosIds(usuario.getUserId());
    Specification<Lead> spec = LeadSpecification.crear(
            filtro,
            usuario.getTenantId(), //Siempre se pide tenantId
            idsPermitidos
    );
    Sort sort = LeadSort.crear(filtro);
    return leadRepository.findAll(spec, sort)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
}
```

### Modelado de métricas: cohorte vs. evento

Un problema no trivial del dominio: ¿cuándo "cuenta" una conversión? Si el ciclo de venta es largo, mezclar en una sola métrica el criterio de *cuándo se cargó el lead* (cohorte) con el de *cuándo se cerró la venta* (evento) genera números que no tienen sentido de negocio. La solución fue separar explícitamente ambos criterios en el diseño de métricas y balance, en vez de forzar una métrica híbrida que llevaría a conclusiones erróneas. Tomé esta decisión porque en mi rubro es habitual que un lead cargado hoy se convierta recién varios meses después, y quería que las métricas reflejaran ese comportamiento real del ciclo de venta.

### Estado "Pendiente" (afiliación provisoria): entidad y scheduler propios

El estado intermedio de afiliación provisoria no es solo un valor de enum: tiene su propia entidad asociada (`AfiliacionPendiente`) que se crea y se destruye según el lead entra o sale de ese estado, y un job diario (`@Scheduled`) que agrupa y notifica por mail a cada vendedor sus casos pendientes de seguimiento — un resumen consolidado, no un mail por lead.

```java
@Scheduled(cron = "0 0 8 * * *") // todos los días a las 8am
public void enviarRecordatoriosPendientes() {
    LocalDate hoy = LocalDate.now();
    List<AfiliacionPendiente> pendientes = afiliacionPendienteRepository
            .findByProximoRecordatorioLessThanEqual(hoy);
    if (pendientes.isEmpty()) {
        return;
    }
    Map<User, List<AfiliacionPendiente>> porVendedor = pendientes.stream()
            .collect(Collectors.groupingBy(a -> a.getLead().getVendedor()));
    for (Map.Entry<User, List<AfiliacionPendiente>> entry : porVendedor.entrySet()) {
        User vendedor = entry.getKey();
        List<AfiliacionPendiente> afiliacionesDelVendedor = entry.getValue();
        mailService.sendRecordatorioPendientes(
                vendedor.getEmail(),
                vendedor.getNombre(),
                afiliacionesDelVendedor.size()
        );
    }
    for (AfiliacionPendiente afiliacion : pendientes) {
        afiliacion.setProximoRecordatorio(afiliacion.getProximoRecordatorio().plusMonths(1));
    }
    afiliacionPendienteRepository.saveAll(pendientes);
}
```

Nota: la API key de Resend la tengo reservada para mi versión en uso; por lo tanto, los correos están deshabilitados en esta versión demo.

### Períodos de reporte con soporte para consultas históricas

Los servicios de métricas y balance aceptan un período (semana, mes, cuatrimestre, histórico) y una fecha de referencia opcional, lo que permite reconsultar cualquier período pasado sin duplicar lógica de servicio ni romper el comportamiento por defecto.

### Manejo de errores centralizado

Excepciones de dominio propias (`BadRequestException`, `ForbiddenException`, `NotFoundException`, `UnauthorizedException`) resueltas por un `GlobalExceptionHandler`, en vez de devolver stack traces o códigos genéricos al cliente.

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> manejarValidacion(
        MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
        String campo = ((FieldError) error).getField();
        String mensaje = error.getDefaultMessage();
        errores.put(campo, mensaje);
    });
    return buildResponse(HttpStatus.BAD_REQUEST, errores);
}

private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, Object error) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", status.value());
    response.put("error", error);
    response.put("timestamp", LocalDateTime.now());
    return ResponseEntity.status(status).body(response);
}
```

## Funcionalidades principales

- **Leads:** alta, edición, filtrado avanzado (`Specification` de JPA), ordenamiento dinámico con máquina de estados (embudo NUEVO → ... → GANADO, pasando por estados intermedios, con rama alternativa PENDIENTE),
  configuración de recordatorios vía email, mediante el campo volverAContacar (seteable en el detalle de lead).
- **Interacciones:** historial tipo timeline por lead (llamadas, WhatsApp, notas, cambios de estado).
- **Usuarios y jerarquía:** roles (vendedor, supervisor, gerente) con consulta de subordinados directos.
- **Multi-tenant:** soporte para múltiples organizaciones sobre el mismo esquema.
- **Métricas y balance:** por cohorte, por evento y por período configurable.
- **Notificaciones:** recordatorios diarios automáticos (leads del día y afiliaciones pendientes) vía email.

## Capturas

**Data table de leads**

<img width="1673" height="442" alt="Tabla de datos de leads" src="https://github.com/user-attachments/assets/be5c9829-bf6c-40aa-8033-3acd4e8a1605" />

<br/><br/>

**Métricas por cohorte y por evento**

<img width="1663" height="940" alt="Métricas filtradas por cohorte y por evento, con selector de período" src="https://github.com/user-attachments/assets/3058f374-7e8a-4203-a858-fa6ef65dc5a3" />

<br/><br/>

**Vista de equipo (supervisor/gerente)**

<img width="1652" height="949" alt="Un gerente viendo el historial y estado de los leads de su equipo" src="https://github.com/user-attachments/assets/4bdedd7d-fed4-4a61-8cd6-3042233969ee" />

<br/><br/>

**Detalle y timeline de interacciones de un lead**

<img width="494" height="945" alt="Historial tipo timeline de interacciones de un lead" src="https://github.com/user-attachments/assets/118945c8-9a48-4f4c-983b-65a35621fa75" />

<br/><br/>

**Ordenamiento, búsqueda por varios campos a la vez y filtrado avanzado de leads**

<img width="1670" height="533" alt="Ordenamiento, búsqueda multicampo y filtrado avanzado de leads" src="https://github.com/user-attachments/assets/c3ddf580-64c7-40f2-b8ad-05f392ebc918" />

## Esquema de base de datos

<img width="1615" height="861" alt="Esquema de la base de datos en Supabase: tablas de leads, interacciones, usuarios y afiliaciones pendientes" src="https://github.com/user-attachments/assets/4098750b-d9e6-4404-aa4c-7eabc992c22d" />

## Estructura del proyecto

```
src/main/java/com/crmVs/crm_vs/
├── config/            # Seguridad, JWT, CORS, manejo global de errores
├── controller/        # Endpoints REST
├── service/           # Lógica de negocio y schedulers
├── model/             # Entidades JPA
│   └── exception/     # Excepciones de dominio
├── dto/                # DTOs de entrada/salida
├── repository/         # Acceso a datos (Spring Data JPA)
└── Specification/       # Filtros y ordenamiento dinámico de leads
```

## Ejecución local

Requiere Java 21 y Maven (o el wrapper incluido).

```bash
./mvnw spring-boot:run
```

La aplicación espera las siguientes variables de entorno (ver `application.properties`): `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `SUPERADMIN_EMAIL`, `SUPERADMIN_PASSWORD`, `RESEND_API_KEY`.

---

## 🚀 Entorno de prueba interactivo

> [!TIP]
> Este proyecto está **desplegado y en producción**. Podés probarlo funcionando en vivo ahora mismo, con datos de ejemplo ya cargados — no hace falta clonar el repo ni levantar nada localmente.

[![Probar demo en vivo](https://img.shields.io/badge/🔗_Probar_demo_en_vivo-crmdemoproject.vercel.app-2ea44f?style=for-the-badge)](https://crmdemoproject.vercel.app)

### Multi-tenancy: separación por gerencia

El sistema soporta separar la operación por gerencia dentro de la misma empresa — cada gerencia tiene su propia jerarquía de supervisores y vendedores, y su propia cartera de leads, sin visibilidad cruzada entre gerencias.

Probá loguearte con un usuario de cada gerencia y vas a ver que los leads, el equipo y las métricas quedan completamente aislados entre sí, aunque corran sobre la misma base de datos.

### Credenciales de prueba

**Gerencia Norte**

| Rol | Email | Contraseña |
|---|---|---|
| Gerente | `gerente1@demo.com` | `demo123` |
| Supervisor | `supervisor1@demo.com` | `demo123` |
| Supervisor | `supervisor2@demo.com` | `demo123` |
| Vendedor | `vendedor1@demo.com` | `demo123` |
| Vendedor | `vendedor2@demo.com` | `demo123` |
| Vendedor | `vendedor3@demo.com` | `demo123` |
| Vendedor | `vendedor4@demo.com` | `demo123` |

**Gerencia Sur**

| Rol | Email | Contraseña |
|---|---|---|
| Gerente | `gerente-sur@demo.com` | `demo123` |
| Vendedor | `vendedor-sur1@demo.com` | `demo123` |
| Vendedor | `vendedor-sur2@demo.com` | `demo123` |

### Qué probar

- Iniciá sesión como **gerente** o **supervisor** para ver las métricas y el balance de todo el equipo (no solo lo propio).
- Iniciá sesión como **vendedor** para ver el flujo de carga y seguimiento de leads día a día.
- Cambiá el estado de un lead a **Pendiente** y mirá cómo se le pide la fecha de confirmación de la afiliación — es el circuito de afiliación provisoria descripto más arriba en "Estado Pendiente".
- Cambiá el estado de un lead a "Ganado" y observá cómo se registra en las métricas.
- Creá un nuevo lead personalizado; una vez creado, editá sus campos, seteá una nueva interacción y una fecha para volver a contactar, y observá los cambios reflejados en el sistema.
- Cambiá el costo y la ganancia de un lead existente y miralo reflejado en **Balance**.
- Mirá **Métricas** y **Balance** con distintos períodos (semana, mes, cuatrimestre) para ver el filtrado por cohorte vs. por evento.

### Nota sobre los datos

**Los leads y usuarios visibles son datos ficticios generados automáticamente. El entorno se resetea cada 15 minutos a un set de datos de ejemplo — no se guarda nada de lo que cargues durante la prueba.**
