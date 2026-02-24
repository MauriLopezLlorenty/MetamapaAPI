package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.DashboardDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudRevisionDTO;
import ar.utn.ba.ddsi.Metamapa.services.ISolicitudService;
import ar.utn.ba.ddsi.Metamapa.services.impl.AdminService;
import ar.utn.ba.ddsi.Metamapa.services.impl.ServicioNormalizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
// El SecurityConfig ya protege toda esta ruta para que solo entre el ADMIN
public class AdminController {

  @Autowired
  private ColeccionRepository coleccionRepository;
  private final AdminService adminService;
  private final ISolicitudService solicitudService;
  private final ServicioNormalizacion servicioNormalizacion;
  public AdminController(AdminService adminService, ISolicitudService solicitudService, ServicioNormalizacion servicioNormalizacion) {
    this.adminService = adminService;
    this.solicitudService = solicitudService;
    this.servicioNormalizacion = servicioNormalizacion;
  }


  @PostMapping("/importar-csv")
  public ResponseEntity<?> importarHechos(@RequestParam("file") MultipartFile file) {
    // Lógica para procesar CSV de +10.000 líneas
    return ResponseEntity.ok("Importación iniciada");
  }

  @GetMapping("/dashboard")
  public ResponseEntity<DashboardDTO> getDashboard() {
    return ResponseEntity.ok(adminService.obtenerResumenDashboard());
  }

  // --- 2. LISTADOS DE SOLICITUDES ---
  @GetMapping("/solicitudes/eliminacion")
  public ResponseEntity<List<SolicitudOutputDTO>> getSolicitudesEliminacion() {
    // Puedes agregar lógica de paginación aquí si son muchas
    return ResponseEntity.ok(solicitudService.obtenerSolicitudesDeEliminacion());
  }

  @GetMapping("/solicitudes/modificacion")
  public ResponseEntity<List<SolicitudModOutputDTO>> getSolicitudesModificacion() {
    // ✅ AHORA SÍ LLAMAMOS A LA BASE DE DATOS
    return ResponseEntity.ok(solicitudService.obtenerSolicitudesDeModificacion());
  }

  // Endpoints para aprobar/rechazar hechos...

  @GetMapping("/solicitudes-eliminacion")
  public ResponseEntity<List<SolicitudOutputDTO>> getAllSolicitudesEliminacion() {
    List<SolicitudOutputDTO> solicitudes = solicitudService.obtenerSolicitudesDeEliminacion();
    return new ResponseEntity<>(solicitudes, HttpStatus.OK);
  }

  @GetMapping("/solicitud-eliminacion/{id}")
  public ResponseEntity<SolicitudDeEliminacion> obtenerSolElimPorId(@PathVariable Long id) {
    try {
      SolicitudDeEliminacion solicitud = solicitudService.obtenerSolicitudDeEliminacion(id);
      return new ResponseEntity<>(solicitud, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PatchMapping("/solicitudes-eliminacion/{id}")
  public ResponseEntity<SolicitudDeEliminacion> procesarSolicitud(@PathVariable Long id) {
    SolicitudDeEliminacion solicitud = solicitudService.obtenerSolicitudDeEliminacion(id);
    solicitudService.procesarSolicitudDeEliminacion(solicitud);
    return new ResponseEntity<>(solicitud, HttpStatus.OK);
  }

  @PatchMapping("/solicitudes-eliminacion/{id}/aceptar")
  public ResponseEntity<SolicitudDeEliminacion> aceptarSolicitud(@PathVariable Long id) {
    SolicitudDeEliminacion solicitud = solicitudService.obtenerSolicitudDeEliminacion(id);
    solicitudService.acpetarSolicitudDeEliminacion(solicitud);
    return new ResponseEntity<>(solicitud, HttpStatus.OK);
  }

  @PatchMapping("/solicitudes-eliminacion/{id}/rechazar")
  public ResponseEntity<SolicitudDeEliminacion> rechazarSolicitud(@PathVariable Long id) {
    SolicitudDeEliminacion solicitud = solicitudService.obtenerSolicitudDeEliminacion(id);
    solicitudService.rechazarSolicitudDeEliminacion(solicitud);
    return new ResponseEntity<>(solicitud, HttpStatus.OK);
  }
  @PatchMapping("/solicitudes-modificacion/{id}/aceptar")
  public ResponseEntity<SolicitudDeModificacion> aceptarSolicitudMod(
      @PathVariable Long id,
      @RequestBody(required = false) SolicitudRevisionDTO revisionAdmin) {

    // 1. Buscamos la solicitud original
    SolicitudDeModificacion solicitud = solicitudService.obtenerSolicitudDeMod(id);

    // 2. Si el admin mandó correcciones, las sobreescribimos en la propuesta
    if (revisionAdmin != null) {

      // Textos simples
      if (revisionAdmin.getTituloFinal() != null) {
        solicitud.setPropuestaTitulo(revisionAdmin.getTituloFinal());
      }
      if (revisionAdmin.getDescripcionFinal() != null) {
        solicitud.setPropuestaDescripcion(revisionAdmin.getDescripcionFinal());
      }
      if (revisionAdmin.getArchivoMultimediaFinal() != null) {
        solicitud.setPropuestaArchivoMultimedia(revisionAdmin.getArchivoMultimediaFinal());
      }
      if (revisionAdmin.getFechaDelHechoFinal() != null) {
        solicitud.setPropuestaFechaDelHecho(revisionAdmin.getFechaDelHechoFinal());
      }

      // Lógica de Ubicación (Lat/Lon -> Objeto Lugar)
      if (revisionAdmin.getLatitudFinal() != null && revisionAdmin.getLongitudFinal() != null) {
        // Usamos el servicio para transformar números en un Lugar válido de la BD
        Lugar nuevoLugar = servicioNormalizacion.buscarOCrearLugar(
            revisionAdmin.getLatitudFinal(),
            revisionAdmin.getLongitudFinal()
        );
        solicitud.setPropuestaLugar(nuevoLugar);
      }
    }

    // 3. Ejecutamos la aceptación (esto copia las propuestas AL HECHO real)
    solicitudService.aceptarSolicitudDeModificacion(solicitud);

    return new ResponseEntity<>(solicitud, HttpStatus.OK);
  }
  @PatchMapping("/solicitudes-modificacion/{id}/rechazar")
  public ResponseEntity<SolicitudDeModificacion> rechazarSolicitudMod(@PathVariable Long id){
    SolicitudDeModificacion solicitud = solicitudService.obtenerSolicitudDeMod(id);
    solicitudService.rechazarSolicitudDeModificacion(solicitud);
    return new ResponseEntity<>(solicitud, HttpStatus.OK);
  }
}