package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FiltroPorFechaDeHecho implements FiltroHecho {
  private LocalDateTime fDesde;
  private LocalDateTime fHasta;

  public FiltroPorFechaDeHecho(LocalDateTime desde, LocalDateTime hasta){
    this.fDesde = desde;
    this.fHasta = hasta;
  }

  @Override
  public boolean aplicaA(Hecho hecho){
    if (hecho.getFechaDelHecho() == null) return false;

    // Lógica: NO debe ser antes de 'desde' Y NO debe ser después de 'hasta'
    // Esto equivale a: desde <= fecha <= hasta
    return !hecho.getFechaDelHecho().isBefore(fDesde) &&
        !hecho.getFechaDelHecho().isAfter(fHasta);
  }
}