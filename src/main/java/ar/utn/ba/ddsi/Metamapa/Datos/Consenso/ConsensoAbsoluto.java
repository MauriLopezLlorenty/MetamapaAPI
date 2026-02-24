package ar.utn.ba.ddsi.Metamapa.Datos.Consenso;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;

import java.util.List;

public class ConsensoAbsoluto implements AlgoritmoConsenso {
  @Override
  public boolean esConsensuado(Hecho hecho, List<Fuente> fuentes) {
    return fuentes.stream()
        // CORRECCIÓN: debe ser h.equals, no equals (que sería this.equals)
        .allMatch(f -> f.obtenerHechos().stream().anyMatch(h -> h.equals(hecho)));
  }
  @Override
  public String getNombre() { return "ABSOLUTO"; }
}
