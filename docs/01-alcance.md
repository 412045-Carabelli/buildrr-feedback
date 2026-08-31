# Alcance

## Qué se construye

- Alta de tickets por parte de Pablo: tipo (BUG o FUNCION_NUEVA), producto afectado
  (SGO o FRESCO), título, descripción, adjuntos (fotos, videos, documentos).
- Ciclo de vida del ticket con 4 estados: NUEVO → EN_PROGRESO → TESTING → COMPLETADO.
  Desde TESTING se puede volver a EN_PROGRESO si algo no funciona (ver
  [03-ciclo-de-vida.md](03-ciclo-de-vida.md)).
- Gestión del estado del lado admin (Gino): cambiar estado, adjuntar captura de
  "así quedó" cuando corresponde, agregar una nota visible para Pablo.
- Registro de horas por ticket (número simple, sin tipos de hora ni contratos),
  para saber cuánto se consumió del contrato de 10 hs/mes de Pablo. Vista de
  acumulado mensual, sin lógica de facturación ni alertas automáticas por ahora.
- Vista de Pablo: sus tickets, estado actual, notas/capturas que dejó el admin,
  horas consumidas en el mes en curso.
- Vista admin: listado/kanban por estado, filtro por producto y tipo, cambio de
  estado, carga de horas, adjuntar capturas.

## Qué queda explícitamente afuera (por ahora)

- Contratos formales, tipos de hora, tarifas, facturación.
- Notificaciones automáticas (email/push) al cambiar de estado.
- Comentarios tipo hilo/chat dentro del ticket — solo una nota del admin por
  cambio de estado relevante.
- Roles múltiples o gestión de permisos — tres actores fijos: Pablo (cliente),
  la dueña de FrezCo (cliente) y Gino (admin). Los tres son cuentas normales
  en `sgo_auth`, ver [00-arquitectura.md](00-arquitectura.md).
- Priorización/SLA.
- Búsqueda o reportes avanzados.

## Decisiones pendientes de confirmar con Pablo

- Tamaño máximo de adjuntos (video pesa) y formatos aceptados.
- Si además de SGO/FRESCO va a hacer falta un tercer "producto" en el futuro.
