package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface SolicitudDeEliminacionRepository extends JpaRepository<SolicitudDeEliminacion, Long> {

    // Búsqueda de solicitudes por su estado
    List<SolicitudDeEliminacion> findByEstado(EstadoSolicitud estado);
    long countByEstado(EstadoSolicitud estado);
    @Query("""
        SELECT 
            CASE WHEN s.esSpam = true THEN 'spam' ELSE 'no_spam' END,
            COUNT(s)
        FROM SolicitudDeEliminacion s
        GROUP BY s.esSpam
    """)
    List<Object[]> contarSolicitudesDeEliminacionSpamYNoSpam();

    @Query("SELECT s.estado, COUNT(s) FROM SolicitudDeEliminacion s GROUP BY s.estado")
    List<Object[]> contarPorEstado();
    List<SolicitudDeEliminacion> findByUsuario_NombreUsuario(String nombreUsuario);
}