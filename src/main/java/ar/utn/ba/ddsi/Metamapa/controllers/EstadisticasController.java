package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.Registro;
import ar.utn.ba.ddsi.Metamapa.services.IEstadisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/estadisticas")

public class EstadisticasController {
  @Autowired
  private IEstadisticasService estadisticasService;

  @GetMapping("/provinciasYHechosPorColeccion/{coleccionId}")
  public List<Registro> estadisticasDeProvinciasYHechosReportados(@PathVariable Long coleccionId) {
    return estadisticasService.generarEstadisticasDeProvinciasDeUnaColeccion(coleccionId);
  }

  @GetMapping("/categoriasYHechos")
  public List<Registro> estadisticasDeCategoriasYHechos() {
    return estadisticasService.generarEstadisticasDeCategoriaYHechos();
  }

  @GetMapping("/horasYHechosPorCategoria/{categoriaId}")
  public List<Registro> estadisticasDeHorasyHechosDeUnaCategoria(@PathVariable Long categoriaId) {
    return estadisticasService.generarEstadisticasDeHorasyHechosDeUnaCategoria(categoriaId);
  }

  @GetMapping("/provinciasYHechosPorCategoria/{categoriaId}")
  public List<Registro> estadisticasDeProvinciasDeUnaCategoria(@PathVariable Long categoriaId) {
    return estadisticasService.generarEstadisticasDeProvinciasDeUnaCategoria(categoriaId);
  }

  @GetMapping("/solicitudesDeEliminacion")
  public List<Registro> estadisticasDeSolicitudesDeEliminacion() {
    return estadisticasService.generarEstadisticasDeSolicitudesDeEliminacion();
  }

  @GetMapping("/provinciasYHechosPorColeccion/{coleccionId}/descargar")
  public ResponseEntity<Resource> exportarEstaditicasProvinciasYHechos(@PathVariable Long coleccionId) throws Exception {
    return  estadisticasService.exportarEstadisticasDeProvinciasDeUnaColeccion(coleccionId);
  }

  @GetMapping("/categoriasYHechos/descargar")
  public ResponseEntity<Resource> exportarEstadisticasDeCategoriasYHechos() throws Exception{
    return estadisticasService.exportarEstadisticasDeCategoriaYHechos();
  }

  @GetMapping("/horasYHechosPorCategoria/{categoriaId}/descargar")
  public ResponseEntity<Resource> exportarEstadisticasDeHorasyHechosDeUnaCategoria(@PathVariable Long categoriaId) throws Exception{
    return estadisticasService.exportarEstadisticasDeHorasyHechosDeUnaCategoria(categoriaId);
  }

  @GetMapping("/provinciasYHechosPorCategoria/{categoriaId}/descargar")
  public ResponseEntity<Resource> exportarEstadisticasDeProvinciasDeUnaCategoria(@PathVariable Long categoriaId)throws Exception {
    return estadisticasService.exportarEstadisticasDeProvinciasDeUnaCategoria(categoriaId);
  }

  @GetMapping("/solicitudesDeEliminacion/descargar")
  public ResponseEntity<Resource> exportarEstadisticasDeSolicitudesDeEliminacion()throws Exception {
    return estadisticasService.exportarEstadisticasDeSolicitudesDeEliminacion();
  }



}
