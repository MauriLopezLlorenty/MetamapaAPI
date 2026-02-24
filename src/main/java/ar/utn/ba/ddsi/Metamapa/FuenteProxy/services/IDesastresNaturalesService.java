package ar.utn.ba.ddsi.Metamapa.FuenteProxy.services;

import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.DesastreResponseDTO;

import reactor.core.publisher.Mono;

import java.util.List;

public interface IDesastresNaturalesService {
    Mono<List<DesastreResponseDTO>>  obtenerDesastresApi();
    Mono<DesastreResponseDTO> getDesastreById(Long id);
    Mono<List<DesastreResponseDTO>> getDesastresPorPaginacion(Integer page, Integer per_page);

}