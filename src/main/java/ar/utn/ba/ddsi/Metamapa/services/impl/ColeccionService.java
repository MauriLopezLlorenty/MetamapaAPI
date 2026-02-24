package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Consenso.AlgoritmoConsenso;
import ar.utn.ba.ddsi.Metamapa.Datos.Criterio;
import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroConstructor;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroCompuesto;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroHecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import ar.utn.ba.ddsi.Metamapa.Fuente.TipoFuente;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.ColeccionInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IAgregacionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.utn.ba.ddsi.Metamapa.services.IColeccionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColeccionService implements IColeccionService{
  @Autowired
  private ColeccionRepository coleccionRepository;
  @Autowired
  private HechosRepository hechosRepository;

  @Override
  public List<HechoOutputDTO> filtrarHechos(
      String handle, String categoria, String etiqueta,
      String longitud, String latitud, String titulo,
      boolean soloVisibles,
      String fechaCargaDesde, String fechaCargaHasta,
      String fechaHechoDesde, String fechaHechoHasta,
      String origen, String modo
  ) {

    // 1. Obtener Base (Igual que antes)
    List<Hecho> hechosCandidatos;
    Coleccion coleccionActiva = null;

    if (handle != null && !handle.trim().isEmpty()) {
      coleccionActiva = coleccionRepository.findByHandle(handle);
      if (coleccionActiva != null) {
        hechosCandidatos = this.ejecutarQueryDeColeccion(coleccionActiva);
      } else {
        return List.of();
      }
    } else {
      hechosCandidatos = hechosRepository.findAllVisibles();
    }

    // =================================================================
    // PASO 2: CONSTRUIR LOS FILTROS USANDO TU CLASE BUILDER
    // =================================================================
    FiltroConstructor constructor = new FiltroConstructor();

    constructor
        .porTitulo(titulo)
        .porCategoria(categoria)
        .porEtiqueta(etiqueta)
        .porLugar(latitud, longitud)
        .porFechaDeHecho(fechaHechoDesde, fechaHechoHasta)
        .porFechaDeCarga(fechaCargaDesde, fechaCargaHasta)
        // Cuidado: Tu constructor espera un String que coincida con el ENUM
        .porOrigen(origen);

    // Obtenemos la lista de estrategias generadas
    List<FiltroHecho> filtrosAplicables = constructor.getFiltros();

    final Coleccion contexto = coleccionActiva;

    // =================================================================
    // PASO 3: STREAM OPTIMIZADO
    // =================================================================
    return hechosCandidatos.stream()
        // A. Aplicar tus Filtros (Strategy Pattern)
        .filter(h -> cumpleTodosLosFiltros(h, filtrosAplicables))

        // B. Aplicar Modo de Navegación (Contexto de Colección)
        .filter(h -> pasaElFiltroDeModo(h, contexto, modo))

        // C. Convertir
        .map(this::convertirADTO)
        .collect(Collectors.toList());
  }

  // =================================================================
  // MÉTODOS PRIVADOS (LA LÓGICA INTERNA)
  // =================================================================

  public List<HechoOutputDTO> obtenerHechosDeColeccion(String handle) {

    // 1. Buscar la colección
    Coleccion coleccion = coleccionRepository.findByHandle(handle);
    if (coleccion == null) {
      throw new RuntimeException("Colección no encontrada con handle: " + handle);
    }

    // 2. Ejecutar la búsqueda dinámica (Usa tu método privado corregido)
    // Este método ya se encarga de traducir Fuentes -> Origenes y aplicar Criterios
    List<Hecho> hechosEntidad = this.ejecutarQueryDeColeccion(coleccion);

    // 3. Convertir a DTO para devolver al front
    return hechosEntidad.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
  }

  private List<Hecho> ejecutarQueryDeColeccion(Coleccion col) {

    // 1. TRADUCCIÓN DE FUENTES
    List<Origen> origenesAceptados = new ArrayList<>();

    // Verificamos si la colección tiene configuración. Si está vacía, asumimos TODAS (fallback)
    if (col.getFuentesPermitidas() == null || col.getFuentesPermitidas().isEmpty()) {
      System.out.println("⚠️ ALERTA: La colección " + col.getNombreColeccion() + " no tiene fuentes configuradas. Usando todas por defecto.");
      origenesAceptados.add(Origen.MANUAL);
      origenesAceptados.add(Origen.CONTRIBUYENTE);
      origenesAceptados.add(Origen.DATASET);
    } else {
      if (col.getFuentesPermitidas().contains(TipoFuente.DINAMICA)) {
        origenesAceptados.add(Origen.MANUAL);
        origenesAceptados.add(Origen.CONTRIBUYENTE);
      }
      if (col.getFuentesPermitidas().contains(TipoFuente.ESTATICA)) {
        origenesAceptados.add(Origen.DATASET);
      }
    }

    // LOG DE DEBUG (Mirá esto en la consola al ejecutar)
    System.out.println(">>> BUSCANDO HECHOS PARA COLECCION: " + col.getNombreColeccion());
    System.out.println(">>> Orígenes permitidos: " + origenesAceptados);

    // 2. CRITERIOS
    Long catId = null;
    Double lat = null;  // Nuevo
    Double lon = null;  // Nuevo
    LocalDateTime ini = null;
    LocalDateTime fin = null;

    if (col.getCriterio() != null) {
      catId = col.getCriterio().getCategoriaId();
      lat = col.getCriterio().getLatitud();
      lon = col.getCriterio().getLongitud();

      if (col.getCriterio().getFechaInicio() != null) ini = col.getCriterio().getFechaInicio().atStartOfDay();
      if (col.getCriterio().getFechaFin() != null) fin = col.getCriterio().getFechaFin().atTime(23, 59, 59);
    }

    // 3. QUERY
    List<Hecho> resultados = hechosRepository.buscarPorCriteriosDeColeccion(
        origenesAceptados,
        catId,
        ini,
        fin,
        lat, // Pasamos Latitud
        lon
    );

    System.out.println(">>> Resultados encontrados: " + resultados.size());
    return resultados;
  }

  // LÓGICA PASO 2: Filtros manuales (Sidebar del frontend)
  private boolean cumpleTodosLosFiltros(Hecho h, List<FiltroHecho> filtros) {
    // Recorremos la lista de filtros creada por tu Constructor.
    // Si falla en uno solo, el hecho se descarta.
    for (FiltroHecho filtro : filtros) {
      if (!filtro.aplicaA(h)) {
        return false;
      }
    }
    return true;
  }

  // LÓGICA PASO 3: El Algoritmo de Consenso
  private boolean pasaElFiltroDeModo(Hecho h, Coleccion col, String modo) {
    // Si el modo es IRRESTRICTO (o null), pasan todos los hechos de la colección
    if (modo == null || "IRRESTRICTO".equalsIgnoreCase(modo)) {
      return true;
    }

    // Si estamos en modo CURADO/VALIDADO
    if (col != null && col.getConsenso() != null) {
      // Ejecutamos la estrategia configurada (Mayoría, Absoluto, etc.)
      // Si el algoritmo dice false, ocultamos el hecho.
      return col.getConsenso().esConsensuado(h, List.of());
    }

    return true; // Si no hay configuración, por defecto dejamos pasar
  }

  // Helpers
  private String sanitizar(String s) {
    return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
  }

  private LocalDateTime parseFecha(String f, boolean inicio) {
    try {
      if (f.contains("T")) return LocalDateTime.parse(f);
      return LocalDate.parse(f).atStartOfDay();
    } catch (Exception e) { return inicio ? LocalDateTime.MIN : LocalDateTime.MAX; }
  }
  private HechoOutputDTO convertirADTO(Hecho h) {
    HechoOutputDTO dto = new HechoOutputDTO();

    // 1. Campos directos
    dto.setId(h.getId());
    dto.setTitulo(h.getTitulo());
    dto.setDescripcion(h.getDescripcion());
    dto.setFechaDelHecho(h.getFechaDelHecho());
    dto.setFechaDeCarga(h.getFechaDeCarga());

    // 2. Mapeo de Categoría (Objeto -> String)
    if (h.getCategoria() != null) {
      dto.setCategoria(h.getCategoria().getNombre());
    }

    // 4. Mapeo de Ubicación (Lugar -> Lat/Lon)
    if (h.getLugar() != null) {
      dto.setLatitud(h.getLugar().getLatitud());
      dto.setLongitud(h.getLugar().getLongitud());
    } else {
      // Valores por defecto si no hay lugar (opcional, pero recomendado)
      dto.setLatitud(0.0);
      dto.setLongitud(0.0);
    }

    // 5. Mapeo de Etiquetas (List<Etiqueta> -> List<String>)
    if (h.getEtiquetas() != null) {
      List<String> nombresEtiquetas = h.getEtiquetas().stream()
          .map(etiqueta -> etiqueta.getNombre()) // Asumiendo que Etiqueta tiene getNombre()
          .collect(Collectors.toList());
      dto.setEtiquetas(nombresEtiquetas);
    } else {
      dto.setEtiquetas(new ArrayList<>()); // Lista vacía para evitar nulls en el front
    }

    return dto;
  }
  @Transactional // Importante para que guarde bien las relaciones
  public Coleccion crearColeccion(ColeccionInputDTO dto) {
    Coleccion col = new Coleccion();

    // 1. Mapear datos básicos
    col.setNombreColeccion(dto.getNombreColeccion());
    col.setHandle(dto.getHandle());
    col.setDescripcion(dto.getDescripcion());
    Criterio criterio = new Criterio();

    if (dto.getCriterioCategoriaId() != null) {
      criterio.setCategoriaId(dto.getCriterioCategoriaId());
    }

    // B. Ubicación (String)
    if (dto.getCriterioLatitud() != null) {
      criterio.setLatitud(dto.getCriterioLatitud());
    }
    if (dto.getCriterioLongitud() != null) {
      criterio.setLongitud(dto.getCriterioLongitud());
    }

    // C. Fechas (String -> LocalDateTime)
    // El HTML input type="date" envía formato "yyyy-MM-dd" (ej: "2024-12-31")
    if (dto.getCriterioFechaInicio() != null && !dto.getCriterioFechaInicio().isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(dto.getCriterioFechaInicio());
        criterio.setFechaInicio(LocalDate.from(fecha.atStartOfDay())); // Lo convierte a 2024-12-31 00:00:00
      } catch (Exception e) {
        System.err.println("Error parseando fecha inicio: " + e.getMessage());
      }
    }

    if (dto.getCriterioFechaFin() != null && !dto.getCriterioFechaFin().isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(dto.getCriterioFechaFin());
        criterio.setFechaFin(LocalDate.from(fecha.atTime(23, 59, 59))); // Lo convierte al final del día
      } catch (Exception e) {
        System.err.println("Error parseando fecha fin: " + e.getMessage());
      }
    }

    // Asignamos el criterio a la colección
    col.setCriterio(criterio);

    // 3. Mapear Fuentes (String -> Enum)
    col.setFuentesPermitidas(new HashSet<>()); // Limpiamos inicial
    if (dto.getFuentes() != null && !dto.getFuentes().isEmpty()) {
      for (String f : dto.getFuentes()) {
        try {
          col.getFuentesPermitidas().add(TipoFuente.valueOf(f));
        } catch (IllegalArgumentException e) {
          System.err.println("Fuente desconocida: " + f);
        }
      }
    } else {
      // Default si no seleccionan nada
      col.getFuentesPermitidas().add(TipoFuente.DINAMICA);
      col.getFuentesPermitidas().add(TipoFuente.ESTATICA);
    }

    return coleccionRepository.save(col);
  }

  @Transactional
  public Coleccion modificarColeccion(Long id, ColeccionInputDTO dto) {
    // 1. Buscar la colección existente
    Coleccion col = coleccionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("No se encontró la colección con ID: " + id));

    // 2. Actualizar Datos Básicos
    col.setNombreColeccion(dto.getNombreColeccion());
    col.setHandle(dto.getHandle());
    col.setDescripcion(dto.getDescripcion());

    // 3. ACTUALIZAR CRITERIOS (ESTA ES LA PARTE QUE FALTABA)
    // Aseguramos que exista el objeto Criterio
    if (col.getCriterio() == null) {
      col.setCriterio(new Criterio());
    }
    Criterio c = col.getCriterio();

    // A. Categoría (ID)
    c.setCategoriaId(dto.getCriterioCategoriaId());

    // B. Ubicación (Texto y Coordenadas para el radio de 100km)
    c.setLatitud(dto.getCriterioLatitud());   // <--- Importante para el mapa
    c.setLongitud(dto.getCriterioLongitud()); // <--- Importante para el mapa

    // C. Fechas (Parseo de String a LocalDateTime)
    // Reseteamos primero para permitir limpiar la fecha si viene vacía
    c.setFechaInicio(null);
    if (dto.getCriterioFechaInicio() != null && !dto.getCriterioFechaInicio().isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(dto.getCriterioFechaInicio());
        c.setFechaInicio(LocalDate.from(fecha.atStartOfDay()));
      } catch (Exception e) {
        System.err.println("Error fecha inicio: " + e.getMessage());
      }
    }

    c.setFechaFin(null);
    if (dto.getCriterioFechaFin() != null && !dto.getCriterioFechaFin().isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(dto.getCriterioFechaFin());
        c.setFechaFin(LocalDate.from(fecha.atTime(23, 59, 59)));
      } catch (Exception e) {
        System.err.println("Error fecha fin: " + e.getMessage());
      }
    }

    // Guardamos los cambios en el objeto padre
    col.setCriterio(c);

    // 4. Actualizar Algoritmo
    if (dto.getAlgoritmo() != null) {
      // Asumiendo que tienes un método factory o lógica simple aquí
      // col.setConsenso(...);
    }

    // 5. Actualizar Fuentes (Esto ya te funcionaba)
    if (dto.getFuentes() != null) {
      col.getFuentesPermitidas().clear();
      for (String f : dto.getFuentes()) {
        try {
          col.getFuentesPermitidas().add(TipoFuente.valueOf(f));
        } catch (Exception e) { }
      }
    }

    // 6. Guardar
    return coleccionRepository.save(col);
  }
}