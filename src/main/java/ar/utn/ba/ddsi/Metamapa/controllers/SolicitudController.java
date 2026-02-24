package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.ISolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

  @Autowired
  private ISolicitudService solicitudService;

  // --- ENDPOINTS PARA EL USUARIO COMÚN ---

  @PostMapping("/modificacion")
  public ResponseEntity<?> crearSolicitudModificacion(
      @RequestBody SolicitudModInputDTO solicitudDTO,
      Principal principal) {

    try {
      // 2. Pasamos el username al servicio.
      // El servicio se encarga de validar si es el dueño y si está en la fecha permitida (7 días).
      SolicitudModOutputDTO resultado = solicitudService.crearSolicitudDeModificacion(
          solicitudDTO,
          principal.getName()
      );

      return ResponseEntity.status(HttpStatus.CREATED).body(resultado);

    } catch (RuntimeException e) {
      // Capturamos las excepciones del servicio (Ej: "Plazo vencido", "No es el dueño")
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/mis-modificaciones")
  public ResponseEntity<List<SolicitudDeModificacion>> verMisSolicitudes(Principal principal) {
    if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    List<SolicitudDeModificacion> misSolicitudes =
        solicitudService.buscarModificacionesPorUsuario(principal.getName());
    return ResponseEntity.ok(misSolicitudes);
  }

  // --- ENDPOINTS PARA EL ADMINISTRADOR ---
  // Idealmente proteger con @PreAuthorize("hasRole('ADMIN')")

  @GetMapping("/pendientes")
  public ResponseEntity<List<SolicitudModOutputDTO>> listarPendientes() {
    return ResponseEntity.ok(solicitudService.obtenerSolicitudesDeModificacion());
  }

  @PostMapping("/modificacion/{id}/aceptar")
  public ResponseEntity<?> aceptarSolicitud(@PathVariable Long id) {
    try {
      // Buscamos la solicitud primero (ya que tu servicio pide la entidad, no el ID)
      SolicitudDeModificacion solicitud = solicitudService.obtenerSolicitudDeMod(id);

      solicitudService.aceptarSolicitudDeModificacion(solicitud);
      return ResponseEntity.ok("Solicitud aceptada y cambios aplicados al hecho.");

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error al aceptar: " + e.getMessage());
    }
  }

  @PostMapping("/modificacion/{id}/rechazar")
  public ResponseEntity<?> rechazarSolicitud(@PathVariable Long id) {
    try {
      SolicitudDeModificacion solicitud = solicitudService.obtenerSolicitudDeMod(id);

      solicitudService.rechazarSolicitudDeModificacion(solicitud);
      return ResponseEntity.ok("Solicitud rechazada.");

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error al rechazar: " + e.getMessage());
    }
  }
}