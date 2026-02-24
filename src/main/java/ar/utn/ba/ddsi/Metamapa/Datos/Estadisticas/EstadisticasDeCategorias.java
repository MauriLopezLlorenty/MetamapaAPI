package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

public class EstadisticasDeCategorias extends Estadisticas{
    private final HechosRepository hechosRepository;

    public EstadisticasDeCategorias(HechosRepository hechosRepository) {
        super();
        this.hechosRepository = hechosRepository;
    }

    @Override
    public void generarEstadisticas() {
        this.registros= new ArrayList<>(); //reseteo estadisticas
        List<Registro> regGenerados= normalizarRegistros(hechosRepository.contarHechosPorCategoria());
        this.registros.addAll(regGenerados);

    }
}
