package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Registro {
  private String atributoX;
  private String atributoY;

  public Registro(String atributoX, String atributoY) {
    this.atributoX = atributoX;
    this.atributoY = atributoY;
  }
}
