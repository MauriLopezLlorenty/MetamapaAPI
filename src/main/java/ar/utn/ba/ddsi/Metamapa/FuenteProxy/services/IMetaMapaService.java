package ar.utn.ba.ddsi.Metamapa.FuenteProxy.services;

import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.ColeccionMetaMapaDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.HechoMetaMapaDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.SolicitudEliminacionDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IMetaMapaService {

    Mono<List<HechoMetaMapaDTO>> obtenerHechos(Map<String, String> filtros);

    Mono<List<ColeccionMetaMapaDTO>> obtenerColecciones();

    Mono<List<HechoMetaMapaDTO>> obtenerHechosDeColeccion(String identificador, Map<String, String> filtros);

    Mono<Void> crearSolicitudEliminacion(SolicitudEliminacionDTO request);
}