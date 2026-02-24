package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltrarPorCategoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltrarPorLugar;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroCompuesto;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroConstructor;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroHecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroPorFechaDeCarga;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroPorFechaDeHecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.Spam.DetectorBasico;
import ar.utn.ba.ddsi.Metamapa.Datos.Spam.DetectorDeSpam;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IAgregacionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AgregacionService implements IAgregacionService {
  private HechosRepository hechosRepository;
  private ColeccionRepository coleccionRepository;
  private DetectorDeSpam detectorDeSpam;

  public AgregacionService(HechosRepository hechosRepository,
                           ColeccionRepository coleccionRepository){
    this.hechosRepository = hechosRepository;
    this.coleccionRepository = coleccionRepository;
    this.detectorDeSpam = new DetectorBasico();
  }
  @Override
  public List<Hecho> obtenerHechosDeColeccion(String handle){
    return coleccionRepository.findByHandle(handle).getHechos();
  }

  @Override
  @Transactional
  public void refrescarColecciones() {
    List<Hecho> nuevosHechos = hechosRepository.findAll();
    if(!nuevosHechos.isEmpty()) {
      nuevosHechos.forEach(h -> h.getColeccion().agregarHecho(h));
      nuevosHechos.forEach(h->coleccionRepository.save(h.getColeccion()));
    }
  }
  @Override
  public List<HechoOutputDTO> filtrarHechos(String handle, String categoria, String etiqueta,
                                            String longitud, String latitud, String titulo,
                                            Boolean visible, String fechaDeCargaDesde, String fechaDeCargaHasta,
                                            String fechaDeHechoDesde, String fechaDeHechoHasta, String origen) {

    // 1. Obtener la fuente de datos (Colección o Todos)
    List<Hecho> hechosAFiltrar;

    if (handle != null && !handle.isEmpty()) {
      Coleccion coleccion = this.coleccionRepository.findByHandle(handle);
      if (coleccion == null) {
        // Retornamos lista vacía o lanzamos error según prefieras
        return List.of();
      }
      hechosAFiltrar = coleccion.getHechos();
    } else {
      hechosAFiltrar = this.hechosRepository.findAll();
    }

    // 2. Construir filtros usando tu FiltroConstructor
    // Pasamos los Strings directamente, tu constructor ya sabe parsearlos o ignorar nulos.
    List<FiltroHecho> filtros = new FiltroConstructor()
        .porTitulo(titulo)
        .porCategoria(categoria)
        .porEtiqueta(etiqueta)
        .porLugar(latitud, longitud)
        .porVisible(visible)
        .porFechaDeCarga(fechaDeCargaDesde, fechaDeCargaHasta)
        .porFechaDeHecho(fechaDeHechoDesde, fechaDeHechoHasta)
        .porOrigen(origen)
        .getFiltros();

    // 3. Crear el Filtro Compuesto
    FiltroCompuesto filtroMaestro = new FiltroCompuesto(filtros);

    // 4. Aplicar filtro y convertir a DTO
    return hechosAFiltrar.stream()
        .filter(hecho -> filtroMaestro.aplicaA(hecho))
        .map(this::hechoToDto)
        .collect(Collectors.toList());
  }

  // --- Métodos auxiliares y otros métodos de la interfaz ---

  private HechoOutputDTO hechoToDto(Hecho hecho) {
    HechoOutputDTO dto = new HechoOutputDTO();
    dto.setId(hecho.getId());
    dto.setTitulo(hecho.getTitulo());
    dto.setDescripcion(hecho.getDescripcion());

    if (hecho.getCategoria() != null) dto.setCategoria(hecho.getCategoria().getNombre());

    if (hecho.getEtiquetas() != null) {
      dto.setEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toList()));
    }

    // Asignamos las fechas directamente (ya que no querías formatear en el backend)
    if (hecho.getFechaDelHecho() != null) dto.setFechaDelHecho(hecho.getFechaDelHecho());
    if (hecho.getFechaDeCarga() != null) dto.setFechaDeCarga(hecho.getFechaDeCarga());

    if (hecho.getLugar() != null) {
      dto.setLatitud(hecho.getLugar().getLatitud());
      dto.setLongitud(hecho.getLugar().getLongitud());
    }
    // dto.setArchivoMultimedia(hecho.getArchivoMultimedia());

    return dto;
  }



}