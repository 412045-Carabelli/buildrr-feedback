package ar.buildrr.feedback.adjunto;

import ar.buildrr.feedback.adjunto.dto.AdjuntoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdjuntoService {

  AdjuntoResponse subir(Long ticketId, Long historialEstadoId, MultipartFile archivo, String subidoPor);

  List<AdjuntoResponse> listarPorTicket(Long ticketId);

  AdjuntoDescarga descargar(Long adjuntoId);
}
