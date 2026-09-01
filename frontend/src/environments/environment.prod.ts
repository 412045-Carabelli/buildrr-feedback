export const environment = {
  production: true,
  // Dominio público real del api-gateway compartido de Buildr. Las rutas
  // /api/tickets y /api/adjuntos están agregadas en buildr-platform pero
  // todavía apuntan a un host pendiente hasta que este backend se despliegue
  // ahí — ver buildr-platform/backend1.0/api-gateway/application-prod.properties.
  apiUrl: 'https://buildrr.cloud'
};
