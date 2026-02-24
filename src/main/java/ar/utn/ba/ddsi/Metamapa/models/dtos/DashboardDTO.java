package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardDTO {

  // --- 1. KPIs (Números para las tarjetas superiores) ---
  private long totalHechos;
  private long totalSolicitudes; // Suma de todas las solicitudes

  // Estos dos son vitales para mostrar las alertas de "Pendientes" en el dashboard
  private long solicitudesEliminacionPendientes;
  private long solicitudesModificacionPendientes;

  // --- 2. Datos para Gráficos (Mapas) ---
  private Map<String, Long> hechosPorCategoria; // Ej: {"Incendio": 10}
  private Map<String, Long> hechosPorOrigen;    // Ej: {"MANUAL": 20}

  // Desglose de estados (puedes usar uno unificado o separados según tu gráfico)
  private Map<String, Long> solicitudesPorEstado; // Unificado (Eliminación + Modificación)

  // (Opcional) Si quieres gráficos separados en el futuro:
  private Map<String, Long> solicitudesEliminacionPorEstado;
  private Map<String, Long> solicitudesModificacionPorEstado;
}