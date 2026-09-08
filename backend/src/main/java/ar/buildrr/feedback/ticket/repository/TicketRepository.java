package ar.buildrr.feedback.ticket.repository;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
  List<Ticket> findByProductoIn(Collection<Producto> productos);

  List<Ticket> findByProducto(Producto producto);

  List<Ticket> findByProductoInAndEstadoIn(Collection<Producto> productos, Collection<String> estados);

  List<Ticket> findByProductoAndEstadoIn(Producto producto, Collection<String> estados);

  long countByProductoInAndEstado(Collection<Producto> productos, String estado);

  long countByProductoAndEstado(Producto producto, String estado);

  long countByProductoIn(Collection<Producto> productos);

  long countByProducto(Producto producto);
}
