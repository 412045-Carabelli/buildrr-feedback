// Todos los interfaces/types en un único archivo — mismo criterio que
// sistema-gestion-obras/frontend/src/app/core/models/models.ts.

// Contrato real de auth-service (mismo que usa el frontend de SGO): POST
// /auth/login contra el api-gateway, no contra este backend.
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
}

export type TipoTicket = 'BUG' | 'FUNCION_NUEVA';
export type Producto = 'SGO' | 'FRESCO';
export type EstadoTicket = 'NUEVO' | 'EN_PROGRESO' | 'TESTING' | 'COMPLETADO';

export interface TicketRequest {
  tipo: TipoTicket;
  producto: Producto;
  titulo: string;
  modulo?: string;
  /** ISO yyyy-MM-dd. Si no se manda, el backend usa hoy. */
  fecha?: string;
  descripcion?: string;
}

export interface TicketResponse {
  id: number;
  tipo: TipoTicket;
  producto: Producto;
  titulo: string;
  modulo?: string;
  fecha: string;
  descripcion?: string;
  estado: EstadoTicket;
  creadoPor: string;
  creadoEn: string;
  ultimaActualizacion?: string;
}

export interface CambiarEstadoRequest {
  estadoNuevo: EstadoTicket;
  nota?: string;
}

export type RolAplicacion = 'CLIENTE' | 'ADMIN';

export interface AplicacionAccesoResponse {
  producto: Producto;
  rol: RolAplicacion;
}

export type TipoAdjunto = 'FOTO' | 'VIDEO' | 'DOCUMENTO';

export interface AdjuntoResponse {
  id: number;
  ticketId: number;
  historialEstadoId?: number;
  tipo: TipoAdjunto;
  nombreOriginal: string;
  subidoPor: string;
  subidoEn: string;
  urlDescarga: string;
}
