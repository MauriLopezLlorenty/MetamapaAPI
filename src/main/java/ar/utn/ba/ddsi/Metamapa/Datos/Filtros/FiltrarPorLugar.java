package ar.utn.ba.ddsi.Metamapa.Datos.Filtros;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;

public class FiltrarPorLugar implements FiltroHecho {

    private final double latitudUsuario;
    private final double longitudUsuario;
    private final double radioEnKm = 1.0; // Radio fijo de 1 KM

    // Constante: Radio de la Tierra en Kilómetros
    private static final double RADIO_TIERRA_KM = 6371.0;

    public FiltrarPorLugar(Lugar lugarUsuario) {
        this.latitudUsuario = lugarUsuario.getLatitud();
        this.longitudUsuario = lugarUsuario.getLongitud();
    }

    @Override
    public boolean aplicaA(Hecho hecho) {
        // Si el hecho no tiene lugar, lo descartamos
        if (hecho.getLugar() == null) return false;

        double latHecho = hecho.getLugar().getLatitud();
        double lonHecho = hecho.getLugar().getLongitud();

        // Calculamos la distancia
        double distancia = calcularDistancia(latitudUsuario, longitudUsuario, latHecho, lonHecho);

        // Retornamos true si está dentro del radio (<= 1.0 km)
        return distancia <= radioEnKm;
    }

    // --- FÓRMULA DE HAVERSINE (Matemática pura) ---
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // Convertir grados a radianes
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c; // Distancia en Kilómetros
    }
}