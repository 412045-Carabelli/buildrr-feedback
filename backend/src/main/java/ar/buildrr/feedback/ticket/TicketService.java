package ar.buildrr.feedback.ticket;

import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.EditarTicketRequest;
import ar.buildrr.feedback.ticket.dto.EstadisticasTicketResponse;
import ar.buildrr.feedback.ticket.dto.HistorialEstadoResponse;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.Producto;

import java.util.List;

public interface TicketService {
  TicketResponse crear(TicketRequest request, String creadoPor);

  TicketResponse obtenerPorId(Long id);

  /** Solo el creador o un admin del producto — no cambia tipo ni producto, eso no se reasigna. */
  TicketResponse editar(Long id, EditarTicketRequest request, String editor);

  List<HistorialEstadoResponse> historial(Long id);

  /**
   * @param filtroProducto opcional — la app seleccionada en el navbar. Sin filtro, todas las que el usuario puede ver.
   * @param filtroEstados opcional — uno o más de NUEVO/EN_PROGRESO/TESTING/COMPLETADO, para los accesos rápidos del dashboard.
   */
  List<TicketResponse> listar(String username, Producto filtroProducto, List<String> filtroEstados);

  TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor);

  /** Conteos por estado sobre los productos accesibles del usuario (o uno solo si se filtra). */
  EstadisticasTicketResponse estadisticas(String username, Producto filtroProducto);
}
