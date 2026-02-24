package ar.utn.ba.ddsi.Metamapa.graphql.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HechoFilterInput {
    private String titulo;
    private String categoria;
    private String origen;
    private String coleccion;
    private LocalDateTime fechaDelHechoDesde;
    private LocalDateTime fechaDelHechoHasta;
    private LocalDateTime fechaDeCargaDesde;
    private LocalDateTime fechaDeCargaHasta;
    private Boolean visible;
    private List<String> etiquetas;
    private Double latitud;
    private Double longitud;
    private Double radio;
}
