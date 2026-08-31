package ar.buildrr.feedback.ticket.exception;

public class TicketNotFoundException extends RuntimeException {
  public TicketNotFoundException(String msg) {
    super(msg);
  }
}
