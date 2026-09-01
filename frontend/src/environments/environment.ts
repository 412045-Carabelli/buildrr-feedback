export const environment = {
  production: false,
  // Todo pasa por el api-gateway compartido de Buildr (buildr-platform) —
  // login (/auth/login) y datos (/api/tickets, /api/adjuntos). El gateway
  // valida el JWT e inyecta headers de identidad a nuestro backend; el
  // navegador nunca le pega directo al puerto 8090.
  apiUrl: 'http://localhost:8080'
};
