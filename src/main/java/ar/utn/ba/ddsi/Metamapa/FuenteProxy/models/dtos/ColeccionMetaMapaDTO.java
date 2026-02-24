package ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.CriterioPertenencia;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import lombok.Data;

import java.util.List;

@Data
public class ColeccionMetaMapaDTO {
    private String nombreColeccion;
    private Fuente fuente;
    private String descripcion;
    private List<Hecho> hechos;
    private CriterioPertenencia criterio;

}