package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter

public class EstadisticasDeProvinciasPorColeccion extends Estadisticas {
    private Long idColeccion;
    private final HechosRepository hechosRepository;

    public EstadisticasDeProvinciasPorColeccion(HechosRepository hechosRepo, Long idColeccion) {
        super();
        this.idColeccion = idColeccion;
        this.hechosRepository=hechosRepo;
    }


    @Override
    public void generarEstadisticas() {
        this.registros=new ArrayList<>();
        List<Registro> regGenerados=normalizarRegistros(hechosRepository.contarHechosPorProvinciaDeUnaColeccion(idColeccion));
        this.registros.addAll(regGenerados);
    }
}
