package ar.buildrr.feedback.ticket.repository;

import ar.buildrr.feedback.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
