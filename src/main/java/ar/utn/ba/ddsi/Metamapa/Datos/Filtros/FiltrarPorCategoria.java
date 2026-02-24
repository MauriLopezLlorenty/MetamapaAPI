package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

public class FiltrarPorCategoria implements FiltroHecho{
  private String categoria;

  public FiltrarPorCategoria(String categoriaFiltro) {

    this.categoria = categoriaFiltro;
  }
  public boolean aplicaA (Hecho hecho) {
    return hecho.getCategoria().getNombre().equalsIgnoreCase(categoria);
  }
}
