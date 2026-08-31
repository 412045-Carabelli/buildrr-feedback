package ar.buildrr.feedback.ticket.factory;

/** Helper chico compartido por los factories — no amerita una clase de servicio propia. */
final class TicketFactoryUtil {

  private TicketFactoryUtil() {
  }

  /** El editor (Quill) manda "&lt;p&gt;&lt;br&gt;&lt;/p&gt;" cuando está vacío, no un string vacío. */
  static boolean esDescripcionVacia(String descripcionHtml) {
    if (descripcionHtml == null) {
      return true;
    }
    String sinTags = descripcionHtml.replaceAll("<[^>]*>", "").trim();
    return sinTags.isEmpty();
  }
}
