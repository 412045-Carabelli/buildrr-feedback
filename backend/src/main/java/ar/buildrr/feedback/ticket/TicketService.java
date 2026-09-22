package ar.buildrr.feedback.ticket;

import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.EditarTicketRequest;
import ar.buildrr.feedback.ticket.dto.EstadisticasTicketResponse;
import ar.buildrr.feedback.ticket.dto.HistorialEstadoResponse;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.Producto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketService {
  /**
   * Crea el ticket y sube los adjuntos (si vienen) en la misma transacción:
   * si la subida a documentos-service falla para alguno, se revierte todo —
   * no queda un ticket a medio adjuntar. {@code archivos} es opcional.
   */
  TicketResponse crearConAdjunto(TicketRequest request, List<MultipartFile> archivos, String creadoPor);

  TicketResponse obtenerPorId(Long id);

  /** Solo el creador o un admin del producto — no cambia tipo ni producto, eso no se reasigna. */
  TicketResponse editar(Long id, EditarTicketRequest request, String editor);

  List<HistorialEstadoResponse> historial(Long id);

  /**
   * @param filtroProducto opcional — la app seleccionada en el navbar. Sin filtro, todas las que el usuario puede ver.
   * @param filtroEstados opcional — uno o más de NUEVO/EN_PROGRESO/TESTING/COMPLETADO, para los accesos rápidos del dashboard.
   * @param busqueda opcional — texto libre, filtra por título o módulo (contains, case-insensitive).
   * Siempre ordenado por creación descendente (más nuevo primero).
   */
  List<TicketResponse> listar(String username, Producto filtroProducto, List<String> filtroEstados, String busqueda);

  TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor);

  /** Conteos por estado sobre los productos accesibles del usuario (o uno solo si se filtra). */
  EstadisticasTicketResponse estadisticas(String username, Producto filtroProducto);
}
