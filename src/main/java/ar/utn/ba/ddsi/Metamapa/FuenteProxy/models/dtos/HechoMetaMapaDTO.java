package ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos;

import lombok.Data;

@Data
public class HechoMetaMapaDTO {

    private long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private Double latitud;
    private Double longitud;
    private String fechaHecho;


}