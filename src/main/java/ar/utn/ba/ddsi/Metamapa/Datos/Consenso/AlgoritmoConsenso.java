package ar.utn.ba.ddsi.Metamapa.Datos.Consenso;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;

import java.util.List;

public interface AlgoritmoConsenso {
  boolean esConsensuado(Hecho hecho, List<Fuente> fuentes);
  String getNombre();
}
