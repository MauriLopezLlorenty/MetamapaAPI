package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
public class EstadisticasDeSolicitudes extends Estadisticas{
    protected final SolicitudDeEliminacionRepository solicitudesRepository;

    public EstadisticasDeSolicitudes(SolicitudDeEliminacionRepository solicRepo) {
        super();
        this.solicitudesRepository=solicRepo;
    }

    @Override
    public void generarEstadisticas() {
        this.registros= new ArrayList<>(); //reseteo estadisticas
        List<Registro> regGenerados= normalizarRegistros(
                solicitudesRepository.contarSolicitudesDeEliminacionSpamYNoSpam());
        this.registros.addAll(regGenerados);
    }
}
