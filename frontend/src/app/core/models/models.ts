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
  refresh_token?: string;
}

// Mismo contrato que auth-service (ChangePasswordRequest) — POST
// /auth/change-password contra el gateway, exige X-User-Id (lo inyecta el
// gateway a partir del Bearer, no lo mandamos nosotros).
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// Mismo contrato que auth-service (ForgotPasswordRequest/ResetPasswordRequest)
// — recuperación de contraseña por código enviado a mail, sin login.
export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
  confirmPassword: string;
}

export type TipoTicket = 'BUG' | 'FUNCION_NUEVA';
export type Producto = 'SGO' | 'FRESCO';
export type EstadoTicket = 'NUEVO' | 'EN_PROGRESO' | 'TESTING' | 'COMPLETADO' | 'ANULADO';

export interface TicketRequest {
  tipo: TipoTicket;
  producto: Producto;
  titulo: string;
  modulo?: string;
  /** ISO yyyy-MM-dd. Si no se manda, el backend usa hoy. */
  fecha?: string;
  descripcion?: string;
}

/** No reasigna tipo ni producto — ver EditarTicketRequest en el backend. */
export interface EditarTicketRequest {
  titulo: string;
  modulo?: string;
  fecha?: string;
  descripcion?: string;
}

export interface HistorialEstadoResponse {
  estadoAnterior?: EstadoTicket;
  estadoNuevo: EstadoTicket;
  nota?: string;
  cambiadoPor: string;
  cambiadoEn: string;
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

export interface EstadisticasTicketResponse {
  total: number;
  nuevos: number;
  enProgreso: number;
  testing: number;
  completados: number;
  pendientes: number;
}

export interface UsuarioAplicacionResponse {
  id: number;
  username: string;
  producto: Producto;
  rol: RolAplicacion;
}

export interface AltaUsuarioAplicacionRequest {
  username: string;
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
  contentType?: string;
  subidoPor: string;
  subidoEn: string;
  urlDescarga: string;
}
