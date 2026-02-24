package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho; // Asegúrate de importar tu entidad Hecho
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeModificacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.DashboardDTO;
import ar.utn.ba.ddsi.Metamapa.services.IAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminService implements IAdminService {

  private final HechosRepository hechosRepository;
  private final SolicitudDeEliminacionRepository solElimRepo;
  private final SolicitudDeModificacionRepository solModRepo;

  public AdminService(HechosRepository hechosRepository,
                      SolicitudDeEliminacionRepository solElimRepo,
                      SolicitudDeModificacionRepository solModRepo) {
    this.hechosRepository = hechosRepository;
    this.solElimRepo = solElimRepo;
    this.solModRepo = solModRepo;
  }

  // --- 1. DASHBOARD ---
  @Override
  public DashboardDTO obtenerResumenDashboard() {
    DashboardDTO dto = new DashboardDTO();

    // 1. Cargar SOLO los hechos que son visibles (visible = 1)
    // Asegúrate de que este método exista en tu Repositorio como te indiqué antes:
    // @Query("SELECT h FROM Hecho h WHERE h.visible = 1")
    List<Hecho> hechosVisibles = hechosRepository.findAllVisibles();

    // =====================================================================
    // A. Totales (KPIs)
    // =====================================================================

    // ERROR ANTERIOR: dto.setTotalHechos(hechosRepository.count());
    // CORRECCIÓN: Usamos el método que filtra los 0.
    dto.setTotalHechos(hechosRepository.countVisibles());

    // Las solicitudes las contamos igual (son gestiones administrativas)
    long pendElim = solElimRepo.countByEstado(EstadoSolicitud.PENDIENTE);
    long pendMod = solModRepo.countByEstado(EstadoSolicitud.PENDIENTE);

    dto.setTotalSolicitudes(solElimRepo.count() + solModRepo.count());
    dto.setSolicitudesEliminacionPendientes(pendElim);
    dto.setSolicitudesModificacionPendientes(pendMod);

    // =====================================================================
    // ✅ B. LÓGICA PARA GRÁFICO DE ORIGEN (Dona)
    // =====================================================================
    // CORRECCIÓN: Usamos 'hechosVisibles' en lugar de 'todosLosHechos'
    Map<String, Long> porOrigen = hechosVisibles.stream()
        .filter(h -> h.getOrigen() != null)
        .collect(Collectors.groupingBy(
            hecho -> hecho.getOrigen().toString(),
            Collectors.counting()
        ));

    dto.setHechosPorOrigen(porOrigen);

    // =====================================================================
    // ✅ C. LÓGICA PARA GRÁFICO DE ESTADOS (Barras de Solicitudes)
    // =====================================================================
    List<SolicitudDeEliminacion> todasElim = solElimRepo.findAll();
    List<SolicitudDeModificacion> todasMod = solModRepo.findAll();

    Map<String, Long> porEstado = Stream.concat(
            todasElim.stream().map(s -> s.getEstado().toString()),
            todasMod.stream().map(s -> s.getEstado().toString())
        )
        .collect(Collectors.groupingBy(
            estado -> estado,
            Collectors.counting()
        ));

    dto.setSolicitudesPorEstado(porEstado);

    // =====================================================================
    // ✅ D. LÓGICA PARA GRÁFICO DE CATEGORÍA
    // =====================================================================
    // CORRECCIÓN: Usamos 'hechosVisibles' para que no cuente categorías de hechos ocultos
    Map<String, Long> porCategoria = hechosVisibles.stream()
        .filter(h -> h.getCategoria() != null)
        .collect(Collectors.groupingBy(
            h -> h.getCategoria().getNombre(),
            Collectors.counting()
        ));

    dto.setHechosPorCategoria(porCategoria);

    return dto;
  }



  // --- 3. RESOLVER SOLICITUD ---
  @Override
  @Transactional
  public void resolverSolicitud(Long id, String tipo, String accion) {
    boolean aprobar = "ACEPTAR".equalsIgnoreCase(accion);

    if ("ELIMINACION".equalsIgnoreCase(tipo)) {
      SolicitudDeEliminacion s = solElimRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

      if (aprobar) s.aceptar();
      else s.rechazar();

      solElimRepo.save(s);
    }
    else if ("MODIFICACION".equalsIgnoreCase(tipo)) {
      SolicitudDeModificacion s = solModRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

      if (aprobar) s.aceptar();
      else s.rechazar();

      solModRepo.save(s);
    } else {
      throw new IllegalArgumentException("Tipo de solicitud desconocido: " + tipo);
    }
  }
}