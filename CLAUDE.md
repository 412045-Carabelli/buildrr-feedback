# CLAUDE.md — Buildrr Feedback

Tracker de bugs/funciones nuevas para el ecosistema Buildrr. Convenciones para
agentes IA trabajando en este repo.

## Principio rector

**Simplicidad sobre completitud.** Dos actores fijos (Pablo cliente, Gino admin),
un ciclo de vida de 4 estados, sin contratos ni facturación. Ante dos soluciones
que cumplen el requisito, elegir la más simple. Ver [docs/01-alcance.md](docs/01-alcance.md)
para lo que está explícitamente afuera antes de agregar nada.

## Leer antes de escribir código

1. [docs/00-arquitectura.md](docs/00-arquitectura.md) — por qué JWT compartido y no
   gateway/service discovery nuevo. No proponer Eureka/Consul/API gateway propio:
   ya se evaluó y se descartó por sobre-ingeniería para este volumen.
2. [docs/01-alcance.md](docs/01-alcance.md)
3. [docs/02-modelo-datos.md](docs/02-modelo-datos.md)
4. [docs/03-ciclo-de-vida.md](docs/03-ciclo-de-vida.md) — transiciones válidas de estado

## Stack

Spring Boot 3.3.5 (Java 17) · Spring Data JPA · SQL Server · Flyway · Angular 19
standalone + PrimeNG v19 · MinIO (adjuntos) · Docker Compose.

## Auth

Sin login propio, sin tabla de usuarios. El filtro `auth/JwtAuthenticationFilter`
valida el JWT HS256 de `auth-service` (SGO) y arma un `AuthenticatedUser` con lo
que traiga el claim (`userId`, `username`, `rol`, `organizacionId`). No pedir
nada por red a auth-service. No hardcodear el secret: viene de `JWT_SECRET`.

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
