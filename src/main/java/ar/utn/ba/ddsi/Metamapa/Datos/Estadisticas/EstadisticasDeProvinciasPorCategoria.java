package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EstadisticasDeProvinciasPorCategoria extends Estadisticas{
    private Long idCategoria;
    private final HechosRepository hechosRepository;

    public EstadisticasDeProvinciasPorCategoria(HechosRepository hechosRepository, Long idCat) {
        super();
        this.hechosRepository = hechosRepository;
        this.idCategoria=idCat;
    }

    @Override
    public void generarEstadisticas() {
        this.registros= new ArrayList<>(); //reseteo estadisticas
        List<Registro> regGenerados= normalizarRegistros(
                hechosRepository.contarHechosPorProvinciaDeUnaCategoria(idCategoria));
        this.registros.addAll(regGenerados);


    }
}
