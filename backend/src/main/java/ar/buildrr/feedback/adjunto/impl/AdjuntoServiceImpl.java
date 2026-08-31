package ar.buildrr.feedback.adjunto.impl;

import ar.buildrr.feedback.adjunto.Adjunto;
import ar.buildrr.feedback.adjunto.AdjuntoDescarga;
import ar.buildrr.feedback.adjunto.AdjuntoRepository;
import ar.buildrr.feedback.adjunto.AdjuntoService;
import ar.buildrr.feedback.adjunto.TipoAdjunto;
import ar.buildrr.feedback.adjunto.dto.AdjuntoResponse;
import ar.buildrr.feedback.adjunto.exception.AdjuntoInvalidoException;
import ar.buildrr.feedback.adjunto.exception.AdjuntoNotFoundException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.ticket.repository.TicketRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Sube a MinIO segmentado por ticket: cada objeto vive bajo el prefijo
 * `ticket/{ticketId}/...`, así que auditar o limpiar los adjuntos de un
 * ticket es un list-by-prefix, sin tocar los de otros tickets.
 *
 * La descarga es un proxy de este backend (getObject + devolver bytes), no
 * una URL directa a MinIO: el bucket es privado y su hostname interno
 * (`minio:9000`, red Docker `sgo_backend`) no es alcanzable desde el
 * navegador — mismo patrón que documentos-service de SGO.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdjuntoServiceImpl implements AdjuntoService {

  private static final Set<String> EXTENSIONES_BLOQUEADAS =
      Set.of("exe", "bat", "cmd", "sh", "ps1", "msi", "jar", "com", "scr");

  private final MinioClient minioClient;
  private final AdjuntoRepository adjuntoRepository;
  private final TicketRepository ticketRepository;

  @Value("${minio.bucket}")
  private String bucket;

  @Override
  @Transactional
  public AdjuntoResponse subir(Long ticketId, Long historialEstadoId, MultipartFile archivo, String subidoPor) {
    if (!ticketRepository.existsById(ticketId)) {
      throw new TicketNotFoundException("Ticket " + ticketId + " no existe");
    }
    if (archivo == null || archivo.isEmpty()) {
      throw new AdjuntoInvalidoException("Archivo vacío");
    }

    String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "archivo";
    validarExtension(nombreOriginal);

    TipoAdjunto tipo = tipoSegunContentType(archivo.getContentType());
    String objectKey = "ticket/%d/%s-%s".formatted(ticketId, UUID.randomUUID(), nombreOriginal);

    try (InputStream input = archivo.getInputStream()) {
      minioClient.putObject(PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectKey)
          .stream(input, archivo.getSize(), -1)
          .contentType(archivo.getContentType())
          .build());
    } catch (Exception e) {
      log.error("Error subiendo adjunto a MinIO", e);
      throw new AdjuntoInvalidoException("No se pudo subir el archivo: " + e.getMessage());
    }

    Adjunto guardado = adjuntoRepository.save(Adjunto.builder()
        .ticketId(ticketId)
        .historialEstadoId(historialEstadoId)
        .tipo(tipo)
        .url(objectKey)
        .nombreOriginal(nombreOriginal)
        .contentType(archivo.getContentType())
        .subidoPor(subidoPor)
        .build());

    return toResponse(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdjuntoResponse> listarPorTicket(Long ticketId) {
    return adjuntoRepository.findByTicketIdOrderBySubidoEnAsc(ticketId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public AdjuntoDescarga descargar(Long adjuntoId) {
    Adjunto adjunto = buscar(adjuntoId);
    try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
        .bucket(bucket)
        .object(adjunto.getUrl())
        .build())) {
      byte[] contenido = in.readAllBytes();
      return new AdjuntoDescarga(contenido, adjunto.getContentType(), adjunto.getNombreOriginal());
    } catch (Exception e) {
      log.error("Error descargando adjunto {} de MinIO", adjuntoId, e);
      throw new AdjuntoInvalidoException("No se pudo descargar el archivo: " + e.getMessage());
    }
  }

  private Adjunto buscar(Long id) {
    return adjuntoRepository.findById(id)
        .orElseThrow(() -> new AdjuntoNotFoundException("Adjunto " + id + " no existe"));
  }

  private void validarExtension(String nombreArchivo) {
    String extension = nombreArchivo.contains(".")
        ? nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase()
        : "";
    if (EXTENSIONES_BLOQUEADAS.contains(extension)) {
      throw new AdjuntoInvalidoException("Tipo de archivo no permitido: ." + extension);
    }
  }

  private TipoAdjunto tipoSegunContentType(String contentType) {
    if (contentType == null) {
      return TipoAdjunto.DOCUMENTO;
    }
    if (contentType.startsWith("image/")) {
      return TipoAdjunto.FOTO;
    }
    if (contentType.startsWith("video/")) {
      return TipoAdjunto.VIDEO;
    }
    return TipoAdjunto.DOCUMENTO;
  }

  private AdjuntoResponse toResponse(Adjunto adjunto) {
    return AdjuntoResponse.builder()
        .id(adjunto.getId())
        .ticketId(adjunto.getTicketId())
        .historialEstadoId(adjunto.getHistorialEstadoId())
        .tipo(adjunto.getTipo())
        .nombreOriginal(adjunto.getNombreOriginal())
        .subidoPor(adjunto.getSubidoPor())
        .subidoEn(adjunto.getSubidoEn())
        .urlDescarga("/api/adjuntos/%d/descargar".formatted(adjunto.getId()))
        .build();
  }
}
