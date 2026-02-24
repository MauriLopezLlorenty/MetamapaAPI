package ar.utn.ba.ddsi.Metamapa;



import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.DatasetCSV;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import ar.utn.ba.ddsi.Metamapa.Fuente.FuenteEstatica;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

public class Escenario_2 {
  @Test
  public void testCargaHechosDesdeCSV() throws Exception { //Chequea q el csv no este vacio y que se puede acceder
    URL resource = getClass().getClassLoader().getResource("csv_test/Escenario1.csv");
    assertNotNull(resource, "CSV no encontrado");

    String ruta = Paths.get(resource.toURI()).toString();

    List<Hecho> hechos = new DatasetCSV().leerArchivo(ruta);
    System.out.println("Se cargaron " + hechos.size() + " hechos.");

    assertNotNull(hechos, "La lista de hechos es null");
    assertFalse(hechos.isEmpty(), "No se cargaron hechos");


  }

  @Test
  public void testCargaHechosDesdeFuente() throws Exception {
    URL resource = getClass().getClassLoader().getResource("csv_test/Escenario1.csv");
    assertNotNull(resource, "CSV no encontrado");

    String ruta = Paths.get(resource.toURI()).toString();
    Fuente fuente = new FuenteEstatica(new DatasetCSV(), ruta);

    List<Hecho> hechos = fuente.obtenerHechos();

    assertNotNull(hechos, "La lista de hechos es null");
    assertFalse(hechos.isEmpty(), "No se cargaron hechos");

    System.out.println("✅ Se cargaron " + hechos.size() + " hechos:");
    hechos.forEach(h -> System.out.println("- " + h.getTitulo()));
  }

  @Test
  public void testCargaHechosDesdeColeccion() throws Exception { //Chequea q el csv no este vacio y que se puede acceder
    URL resource = getClass().getClassLoader().getResource("csv_test/Escenario1.csv");
    assertNotNull(resource, "El archivo CSV no se encontró");

    System.out.println("Ruta CSV encontrada: " + resource.getPath());

    Fuente fuente = new FuenteEstatica(new DatasetCSV(), resource.getPath());
    Coleccion coleccion = new Coleccion("Colección prueba", fuente, "Esto es una prueba");

    coleccion.cargarHechosDesdeFuente();

    List<Hecho> hechos = coleccion.getHechos();
    hechos.forEach(h -> System.out.println("- " + h.getTitulo() + ":" + h.getDescripcion() + " Categoria:" + h.getCategoria() + h.getFechaDelHecho()));

    assertNotNull(coleccion.getHechos(), "La lista de hechos es null");
  }

  @Test
  public void testProcesaArchivoDeMasDe10000Hechos() {
    String rutaArchivo = Objects.requireNonNull(getClass().getResource("/csv_test/desastres_tecnologicos_argentina.csv")).getPath();
    Fuente fuenteEstatica = new FuenteEstatica(new DatasetCSV(), rutaArchivo);
    List<Hecho> hechos = fuenteEstatica.obtenerHechos();

    assertFalse(hechos.isEmpty());
  }

  @Test
  public void testProcesaArchivoConHechosRepetidos() {
    String rutaArchivo = Objects.requireNonNull(getClass().getResource("/csv_test/4HechosRepetidos.csv")).getPath();
    Fuente fuenteEstatica = new FuenteEstatica(new DatasetCSV(), rutaArchivo);
    List<Hecho> hechos = fuenteEstatica.obtenerHechos();

    assertEquals(4, hechos.size(), "Se cargaron hechos repetidos");
  }

  @Test
  public void testSeInicializanHechosObtenidosDesdeArchivoCSV() {
    String rutaArchivo = Objects.requireNonNull(getClass().getResource("/csv_test/4HechosRepetidos.csv")).getPath();
    Fuente fuenteEstatica = new FuenteEstatica(new DatasetCSV(), rutaArchivo);
    List<Hecho> hechos = fuenteEstatica.obtenerHechos();

    assertTrue(hechos.stream().allMatch(hecho -> hecho != null));

    for (Hecho hecho : hechos) {

      System.out.printf("--Titulo: %s--%n -Descripcion: %s%n -Categoria: %s%n -Lugar: Latitud %f y Longitud %f%n" +
              " -Fecha del hecho: %s%n -Fecha de carga: %s%n -Origen: %s%n -Visible: %s%n -Contribuyente: %s%n -Etiquetas: %s%n%n",
          hecho.getTitulo(), hecho.getDescripcion(), hecho.getCategoria(), hecho.getLugar().getLatitud(), hecho.getLugar().getLongitud(),
          hecho.getFechaDelHecho(), hecho.getFechaDeCarga(), hecho.getOrigen(), hecho.getVisible(), hecho.getContribuyente(), hecho.getEtiquetas());
    }

  }
}
