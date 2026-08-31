# Modelo de datos

Base `buildrr_feedback`, SQL Server, misma instancia que SGO.

## ticket

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| tipo | NVARCHAR(20) | `BUG` \| `FUNCION_NUEVA` |
| producto | NVARCHAR(20) | `SGO` \| `FRESCO` |
| titulo | NVARCHAR(255) | |
| descripcion | NVARCHAR(MAX) | |
| estado | NVARCHAR(20) | `NUEVO` \| `EN_PROGRESO` \| `TESTING` \| `COMPLETADO` |
| creado_por | NVARCHAR(100) | username del JWT (Pablo) |
| creado_en | DATETIME2 | |
| ultima_actualizacion | DATETIME2 | |

## adjunto

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| ticket_id | BIGINT FK → ticket | |
| tipo | NVARCHAR(20) | `FOTO` \| `VIDEO` \| `DOCUMENTO` |
| url | NVARCHAR(500) | ubicación en MinIO |
| subido_por | NVARCHAR(100) | |
| subido_en | DATETIME2 | |

Un adjunto puede pertenecer al ticket en sí (evidencia inicial de Pablo) o a un
`historial_estado` puntual (captura de "así quedó" del admin) — se linkea por
`historial_estado_id` opcional además de `ticket_id`.

## registro_horas

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| ticket_id | BIGINT FK → ticket | |
| horas | DECIMAL(5,2) | |
| fecha | DATE | |
| nota | NVARCHAR(500) | opcional |
| cargado_por | NVARCHAR(100) | |

Acumulado mensual = `SUM(horas) WHERE MONTH(fecha) = ... AND YEAR(fecha) = ...`,
query directo, sin tabla de saldos denormalizada (mismo criterio que FrezCo).

## historial_estado

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| ticket_id | BIGINT FK → ticket | |
| estado_anterior | NVARCHAR(20) | |
| estado_nuevo | NVARCHAR(20) | |
| nota | NVARCHAR(MAX) | opcional, visible para Pablo |
| cambiado_por | NVARCHAR(100) | |
| cambiado_en | DATETIME2 | |

Sin tabla de usuarios propia: `creado_por`, `cargado_por`, `cambiado_por` guardan
el username tal cual viene en el claim del JWT de `auth-service` (ver
[00-arquitectura.md](00-arquitectura.md)).
