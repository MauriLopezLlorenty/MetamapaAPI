package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.Estadisticas;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.EstadisticasDeCategorias;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.EstadisticasDeHorasPorCategoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.EstadisticasDeProvinciasPorCategoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.EstadisticasDeProvinciasPorColeccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.EstadisticasDeSolicitudes;
import ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas.Registro;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import ar.utn.ba.ddsi.Metamapa.services.IEstadisticasService;
import com.opencsv.CSVWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class EstadisticasService implements IEstadisticasService {

  private HechosRepository hechosRepository;
  private ColeccionRepository coleccionRepository;
  private SolicitudDeEliminacionRepository solicitudesRepository;
  public List<Estadisticas> estadisticas = new ArrayList<>();
  private CategoriaRepository categoriaRepository;



  public EstadisticasService(HechosRepository hRepository,
                             ColeccionRepository cRepository,
                             SolicitudDeEliminacionRepository sRepository,
                              CategoriaRepository categoriaRepo){
    this.hechosRepository=hRepository;
    this.coleccionRepository=cRepository;
    this.solicitudesRepository=sRepository;
    this.categoriaRepository=categoriaRepo;
  }
  @Override
  public void calcularEstadisticas() {
    this.estadisticas = new ArrayList<>();

    //categorias y hechos
    EstadisticasDeCategorias estadisticasDeCat = new EstadisticasDeCategorias(this.hechosRepository);
    estadisticas.add(estadisticasDeCat);

    //horas y cant de hechos de cada categoria
    List<Long> idsCat = this.categoriaRepository.findAll().stream().map(c -> c.getId()).toList();
    List<EstadisticasDeHorasPorCategoria> listaEstadisticasDeHorasPorCat = new ArrayList<>();
    idsCat.forEach(i -> {
      listaEstadisticasDeHorasPorCat.add(
              new EstadisticasDeHorasPorCategoria(this.hechosRepository, i));
    });
    estadisticas.addAll(listaEstadisticasDeHorasPorCat);

    //spam y solicitudes de eliminacion
    EstadisticasDeSolicitudes estadisticasDeSolic = new EstadisticasDeSolicitudes(this.solicitudesRepository);
    estadisticas.add(estadisticasDeSolic);

    //provincias y cant de hechos de cada categoria
    List<EstadisticasDeProvinciasPorCategoria> listaEstadisticasDeProvinciasPorCat = new ArrayList<>();
    idsCat.forEach(i -> {
      listaEstadisticasDeProvinciasPorCat.add(
              new EstadisticasDeProvinciasPorCategoria(this.hechosRepository, i));

    });
    estadisticas.addAll(listaEstadisticasDeProvinciasPorCat);

    //provincias y cant de hechos de cada coleccion
    List<Long> idsColec = coleccionRepository.findAll().stream().map(c -> c.getId()).toList();
    List<EstadisticasDeProvinciasPorColeccion> listaEstadisticasDeProvinciasPorColec = new ArrayList<>();
    idsColec.forEach(i -> {
      listaEstadisticasDeProvinciasPorColec.add(
              new EstadisticasDeProvinciasPorColeccion(this.hechosRepository, i));
    });
    estadisticas.addAll(listaEstadisticasDeProvinciasPorColec);

    estadisticas.forEach(Estadisticas::generarEstadisticas);


  }

  @Override
  public List<Registro> generarEstadisticasDeCategoriaYHechos(){
    return this.estadisticas.stream().filter(e->e instanceof EstadisticasDeCategorias).
            findFirst().map(e -> ((EstadisticasDeCategorias) e).getRegistros())
            .stream().findFirst()
            .orElse(List.of());
  }

  @Override
  public List<Registro> generarEstadisticasDeHorasyHechosDeUnaCategoria(Long categoria_id) {
    List<Registro> registros = this.estadisticas.stream()
            .filter(e->e instanceof EstadisticasDeHorasPorCategoria)
            .filter(e->((EstadisticasDeHorasPorCategoria) e).getIdCategoria().equals(categoria_id))
            .findFirst().orElseThrow(()->new RuntimeException("No se encontró la categoria")).getRegistros();
    return registros;
  }

  @Override
  public List<Registro> generarEstadisticasDeSolicitudesDeEliminacion() {
    return this.estadisticas.stream().filter(e-> e instanceof EstadisticasDeSolicitudes)
            .findFirst().map(e->((EstadisticasDeSolicitudes)e).getRegistros())
            .stream().findFirst()
            .orElse(List.of());
  }

  @Override
  public List<Registro> generarEstadisticasDeProvinciasDeUnaColeccion(Long coleccionId) {
    List<Registro> registros = this.estadisticas.stream().filter(e->e instanceof EstadisticasDeProvinciasPorColeccion)
            .filter(e->((EstadisticasDeProvinciasPorColeccion) e)
                    .getIdColeccion().equals(coleccionId))
            .findFirst().orElseThrow(()->new RuntimeException("No se encontró la coleccion")).getRegistros();
    return registros;
  }

  @Override
  public List<Registro> generarEstadisticasDeProvinciasDeUnaCategoria(Long categoriaId) {
    List<Registro> registros = this.estadisticas.stream().filter(e->e instanceof EstadisticasDeProvinciasPorCategoria)
            .filter(e->((EstadisticasDeProvinciasPorCategoria) e)
                    .getIdCategoria().equals(categoriaId))
            .findFirst().orElseThrow(()->new RuntimeException("No se encontró la categoria")).getRegistros();

    return registros;
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasAArchivoCSV(List<Registro> registros, String atributoX, String atributoY) throws Exception {
    Resource resource = generarCSV(registros,atributoX,atributoY);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estadisticas.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(resource);
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasDeCategoriaYHechos() throws Exception {
    return exportarEstadisticasAArchivoCSV(
            this.generarEstadisticasDeCategoriaYHechos(),"Categoria","Cantidad de hechos");
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasDeHorasyHechosDeUnaCategoria(Long categoria_id) throws Exception {
    return exportarEstadisticasAArchivoCSV(
            this.generarEstadisticasDeHorasyHechosDeUnaCategoria(categoria_id),"Hora","Cantidad de hechos");
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasDeSolicitudesDeEliminacion() throws Exception {
    return exportarEstadisticasAArchivoCSV(
            this.generarEstadisticasDeSolicitudesDeEliminacion(),"Spam/No spam","Cantidad de solicitudes");
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasDeProvinciasDeUnaColeccion(Long coleccionId) throws Exception {
    return exportarEstadisticasAArchivoCSV(
            this.generarEstadisticasDeProvinciasDeUnaColeccion(coleccionId),"Provincia","Cantidad de hechos");
  }

  @Override
  public ResponseEntity<Resource> exportarEstadisticasDeProvinciasDeUnaCategoria(Long categoriaId) throws Exception {
    return exportarEstadisticasAArchivoCSV(
            this.generarEstadisticasDeProvinciasDeUnaCategoria(categoriaId),"Provincia","Cantidad de hechos");
  }

  public Resource generarCSV(List<Registro> registros, String atributoX, String atributoY) throws Exception {

    StringWriter sw = new StringWriter();
    CSVWriter writer = new CSVWriter(sw);

    //encabezado
    writer.writeNext(new String[]{atributoX, atributoY});

    for (Registro registro : registros) {
      writer.writeNext(new String[]{registro.getAtributoX(), registro.getAtributoY()});
    }
    writer.close();

    byte[] csvBytes = sw.toString().getBytes(StandardCharsets.UTF_8);

    return new ByteArrayResource(csvBytes);
  }

  private List<Registro> normalizarRegistros(List<Object[]> registros){
    List<Registro> registrosNormalizados = new ArrayList<>();

    for (Object[] fila : registros) {
      String atributoX = (fila[0] == null || fila[0].toString().isBlank())
              ? "Sin datos"
              : fila[0].toString();

      String atributoY = (fila[1] == null)
              ? "0"
              : fila[1].toString();

      registrosNormalizados.add(new Registro(atributoX, atributoY));
    }

    return registrosNormalizados;
  }
/*
  private List<Registro> normalizarRegistros(List<Object[]> registros){
    List<Registro> registrosNormalizados = new ArrayList<>();
    for(Object[] fila : registros){
      registrosNormalizados.add(new Registro(fila[0].toString(),fila[1].toString()));
    }
    return registrosNormalizados;
  }*/


}


