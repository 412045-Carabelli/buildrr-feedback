package ar.buildrr.feedback.ticket.repository;

import ar.buildrr.feedback.ticket.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
  List<HistorialEstado> findByTicketIdOrderByCambiadoEnAsc(Long ticketId);
}
