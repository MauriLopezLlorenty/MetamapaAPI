package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.EstadoSolicitud;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.Spam.DetectorBasico;
import ar.utn.ba.ddsi.Metamapa.Datos.Spam.DetectorDeSpam;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.LugarRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeEliminacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.SolicitudDeModificacionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.UsuarioRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.ISolicitudService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService implements ISolicitudService {

    private final HechosRepository hechosRepository;
    private final ColeccionRepository coleccionRepository;
    private final SolicitudDeEliminacionRepository solicitudesRepository;
    private final DetectorDeSpam detectorDeSpam;
    private final SolicitudDeModificacionRepository solicitudDeModificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ServicioNormalizacion servicioNormalizacion;


    public SolicitudService(HechosRepository hechosRepository,
                            ColeccionRepository coleccionRepository,
                            SolicitudDeEliminacionRepository solicitudesRepository,
                            SolicitudDeModificacionRepository solicitudDeModificacionRepository, UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository, LugarRepository lugarRepository, ServicioNormalizacion servicioNormalizacion) {
        this.hechosRepository = hechosRepository;
        this.coleccionRepository = coleccionRepository;
        this.solicitudesRepository = solicitudesRepository;
      this.usuarioRepository = usuarioRepository;
      this.categoriaRepository = categoriaRepository;
      this.servicioNormalizacion = servicioNormalizacion;
      this.detectorDeSpam = new DetectorBasico();
        this.solicitudDeModificacionRepository = solicitudDeModificacionRepository;
    }

    @Override
    public void procesarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud) {
        if (detectorDeSpam.esSpam(solicitud.getMotivo())) {
            solicitud.rechazar();
            solicitud.setEsSpam(true);
            solicitudesRepository.save(solicitud);
            return;
        }
        for (Coleccion coleccion : coleccionRepository.findAll()) {
            if (coleccion.contiene(solicitud.getHecho())) {
                coleccion.eliminarHecho(solicitud.getHecho());
                coleccionRepository.save(coleccion);
            }
        }

        solicitud.aceptar();
        solicitud.setEsSpam(false);
        solicitudesRepository.save(solicitud);
    }

    @Override
    public SolicitudOutputDTO crearSolicitudDeEliminacion(SolicitudInputDTO solicitudInputDTO,String username) {
        Hecho hecho = hechosRepository.findById(solicitudInputDTO.getHechoId())
                .orElseThrow(() -> new RuntimeException("Hecho no encontrado con id: " + solicitudInputDTO.getHechoId()));
        Usuario usuario = null;
        usuario = null;
        if (username != null) {
            // Si viene un usuario, lo buscamos. Si no existe, lanzamos error o dejamos null según tu preferencia.
            usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        }

        SolicitudDeEliminacion solicitud = new SolicitudDeEliminacion(hecho, solicitudInputDTO.getMotivo(),usuario);
        solicitudesRepository.save(solicitud);
        return this.solicitudOutputDTO(solicitud);
    }

    @Override
    public List<SolicitudOutputDTO> obtenerSolicitudesDeEliminacion() {
        List<SolicitudDeEliminacion> solicitudes = solicitudesRepository.findAll();
        return solicitudes.stream().map(this::solicitudOutputDTO).toList();
    }

    @Override
    public SolicitudDeEliminacion obtenerSolicitudDeEliminacion(Long id) {
        Optional<SolicitudDeEliminacion> solicitud = solicitudesRepository.findById(id);

        return solicitud.orElseThrow(() ->
                new RuntimeException("Solictud no encontrada con id: " + id)
        );
    }

    @Override
    public void acpetarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud) {
        solicitud.aceptar();
        solicitudesRepository.save(solicitud);
    }

    @Override
    public void rechazarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud) {
        solicitud.rechazar();
        solicitudesRepository.save(solicitud);
    }

    @Override
    public SolicitudModOutputDTO crearSolicitudDeModificacion(SolicitudModInputDTO solicitudInput, String username) {

        // 1. REGLA: Usuario debe estar registrado
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Solo usuarios registrados pueden solicitar modificaciones.");
        }
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Hecho hecho = hechosRepository.findById(solicitudInput.getHechoId())
            .orElseThrow(() -> new RuntimeException("Hecho no encontrado"));

        // 2. REGLA: Usuario debe ser el dueño (Opcional, pero recomendado)
        if (hecho.getContribuyente() == null || !hecho.getContribuyente().getId().equals(usuario.getId())) {
            throw new RuntimeException("Solo el autor del hecho puede modificarlo.");
        }

        // 3. REGLA: Plazo de 1 semana desde la fecha de CARGA
        // Usamos fechaDeCarga porque es cuando el sistema registró el dato
        LocalDateTime fechaLimite = hecho.getFechaDeCarga().plusDays(7);
        if (LocalDateTime.now().isAfter(fechaLimite)) {
            throw new RuntimeException("El periodo de edición de 1 semana ha expirado.");
        }

        // 4. Crear la solicitud base
        SolicitudDeModificacion solicitud = new SolicitudDeModificacion(hecho, usuario);

        // 5. Cargar las propuestas (Solo si vienen en el DTO)
        solicitud.setPropuestaTitulo(solicitudInput.getNuevoTitulo());
        solicitud.setPropuestaDescripcion(solicitudInput.getNuevaDescripcion());
        solicitud.setPropuestaArchivoMultimedia(solicitudInput.getNuevoArchivoMultimedia());
        solicitud.setPropuestaFechaDelHecho(solicitudInput.getNuevaFechaDelHecho());

        // Búsqueda de Entidades Relacionadas (Categoria y Lugar)
        if (solicitudInput.getNuevaCategoriaId() != null) {
            Categoria nuevaCat = categoriaRepository.findById(solicitudInput.getNuevaCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            solicitud.setPropuestaCategoria(nuevaCat);
        }

        if (solicitudInput.getNuevaLatitud() != null && solicitudInput.getNuevaLongitud() != null) {

            // Aquí ocurre la magia:
            // El servicio busca si existe esa lat/long. Si no, crea un Lugar nuevo y lo guarda.
            // Nos devuelve el objeto Lugar listo para usar.
            Lugar lugarPropuesto = servicioNormalizacion.buscarOCrearLugar(
                solicitudInput.getNuevaLatitud(),
                solicitudInput.getNuevaLongitud()
            );

            solicitud.setPropuestaLugar(lugarPropuesto);
        }

        solicitudDeModificacionRepository.save(solicitud);
        return solicitudModOutputDTO(solicitud);
    }

    @Override
    public void aceptarSolicitudDeModificacion(SolicitudDeModificacion solicitud) {
        // Marcar solicitud como aceptada
        solicitud.aceptar();

        Hecho hecho = solicitud.getHecho();

        // Aplicar cambios al Hecho SOLO si hay una propuesta (no es null)
        if (solicitud.getPropuestaTitulo() != null) {
            hecho.setTitulo(solicitud.getPropuestaTitulo());
        }
        if (solicitud.getPropuestaDescripcion() != null) {
            hecho.setDescripcion(solicitud.getPropuestaDescripcion());
        }
        if (solicitud.getPropuestaArchivoMultimedia() != null) {
            hecho.setArchivoMultimedia(solicitud.getPropuestaArchivoMultimedia());
        }
        if (solicitud.getPropuestaFechaDelHecho() != null) {
            hecho.setFechaDelHecho(solicitud.getPropuestaFechaDelHecho());
        }

        // Relaciones complejas
        if (solicitud.getPropuestaCategoria() != null) {
            hecho.setCategoria(solicitud.getPropuestaCategoria());
        }
        if (solicitud.getPropuestaLugar() != null) {
            hecho.setLugar(solicitud.getPropuestaLugar());
        }

        // Guardamos el Hecho modificado y la Solicitud actualizada
        hechosRepository.save(hecho);
        solicitudDeModificacionRepository.save(solicitud);
    }

    @Override
    public void rechazarSolicitudDeModificacion(SolicitudDeModificacion solicitud) {
        solicitud.rechazar();

        solicitudDeModificacionRepository.save(solicitud);
    }

    @Override
    public SolicitudDeModificacion obtenerSolicitudDeMod(Long id) {
        Optional<SolicitudDeModificacion> solicitud = solicitudDeModificacionRepository.findById(id);

        return solicitud.orElseThrow(() ->
                new RuntimeException("Solictud no encontrada con id: " + id)
        );
    }

    public SolicitudOutputDTO solicitudOutputDTO(SolicitudDeEliminacion solicitud) {
        SolicitudOutputDTO solicitudOutputDTO = new SolicitudOutputDTO();
        solicitudOutputDTO.setId(solicitud.getId());
        solicitudOutputDTO.setHecho(solicitud.getHecho());
        solicitudOutputDTO.setMotivo(solicitud.getMotivo());
        solicitudOutputDTO.setEstado(solicitud.getEstado());
        solicitudOutputDTO.setFechaSolicitud(solicitud.getFechaSolicitud());
        solicitudOutputDTO.setFechaEstado(solicitud.getFechaEstado());
        solicitudOutputDTO.setUsuario(solicitud.getUsuario());

        return solicitudOutputDTO;
    }
    public SolicitudModOutputDTO solicitudModOutputDTO(SolicitudDeModificacion solicitudDeModificacion){
        SolicitudModOutputDTO solicitudModOutputDTO = new SolicitudModOutputDTO();
        solicitudModOutputDTO.setId(solicitudDeModificacion.getId());
        solicitudModOutputDTO.setHecho(solicitudDeModificacion.getHecho());
        solicitudModOutputDTO.setEstado(solicitudDeModificacion.getEstado());
        solicitudModOutputDTO.setFechaSolicitud(solicitudDeModificacion.getFechaSolicitud());
        solicitudModOutputDTO.setFechaEstado(solicitudDeModificacion.getFechaEstado());
        if (solicitudDeModificacion.getUsuario() != null) {
            solicitudModOutputDTO.setNombreUsuarioTexto(solicitudDeModificacion.getUsuario().getNombreUsuario());
        } else {
            solicitudModOutputDTO.setNombreUsuarioTexto("Anónimo");
        }
        return solicitudModOutputDTO;
    }
    @Override
    public List<SolicitudModOutputDTO> obtenerSolicitudesDeModificacion() {
        // 1. Buscamos las solicitudes (por ejemplo, solo las pendientes)
        List<SolicitudDeModificacion> solicitudes = solicitudDeModificacionRepository.findByEstado(EstadoSolicitud.PENDIENTE);

        // 2. Convertimos a DTO
        return solicitudes.stream()
            .map(this::mapearSolicitudModADTO)
            .toList();
    }

    // Método auxiliar de mapeo
    private SolicitudModOutputDTO mapearSolicitudModADTO(SolicitudDeModificacion entity) {
        SolicitudModOutputDTO dto = new SolicitudModOutputDTO();

        dto.setId(entity.getId());
        dto.setHecho(entity.getHecho());
        dto.setEstado(entity.getEstado());
        dto.setFechaSolicitud(entity.getFechaSolicitud());

        if (entity.getUsuario() != null) {
            dto.setNombreUsuarioTexto(entity.getUsuario().getNombreUsuario());
        } else {
            dto.setNombreUsuarioTexto("Anónimo");
        }

        // --- AGREGA ESTO PARA QUE VIAJEN LOS DATOS ---
        dto.setPropuestaTitulo(entity.getPropuestaTitulo());
        dto.setPropuestaDescripcion(entity.getPropuestaDescripcion()); // <--- AQUÍ ESTÁ LA MAGIA
        dto.setPropuestaArchivoMultimedia(entity.getPropuestaArchivoMultimedia());
        dto.setPropuestaFechaDelHecho(entity.getPropuestaFechaDelHecho());
        dto.setPropuestaCategoria(entity.getPropuestaCategoria());
        dto.setPropuestaLugar(entity.getPropuestaLugar());

        return dto;
    }

    @Override
    public List<SolicitudDeEliminacion> buscarEliminacionesPorUsuario(String username) {
        return solicitudesRepository.findByUsuario_NombreUsuario(username);
    }

    @Override
    public List<SolicitudDeModificacion> buscarModificacionesPorUsuario(String username) {
        return solicitudDeModificacionRepository.findByUsuario_NombreUsuario(username);
    }
}
