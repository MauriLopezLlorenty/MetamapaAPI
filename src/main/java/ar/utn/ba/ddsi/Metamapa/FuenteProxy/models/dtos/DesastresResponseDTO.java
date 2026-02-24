package ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos;

import lombok.Data;

import java.util.List;

@Data
public class DesastresResponseDTO {
    private List<DesastreResponseDTO> data;
}