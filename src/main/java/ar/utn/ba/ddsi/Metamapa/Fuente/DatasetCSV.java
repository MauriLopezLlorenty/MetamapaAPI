package ar.utn.ba.ddsi.Metamapa.Fuente;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatasetCSV implements DataSet {


  public List<Hecho> leerArchivo(String rutaArchivo) {
    List<Hecho> hechos = new ArrayList<>();


    /*try {
      // abre el archivo
      CSVReader reader = new CSVReader(new FileReader(rutaArchivo));
      reader.readNext(); //lee la primer linea para saltar el encabezado
      String[] linea;
      while ((linea = reader.readNext()) != null) {
        Hecho nuevoHecho = inicializarHecho(linea);
        gestionarRepetidos(nuevoHecho, hechos);
      }
    } catch (IOException e) {
      System.out.printf("No se pudo leer el archivo. Error: "+ e.getMessage());
    } catch (CsvValidationException e) {
      System.out.printf("Error con el contenido del archivo CSV. Error: "+ e.getMessage());
    }*/

    try (CSVReader reader = new CSVReader(new FileReader(rutaArchivo))){
      reader.readNext();
      String[] linea;
      while((linea = reader.readNext())!= null){
        Hecho nuevoHecho = inicializarHecho(linea);
        gestionarRepetidos(nuevoHecho,hechos);
      }
    } catch (IOException | CsvValidationException e){
      System.out.println("Error leyendo el archivo:"+ e.getMessage());
    }
    return hechos;
  }


  public Hecho inicializarHecho(String[] linea) {
    String titulo = linea[0];
    String descripcion = linea[1];
    String categoria = linea[2];
    double latitud = Double.parseDouble(linea[3]); // String -> double
    double longitud = Double.parseDouble(linea[4]);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDateTime fechaDelHecho = LocalDateTime.parse(linea[5], formatter); // String -> LocalDate

    Origen origen = Origen.DATASET;

    return new Hecho(titulo,descripcion,null,categoria,latitud,longitud,fechaDelHecho,origen,null );
  }


  public void gestionarRepetidos(Hecho nuevoHecho, List<Hecho> hechos) {
   for (int i = 0; i < hechos.size(); i++) {
     if (hechos.get(i).getTitulo().equalsIgnoreCase(nuevoHecho.getTitulo())) {
   //Recorre la lista y compara el titulo del nuevo con los de la lista
       hechos.set(i, nuevoHecho); // Si lo encuentra,lo pisa
       return;
   }

  }
    hechos.add(nuevoHecho); //agregar hecho si no esta repetido
}

}

