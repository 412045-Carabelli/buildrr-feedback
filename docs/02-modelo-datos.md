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
| estado | NVARCHAR(20) | `NUEVO` \| `EN_PROGRESO` \| `TESTING` \| `COMPLETADO` \| `ANULADO` |
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

### Endpoints

- `GET /api/usuario-aplicacion/mis-aplicaciones` — accesos del usuario logueado.
- `GET /api/admin/usuarios-aplicacion?producto=` — accesos de ese producto,
  solo un admin del producto.
- `POST /api/admin/usuarios-aplicacion` `{username, producto, rol}` — alta vía
  Factory Method por rol (`usuarioaplicacion/factory/`, mismo criterio que
  `ticket/factory/` para tipo de ticket), solo un admin del producto.
- `DELETE /api/admin/usuarios-aplicacion/{id}` — revocar acceso, solo un admin
  del producto de ese acceso.
- `GET /api/tickets/stats?producto=` — conteos por estado (dashboard del
  admin), sobre los productos accesibles del usuario.
- `GET /api/tickets?producto=&estado=` — `estado` acepta uno o varios
  separados por coma (ej. `estado=NUEVO,EN_PROGRESO,TESTING` para "pendientes").
- `PUT /api/tickets/{id}` — edita título/módulo/fecha/descripción, solo quien
  creó el ticket o un admin del producto; no funciona sobre un ticket ANULADO.
- `GET /api/tickets/{id}/historial` — transiciones de `historial_estado`
  ordenadas por fecha, para el timeline del detalle.

## adjunto

| Columna | Tipo | Notas |
|---|---|---|
| id | BIGINT IDENTITY PK | |
| ticket_id | BIGINT FK → ticket | |
| historial_estado_id | BIGINT FK → historial_estado, NULL | ver abajo |
| tipo | NVARCHAR(20) | `FOTO` \| `VIDEO` \| `DOCUMENTO` (inferido del content-type al subir) |
| url | NVARCHAR(500) | `id_documento` en `documentos-service` (SGO), NO un object key propio ni una URL |
| nombre_original | NVARCHAR(255) | nombre del archivo tal como lo subió el usuario |
| content_type | NVARCHAR(100) | para reconstruir la respuesta HTTP de descarga |
| subido_por | NVARCHAR(100) | |
| subido_en | DATETIME2 | |

Un adjunto puede pertenecer al ticket en sí (evidencia inicial de Pablo) o a un
`historial_estado` puntual (captura de "así quedó" del admin) — se linkea por
`historial_estado_id` opcional además de `ticket_id`.

Storage: sin bucket propio — este backend le pega directo a `documentos-service`
de SGO (`POST /api/documentos` con `producto=BUILDRR_FEEDBACK`,
`tipo_asociado=ticket`, `id_asociado={ticketId}`; ver
`ar.buildrr.feedback.adjunto.impl.AdjuntoServiceImpl`). Del lado de
`documentos-service`, un Strategy por producto (`strategy/`) arma el prefijo
de carpeta real dentro del bucket y valida acceso — `buildrr-feedback` nunca
ve el bucket ni las credenciales de MinIO. La descarga es un proxy del propio
backend (`GET /api/adjuntos/{id}/descargar` → `documentos-service/{id}/view`),
nunca una URL directa a MinIO desde el navegador. Ver
[00-arquitectura.md](00-arquitectura.md).

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
