# CLAUDE.md — Buildrr Feedback

Tracker de bugs/funciones nuevas para el ecosistema Buildrr. Convenciones para
agentes IA trabajando en este repo.

## Principio rector

**Simplicidad sobre completitud.** Tres actores fijos (Pablo cliente SGO, la
dueña de FrezCo cliente, Gino admin), un ciclo de vida de 4 estados, sin
contratos ni facturación. Ante dos soluciones que cumplen el requisito, elegir
la más simple. Ver [docs/01-alcance.md](docs/01-alcance.md) para lo que está
explícitamente afuera antes de agregar nada.

## Leer antes de escribir código

1. [docs/00-arquitectura.md](docs/00-arquitectura.md) — cómo se conecta con el
   api-gateway compartido de Buildr (`buildr-platform`). No proponer un
   service discovery (Eureka/Consul): las rutas del gateway son estáticas
   a propósito, ya se evaluó.
2. [docs/01-alcance.md](docs/01-alcance.md)
3. [docs/02-modelo-datos.md](docs/02-modelo-datos.md)
4. [docs/03-ciclo-de-vida.md](docs/03-ciclo-de-vida.md) — transiciones válidas de estado

## Stack

Spring Boot 3.3.5 (Java 17) · Spring Data JPA · SQL Server · Flyway · Angular 19
standalone + PrimeNG v19 · Docker Compose. Adjuntos van al `documentos-service`
compartido de SGO (sin MinIO ni bucket propio acá — ver "Adjuntos" abajo).

## Auth

Sin tabla de usuarios propia y **sin login ni validación de JWT acá**. El
frontend loguea directo contra el api-gateway compartido de Buildr
(`buildr-platform`), que valida el JWT e inyecta headers de identidad
(`X-User-Id`, `X-Username`, `X-User-Rol`, `X-Organizacion-Id`).
`GatewayAuthFilter` (`auth/`) solo lee esos headers — nunca ve JWT ni
contraseña. Ver [docs/00-arquitectura.md](docs/00-arquitectura.md) antes de
tocar `auth/` o las rutas del gateway.

No reintroducir validación de JWT propia (secret, jjwt como dependencia,
firmar tokens) — eso es responsabilidad del gateway, no de este backend.
Tampoco un Mediator/AuthColleague por "fuente de identidad": Pablo (SGO) y la
dueña de FrezCo son cuentas normales en la misma base de auth compartida, no
dos sistemas distintos que rutear.

## Patrones aplicados (no agregar otros sin necesidad concreta)

- **Factory Method** (`ticket/factory/`) — un `TicketFactory` por `TipoTicket`
  (BUG, FUNCION_NUEVA), cada uno con sus propias reglas de armado/validación.
  Agregar un tipo de ticket es agregar una clase + registrarla en el enum, no
  tocar `TicketServiceImpl`.
- **State** (`ticket/estado/`) — un `EstadoTicket` por estado del ciclo de
  vida, cada uno declara a qué estados puede moverse. `EstadoTicketResolver`
  valida transiciones sin switch. Agregar un estado nuevo (si algún día hace
  falta "cancelado") es agregar una clase `@Component`, no tocar el service.
- **Factory Method** (`usuarioaplicacion/factory/`) — mismo patrón que el de
  tickets, pero por `RolAplicacion` (CLIENTE/ADMIN) para el alta de accesos
  (`POST /api/admin/usuarios-aplicacion`). No es un patrón nuevo, es el mismo
  Factory Method aplicado a otra jerarquía.

## Adjuntos — documentos-service compartido

Sin storage propio: los adjuntos se suben al `documentos-service` de SGO
(`services.documentos.url`, config `documentos.service.url` acá) como
`producto=BUILDRR_FEEDBACK`, `tipo_asociado=ticket`, `id_asociado={ticketId}`.
Del lado de `documentos-service` eso se resuelve con un **Strategy**
(`strategy/BuildrrFeedbackDocumentoStrategy`, junto a `SgoDocumentoStrategy` y
`FrezcoDocumentoStrategy`) que arma el prefijo de carpeta y valida acceso por
producto — agregar un producto nuevo ahí es agregar una clase, no tocar
`DocumentoService`. `Adjunto.url` guarda el `id_documento` remoto, no un
object key propio. La descarga sigue siendo un proxy de este backend
(`GET /api/adjuntos/{id}/descargar`) que a su vez pega a
`documentos-service/{id}/view` — nunca una URL directa a MinIO desde el
navegador. Ver [docs/02-modelo-datos.md](docs/02-modelo-datos.md).

## Convenciones (mismo estilo que sistema-gestion-obras y frezco)

- Entidades: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`,
  nunca `@Data` (lazy loading de Hibernate).
- DTOs: split Request/Response, `@JsonInclude(NON_NULL)` en Response.
- `ddl-auto: none` siempre — el esquema lo maneja Flyway
  (`backend/src/main/resources/db/migration/V{n}__descripcion.sql`, sintaxis SQL
  Server: `IDENTITY(1,1)`, `BIT`, `NVARCHAR(MAX)`, `DATETIME2`, `DECIMAL(14,2)`).
- Dinero/horas: `BigDecimal`/`DECIMAL`, nunca `double`/`float`.
- Nomenclatura de dominio y comentarios en español.
- Angular: componentes `standalone: true`, imports explícitos, Reactive Forms en
  create/edit, modelos en `core/models/models.ts`.

## Qué NO hacer

- No armar API gateway ni service discovery propio (ver docs/00-arquitectura.md).
- No agregar roles/permisos más allá de admin vs. cliente implícito en el JWT.
- No agregar notificaciones automáticas, chat/hilos de comentarios, ni
  facturación/contratos — fuera de alcance (docs/01-alcance.md).
- No usar librerías de generación de PDF si en algún momento hace falta un
  resumen imprimible — HTML con `@media print`, mismo criterio que FrezCo.

## Comandos

```bash
# Backend
cd backend && ./mvnw spring-boot:run
cd backend && ./mvnw test

# Frontend
cd frontend && npm start
cd frontend && npm run build

# Todo junto
docker compose up --build
```
