package ar.utn.ba.ddsi.Metamapa.models.dtos;


import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudModInputDTO {
    private Long hechoId;

    // Campos opcionales (si vienen null, es que no se modifican)
    private String nuevoTitulo;
    private String nuevaDescripcion;
    private String nuevoArchivoMultimedia;
    private LocalDateTime nuevaFechaDelHecho;

    private Long nuevaCategoriaId; // ID de la categoría seleccionada
    private Double nuevaLatitud;
    private Double nuevaLongitud;
}