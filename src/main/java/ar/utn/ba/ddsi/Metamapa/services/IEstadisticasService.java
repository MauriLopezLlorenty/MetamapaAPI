package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.Registro;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IEstadisticasService {
  void calcularEstadisticas();
  List<Registro> generarEstadisticasDeCategoriaYHechos();
  List<Registro> generarEstadisticasDeHorasyHechosDeUnaCategoria(Long categoria_id);
  List<Registro> generarEstadisticasDeSolicitudesDeEliminacion();
  List<Registro> generarEstadisticasDeProvinciasDeUnaColeccion(Long coleccionId);
  List<Registro> generarEstadisticasDeProvinciasDeUnaCategoria(Long categoriaId);
  ResponseEntity<Resource> exportarEstadisticasAArchivoCSV(List<Registro> registros, String atributoX, String atributoY)throws Exception;

  ResponseEntity<Resource> exportarEstadisticasDeCategoriaYHechos()throws Exception;
  ResponseEntity<Resource> exportarEstadisticasDeHorasyHechosDeUnaCategoria(Long categoria_id) throws Exception;
  ResponseEntity<Resource> exportarEstadisticasDeSolicitudesDeEliminacion() throws Exception;
  ResponseEntity<Resource> exportarEstadisticasDeProvinciasDeUnaColeccion(Long coleccionId) throws Exception;
  ResponseEntity<Resource> exportarEstadisticasDeProvinciasDeUnaCategoria(Long categoriaId) throws Exception;
}

