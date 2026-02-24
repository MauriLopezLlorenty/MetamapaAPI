package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import java.util.List;

public interface IColeccionService {
  List<HechoOutputDTO> filtrarHechos(
      String handle, String categoria, String etiqueta,
      String longitud, String latitud, String titulo,
      boolean soloVisibles,
      String fechaCargaDesde, String fechaCargaHasta,
      String fechaHechoDesde, String fechaHechoHasta,
      String origen, String modo
  );
  public List<HechoOutputDTO> obtenerHechosDeColeccion(String handle);

}
