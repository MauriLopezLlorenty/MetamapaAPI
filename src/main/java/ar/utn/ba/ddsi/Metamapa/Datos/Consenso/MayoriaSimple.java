package ar.utn.ba.ddsi.Metamapa.Datos.Consenso;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;

import java.util.List;

public class MayoriaSimple implements AlgoritmoConsenso {
  @Override
  public boolean esConsensuado(Hecho hecho, List<Fuente> fuentes) {
    long contador = fuentes.stream()
        .filter(f -> f.obtenerHechos().stream().anyMatch(h -> h.equals(hecho)))
        .count();
    double mitad = Math.ceil(fuentes.size() / 2.0);
    return contador >= mitad;
  }
  @Override
  public String getNombre() { return "MAYORIA"; }
}
