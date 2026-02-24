package ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.impl;

import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.ColeccionMetaMapaDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.HechoMetaMapaDTO;
//import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.HechoProxyDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.SolicitudEliminacionDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.IDesastresNaturalesService;
import org.springframework.beans.factory.annotation.Value;

import ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.IMetaMapaService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class MetaMapaService implements IMetaMapaService {
    private WebClient webClientMetaMapa;

    public MetaMapaService(
            @Value("${metamapa.api.base-url:http://localhost:8080}") String baseUrlMetaMapa
    ) {
        this.webClientMetaMapa = WebClient.builder()
                .baseUrl(baseUrlMetaMapa)
                .build();
    }


    @Override
    public Mono<List<HechoMetaMapaDTO>> obtenerHechos(Map<String, String> filtros) {
        return webClientMetaMapa.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/hechos");
                    filtros.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToFlux(HechoMetaMapaDTO.class)
                .collectList();
    }

    @Override
    public Mono<List<ColeccionMetaMapaDTO>> obtenerColecciones() {
        return webClientMetaMapa.get()
                .uri("/colecciones")
                .retrieve()
                .bodyToFlux(ColeccionMetaMapaDTO.class)
                .collectList();
    }

    @Override
    public Mono<List<HechoMetaMapaDTO>> obtenerHechosDeColeccion(String identificador, Map<String, String> filtros) {
        return webClientMetaMapa.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/colecciones/" + identificador + "/hechos");
                    filtros.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToFlux(HechoMetaMapaDTO.class)
                .collectList();
    }

    @Override
    public Mono<Void> crearSolicitudEliminacion(SolicitudEliminacionDTO request) {
        return webClientMetaMapa.post()
                .uri("/solicitudes")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }


}