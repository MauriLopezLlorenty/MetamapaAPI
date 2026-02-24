package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Criterio;
import ar.utn.ba.ddsi.Metamapa.Fuente.TipoFuente;
import lombok.Data;

import java.util.Set;

@Data
public class ColeccionOutputDTO {
  private String nombreColeccion;
  private String descripcion;
  private String handle;

  // Enviamos el objeto Criterio completo para que el JS genere los badges
  private Criterio criterio;

  // Enviamos el nombre del algoritmo (ej: "MayoriaSimple")
  private String consenso;
  private Set<TipoFuente> fuentesPermitidas;
}