package ar.utn.ba.ddsi.Metamapa.Datos.Consenso;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;

import java.util.List;

public class MultiplesMenciones implements AlgoritmoConsenso {
  @Override
  public boolean esConsensuado(Hecho hecho, List<Fuente> fuentes) {
    // Obtenemos todos los hechos de todas las fuentes en un solo stream
    var todosLosHechos = fuentes.stream()
        .flatMap(f -> f.obtenerHechos().stream())
        .toList();

    long coincidenciasExactas = todosLosHechos.stream()
        .filter(h -> h.getTitulo().equalsIgnoreCase(hecho.getTitulo()))
        .filter(h -> h.equals(hecho))
        .count();

    long titulosIgualesPeroDistintos = todosLosHechos.stream()
        .filter(h -> h.getTitulo().equalsIgnoreCase(hecho.getTitulo()))
        .filter(h -> !h.equals(hecho))
        .count();

    return coincidenciasExactas >= 2 && titulosIgualesPeroDistintos == 0;
  }
  @Override
  public String getNombre() { return "MULTIPLES"; }
}
