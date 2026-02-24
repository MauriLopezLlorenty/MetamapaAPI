package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HechosRepository extends JpaRepository<Hecho, Long> {

    // ✅ MODIFICADO: Mantenemos el nombre pero inyectamos filtro visible=true
    @Query("SELECT h FROM Hecho h WHERE h.origen = :origen AND h.visible = true")
    Page<Hecho> findByOrigen(@Param("origen") Origen origen, Pageable pageable);

    // ✅ MODIFICADO: Mantenemos el nombre pero inyectamos filtro visible=true
    @Query("SELECT h FROM Hecho h WHERE h.coleccion.id = :idColeccion AND h.visible = true")
    Page<Hecho> findByColeccion_Id(@Param("idColeccion") Long idColeccion, Pageable pageable);

    // ✅ MODIFICADO: Agregado WHERE h.visible = true
    @Query("SELECT DISTINCT h FROM Hecho h " +
        "LEFT JOIN FETCH h.etiquetas " +
        "LEFT JOIN FETCH h.categoria " +
        "LEFT JOIN FETCH h.lugar " +
        "WHERE h.visible = true")
    List<Hecho> findAllConRelaciones();

    // ✅ MODIFICADO: Query manual para fecha
    @Query("SELECT h FROM Hecho h WHERE h.fechaDeCarga > :fecha AND h.visible = true")
    List<Hecho> findByFechaDeCargaAfter(@Param("fecha") LocalDateTime fecha);

    // ✅ MODIFICADO: Query manual para usuario
    @Query("SELECT h FROM Hecho h WHERE h.contribuyente.nombreUsuario = :nombreUsuario AND h.visible = true")
    List<Hecho> findByContribuyente_NombreUsuario(@Param("nombreUsuario") String nombreUsuario);

    // ✅ MODIFICADO: Conteo solo de visibles
    @Query("SELECT COUNT(h) FROM Hecho h WHERE h.contribuyente.nombreUsuario = :nombreUsuario AND h.visible = true")
    long countByContribuyente_NombreUsuario(@Param("nombreUsuario") String nombreUsuario);

    // ✅ MODIFICADO: Existe solo si es visible
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Hecho h WHERE h.fechaDeCarga > :fecha AND h.visible = true")
    boolean existsByFechaDeCargaAfter(@Param("fecha") LocalDateTime fecha);


    // --- ESTADÍSTICAS (También deben filtrar los ocultos para no falsear datos) ---

    // ✅ MODIFICADO
    @Query("SELECT h.categoria.nombre, COUNT(h) as total " +
        "FROM Hecho h WHERE h.visible = true " +
        "GROUP BY h.categoria.nombre ORDER BY total DESC")
    List<Object[]> contarHechosPorCategoria();

    // ✅ MODIFICADO
    @Query("""
        SELECT FUNCTION('HOUR', h.fechaDelHecho), COUNT(h)
        FROM Hecho h
        WHERE h.categoria.id = :categoriaId AND h.visible = true
        GROUP BY FUNCTION('HOUR', h.fechaDelHecho)
        ORDER BY COUNT(h) DESC
    """)
    List<Object[]> contarHechosPorHorasDeUnaCategoria(@Param("categoriaId") Long categoriaId);

    // ✅ MODIFICADO
    @Query("""
        SELECT h.lugar.nombreNormalizado, COUNT(h)
        FROM Hecho h
        WHERE h.coleccion.id = :coleccionId AND h.visible = true
        GROUP BY h.lugar.nombreNormalizado
        ORDER BY COUNT(h) DESC
    """)
    List<Object[]> contarHechosPorProvinciaDeUnaColeccion(@Param("coleccionId") Long coleccionId);

    // ✅ MODIFICADO
    @Query("""
        SELECT h.lugar.nombreNormalizado, COUNT(h)
        FROM Hecho h
        WHERE h.categoria.id = :categoriaId AND h.visible = true
        GROUP BY h.lugar.nombreNormalizado
        ORDER BY COUNT(h) DESC
    """)
    List<Object[]> contarHechosPorProvinciaDeUnaCategoria(@Param("categoriaId") Long categoriaId);

    // ✅ MODIFICADO
    @Query("SELECT h.origen, COUNT(h) FROM Hecho h WHERE h.visible = true GROUP BY h.origen")
    List<Object[]> contarHechosPorOrigen();

    // Métodos explícitos que ya tenías (Correctos)
    @Query("SELECT h FROM Hecho h WHERE h.visible = true")
    List<Hecho> findAllVisibles();

    @Query("SELECT COUNT(h) FROM Hecho h WHERE h.visible = true")
    long countVisibles();

    @Query("SELECT h FROM Hecho h WHERE " +
        "h.visible = true AND " +
        "h.origen IN :origenesPermitidos AND " +

        // 1. Filtro de Categoría
        "(:catId IS NULL OR h.categoria.id = :catId) AND " +

        // 2. Filtro de Fechas
        "(:ini IS NULL OR h.fechaDelHecho >= :ini) AND " +
        "(:fin IS NULL OR h.fechaDelHecho <= :fin) AND " +
        "(:latCentro IS NULL OR :lonCentro IS NULL OR " +
        "  (h.lugar IS NOT NULL AND " +
        "   (6371 * acos(cos(radians(:latCentro)) * cos(radians(h.lugar.latitud)) * " +
        "   cos(radians(h.lugar.longitud) - radians(:lonCentro)) + " +
        "   sin(radians(:latCentro)) * sin(radians(h.lugar.latitud)))) <= 5))"
    )
    List<Hecho> buscarPorCriteriosDeColeccion(
        @Param("origenesPermitidos") List<Origen> origenesPermitidos,
        @Param("catId") Long categoriaId,
        @Param("ini") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin,
        @Param("latCentro") Double latCentro, // Nuevo
        @Param("lonCentro") Double lonCentro  // Nuevo
    );
    Page<Hecho> findByVisibleTrue(Pageable pageable);



}