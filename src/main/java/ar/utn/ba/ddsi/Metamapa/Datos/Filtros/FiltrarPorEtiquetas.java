package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

public class FiltrarPorEtiquetas implements FiltroHecho{
  private String filtroEtiqueta;

  public FiltrarPorEtiquetas(String etiqueta) {
    this.filtroEtiqueta = etiqueta;
  }
  public boolean aplicaA (Hecho hecho) {
    return hecho.getEtiquetas().contains(filtroEtiqueta);
  }
}
