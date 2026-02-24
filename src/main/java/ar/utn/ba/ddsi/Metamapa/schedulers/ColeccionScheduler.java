package ar.utn.ba.ddsi.Metamapa.schedulers;

import ar.utn.ba.ddsi.Metamapa.services.IAgregacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ColeccionScheduler {
  private final IAgregacionService agregacionService;

  public ColeccionScheduler(IAgregacionService agregacionService) {
    this.agregacionService = agregacionService;
  }

  @Scheduled(cron = "0 0 * * * *")
  public void refrescarColeccion() {
    agregacionService.refrescarColecciones();
  }

}