package ar.buildrr.feedback.adjunto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {
  List<Adjunto> findByTicketIdOrderBySubidoEnAsc(Long ticketId);
}
