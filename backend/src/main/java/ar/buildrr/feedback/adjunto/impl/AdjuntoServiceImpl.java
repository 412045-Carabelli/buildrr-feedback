package ar.buildrr.feedback.adjunto.impl;

import ar.buildrr.feedback.adjunto.Adjunto;
import ar.buildrr.feedback.adjunto.AdjuntoDescarga;
import ar.buildrr.feedback.adjunto.AdjuntoRepository;
import ar.buildrr.feedback.adjunto.AdjuntoService;
import ar.buildrr.feedback.adjunto.TipoAdjunto;
import ar.buildrr.feedback.adjunto.dto.AdjuntoResponse;
import ar.buildrr.feedback.adjunto.dto.DocumentoRemotoResponse;
import ar.buildrr.feedback.adjunto.exception.AdjuntoInvalidoException;
import ar.buildrr.feedback.adjunto.exception.AdjuntoNotFoundException;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.exception.AccesoDenegadoException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.ticket.repository.TicketRepository;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * No maneja storage propio — sube y descarga a través del documentos-service
 * compartido de SGO (mismo MinIO, otro servicio), identificado como
 * producto=BUILDRR_FEEDBACK y tipo_asociado=ticket/id_asociado=ticketId (ver
 * strategy/BuildrrFeedbackDocumentoStrategy del lado de documentos-service).
 * `Adjunto.url` guarda el `id_documento` remoto, no un object key propio —
 * este backend nunca ve el bucket ni las credenciales de MinIO.
 */
@Service
@Slf4j
public class AdjuntoServiceImpl implements AdjuntoService {

  private static final Set<String> EXTENSIONES_BLOQUEADAS =
      Set.of("exe", "bat", "cmd", "sh", "ps1", "msi", "jar", "com", "scr");

  private final RestClient documentosClient;
  private final AdjuntoRepository adjuntoRepository;
  private final TicketRepository ticketRepository;
  private final UsuarioAplicacionService usuarioAplicacionService;

  public AdjuntoServiceImpl(
      @Value("${documentos.service.url}") String documentosServiceUrl,
      AdjuntoRepository adjuntoRepository,
      TicketRepository ticketRepository,
      UsuarioAplicacionService usuarioAplicacionService) {
    // Sin timeout, un documentos-service colgado (o su propia dependencia,
    // p. ej. MinIO) deja el request de subida pendiente para siempre — el
    // hilo de Tomcat que lo atiende nunca libera la conexión.
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
    factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
    this.documentosClient = RestClient.builder()
        .baseUrl(documentosServiceUrl)
        .requestFactory(factory)
        .build();
    this.adjuntoRepository = adjuntoRepository;
    this.ticketRepository = ticketRepository;
    this.usuarioAplicacionService = usuarioAplicacionService;
  }

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

    MultipartBodyBuilder body = new MultipartBodyBuilder();
    body.part("tipo_asociado", "ticket");
    body.part("id_asociado", ticketId.toString());
    body.part("producto", "BUILDRR_FEEDBACK");
    body.part("tipo_documento", "OTRO");
    try {
      body.part("file", new ByteArrayResource(archivo.getBytes()) {
        @Override
        public String getFilename() {
          return nombreOriginal;
        }
      }).contentType(MediaType.parseMediaType(
          archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream"));
    } catch (IOException e) {
      throw new AdjuntoInvalidoException("No se pudo leer el archivo: " + e.getMessage());
    }

    DocumentoRemotoResponse remoto;
    try {
      remoto = documentosClient.post()
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(body.build())
          .retrieve()
          .body(DocumentoRemotoResponse.class);
    } catch (Exception e) {
      log.error("Error subiendo adjunto a documentos-service", e);
      throw new AdjuntoInvalidoException("No se pudo subir el archivo: " + e.getMessage());
    }

    if (remoto == null || remoto.getId_documento() == null) {
      throw new AdjuntoInvalidoException("documentos-service no devolvió un id de documento");
    }

    Adjunto guardado = adjuntoRepository.save(Adjunto.builder()
        .ticketId(ticketId)
        .historialEstadoId(historialEstadoId)
        .tipo(tipo)
        .url(remoto.getId_documento().toString())
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
    try {
      byte[] contenido = documentosClient.get()
          .uri("/{id}/view", adjunto.getUrl())
          .retrieve()
          .body(byte[].class);
      return new AdjuntoDescarga(contenido, adjunto.getContentType(), adjunto.getNombreOriginal());
    } catch (Exception e) {
      log.error("Error descargando adjunto {} de documentos-service", adjuntoId, e);
      throw new AdjuntoInvalidoException("No se pudo descargar el archivo: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public void eliminar(Long adjuntoId, String solicitante) {
    Adjunto adjunto = buscar(adjuntoId);
    Ticket ticket = ticketRepository.findById(adjunto.getTicketId())
        .orElseThrow(() -> new TicketNotFoundException("Ticket " + adjunto.getTicketId() + " no existe"));

    boolean esQuienSubio = adjunto.getSubidoPor().equals(solicitante);
    if (!esQuienSubio && !usuarioAplicacionService.esAdmin(solicitante, ticket.getProducto())) {
      throw new AccesoDenegadoException("Solo quien subió el adjunto o un admin de " + ticket.getProducto() + " puede borrarlo");
    }

    try {
      documentosClient.delete().uri("/{id}", adjunto.getUrl()).retrieve().toBodilessEntity();
    } catch (Exception e) {
      // No cortamos el borrado local por esto — puede ser un adjunto huérfano
      // de antes de este cambio (id viejo, ya no existe en documentos-service).
      log.warn("No se pudo borrar el documento remoto {} de documentos-service: {}", adjunto.getUrl(), e.getMessage());
    }

    adjuntoRepository.delete(adjunto);
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
        .contentType(adjunto.getContentType())
        .nombreOriginal(adjunto.getNombreOriginal())
        .subidoPor(adjunto.getSubidoPor())
        .subidoEn(adjunto.getSubidoEn())
        .urlDescarga("/api/adjuntos/%d/descargar".formatted(adjunto.getId()))
        .build();
  }
}
