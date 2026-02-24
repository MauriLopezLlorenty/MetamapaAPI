package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.ColeccionInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.impl.ColeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import ar.utn.ba.ddsi.Metamapa.Fuente.FuenteEstatica;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.FuenteEstaticaRepository;

@RestController
@RequestMapping("/api/colecciones")
public class ColeccionController {

    @Autowired
    private ColeccionRepository coleccionRepository;

    @Autowired
    private FuenteEstaticaRepository fuenteEstaticaRepository;
    @Autowired
    private ColeccionService coleccionService;

    @GetMapping
    public List<Coleccion> listar() {
        return coleccionRepository.findAll();
    }


    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ColeccionInputDTO dto) {
        System.out.println(">>> POST (NUEVA COLECCIÓN): " + dto.toString());

        // Validaciones básicas
        if (dto.getNombreColeccion() == null || dto.getHandle() == null) {
            return ResponseEntity.badRequest().body("Falta nombre o handle");
        }
        if (coleccionRepository.findByHandle(dto.getHandle()) != null) {
            return ResponseEntity.badRequest().body("El handle ya existe.");
        }

        try {
            // ✅ CAMBIO PRINCIPAL: Delegamos al servicio
            // El servicio se encarga de convertir List<String> fuentes -> Set<TipoFuente>
            Coleccion nueva = coleccionService.crearColeccion(dto);

            return ResponseEntity.ok(nueva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody ColeccionInputDTO dto) {
        System.out.println(">>> PUT (ID: " + id + "): " + dto.toString());

        try {
            // ✅ CAMBIO PRINCIPAL: Delegamos al servicio
            // El servicio busca la ID, actualiza campos y actualiza las fuentes
            Coleccion actualizada = coleccionService.modificarColeccion(id, dto);

            return ResponseEntity.ok(actualizada); // Devolvemos el objeto actualizado
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id) {
        if (coleccionRepository.existsById(id)) {
            coleccionRepository.deleteById(id);
            return ResponseEntity.ok("Colección eliminada");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{idColeccion}/fuentes/{idFuente}")
    public ResponseEntity<?> agregarFuente(@PathVariable Long idColeccion, @PathVariable Long idFuente) {
        Optional<Coleccion> colOpt = coleccionRepository.findById(idColeccion);
        Optional<FuenteEstatica> fuenteOpt = fuenteEstaticaRepository.findById(idFuente);

        if (colOpt.isPresent() && fuenteOpt.isPresent()) {
            Coleccion coleccion = colOpt.get();
            FuenteEstatica fuente = fuenteOpt.get();

            coleccion.agregarFuenteEstatica(fuente);
            coleccionRepository.save(coleccion);

            return ResponseEntity.ok("Fuente asignada correctamente a la colección");
        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{idColeccion}/fuentes/{idFuente}")
    public ResponseEntity<?> quitarFuente(@PathVariable Long idColeccion, @PathVariable Long idFuente) {
        Optional<Coleccion> colOpt = coleccionRepository.findById(idColeccion);

        if (colOpt.isPresent()) {
            Coleccion coleccion = colOpt.get();

            boolean removido = coleccion.getFuentesEstaticas().removeIf(f -> f.getId().equals(idFuente));

            if (removido) {
                coleccionRepository.save(coleccion);
                return ResponseEntity.ok("Fuente quitada correctamente");
            } else {
                return ResponseEntity.badRequest().body("La fuente no estaba asignada a esta colección");
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/fuentes-disponibles")
    public List<FuenteEstatica> listarFuentes() {
        return fuenteEstaticaRepository.findAll();
    }


}