export const environment = {
  production: false,
  // El backend corre en el puerto 8090 (server.port en application.yml). El
  // 8091 es solo el mapeo de host que usa docker-compose (8091:8090) cuando
  // todo corre en contenedores — corriendo el backend suelto desde el IDE,
  // como ahora, escucha directo en 8090.
  apiUrl: 'http://localhost:8090'
};
