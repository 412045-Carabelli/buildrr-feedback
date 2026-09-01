# Arquitectura — cómo se conecta con el resto del ecosistema

## Service registry vs. gateway con rutas estáticas

No hay service registry (Eureka/Consul) en este ecosistema — los servicios no
se anuncian ni se descubren solos. Hay un **API Gateway con rutas estáticas**
(Spring Cloud Gateway, `spring.cloud.gateway.routes[N].uri=...` a mano en
`application-{profile}.properties`). Agregar un servicio nuevo = agregar una
línea de ruta. Nada dinámico.

## buildr-platform: la plataforma compartida

`auth-service`, `documentos-service` y `api-gateway` salieron de
`sistema-gestion-obras` y viven en un repo aparte, `buildr-platform`
(WIP al momento de escribir esto, todavía no reemplaza lo que corre en
producción — ver su propio README). SGO, FrezCo y este proyecto ("la
tiquetera") comparten esos tres servicios.

Este backend **ya no valida JWT ni hace login propio**. El flujo es:

1. El frontend loguea directo contra el gateway: `POST {gateway}/auth/login`
   (`{email, password}`, mismo contrato que `auth-service`). Devuelve
   `access_token` — un JWT real de `auth-service`, este proyecto nunca lo
   toca ni lo firma.
2. El frontend manda ese token como `Authorization: Bearer` en cada request,
   siempre contra el **gateway** (`/api/tickets/**`, `/api/adjuntos/**`),
   nunca contra este backend directo.
3. El gateway valida el JWT y reenvía la request a este backend inyectando
   headers de identidad: `X-User-Id`, `X-Username`, `X-User-Rol`,
   `X-Organizacion-Id`.
4. `GatewayAuthFilter` (`auth/`) solo lee esos headers y arma un
   `AuthenticatedUser` — no ve el JWT, no ve la contraseña. Si los headers no
   están, la request no pasó por el gateway.

Mismo patrón que `frezco/backend/.../GatewayAuthFilter.java`. Diferencia con
FrezCo: ellos son tenant único (organización fija `id=1`, la validan en el
filtro); acá no — Pablo (SGO) y la dueña de FrezCo son organizaciones
distintas, ambas legítimas, así que `GatewayAuthFilter` no filtra por
`organizacionId`, solo exige que los headers existan.

## Rutas registradas en el gateway

Agregadas en `buildr-platform/backend1.0/api-gateway/src/main/resources/`:

| Ambiente | Archivo | `tickets`/`adjuntos` apuntan a |
|---|---|---|
| Dev | `application-dev.properties` | `http://localhost:8090` (backend corrido suelto desde el IDE) |
| Prod | `application-prod.properties` | `http://tickets-service:8090` — **hostname pendiente**, este backend todavía no está desplegado en el stack compartido |

## Qué NO hace este proyecto

- No valida JWT, no lo firma, no tiene secret propio.
- No tiene tabla de usuarios propia. La identidad sale entera de los headers
  que inyecta el gateway.
- No expone su API directo al navegador en el flujo normal — el navegador
  solo habla con el gateway. El backend sigue escuchando en su puerto propio
  (8090) por si hace falta pegarle directo en dev/debug, pero eso no es el
  camino de producción.

## Qué falta para que esto funcione en prod

Ver `buildr-platform/README.md` — el resumen es: `buildr-platform` todavía no
comparte instancia de SQL Server/Minio con `sistema-gestion-obras` (corre
duplicado a propósito para probarlo aislado), y este backend todavía no está
containerizado dentro de ese stack (por eso la ruta prod del gateway apunta a
un hostname que no existe todavía). Migrar de verdad implica esas dos cosas.
