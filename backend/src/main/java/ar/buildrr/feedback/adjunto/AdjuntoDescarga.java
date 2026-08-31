package ar.buildrr.feedback.adjunto;

/** Bytes + metadata para armar la respuesta HTTP de descarga (ver AdjuntoController). */
public record AdjuntoDescarga(byte[] contenido, String contentType, String nombreArchivo) {
}
