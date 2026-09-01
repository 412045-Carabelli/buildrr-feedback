# Buildrr Feedback

Tracker simple de bugs y solicitudes de funcionalidad para el ecosistema Buildrr
(SGO — Sistema de Gestión de Obras, y FrezCo). Pablo carga tickets con fotos, videos
y documentos; desde el otro lado se gestiona el ciclo de vida y se lleva el control
de horas contra su contrato de 10 hs/mes.

## Stack

Spring Boot 3.3.5 (Java 17) · Spring Data JPA · SQL Server · Flyway · Angular 19
standalone con PrimeNG v19 · MinIO (adjuntos) · Docker Compose sobre el VPS existente.

La instancia de SQL Server y MinIO son las mismas que usa el Sistema de Gestión de
Obras; este proyecto usa una base separada (`buildr_supp`) y un bucket separado.

## Auth

Sin tabla de usuarios propia y sin login/validación de JWT acá. El frontend
loguea directo contra el api-gateway compartido de Buildr (`buildr-platform`)
y le pega a `/api/tickets`/`/api/adjuntos` siempre a través del gateway — éste
valida el JWT e inyecta headers de identidad que este backend solo lee.
Ver [docs/00-arquitectura.md](docs/00-arquitectura.md).

## Estructura

```
backend/       Spring Boot (un módulo por dominio)
  ticket/        alta (Factory Method por tipo), listado, cambio de estado (State)
  adjunto/       fotos/videos/docs — sube a MinIO segmentado por ticket, descarga
                 vía proxy del propio backend (bucket privado)
  registrohoras/ carga de horas por ticket (pendiente)
  auth/          GatewayAuthFilter — confía en los headers del api-gateway
  config/        CORS, MinIO, seguridad
frontend/      Angular standalone
  features/      vista Pablo (crear/ver tickets) + vista admin (kanban, estados, horas)
  core/          servicios HTTP, interceptor JWT
  shared/        componentes reutilizables
db/             creación de base y migraciones iniciales
backup/         backup diario de la base
docs/           documentación funcional y técnica
```

## Documentación

| Documento | Contenido |
|---|---|
| [docs/00-arquitectura.md](docs/00-arquitectura.md) | cómo se conecta con el api-gateway compartido de Buildr |
| [docs/01-alcance.md](docs/01-alcance.md) | qué se construye y qué queda afuera |
| [docs/02-modelo-datos.md](docs/02-modelo-datos.md) | esquema de tablas |
| [docs/03-ciclo-de-vida.md](docs/03-ciclo-de-vida.md) | estados de un ticket y transiciones válidas |

Convenciones de código en [CLAUDE.md](CLAUDE.md).

## Puesta en marcha

```bash
cp .env.example .env   # completar credenciales de base y MinIO
docker compose up --build
```

El stack se engancha a la red Docker `sgo_backend` del Sistema de Gestión de Obras,
donde viven SQL Server y MinIO. Esa red tiene que existir antes de levantar.

Desarrollo local:

```bash
cd backend && ./mvnw spring-boot:run
cd frontend && npm start
```

## Deploy

Mismo patrón que FrezCo: push a `main` publica imágenes en GHCR, el VPS las
consume vía `docker-compose.ghcr.yml`. Subdominio propio (`soporte.<dominio real
a definir>`) apuntado al frontend, proxy reverso ya existente en el VPS.
