# Arquitectura — cómo se conecta con el resto del ecosistema

## Por qué no service discovery / gateway nuevo

El ecosistema Buildrr (SGO) ya tiene un `api-gateway` con `auth-service` propio detrás
(entidades `Usuario`, `Organizacion`, `RefreshToken`, JWT HS256). Para este tamaño de
proyecto meter un service discovery (Eureka/Consul) o registrar rutas nuevas en el
gateway de SGO es sobre-ingeniería y además acopla el deploy de este proyecto al de SGO.

Se eligió el patrón más simple posible: **JWT compartido, sin gateway propio.**

## Dos fuentes de identidad reales

Hay dos actores que suben tickets, cada uno con su propio sistema de login real
en producción:

- **Pablo** (SGO): `POST https://buildrr.cloud/auth/login` (email + password).
  El nginx del frontend de SGO proxea `/auth` internamente al `api-gateway` —
  no hace falta pasar por un subdominio de gateway separado.
- **La dueña de FrezCo**: FrezCo no tiene tabla de usuarios — un único usuario,
  autenticado por sesión (`POST https://frezco.buildrr.cloud/api/auth/login`,
  body `{"usuario", "clave"}`, ver `frezco/backend/.../AutenticacionController.java`).

Unificar esto es el trabajo del **patrón Mediator** (`auth/mediator/`):

- `LoginMediator` es el único punto de entrada (`POST /auth/login`, usuario +
  contraseña). Recorre una lista de `AuthColleague` en orden y delega en el
  primero que "soporta" el request. Los colleagues no se conocen entre sí.
- `FrezcoAuthColleague` (orden 1): `soporta()` es un chequeo local barato — el
  usuario coincide con `frezco.app-user` (env var, solo para rutear, NUNCA la
  contraseña). Para autenticar, llama al login real de FrezCo
  (`POST {frezco.login-url}/api/auth/login`) — si FrezCo lo acepta, firma un
  JWT **propio** de buildrr-feedback (`jwt.own-secret`, nunca el secret de
  SGO). La contraseña de la dueña de FrezCo no se duplica en ningún lado: la
  valida el propio backend de FrezCo, siempre.
- `SgoAuthColleague` (orden 2, fallback): reenvía las credenciales al login real
  de SGO (`POST {sgo.gateway-url}/auth/login`) y **re-emite el JWT que devuelve
  tal cual** — este backend nunca firma "tokens de SGO", solo los valida.

## Cómo funciona en cada request posterior

1. El frontend guarda el token que devolvió `/auth/login` (sin importar cuál
   colleague lo emitió) y lo manda como `Authorization: Bearer` en cada request.
2. `JwtAuthenticationFilter` prueba verificar la firma contra 2 secrets fijos y
   conocidos: primero `jwt.secret` (SGO), después `jwt.own-secret` (propio). No
   hay llamada de red en cada request, solo verificación de firma/expiración.
   Probar 2 secrets fijos es seguro: el cliente no elige cuál usar, el servidor
   simplemente intenta los dos que él mismo controla.
3. Se arma un `AuthenticatedUser` con `origen` (`SGO` o `FRESCO`) según qué
   secret validó. Los claims de un token FrezCo no tienen `userId` ni
   `organizacionId` — quedan `null`.

## Qué NO hace este proyecto

- No tiene tabla de usuarios propia. La identidad sale de los claims del JWT
  (SGO) o se sintetiza al momento del login (FrezCo) — nunca se persiste.
- No pasa por el `api-gateway` de SGO para servir sus propias rutas. Tiene su
  propio dominio/subdominio y su propio backend expuesto directamente (detrás
  del proxy reverso del VPS). Sí actúa como *cliente* del gateway de SGO para
  reenviar el login (`SgoAuthColleague`).

## Riesgo aceptado

Si en SGO rota `jwt.secret`, hay que rotarlo también acá (`JWT_SECRET`, variable
de entorno compartida, sin sincronización automática). `JWT_OWN_SECRET` es
independiente y solo lo rota este proyecto. Con este volumen de usuarios es
aceptable; si crece, ahí sí vale la pena pasar a registrar rutas en el
`api-gateway` de SGO en vez de duplicar el login.
