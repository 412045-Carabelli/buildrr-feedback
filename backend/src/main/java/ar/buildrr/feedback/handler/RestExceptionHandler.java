package ar.buildrr.feedback.handler;

import ar.buildrr.feedback.adjunto.exception.AdjuntoInvalidoException;
import ar.buildrr.feedback.adjunto.exception.AdjuntoNotFoundException;
import ar.buildrr.feedback.ticket.exception.TicketInvalidoException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.ticket.exception.TransicionInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestExceptionHandler {

  @ExceptionHandler({TicketNotFoundException.class, AdjuntoNotFoundException.class})
  public ResponseEntity<ErrorApi> handleNotFound(RuntimeException ex, HttpServletRequest request) {
    return build(404, ex.getMessage(), request);
  }

  @ExceptionHandler({TicketInvalidoException.class, TransicionInvalidaException.class, AdjuntoInvalidoException.class})
  public ResponseEntity<ErrorApi> handleInvalido(RuntimeException ex, HttpServletRequest request) {
    return build(400, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(err ->
        errores.put(err.getField(), err.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errores);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorApi> handleGeneric(RuntimeException ex, HttpServletRequest request) {
    log.error("Error interno", ex);
    return build(500, "Error interno del servidor", request);
  }

  private ResponseEntity<ErrorApi> build(int status, String message, HttpServletRequest request) {
    ErrorApi error = new ErrorApi();
    error.setMessage(message);
    error.setStatus(status);
    error.setPath(request.getRequestURI());
    error.setTimestamp(Instant.now());
    return ResponseEntity.status(status).body(error);
  }
}
