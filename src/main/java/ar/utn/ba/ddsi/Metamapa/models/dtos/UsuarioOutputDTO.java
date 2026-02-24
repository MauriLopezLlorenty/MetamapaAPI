package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class UsuarioOutputDTO {

  private Long id;
  private String nombre;
  private String apellido;
  private String nombreUsuario;
  private String mail;
  private String ubicacion;
  private String rol;
  private long hechosCargados;
  private List<Map<String, String>> ultimasSolicitudes;
}