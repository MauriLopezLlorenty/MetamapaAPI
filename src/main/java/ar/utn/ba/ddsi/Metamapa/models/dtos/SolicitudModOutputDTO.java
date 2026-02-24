package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class SolicitudModOutputDTO {
    private Long id;
    private Hecho hecho;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEstado;
    private String nombreUsuarioTexto;
    private String propuestaTitulo;
    private String propuestaDescripcion; // <--- ESTE ES EL QUE TE FALTA
    private String propuestaArchivoMultimedia;
    private LocalDateTime propuestaFechaDelHecho;
    private Categoria propuestaCategoria;
    private Lugar propuestaLugar;

}
