package ar.utn.ba.ddsi.Metamapa.controllers;


import ar.utn.ba.ddsi.Metamapa.Datos.*;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.EtiquetaRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.*;
import ar.utn.ba.ddsi.Metamapa.services.*;
import ar.utn.ba.ddsi.Metamapa.services.impl.ColeccionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequestMapping("/api/public")
@RestController

public class PublicController {
  private final IHechoService hechoService;
  private final ISolicitudService solicitudService;
  private final EtiquetaRepository etiquetaRepository;
  private final ColeccionRepository coleccionRepository;
  private final IColeccionService coleccionService;
  private final CategoriaRepository categoriaRepository;

  public PublicController(CategoriaRepository categoriaRepository ,IColeccionService coleccionService, IHechoService hechoService, EtiquetaRepository etiquetaRepository, ColeccionRepository coleccionRepository, ISolicitudService solicitudService) {
    this.hechoService = hechoService;
    this.categoriaRepository = categoriaRepository;
    this.etiquetaRepository = etiquetaRepository;
    this.coleccionRepository = coleccionRepository;
    this.coleccionService = coleccionService;
    this.solicitudService = solicitudService;
  }

  @GetMapping("/colecciones")
  public List<Coleccion> getAllColecciones() {
    return coleccionRepository.findAll();
  }


  @PostMapping("/crearhecho") // La ruta se combinará con /api/public
  public ResponseEntity<?> crearHecho(
      @RequestPart("hecho") HechoInputDTO hechoDTO,
      @RequestPart(value = "archivo", required = false) MultipartFile archivo,
      @AuthenticationPrincipal UserDetails userDetails)
  {
    try {
      String username = (userDetails != null) ? userDetails.getUsername() : null;
      Hecho hechoGuardado = hechoService.crearHechoDesdeDTO(hechoDTO, archivo,username);

      // Si todo va bien, devolvés 201 Created.
      return new ResponseEntity<>(hechoGuardado, HttpStatus.CREATED);

    } catch (IllegalArgumentException e) {
      // Atrapa los errores de validación (ej. categoría nula).
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

    } catch (IOException e) { // <-- ✅ AGREGÁ ESTE BLOQUE
      // Atrapa el error si hay un problema al guardar el archivo en el disco.
      System.err.println("Error de I/O al guardar archivo: " + e.getMessage());
      return new ResponseEntity<>("Error interno al procesar el archivo.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  @GetMapping("/hechos")
  public ResponseEntity<Page<Hecho>> obtenerHechosPaginados(Pageable pageable) {
    Page<Hecho> hechos = hechoService.obtenerTodosPaginados(pageable); // Necesitarás crear este método.
    return new ResponseEntity<>(hechos, HttpStatus.OK);
  }
  @GetMapping("/hechoindv/{id}")
  public ResponseEntity<Hecho> obtenerHechoPorId(@PathVariable Long id) {
    try {
      // Now the service returns the 'gift' directly, or throws an exception if not found.
      Hecho hecho = hechoService.buscarPorId(id);
      return new ResponseEntity<>(hecho, HttpStatus.OK);
    } catch (RuntimeException e) {
      // If the service threw the exception, we return a 404 Not Found.
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
  @GetMapping("/hechos/filtrar")
  public ResponseEntity<List<HechoOutputDTO>> filtrarHechos(
      @RequestParam(required = false) String handle, // Para filtrar dentro de una colección
      @RequestParam(required = false) String titulo,
      @RequestParam(required = false) String origen,
      @RequestParam(required = false) String etiqueta,
      @RequestParam(required = false) String fechaDeHechoDesde,
      @RequestParam(required = false) String fechaDeHechoHasta,
      @RequestParam(required = false) String fechaDeCargaDesde,
      @RequestParam(required = false) String fechaDeCargaHasta,
      @RequestParam(required = false) String categoria,
      @RequestParam(required = false) String latitud,
      @RequestParam(required = false) String longitud,
      @RequestParam(required = false, defaultValue = "IRRESTRICTO") String modo
  ) {
    List<Hecho> hechosBase;
    Coleccion coleccionActiva = null;

    List<HechoOutputDTO> hechosFiltrados = coleccionService.filtrarHechos(
        handle, categoria, etiqueta, longitud, latitud, titulo,
        true, fechaDeCargaDesde, fechaDeCargaHasta,
        fechaDeHechoDesde, fechaDeHechoHasta, origen,
        modo
    );

    return ResponseEntity.ok(hechosFiltrados);
  }
  @GetMapping("/categorias")
  public ResponseEntity<List<Categoria>> obtenerTodasLasCategorias() {
    return ResponseEntity.ok(categoriaRepository.findAll());

  }
  @PostMapping("/solicitud-eliminacion")
  public SolicitudOutputDTO crearSolicitudDeEliminacion(@RequestBody SolicitudInputDTO solicitud, @AuthenticationPrincipal UserDetails userDetails) {
    String username = (userDetails != null) ? userDetails.getUsername() : null;
    return solicitudService.crearSolicitudDeEliminacion(solicitud, username);
  }


  @GetMapping("/mis-hechos")
  public ResponseEntity<List<Hecho>> obtenerMisHechos(@AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    List<Hecho> misHechos = hechoService.buscarPorUsuario(username);
    return ResponseEntity.ok(misHechos);
  }

  @GetMapping("/coleccion/{handle}")
  public ResponseEntity<ColeccionOutputDTO> obtenerDetalleColeccion(@PathVariable String handle) {

    // 1. Buscar en BD
    Coleccion col = coleccionRepository.findByHandle(handle);

    if (col == null) {
      return ResponseEntity.notFound().build();
    }

    // 2. Mapear a DTO (Manual para no complicarnos con Mappers ahora)
    ColeccionOutputDTO dto = new ColeccionOutputDTO();
    dto.setNombreColeccion(col.getNombreColeccion());
    dto.setDescripcion(col.getDescripcion());
    dto.setHandle(col.getHandle());
    dto.setCriterio(col.getCriterio()); // Pasamos los IDs y reglas
    dto.setFuentesPermitidas(col.getFuentesPermitidas());

    // Extraer nombre del algoritmo si existe
    if (col.getConsenso() != null) {
      // Asumiendo que AlgoritmoConsenso tiene un método getNombre() o usas getClass().getSimpleName()
      // Si no tienes getNombre(), usa: col.getConsenso().getClass().getSimpleName();
      dto.setConsenso(col.getConsenso().getClass().getSimpleName().replace("Strategy", ""));
    }

    return ResponseEntity.ok(dto);
  }
  @GetMapping("/{handle}/hechos")
  public ResponseEntity<List<HechoOutputDTO>> verHechosDeColeccion(@PathVariable String handle) {
    try {
      List<HechoOutputDTO> hechos = coleccionService.obtenerHechosDeColeccion(handle);
      return ResponseEntity.ok(hechos);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }



}





/*

API pública para otras instancias de MetaMapa (ver entrega anterior):
Consulta de hechos dentro de una colección.
Generar una solicitud de eliminación a un hecho.
Navegación filtrada sobre una colección.
Navegación curada o irrestricta sobre una colección.

*/