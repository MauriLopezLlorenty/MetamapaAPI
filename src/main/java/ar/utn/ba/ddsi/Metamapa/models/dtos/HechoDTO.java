package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HechoDTO {
    private Long id;
    private Long idColeccion;
    private String descripcion;
    private String categoria;
    private double latitud;
    private double longitud;
    private LocalDateTime fechaDelHecho;
    private String origen;
    private List<String> etiquetas;
    private String titulo;
    private LocalDateTime fechaDeCarga;
    private String archivoMultimedia;
}
