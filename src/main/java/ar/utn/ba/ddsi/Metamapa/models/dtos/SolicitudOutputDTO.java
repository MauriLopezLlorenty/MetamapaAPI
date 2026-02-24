package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudOutputDTO {
  private Long id;
  private Hecho hecho;
  private String motivo;
  private EstadoSolicitud estado;
  private LocalDateTime fechaSolicitud;
  private LocalDateTime fechaEstado;
  private Usuario usuario;
}
