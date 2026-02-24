package ar.utn.ba.ddsi.Metamapa.Datos;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable // ✅ Se integra en la tabla de Colección
@Getter
@Setter
@NoArgsConstructor
public class Criterio implements CriterioPertenencia {

    // --- DATOS (Lo que se guarda en BD) ---
    private String ubicacionObjetivo;
    private Long categoriaId;
    private Double latitud;           // Coordenada del centro
    private Double longitud;          // Coordenada del centro
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // --- LÓGICA (Lo que define la Interfaz) ---

    @Override
    public boolean cumpleCondicion(Hecho h) {
        // 1. Si el criterio está vacío (null), pasa el filtro
        // 2. Si tiene dato, verificamos que coincida

        // Filtro Categoría
        if (categoriaId != null) {
            if (h.getCategoria() == null || !h.getCategoria().getId().equals(categoriaId)) {
                return false;
            }
        }

        // Filtro Ubicación (Contiene texto)
        if (ubicacionObjetivo != null && !ubicacionObjetivo.isEmpty()) {
            // Asumiendo que Lugar tiene nombreNormalizado. Validar nulos.
            if (h.getLugar() == null || h.getLugar().getNombreNormalizado() == null ||
                !h.getLugar().getNombreNormalizado().toLowerCase().contains(ubicacionObjetivo.toLowerCase())) {
                return false;
            }
        }

        // Filtro Fechas
        if (fechaInicio != null) {
            if (h.getFechaDelHecho() == null || h.getFechaDelHecho().toLocalDate().isBefore(fechaInicio)) {
                return false;
            }
        }

        if (fechaFin != null) {
            if (h.getFechaDelHecho() == null || h.getFechaDelHecho().toLocalDate().isAfter(fechaFin)) {
                return false;
            }
        }

        return true; // Pasó todos los filtros activos
    }

    // --- MÉTODOS DE PREDICATE (Java Standard) ---

    @Override
    public boolean test(Hecho h) {
        return cumpleCondicion(h); // Redirige a tu lógica
    }
    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA = 6371; // Radio de la tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA * c; // Distancia en km
    }
}

