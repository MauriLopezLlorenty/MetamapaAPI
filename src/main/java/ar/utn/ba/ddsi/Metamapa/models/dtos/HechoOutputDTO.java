package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HechoOutputDTO {
  private Long id;
  private String descripcion;
  private String categoria;
  private double latitud;
  private double longitud;
  private LocalDateTime fechaDelHecho;
  private String origen;
  private List<String> etiquetas;
  private String titulo;
  private LocalDateTime fechaDeCarga;
}
