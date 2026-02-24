package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

public class FiltrarPorVisible implements FiltroHecho{
  
  public boolean aplicaA(Hecho hecho) {
    return hecho.getVisible();
  }
}
