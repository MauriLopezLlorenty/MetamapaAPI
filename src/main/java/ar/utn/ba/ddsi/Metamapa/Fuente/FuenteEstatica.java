package ar.utn.ba.ddsi.Metamapa.Fuente;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter; // <--- IMPORTANTE
import lombok.NoArgsConstructor;
import lombok.Setter; // <--- Agregalo por las dudas

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "fuente_estatica")
public class FuenteEstatica implements Fuente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Transient
  private DataSet dataSet;

  private String ruta;

  public FuenteEstatica(DataSet dataSet, String ruta) {
    this.dataSet = dataSet;
    this.ruta = ruta;
  }

  @Override
  public List<Hecho> obtenerHechos() {

    if (dataSet == null) {
      return new ArrayList<>();
    }
    List<Hecho> hechos = dataSet.leerArchivo(ruta);
    return hechos != null ? hechos : new ArrayList<>();
  }
}