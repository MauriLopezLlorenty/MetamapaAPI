package ar.utn.ba.ddsi.Metamapa.Fuente;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

import java.util.List;

public interface DataSet {
  public default List<Hecho> leerArchivo(String rutaArchivo) {
    return null;
  }
}
