package ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class DesastreResponseDTO {
    private long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private Double latitud;
    private Double longitud;
    private String fecha_hecho;
    private String created_at;
}