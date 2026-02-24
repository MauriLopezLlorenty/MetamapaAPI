package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SolicitudDeModificacionRepository extends JpaRepository<SolicitudDeModificacion,Long> {
  List<SolicitudDeModificacion> findByEstado(EstadoSolicitud estado);
  @Query("SELECT s.estado, COUNT(s) FROM SolicitudDeEliminacion s GROUP BY s.estado")
  List<Object[]> contarPorEstado();

  long countByEstado(EstadoSolicitud estado);
  List<SolicitudDeModificacion> findByHecho_IdAndEstado(Long hechoId, EstadoSolicitud estado);

  List<SolicitudDeModificacion> findByUsuario_NombreUsuario(String nombreUsuario);
}
