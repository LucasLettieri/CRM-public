# CRM

CRM multi-tenant para gestión de leads y seguimiento comercial, desarrollado en Spring Boot. **En producción y en uso activo**.

> ⚠️ Este repositorio corresponde solo al **backend**. El frontend (React) vive en un repositorio aparte.


## El problema que resuelve

El sistema gestiona el ciclo de vida completo de un lead comercial, desde su carga hasta la conversión. Se encarga de recordarle al vendedor todos los leads que tiene que contactar en el día (configurado por el usuario), y de categorizar cada estado que puede tener el lead antes de convertirse.
A esto se suma un sistema de métricas y balance económico (costos, ganancias, tasas de conversión) segmentado por jerarquía de usuarios (vendedor → supervisor → gerente), un historial de interacciones por lead, y notificaciones automáticas por email.

La particularidad de este CRM es que deja que los superiores (regidos por roles) se puedan encargar de la adecuada gestión de contactos de cada subordinado, ver todo su historial de interacciones, cambios de estado,
ver y/o setear una fecha para volver a contactarlos y, en general, controlar el flujo de sus leads. Todo esto con fines de capacitación y optimización del flujo de trabajo de un equipo comercial.
De esta misma forma se pueden observar las métricas, permitiendo que un supervisor vea las suyas, las de todo su equipo y las de un subordinado en particular, y que al mismo tiempo un gerente vea las de los equipos
de sus supervisores y las de su gerencia completa (conjunto de supervisores).

Para simplificar el entendimiento del modelo de negocio dejo un árbol jerárquico con el cual poder orientarse:

<img width="1024" height="559" alt="image" src="https://github.com/user-attachments/assets/86fc6538-8fea-4905-8d5f-f7872e1757a8" />

Esto logra una aislación total de datos entre gerencias y, al mismo tiempo, permite el control del flujo de ventas en cada una de ellas, haciendo posible el acceso de cada equipo de la gerencia a los datos pertenecientes
al mismo, facilitando la correcta auditoría y capacitación de cada nivel de la empresa.

En resumen, este sistema permite a los supervisores y gerentes tomar decisiones sobre las estrategias de venta de sus equipos. Al tener los datos listos sobre el desempeño de su equipo en tiempo real,
el superior puede concentrarse en lo que realmente es importante y aporta gran valor a la empresa: pipelines, decisiones sobre el presupuesto de los leads (bajarlo, subirlo), cuándo felicitar a un vendedor y cuándo tomar acción para hacer crecer los números, etc.

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA / Hibernate
- **Base de datos:** PostgreSQL (Supabase), conexión directa vía JDBC
- **Auth:** JWT + `@PreAuthorize` a nivel de método
- **Validación:** Bean Validation (`spring-boot-starter-validation`)
- **Mail transaccional:** Resend (API HTTP)
- **Deploy:** Railway
- **Build:** Maven

## Arquitectura y decisiones técnicas

Algunos puntos del diseño que creo que vale la pena destacar:

### Multi-tenancy con seguridad en el backend
La aplicación es multi-tenant (varias organizaciones sobre el mismo esquema), y la seguridad vive enteramente en el backend: cada request valida `tenantId` y rol vía JWT y `@PreAuthorize`.

### Modelado de métricas: cohorte vs. evento
Un problema no trivial del dominio: ¿cuándo "cuenta" una conversión? Si el ciclo de venta es largo, mezclar en una sola métrica el criterio de *cuándo se cargó el lead* (cohorte) con el de *cuándo se cerró la venta* (evento) genera números que no tienen sentido de negocio. La solución fue separar explícitamente ambos criterios en el diseño de métricas y balance, en vez de forzar una métrica híbrida que llevaría a conclusiones erróneas. Tomé esta decisión porque en mi rubro es habitual que un lead cargado hoy se convierta recién varios meses después, y quería que las métricas reflejaran ese comportamiento real del ciclo de venta.

### Feature de "Pendientes": estado con entidad y scheduler propios
El estado intermedio de afiliación provisoria no es solo un valor de enum: tiene su propia entidad asociada (`AfiliacionPendiente`) que se crea y destruye según el lead entra o sale de ese estado, y un job diario (`@Scheduled`) que agrupa y notifica por mail a cada vendedor sus casos pendientes de seguimiento — un resumen consolidado, no un mail por lead.

### Períodos de reporte con soporte para consultas históricas
Los servicios de métricas y balance aceptan un período (semana, mes, cuatrimestre, histórico) y una fecha de referencia opcional, lo que permite reconsultar cualquier período pasado sin duplicar lógica de servicio ni romper el comportamiento por defecto.

### Manejo de errores centralizado
Excepciones de dominio propias (`BadRequestException`, `ForbiddenException`, `NotFoundException`, `UnauthorizedException`) resueltas por un `GlobalExceptionHandler`, en vez de devolver stack traces o códigos genéricos al cliente.

## Funcionalidades principales

- **Leads:** alta, edición, filtrado avanzado (`Specification` de JPA) y ordenamiento dinámico, con máquina de estados (embudo NUEVO → ... → GANADO, pasando por estados intermedios, con rama alternativa PENDIENTE).
- **Interacciones:** historial tipo timeline por lead (llamadas, WhatsApp, notas, cambios de estado).
- **Usuarios y jerarquía:** roles (vendedor, supervisor, gerente) con consulta de subordinados directos.
- **Multi-tenant:** soporte para múltiples organizaciones sobre el mismo esquema.
- **Métricas y balance:** por cohorte, por evento y por período configurable.
- **Notificaciones:** recordatorios diarios automáticos (leads del día y afiliaciones pendientes) vía email.

## Esquema de base de datos

<img width="1615" height="861" alt="supabase-schema-lvorprwfvtgjshfxbyje (1)" src="https://github.com/user-attachments/assets/4098750b-d9e6-4404-aa4c-7eabc992c22d" />


## Estructura del proyecto

```
src/main/java/com/crmVs/crm_vs/
├── config/          # Seguridad, JWT, CORS, manejo global de errores
├── controller/       # Endpoints REST
├── service/          # Lógica de negocio y schedulers
├── model/             # Entidades JPA
│   └── exception/      # Excepciones de dominio
├── dto/                # DTOs de entrada/salida
├── repository/         # Acceso a datos (Spring Data JPA)
└── Specification/      # Filtros y ordenamiento dinámico de leads
```

## Ejecución local

Requiere Java 21 y Maven (o el wrapper incluido).

```bash
./mvnw spring-boot:run
```

La aplicación espera las siguientes variables de entorno (ver `application.properties`): `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `SUPERADMIN_EMAIL`, `SUPERADMIN_PASSWORD`, `RESEND_API_KEY`.

## Entorno de pruebas en desarrollo...
