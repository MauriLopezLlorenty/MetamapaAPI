package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

import java.util.List;


public class FiltroCompuesto implements FiltroHecho{
  private List<FiltroHecho> filtros;

  public FiltroCompuesto(List<FiltroHecho> filtros){
    this.filtros = filtros;
  }
  public boolean aplicaA(Hecho hecho){
    return filtros.stream().allMatch(f->f.aplicaA(hecho));
  }

}
