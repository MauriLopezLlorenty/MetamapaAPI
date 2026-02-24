package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.*;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.EtiquetaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository; // Asegúrate que la ruta a tu repo sea correcta
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.UsuarioRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeModificacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IHechoService;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.rest.core.Path;rompe el path
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.util.UUID;

@Service
public class HechoService implements IHechoService {
  private final HechosRepository hechosRepository;
  private final EtiquetaRepository etiquetaRepository;
  private final CategoriaRepository categoriaRepository;
  private final Path rootLocation;
  private final UsuarioRepository usuarioRepository;
  private final SolicitudDeModificacionRepository solicitudDeModificacionRepository;
  private final ServicioNormalizacion servicioNormalizacion;
  // private final FileStorageService fileStorageService; // (Opcional) Si tienes un servicio para guardar archivos

  public HechoService(HechosRepository hechosRepository, EtiquetaRepository etiquetaRepository, CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository, SolicitudDeModificacionRepository solicitudDeModificacionRepository, ServicioNormalizacion servicioNormalizacion) {
    this.hechosRepository = hechosRepository;
    this.etiquetaRepository = etiquetaRepository;
    this.categoriaRepository = categoriaRepository;
    this.usuarioRepository = usuarioRepository;
    this.solicitudDeModificacionRepository = solicitudDeModificacionRepository;
    this.servicioNormalizacion = servicioNormalizacion;
    this.rootLocation = Paths.get("uploads/media");
    try {
      Files.createDirectories(rootLocation);
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize file storage location", e);
    }
  }


  @Override
  public Hecho crearHechoDesdeDTO(HechoInputDTO hechoDTO, MultipartFile archivo, String username)throws IOException {
    System.out.println("--- DATOS RECIBIDOS EN EL SERVICIO ---");
    System.out.println("Título: " + hechoDTO.getTitulo());
    System.out.println("Latitud: " + hechoDTO.getLatitud());
    System.out.println("Longitud: " + hechoDTO.getLongitud());
    System.out.println("Nombres de Etiquetas: " + hechoDTO.getEtiquetasNombres());
    System.out.println("------------------------------------");
    // 2. Mapeo de Datos: Creamos un Hecho y copiamos los datos del DTO.
    Hecho nuevoHecho = new Hecho();

    nuevoHecho.setTitulo(hechoDTO.getTitulo());
    nuevoHecho.setDescripcion(hechoDTO.getDescripcion());
    nuevoHecho.setFechaDelHecho(hechoDTO.getFechaDelHecho());
    nuevoHecho.setFechaDeCarga(LocalDateTime.now()); // La fecha de carga se genera aquí.

    // Convertimos el String del DTO al tipo Enum que espera la entidad Hecho
    nuevoHecho.setOrigen(Origen.valueOf(hechoDTO.getOrigen()));
    // Para Categoria: Creamos un nuevo objeto Categoria.
    // Una versión más avanzada buscaría si la categoría ya existe usando un CategoriaRepository.
    if (hechoDTO.getCategoriaNombre() != null && !hechoDTO.getCategoriaNombre().isEmpty()) {
      Categoria cat = servicioNormalizacion.buscarOCrearCategoria(hechoDTO.getCategoriaNombre());
      nuevoHecho.setCategoria(cat);
    } else {
      throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo.");
    }

    // Para Lugar: Creamos un nuevo objeto Lugar con la latitud y longitud.
    Lugar lugar = servicioNormalizacion.buscarOCrearLugar(
        hechoDTO.getLatitud(),
        hechoDTO.getLongitud()
    );
    nuevoHecho.setLugar(lugar);

    // Para Etiquetas: Recorremos la lista de nombres y creamos los objetos Etiqueta.
    if (hechoDTO.getEtiquetasNombres() != null && !hechoDTO.getEtiquetasNombres().isEmpty()) {
      List<Etiqueta> listaDeEtiquetas = new ArrayList<>();
      for (String nombreEtiqueta : hechoDTO.getEtiquetasNombres()) {
        Etiqueta etiqueta = etiquetaRepository.findByNombre(nombreEtiqueta)
            .orElseGet(() -> new Etiqueta(nombreEtiqueta));
        listaDeEtiquetas.add(etiqueta);
      }
      // ¡Crucial! Asignar la lista de objetos Etiqueta al Hecho
      nuevoHecho.setEtiquetas(listaDeEtiquetas);
    }

    // 4. (Opcional) Lógica para guardar el archivo multimedia.
    if (archivo != null && !archivo.isEmpty()) {
      System.out.println("¡ENTRANDO AL BLOQUE IF PARA GUARDAR EL ARCHIVO!"); // ¿Ves este mensaje?
      String nombreUnico = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
      Path destino = this.rootLocation.resolve(nombreUnico);
      Files.copy(archivo.getInputStream(), destino);
      String rutaParaGuardar = destino.toString().replace("\\", "/");
      nuevoHecho.setArchivoMultimedia(rutaParaGuardar);
    }
    //5. Logica para el creador
    if (username != null) {
      // CASO 1: Usuario Logueado
      Usuario contribuyente = usuarioRepository.findByNombreUsuario(username).orElse(null);

      if (contribuyente != null) {
        nuevoHecho.setContribuyente(contribuyente);
        nuevoHecho.setOrigen(Origen.CONTRIBUYENTE); // O el enum que prefieras para registrados
      }
    } else {
      // CASO 2: Usuario Anónimo
      nuevoHecho.setContribuyente(null);
      nuevoHecho.setOrigen(Origen.CONTRIBUYENTE);
    }
    // 6. Persistencia: Usamos el repositorio para guardar el Hecho completo en la base de datos.
    return hechosRepository.save(nuevoHecho);
  }
  @Override
  public Page<Hecho> obtenerTodosPaginados(Pageable pageable) {
    return hechosRepository.findByVisibleTrue(pageable); // JpaRepository ya sabe cómo hacer esto.
  }
  @Override
  public Hecho buscarPorId(Long id) {

    Optional<Hecho> hechoOptional = hechosRepository.findById(id);

    return hechoOptional.orElseThrow(() ->
        new RuntimeException("Hecho no encontrado con id: " + id)
    );
  }
  @Override
  public List<Hecho> buscarPorUsuario(String nombreUsuario) {
    return hechosRepository.findByContribuyente_NombreUsuario(nombreUsuario);
  }

