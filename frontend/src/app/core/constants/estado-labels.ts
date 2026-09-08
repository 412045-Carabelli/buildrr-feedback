import { EstadoTicket } from '../models/models';

export const NOMBRE_ESTADO: Record<EstadoTicket, string> = {
  NUEVO: 'Nuevo',
  EN_PROGRESO: 'En progreso',
  TESTING: 'Testing',
  COMPLETADO: 'Completado',
  ANULADO: 'Anulado'
};
