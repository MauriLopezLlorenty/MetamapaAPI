package ar.utn.ba.ddsi.Metamapa.Datos;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lugar")
@Getter
@Setter
@NoArgsConstructor
public class Lugar {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String nombreNormalizado;
  @Column
  private double latitud;
  @Column
  private double longitud;

  public Lugar(double latitud, double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;

  }
  public boolean sonIguales(Lugar otrolugar){
    return this.latitud == otrolugar.getLatitud()&&
    this.longitud == otrolugar.getLongitud();
  }

}