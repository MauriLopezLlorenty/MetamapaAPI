package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;


import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;

public class FiltrarPorTitulo implements FiltroHecho{
    private String tituloBuscado;

    public FiltrarPorTitulo(String titulo) {
        this.tituloBuscado = titulo.toLowerCase(); // Guardamos en minúscula
    }

    @Override
    public boolean aplicaA(Hecho hecho) {
        if (hecho.getTitulo() == null) return false;
        // Verificamos si el título del hecho contiene la palabra clave
        return hecho.getTitulo().toLowerCase().contains(this.tituloBuscado);
    }
}
