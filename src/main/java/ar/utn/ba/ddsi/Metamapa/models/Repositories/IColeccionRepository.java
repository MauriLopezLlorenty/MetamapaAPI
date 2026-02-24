package ar.utn.ba.ddsi.Metamapa.models.Repositories;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import java.util.List;

public interface IColeccionRepository {
  void actualizarColecciones(Hecho hechos);
  Coleccion obtenerColeccionPor(String handle);

}