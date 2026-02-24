package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class HechoInputDTO {
    private String titulo;
    private String descripcion;
    private String categoriaNombre;
    private double latitud;
    private double longitud;
    private LocalDateTime fechaDelHecho;
    private String origen;
    private List<String> etiquetasNombres;
}
