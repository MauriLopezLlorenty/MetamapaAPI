package ar.utn.ba.ddsi.Metamapa.FuenteProxy.controllers;


import ar.utn.ba.ddsi.Metamapa.FuenteProxy.models.dtos.DesastreResponseDTO;
import ar.utn.ba.ddsi.Metamapa.FuenteProxy.services.IDesastresNaturalesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;

@RequestMapping("/api/desastres")
@RestController
public class DesastresNaturalesController {
  private IDesastresNaturalesService desastresNaturalesService;

  public DesastresNaturalesController(IDesastresNaturalesService desastresNaturalesService) {
    this.desastresNaturalesService = desastresNaturalesService;
  }

  @GetMapping
  public Mono<List<DesastreResponseDTO>> getDesastresPorPaginacion(
        @RequestParam (defaultValue = "1") Integer page,
        @RequestParam (defaultValue = "10") Integer per_page) {
    return desastresNaturalesService.getDesastresPorPaginacion(page,per_page);
  }

  @GetMapping("/{id}")
  public Mono<DesastreResponseDTO> getDesastreById(@PathVariable Long id){
    return desastresNaturalesService.getDesastreById(id);
  }

}
