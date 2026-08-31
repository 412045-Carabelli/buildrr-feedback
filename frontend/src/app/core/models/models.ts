// Todos los interfaces/types en un único archivo — mismo criterio que
// sistema-gestion-obras/frontend/src/app/core/models/models.ts.

export type OrigenCuenta = 'SGO' | 'FRESCO';

export interface LoginRequest {
  usuario: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  origen: OrigenCuenta;
}

export type TipoTicket = 'BUG' | 'FUNCION_NUEVA';
export type Producto = 'SGO' | 'FRESCO';
export type EstadoTicket = 'NUEVO' | 'EN_PROGRESO' | 'TESTING' | 'COMPLETADO';

export interface TicketRequest {
  tipo: TipoTicket;
  producto: Producto;
  titulo: string;
  descripcion?: string;
}

export interface TicketResponse {
  id: number;
  tipo: TipoTicket;
  producto: Producto;
  titulo: string;
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