  @Override
  public long contarHechosDeUsuario(String nombreUsuario) {
    return hechosRepository.countByContribuyente_NombreUsuario(nombreUsuario);
  }

  @Override
  public Hecho editarHecho(Long id, HechoInputDTO hechoDTO, MultipartFile archivo, String username) throws IOException {
    
    Hecho hecho = hechosRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id: " + id));

  
    if (hecho.getContribuyente() == null) {
      throw new IllegalStateException("Este hecho no tiene un contribuyente asociado y no puede ser editado.");
    }

    if (!hecho.getContribuyente().getNombreUsuario().equals(username)) {
      throw new IllegalStateException("No tienes permiso para editar este hecho. Solo el creador puede editarlo.");
    }

    
    LocalDateTime fechaDeCarga = hecho.getFechaDeCarga();
    long diasTranscurridos = ChronoUnit.DAYS.between(fechaDeCarga, LocalDateTime.now());
    
    if (diasTranscurridos > 7) {
      throw new IllegalStateException("El hecho ya no es editable pasados los 7 días.");
    }

    
    var solicitudRechazada = solicitudDeModificacionRepository.findByHecho_IdAndEstado(id, EstadoSolicitud.RECHAZADA);
    if (!solicitudRechazada.isEmpty()) {
      throw new IllegalStateException("El hecho no es editable debido a una solicitud de modificación rechazada.");
    }

    
    if (hechoDTO.getTitulo() != null && !hechoDTO.getTitulo().isEmpty()) {
      hecho.setTitulo(hechoDTO.getTitulo());
    }
    
    if (hechoDTO.getDescripcion() != null) {
      hecho.setDescripcion(hechoDTO.getDescripcion());
    }

    if (hechoDTO.getFechaDelHecho() != null) {
      hecho.setFechaDelHecho(hechoDTO.getFechaDelHecho());
    }

    
    if (hechoDTO.getCategoriaNombre() != null && !hechoDTO.getCategoriaNombre().isEmpty()) {
      Categoria categoria = categoriaRepository.findByNombreIgnoreCase(hechoDTO.getCategoriaNombre())
          .orElseGet(() -> new Categoria(hechoDTO.getCategoriaNombre()));
      hecho.setCategoria(categoria);
    }

    
    if (hechoDTO.getLatitud() != 0.0 || hechoDTO.getLongitud() != 0.0) {
      Lugar lugar = new Lugar(hechoDTO.getLatitud(), hechoDTO.getLongitud());
      hecho.setLugar(lugar);
    }

    
    if (hechoDTO.getEtiquetasNombres() != null && !hechoDTO.getEtiquetasNombres().isEmpty()) {
      List<Etiqueta> listaDeEtiquetas = new ArrayList<>();
      for (String nombreEtiqueta : hechoDTO.getEtiquetasNombres()) {
        Etiqueta etiqueta = etiquetaRepository.findByNombre(nombreEtiqueta)
            .orElseGet(() -> new Etiqueta(nombreEtiqueta));
        listaDeEtiquetas.add(etiqueta);
      }
      hecho.setEtiquetas(listaDeEtiquetas);
    }

    
    if (archivo != null && !archivo.isEmpty()) {
      String nombreUnico = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
      Path destino = this.rootLocation.resolve(nombreUnico);
      Files.copy(archivo.getInputStream(), destino);
      String rutaParaGuardar = destino.toString().replace("\\", "/");
      hecho.setArchivoMultimedia(rutaParaGuardar);
    }

  
    return hechosRepository.save(hecho);
  }
}