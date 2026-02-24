package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
//TODO chequear repeticion de logica con fecha de hecho

public class FiltroPorFechaDeCarga implements FiltroHecho{
  private LocalDateTime fDesde;
  private LocalDateTime fHasta;

  public FiltroPorFechaDeCarga(LocalDateTime desde, LocalDateTime hasta){ //Filtra hechos que ocurrieron entre dos fechas
    this.fDesde = desde;
    this.fHasta = hasta;
  }

  public boolean aplicaA(Hecho hecho){
    if (hecho.getFechaDeCarga() == null) return false;
    return !hecho.getFechaDeCarga().isBefore(fDesde) &&
        !hecho.getFechaDeCarga().isAfter(fHasta);
  }
}
