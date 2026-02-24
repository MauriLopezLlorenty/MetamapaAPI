package ar.utn.ba.ddsi.Metamapa;


import ar.utn.ba.ddsi.Metamapa.Datos.*;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltrarPorCategoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltrarPorTitulo;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroCompuesto;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroHecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.DatasetCSV;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import ar.utn.ba.ddsi.Metamapa.Fuente.FuenteEstatica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.time.LocalDate;
import java.util.stream.Collectors;


public class Escenario_1 {
  private Coleccion coleccion;
  private Fuente fuente;


  URL resource = getClass().getClassLoader().getResource("csv_test/Escenario1.csv");


  @BeforeEach
  void setUp() {
    coleccion = new Coleccion("Colección prueba",fuente, "Esto es una prueba");


    coleccion.agregarHecho(new Hecho("Caída de aeronave impacta en Olavarría", "...",null , "Caída de aeronave", -36.8683, -60.3432, LocalDateTime.of(2001, 11, 29,0,0),null,null));
    coleccion.agregarHecho(new Hecho("Accidente con maquinaria industrial en Chos Malal", "...",null , "Accidente con maquinaria industrial", -37.3455, -70.2414, LocalDateTime.of(2001, 8, 16,0,0),null,null));
    coleccion.agregarHecho(new Hecho("Caída de aeronave impacta en Venado Tuerto", "...",null , "Caída de aeronave", -33.7680, -61.9210, LocalDateTime.of(2008, 8, 8,0,0),null,null));
    coleccion.agregarHecho(new Hecho("Accidente en paso a nivel deja múltiples daños", "...",null , "Accidente en paso a nivel", -35.8558, -61.9405, LocalDateTime.of(2020, 1, 27,0,0),null,null));
    coleccion.agregarHecho(new Hecho("Devastador derrumbe en obra afecta a Sáenz Peña", "...", null ,"Derrumbe en obra en construcción", -26.7800, -60.4587, LocalDateTime.of(2016, 6, 4,0,0),null,null));


  }
  @Test
  void crearColeccionManual() {
    List<Hecho> hechos = coleccion.getHechos();

    assertFalse(hechos.isEmpty());
    assertEquals(5, hechos.size());
  }





  /*URL resource = getClass().getClassLoader().getResource("csv_test/Escenario1.csv");
@Test
void crearColeccionManual(){
  Fuente fuente = new FuenteEstatica(new DatasetCSV(),resource.getPath());
  System.out.println("Ruta CSV encontrada: " + fuente);
  Coleccion coleccion = new Coleccion("Colección prueba",fuente, "Esto es una prueba");

  coleccion.cargarHechosDesdeFuente();
  System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");

  List<Hecho> hechos = coleccion.getHechos();
  hechos.forEach(h -> System.out.println("- " + h.getTitulo()+":"+ h.getDescripcion() + " Categoria:" + h.getCategoria() + h.getFechaDelHecho()));

  assertFalse(coleccion.getHechos().isEmpty());

}*/
@Test
public void agregarCriterios(){
  Predicate<Hecho> porFecha = h ->
          !h.getFechaDelHecho().isBefore(LocalDateTime.of(2000, 1, 1,0,0)) &&
                  !h.getFechaDelHecho().isAfter(LocalDateTime.of(2010, 1, 1,0,0));
  CriterioPertenencia criterio = new Criterio(porFecha);
  coleccion.setCriterio(criterio);

  List<Hecho> filtradosFecha = coleccion.getHechos().stream()
          .filter(coleccion.getCriterio()::cumpleCondicion)
          .collect(Collectors.toList());

  assertEquals(3, filtradosFecha.size());

  Predicate<Hecho> nuevoCriterio = h ->
          h.getCategoria().getNombre().equalsIgnoreCase("Caída de aeronave") &&
                  !h.getFechaDelHecho().isBefore(LocalDateTime.of(2000, 1, 1,0,0)) &&
                  !h.getFechaDelHecho().isAfter(LocalDateTime.of(2010, 1, 1,0,0));

  CriterioPertenencia criterioC = new Criterio(nuevoCriterio);
  coleccion.setCriterio(criterioC);

  List<Hecho> filtradosFinal = coleccion.getHechos().stream()
          .filter(coleccion.getCriterio())
          .collect(Collectors.toList());

  assertEquals(2, filtradosFinal.size());
  assertTrue(filtradosFinal.stream().allMatch(h -> h.getCategoria().getNombre().equals("Caída de aeronave")));
}



@Test
public void aplicarFiltros() throws URISyntaxException {

  System.out.println("Ruta CSV encontrada: " + resource.getPath());

  Fuente fuente = new FuenteEstatica(new DatasetCSV(),resource.getPath());
  Coleccion coleccion = new Coleccion("Colección prueba",fuente, "Esto es una prueba");

  FiltroHecho filtroCategoria = new FiltrarPorCategoria("Caída aereonave");
  FiltroHecho filtroTitulo = new FiltrarPorTitulo("un título");
  FiltroCompuesto compuesto = new FiltroCompuesto(List.of(filtroCategoria,filtroTitulo));

  coleccion.cargarHechosDesdeFuente();

  System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");

  List <Hecho> filtrados = coleccion.filtrarHechos(compuesto);
  System.out.println("Hechos que cumplen filtros"+ filtrados);

  assertTrue(filtrados.isEmpty(), "No debería haber hechos con esa categoría y ese título");
}
@Test
void etiquetar(){
  System.out.println("Ruta CSV encontrada: " + resource.getPath());

  Fuente fuente = new FuenteEstatica(new DatasetCSV(),resource.getPath());
  Coleccion coleccion = new Coleccion("Colección prueba",fuente, "Esto es una prueba");

  coleccion.cargarHechosDesdeFuente();

  System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");

  Hecho hecho = coleccion.getHechos().stream()
      .filter(h -> h.getTitulo().equalsIgnoreCase("Caida de aeronave impacta en Olavarria"))
      .findFirst().orElse(null);

  Etiqueta olavarria = new Etiqueta("Olavarria");
  Etiqueta grave = new Etiqueta("Grave");


  hecho.agregarEtiqueta(olavarria);
  hecho.agregarEtiqueta(grave);

  hecho.getEtiquetas().forEach(e -> System.out.println(e.getNombre()));

  assertEquals(2, hecho.getEtiquetas().size(), "Debe haber exactamente 2 etiquetas");
  assertTrue(hecho.getEtiquetas().contains(olavarria),"Debe contener Olavarria");
  assertTrue(hecho.getEtiquetas().contains(grave),"Debe contener Grave");

}

//******************FIN ESCENARIO 1***************************

}
