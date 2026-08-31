# Arquitectura — cómo se conecta con el resto del ecosistema

## Por qué no service discovery / gateway nuevo

El ecosistema Buildrr (SGO) ya tiene un `api-gateway` con `auth-service` propio detrás
(entidades `Usuario`, `Organizacion`, `RefreshToken`, JWT HS256). Para este tamaño de
proyecto meter un service discovery (Eureka/Consul) o registrar rutas nuevas en el
gateway de SGO es sobre-ingeniería y además acopla el deploy de este proyecto al de SGO.

Se eligió el patrón más simple posible: **JWT compartido, sin gateway propio.**

## Una sola fuente de identidad: sgo_auth

Pablo y la dueña de FrezCo son **las dos cuentas normales en `auth-service` de SGO**
(misma base `sgo_auth`, misma tabla `usuarios`). No hay una fuente de identidad para
SGO y otra para FrezCo — se unificó a propósito para no tener que llamar a dos
sistemas distintos ni mantener dos formas de login.

> Antes de esto, la dueña de FrezCo se autenticaba contra el backend propio de
> FrezCo (login por sesión/cookie) y este proyecto le emitía un JWT propio para
> igualar el contrato. Se descartó: significaba mantener 2 flujos de login, un
> segundo secret JWT (`jwt.own-secret`) y un `Mediator` con un solo colleague real
> por rama — justo lo que el proyecto evita (`CLAUDE.md`: "no interfaces con una
> sola implementación"). Ahora la cuenta de FrezCo se creó directamente en
> `sgo_auth` vía `POST /auth/register` (la vía segura y con las reglas de negocio
> de SGO, no un INSERT SQL a mano).

## Cómo funciona

1. El usuario (Pablo o la dueña de FrezCo) manda `{usuario, password}` a
   `POST /auth/login` de este backend.
2. `SgoLoginClient` reenvía la request tal cual a `{sgo.gateway-url}/auth/login`
   y **re-emite el JWT que devuelve SGO sin tocarlo** — este backend nunca firma
   tokens propios, solo actúa de passthrough.
3. El frontend guarda ese token y lo manda como `Authorization: Bearer` en cada
   request siguiente.
4. `JwtAuthenticationFilter` valida la firma contra `jwt.secret` (el mismo que
   usa `auth-service` de SGO) y arma un `AuthenticatedUser` con los claims
   (`userId`, `username`, `rol`, `organizacionId`). No hay llamada de red en
   cada request, solo verificación de firma/expiración.

## Prod vs. test — `sgo.gateway-url`

No hay una URL pública separada para "ambiente de test": `sgo_auth_test` solo
existe como base de datos, alcanzable únicamente si alguien corre el
`auth-service` de SGO en su máquina con perfil `dev`
(`sistema-gestion-obras/backend1.0/auth-service`, puerto `8089`, ver su propio
`application-dev.properties`). Por eso `sgo.gateway-url` es lo único que cambia
entre ambientes:

| Ambiente | `SGO_GATEWAY_URL` | Base real |
|---|---|---|
| Prod (default) | `https://buildrr.cloud` | `sgo_auth` |
| Test local | `http://localhost:8089` (con auth-service de SGO corriendo local, perfil `dev`) | `sgo_auth_test` |

Ver `backend/src/main/resources/application-dev.properties.example`.

## Qué NO hace este proyecto

- No tiene tabla de usuarios propia. La identidad sale entera de los claims
  del JWT de SGO.
- No pasa por el `api-gateway` de SGO para servir sus propias rutas. Tiene su
  propio dominio/subdominio y su propio backend expuesto directamente (detrás
  del proxy reverso del VPS). Sí actúa como *cliente* del login de SGO
  (`SgoLoginClient`).
- No firma JWT propios. Todo token que circula en este proyecto lo emitió SGO.

## Riesgo aceptado

Si en SGO rota `jwt.secret`, hay que rotarlo también acá (`JWT_SECRET`, variable
de entorno compartida, sin sincronización automática). Con este volumen de
usuarios es aceptable; si crece, ahí sí vale la pena pasar a registrar rutas en
el `api-gateway` de SGO en vez de duplicar el login.
