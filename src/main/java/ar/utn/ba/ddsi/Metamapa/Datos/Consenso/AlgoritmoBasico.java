package ar.utn.ba.ddsi.Metamapa.Datos.Consenso;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;

import java.util.List;

public class AlgoritmoBasico implements AlgoritmoConsenso {
  @Override
  public boolean esConsensuado(Hecho hecho, List<Fuente> fuentes) {
    return true;
  }
  @Override
  public String getNombre() { return "BASICO"; }
}
