package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.models.dtos.DashboardDTO;
import java.util.List;

public interface IAdminService {

  // Obtiene los datos para los gráficos y KPIs
  DashboardDTO obtenerResumenDashboard();

  // Obtiene la lista unificada de solicitudes pendientes

  // Procesa la aprobación o rechazo
  void resolverSolicitud(Long id, String tipo, String accion);

}