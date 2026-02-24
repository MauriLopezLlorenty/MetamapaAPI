package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;

public class FiltroPorOrigen implements FiltroHecho{
  private Origen origenBuscado;
  
  public FiltroPorOrigen(Origen origen){
    this.origenBuscado = origen;
  }
  
  public boolean aplicaA(Hecho hecho){
    return hecho.getOrigen().equals(origenBuscado);
  }

}
