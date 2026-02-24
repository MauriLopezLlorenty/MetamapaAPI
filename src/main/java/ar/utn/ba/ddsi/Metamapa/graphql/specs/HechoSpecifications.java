package ar.utn.ba.ddsi.Metamapa.graphql.specs;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.graphql.dto.HechoFilterInput;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class HechoSpecifications {

    public static Specification<Hecho> withFilter(HechoFilterInput filter) {
        if (filter == null) {
            return Specification.where(null);
        }

        return Specification.where(hasTitulo(filter.getTitulo()))
                .and(hasCategoria(filter.getCategoria()))
                .and(hasOrigen(filter.getOrigen()))
                .and(hasColeccion(filter.getColeccion()))
                .and(isFechaDelHechoBetween(filter.getFechaDelHechoDesde(), filter.getFechaDelHechoHasta()))
                .and(isFechaDeCargaBetween(filter.getFechaDeCargaDesde(), filter.getFechaDeCargaHasta()))
                .and(isVisible(filter.getVisible()))
                .and(hasEtiquetas(filter.getEtiquetas()))
                .and(isWithinRadius(filter.getLatitud(), filter.getLongitud(), filter.getRadio()));
    }

    private static Specification<Hecho> hasTitulo(String titulo) {
        return (root, query, builder) ->
                titulo == null ? builder.conjunction() : builder.like(root.get("titulo"), "%" + titulo + "%");
    }

    private static Specification<Hecho> hasCategoria(String categoria) {
        return (root, query, builder) ->
                categoria == null ? builder.conjunction() : builder.equal(root.get("categoria").get("nombre"), categoria);
    }

    private static Specification<Hecho> hasOrigen(String origen) {
        return (root, query, builder) ->
                origen == null ? builder.conjunction() : builder.equal(root.get("origen").as(String.class), origen);
    }

    private static Specification<Hecho> hasColeccion(String coleccion) {
        return (root, query, builder) ->
                coleccion == null ? builder.conjunction() : builder.equal(root.get("coleccion").get("nombre"), coleccion);
    }

    private static Specification<Hecho> isFechaDelHechoBetween(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return (root, query, builder) -> {
            if (desde == null && hasta == null) {
                return builder.conjunction();
            }
            if (desde == null) {
                return builder.lessThanOrEqualTo(root.get("fechaDelHecho"), hasta);
            }
            if (hasta == null) {
                return builder.greaterThanOrEqualTo(root.get("fechaDelHecho"), desde);
            }
            return builder.between(root.get("fechaDelHecho"), desde, hasta);
        };
    }

    private static Specification<Hecho> isFechaDeCargaBetween(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return (root, query, builder) -> {
            if (desde == null && hasta == null) {
                return builder.conjunction();
            }
            if (desde == null) {
                return builder.lessThanOrEqualTo(root.get("fechaDeCarga"), hasta);
            }
            if (hasta == null) {
                return builder.greaterThanOrEqualTo(root.get("fechaDeCarga"), desde);
            }
            return builder.between(root.get("fechaDeCarga"), desde, hasta);
        };
    }

    private static Specification<Hecho> isVisible(Boolean visible) {
        return (root, query, builder) ->
                visible == null ? builder.conjunction() : builder.equal(root.get("visible"), visible);
    }

    private static Specification<Hecho> hasEtiquetas(java.util.List<String> etiquetas) {
        return (root, query, builder) -> {
            if (etiquetas == null || etiquetas.isEmpty()) {
                return builder.conjunction();
            }
            // This ensures that we don't get duplicate Hecho entities in the result
            query.distinct(true);
            return root.join("etiquetas", JoinType.INNER).get("nombre").in(etiquetas);
        };
    }

    private static Specification<Hecho> isWithinRadius(Double latitud, Double longitud, Double radio) {
        return (root, query, builder) -> {
            if (latitud == null || longitud == null || radio == null) {
                return builder.conjunction();
            }

            // Using a simple bounding box for filtering
            double latMin = latitud - (radio / 111.32);
            double latMax = latitud + (radio / 111.32);
            double lonMin = longitud - (radio / (111.32 * Math.cos(Math.toRadians(latitud))));
            double lonMax = longitud + (radio / (111.32 * Math.cos(Math.toRadians(latitud))));

            return builder.and(
                    builder.between(root.get("lugar").get("latitud"), latMin, latMax),
                    builder.between(root.get("lugar").get("longitud"), lonMin, lonMax)
            );
        };
    }
}
