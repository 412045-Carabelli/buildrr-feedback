package ar.buildrr.feedback.ticket.factory;

import ar.buildrr.feedback.ticket.entity.TipoTicket;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TicketFactoryResolver {

  private final Map<TipoTicket, TicketFactory> factoriesPorTipo;

  public TicketFactoryResolver(List<TicketFactory> factories) {
    this.factoriesPorTipo = factories.stream()
        .collect(Collectors.toMap(TicketFactory::tipoSoportado, Function.identity()));
  }

  public TicketFactory resolver(TipoTicket tipo) {
    TicketFactory factory = factoriesPorTipo.get(tipo);
    if (factory == null) {
      throw new IllegalStateException("Sin factory para tipo de ticket: " + tipo);
    }
    return factory;
  }
}
