# Modelo de datos

Base `buildr_supp`, SQL Server, misma instancia (VPS) que SGO.

## ticket

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| tipo | NVARCHAR(20) | `BUG` \| `FUNCION_NUEVA` |
| producto | NVARCHAR(20) | `SGO` \| `FRESCO` |
| titulo | NVARCHAR(255) | |
| modulo | NVARCHAR(255) | opcional, texto libre (ej. "Movimientos", "Facturación") |
| fecha | DATE | fecha del hecho/reporte, la carga el usuario — default hoy, no es auditoría |
| descripcion | NVARCHAR(MAX) | HTML que arma el editor (p-editor/Quill) del front |
| estado | NVARCHAR(20) | `NUEVO` \| `EN_PROGRESO` \| `TESTING` \| `COMPLETADO` |
| creado_por | NVARCHAR(100) | username del JWT (Pablo) |
| creado_en | DATETIME2 | |
| ultima_actualizacion | DATETIME2 | |

## usuario_aplicacion

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| username | NVARCHAR(100) | mismo username que viene en `X-Username` del gateway |
| producto | NVARCHAR(20) | `SGO` \| `FRESCO` — a qué aplicación puede cargar/ver tickets |
| rol | NVARCHAR(20) | `CLIENTE` (crea/ve los propios) \| `ADMIN` (gestiona el ciclo de vida de esos tickets) |

Único (`username`, `producto`). No es una tabla de usuarios — es un control de
acceso por producto. No tiene email/password/nombre, nada de eso vive acá
(sigue en `auth-service`). Determina:

- Qué aplicaciones puede elegir el usuario en el selector del navbar
  (`GET /api/usuario-aplicacion/mis-aplicaciones`).
- A qué producto puede cargar un ticket nuevo (`POST /api/tickets` valida que
  el `producto` del request esté en esta tabla para ese `username`).
- Quién puede cambiar el estado de un ticket (`PATCH /api/tickets/{id}/estado`
  exige rol `ADMIN` para el `producto` de ese ticket puntual).
- Qué tickets ve en el listado (`GET /api/tickets` sin filtro devuelve todos
  los productos accesibles; con `?producto=X` sólo ese, si tiene acceso).

Seed inicial (`V4__usuario_aplicacion.sql`): Pablo cliente de SGO, la dueña de
FrezCo cliente de FrezCo, Gino admin de ambos.

## adjunto

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| ticket_id | BIGINT FK → ticket | |
| historial_estado_id | BIGINT FK → historial_estado, NULL | ver abajo |
| tipo | NVARCHAR(20) | `FOTO` \| `VIDEO` \| `DOCUMENTO` (inferido del content-type al subir) |
| url | NVARCHAR(500) | **object key** dentro del bucket MinIO (`ticket/{ticketId}/{uuid}-{nombre}`), NO una URL pública — el bucket es privado |
| nombre_original | NVARCHAR(255) | nombre del archivo tal como lo subió el usuario |
| content_type | NVARCHAR(100) | para reconstruir la respuesta HTTP de descarga |
| subido_por | NVARCHAR(100) | |
| subido_en | DATETIME2 | |

Un adjunto puede pertenecer al ticket en sí (evidencia inicial de Pablo) o a un
`historial_estado` puntual (captura de "así quedó" del admin) — se linkea por
`historial_estado_id` opcional además de `ticket_id`.

Storage: MinIO, mismo servidor que usa SGO (`documentos-service`), bucket propio
`buildrr-feedback` separado del de SGO. Segmentado por ticket vía prefijo de
object key (`ticket/{ticketId}/...`), no por bucket — un bucket por ticket sería
miles de buckets con volumen real. La descarga es un proxy del propio backend
(`GET /api/adjuntos/{id}/descargar`), no una URL directa a MinIO: el hostname
interno (`minio:9000`, red Docker `sgo_backend`) no es alcanzable desde el
navegador. Ver [00-arquitectura.md](00-arquitectura.md).

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
