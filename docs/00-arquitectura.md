# Arquitectura — cómo se conecta con el resto del ecosistema

## Por qué no service discovery / gateway nuevo

El ecosistema Buildrr (SGO) ya tiene un `api-gateway` con `auth-service` propio detrás
(entidades `Usuario`, `Organizacion`, `RefreshToken`, JWT HS256). Para este tamaño de
proyecto (un tracker, un usuario cliente activo: Pablo) meter un service discovery
(Eureka/Consul) o registrar rutas nuevas en el gateway de SGO es sobre-ingeniería y
además acopla el deploy de este proyecto al de SGO.

Se eligió el patrón más simple posible: **JWT compartido, sin gateway propio.**

## Cómo funciona

1. Pablo inicia sesión donde siempre: contra el `api-gateway`/`auth-service` de SGO
   (no hay pantalla de login nueva — o hay una que simplemente pega el mismo request
   de login que ya usa SGO).
2. El JWT que devuelve `auth-service` es HS256, firmado con el mismo secret que usa
   SGO (`jwt.secret`, ver `AuthLoginSystem`/`api-gateway/.../JwtProperties.java` para
   el valor real de producción — se copia a `.env` de este proyecto, nunca se hardcodea).
3. `buildrr-feedback-backend` valida ese JWT localmente con un filtro propio
   (`auth/JwtAuthenticationFilter`, copiado del patrón de `api-gateway` de SGO) — no
   hay llamada de red a `auth-service` en cada request, solo verificación de firma y
   expiración.
4. El frontend de este proyecto guarda el JWT (igual que el frontend de SGO) y lo
   manda en el header `Authorization: Bearer` en cada request.

## Qué NO hace este proyecto

- No emite tokens.
- No tiene tabla de usuarios propia. El `sub`/username del JWT es la identidad;
  si hace falta mostrar nombre/email se lee del propio JWT (claims), no se pide a
  `auth-service`.
- No pasa por el `api-gateway` de SGO. Tiene su propio dominio/subdominio y su
  propio backend expuesto directamente (detrás del proxy reverso del VPS).

## Riesgo aceptado

Si en SGO rota el JWT secret, hay que rotarlo también acá (variable de entorno
compartida, no hay sincronización automática). Con un solo secret y despliegues
manuales del usuario esto es aceptable; si el volumen de usuarios crece, ahí sí
vale la pena pasar a la Opción B (rutas en el api-gateway de SGO).
