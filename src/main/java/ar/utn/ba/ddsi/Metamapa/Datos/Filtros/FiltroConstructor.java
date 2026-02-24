package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;

import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FiltroConstructor {
  private List<FiltroHecho> filtros = new ArrayList<>();

  public FiltroConstructor porCategoria(String categoria) {
    if (categoria != null)
      filtros.add(new FiltrarPorCategoria(categoria));
    return this;
  }
  public FiltroConstructor porEtiqueta(String etiqueta) {
    if (etiqueta != null)
      filtros.add(new FiltrarPorEtiquetas(etiqueta));
    return this;
  }
  public FiltroConstructor porLugar(String latitud, String longitud) {
    if (latitud != null && longitud != null)
      filtros.add(new FiltrarPorLugar(new Lugar(Double.parseDouble(latitud),Double.parseDouble(longitud))));
    return this;
  }
  public FiltroConstructor porTitulo(String titulo) {
    if (titulo != null)
      filtros.add(new FiltrarPorTitulo(titulo));
    return this;
  }
  public FiltroConstructor porVisible(Boolean visible) {
    if (visible)
      filtros.add(new FiltrarPorVisible());
    return this;
  }
  public FiltroConstructor porFechaDeCarga(String desde, String hasta) {
    // Si ambos son nulos, no hacemos nada
    if ((desde == null || desde.isEmpty()) && (hasta == null || hasta.isEmpty())) {
      return this;
    }

    // Si falta 'desde', asumimos el inicio de los tiempos
    LocalDateTime fDesde = (desde != null && !desde.isEmpty())
        ? LocalDateTime.parse(desde)
        : LocalDateTime.MIN;

    // Si falta 'hasta', asumimos el futuro lejano
    LocalDateTime fHasta = (hasta != null && !hasta.isEmpty())
        ? LocalDateTime.parse(hasta)
        : LocalDateTime.MAX;

    filtros.add(new FiltroPorFechaDeCarga(fDesde, fHasta));
    return this;
  }
  public FiltroConstructor porFechaDeHecho(String desde, String hasta) {
    // Si ambos son nulos, no hacemos nada
    if ((desde == null || desde.isEmpty()) && (hasta == null || hasta.isEmpty())) {
      return this;
    }

    LocalDateTime fDesde = (desde != null && !desde.isEmpty())
        ? LocalDateTime.parse(desde)
        : LocalDateTime.MIN;

    LocalDateTime fHasta = (hasta != null && !hasta.isEmpty())
        ? LocalDateTime.parse(hasta)
        : LocalDateTime.MAX;

    filtros.add(new FiltroPorFechaDeHecho(fDesde, fHasta));
    return this;
  }
  public FiltroConstructor porOrigen(String origen) {
    if (origen != null)
      filtros.add(new FiltroPorOrigen(Origen.valueOf(origen)));
    return this;
  }
}
