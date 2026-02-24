package ar.utn.ba.ddsi.Metamapa;


import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import ar.utn.ba.ddsi.Metamapa.services.IAgregacionService;
import ar.utn.ba.ddsi.Metamapa.services.impl.AgregacionService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class RefrescoDeColeccionesTest {

  // refresco de colecciones - test con mockito
  @Test
  public void hayNuevosHechosSeActualizanLasColecciones() {
    HechosRepository hechosRepo = mock(HechosRepository.class);
    ColeccionRepository coleccionesRepo = mock(ColeccionRepository.class);
    SolicitudDeEliminacionRepository solicitudesRepo = mock(SolicitudDeEliminacionRepository.class);
    IAgregacionService agregacionService = new AgregacionService(hechosRepo,coleccionesRepo);

    List<Hecho> nuevosHechos = List.of(mock(Hecho.class),mock(Hecho.class));
    when(hechosRepo.findByFechaDeCargaAfter(any(LocalDateTime.class))).thenReturn(nuevosHechos);
    doNothing().when(coleccionesRepo).save(any(Coleccion.class));

    agregacionService.refrescarColecciones();

    verify(coleccionesRepo, times(2))
        .save(any(Coleccion.class));
  }
  @Test
  public void noHayNuevosHechosNoSeActualizanLasColecciones() {
    HechosRepository hechosRepo = mock(HechosRepository.class);
    ColeccionRepository coleccionesRepo = mock(ColeccionRepository.class);
    SolicitudDeEliminacionRepository solicitudesRepo = mock(SolicitudDeEliminacionRepository.class);
    IAgregacionService agregacionService = new AgregacionService(hechosRepo,coleccionesRepo);

    when(hechosRepo.findByFechaDeCargaAfter(any(LocalDateTime.class))).thenReturn(List.of());

    agregacionService.refrescarColecciones();

    verify(coleccionesRepo, times(0))
        .save(any(Coleccion.class));
  }

}