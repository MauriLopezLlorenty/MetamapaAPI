package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudRevisionDTO {   // Unifico los dos tipos de pedidos
  private String tituloFinal;
  private String descripcionFinal;
  private String archivoMultimediaFinal;
  private LocalDateTime fechaDelHechoFinal;
  private Double latitudFinal;
  private Double longitudFinal;
}