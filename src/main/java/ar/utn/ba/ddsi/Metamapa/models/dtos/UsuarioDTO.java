package ar.utn.ba.ddsi.Metamapa.models.dtos;
import lombok.Data;

@Data
public class UsuarioDTO {
  private String nombre;
  private String apellido;
  private String nombreUsuario;
  private String mail;
  private String contrasena;
  private String ubicacion;
}