package ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.impl;

import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.DesastreResponseDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.DesastresResponseDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.IDesastresNaturalesService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class DesastresNaturalesService implements IDesastresNaturalesService {
    private WebClient webClientApiDesastres;

    public DesastresNaturalesService(
            @Value("${proxy.api.base-url}") String baseUrl,
            @Value("${proxy.api.token}") String token) {
        this.webClientApiDesastres = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }
    //Consultar sobre Paginacion
//Consultar sobre Token
    @Override
    public Mono<List<DesastreResponseDTO>> obtenerDesastresApi() {
        return webClientApiDesastres
                .get()
                .uri("/api/desastres")
                .retrieve()
                .bodyToMono(DesastresResponseDTO.class)
                .map(DesastresResponseDTO::getData);
    }
    @Override
    public Mono<DesastreResponseDTO> getDesastreById(Long id) {
        return webClientApiDesastres
            .get()
            .uri("/api/desastres/{id}",id)
            .retrieve()
            .bodyToMono(DesastreResponseDTO.class);
    }
    @Override
    public Mono<List<DesastreResponseDTO>> getDesastresPorPaginacion(Integer page, Integer per_page) {
        return webClientApiDesastres
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/desastres")
                .queryParam("page",page)
                .queryParam("per_page",per_page)
                .build())
            .retrieve()
            .bodyToMono(DesastresResponseDTO.class)
            .map(DesastresResponseDTO::getData);
    }




}