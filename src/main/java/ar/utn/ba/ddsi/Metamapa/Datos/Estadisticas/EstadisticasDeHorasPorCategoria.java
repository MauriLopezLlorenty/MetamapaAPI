package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class EstadisticasDeHorasPorCategoria extends Estadisticas{

    private Long idCategoria;
    private final HechosRepository hechosRepository;

    public EstadisticasDeHorasPorCategoria(HechosRepository hechosRepository, Long idCat) {
        super();
        this.idCategoria=idCat;
        this.hechosRepository=hechosRepository;
    }

    @Override
    public void generarEstadisticas() {
        this.registros= new ArrayList<>(); //reseteo estadisticas
        List<Registro> regGenerados= normalizarRegistros(
                hechosRepository.contarHechosPorHorasDeUnaCategoria(idCategoria));
        this.registros.addAll(regGenerados);

    }
}
