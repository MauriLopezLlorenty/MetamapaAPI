package ar.utn.ba.ddsi.Metamapa.schedulers;

import ar.utn.ba.ddsi.Metamapa.services.IEstadisticasService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EstadisticasScheduler {
  private final IEstadisticasService servicioEstadisticas;

  public EstadisticasScheduler(IEstadisticasService servicioEstadisticas) {
    this.servicioEstadisticas = servicioEstadisticas;
  }
// actualiza cada hora
//  @Scheduled(cron = "0 0 * * * *")
  @Scheduled(fixedRate = 300000) // 300000 ms = 5 minutos
  public void calcularEstadisticas(){
    servicioEstadisticas.calcularEstadisticas();
  }
}
