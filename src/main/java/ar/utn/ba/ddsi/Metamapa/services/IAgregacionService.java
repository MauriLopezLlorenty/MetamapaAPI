package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface IAgregacionService {
  void refrescarColecciones();
  List<Hecho> obtenerHechosDeColeccion(String handle);

  List<HechoOutputDTO> filtrarHechos(
      String handle,
      String categoria,
      String etiqueta,
      String longitud,
      String latitud,
      String titulo,
      Boolean visible,
      String fechaDeCargaDesde,
      String fechaDeCargaHasta,
      String fechaDeHechoDesde,
      String fechaDeHechoHasta,
      String origen
  );

}