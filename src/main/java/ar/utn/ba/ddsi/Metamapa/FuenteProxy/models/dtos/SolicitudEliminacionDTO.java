package ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudEliminacionDTO {
    private Hecho hecho;
    private String motivo;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEstado;
}