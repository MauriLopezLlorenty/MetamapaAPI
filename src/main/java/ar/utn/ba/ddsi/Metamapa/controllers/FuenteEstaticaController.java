package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IEstaticasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/estaticas")
@RestController
public class FuenteEstaticaController {

    private final IEstaticasService estaticasService;


    public FuenteEstaticaController(IEstaticasService estaticasService) {
        this.estaticasService = estaticasService;
    }

    @PostMapping("/importarArchivo")
    public  ResponseEntity<?> importarArchivoCSV(@RequestParam("archivo") MultipartFile archivo) throws Exception{

        String hechosImportados = estaticasService.leerArchivo(archivo.getInputStream());
        return ResponseEntity.ok(
                Map.of(
                        "mensaje", "Importado correctamente"
                )
        );
    }
    @GetMapping("/hechos")
    public Page<HechoOutputDTO> obtenerHechos(Pageable pageable){
        return estaticasService.obtenerHechosEstaticos(pageable);
    }
    @GetMapping("/hechos/{id}")
    public HechoDTO obtenerHechoInd(@PathVariable Long id){
        return estaticasService.obtenerHechoInd(id);
    }

    @GetMapping("/colecciones/{id}/hechos")
    public Page<HechoDTO> obtenerHechosDeColeccion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return estaticasService.obtenerHechosPorColeccionPaginado(id, PageRequest.of(page, size));
    }

    @PutMapping("/hecho/{id}")
    public ResponseEntity<?> editarHecho(
            @PathVariable Long id,
            @RequestParam(value = "idColeccion", required = false) Long idColeccion,
            @RequestPart("hecho") HechoInputDTO hechoDTO,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) throws IOException {

        HechoDTO hechoModificado = estaticasService.modificarHechoEstatico(hechoDTO, archivo, id, idColeccion);

        return ResponseEntity.ok(hechoModificado);
    }
}
