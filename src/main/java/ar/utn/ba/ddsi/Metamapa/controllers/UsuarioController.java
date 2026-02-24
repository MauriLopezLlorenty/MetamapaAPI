package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.UsuarioDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.UsuarioOutputDTO; // Necesitamos este DTO de salida
import ar.utn.ba.ddsi.Metamapa.services.IHechoService;
import ar.utn.ba.ddsi.Metamapa.services.ISolicitudService;
import ar.utn.ba.ddsi.Metamapa.services.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
// Ya no necesitas @CrossOrigin aquí si tenés la configuración global en WebConfig
public class UsuarioController {

  private final IUsuarioService usuarioService;
  private final IHechoService hechoService;
  private final ISolicitudService solicitudService;

  // Inyección del servicio por constructor
  public UsuarioController(IUsuarioService usuarioService, IHechoService hechoService, ISolicitudService solicitudService) {
    this.usuarioService = usuarioService;
    this.hechoService = hechoService;
    this.solicitudService = solicitudService;
  }

  @PostMapping("/registro")
  public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
    try {
      Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuarioDTO);
      // Convertimos a DTO de salida para no exponer la contraseña
      UsuarioOutputDTO usuarioOutput = convertirAUsuarioOutputDTO(usuarioRegistrado);
      return new ResponseEntity<>(usuarioOutput, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      // Captura errores de validación (ej. usuario/email duplicado)
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      // Captura otros errores inesperados
      return new ResponseEntity<>("Ocurrió un error inesperado durante el registro.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/perfil")
  public ResponseEntity<?> obtenerPerfilUsuario(@AuthenticationPrincipal UserDetails userDetails) {
    // Si userDetails es null, el filtro JWT no autenticó al usuario
    if (userDetails == null) {
      return new ResponseEntity<>("Acceso no autorizado. Token inválido o ausente.", HttpStatus.UNAUTHORIZED);
    }

    try {
      String nombreUsuario = userDetails.getUsername();
      Usuario usuario = usuarioService.buscarPorNombreUsuario(nombreUsuario);
      UsuarioOutputDTO perfilDTO = convertirAUsuarioOutputDTO(usuario);
      System.out.println(">>> DEBUG PERFIL: Nombre en el token: " + nombreUsuario);

      long cantidad = hechoService.contarHechosDeUsuario(nombreUsuario);
      perfilDTO.setHechosCargados(cantidad);
      System.out.println(">>> DEBUG PERFIL: Hechos contados: " + cantidad);
      // --- LOGICA DE UNIFICACION PARA EL GRAFICO ---
      List<Map<String, String>> listaCombinada = new ArrayList<>();

      // A. Buscar Eliminaciones
      List<SolicitudDeEliminacion> elims = solicitudService.buscarEliminacionesPorUsuario(nombreUsuario);
      if (elims != null) {
        for (SolicitudDeEliminacion s : elims) {
          Map<String, String> item = new HashMap<>();
          String titulo = (s.getHecho() != null) ? s.getHecho().getTitulo() : "Desconocido";

          // Prefijo visual para el gráfico
          item.put("titulo", titulo);
          item.put("estado", s.getEstado().toString());
          item.put("tipo", "ELIMINACION");
          item.put("fecha_estado", s.getFechaEstado().toString());

          listaCombinada.add(item);
        }
      }

      // B. Buscar Modificaciones
      List<SolicitudDeModificacion> mods = solicitudService.buscarModificacionesPorUsuario(nombreUsuario);

      if (mods != null) {
        for (SolicitudDeModificacion s : mods) {
          Map<String, String> item = new HashMap<>();

          // Validación del título
          String titulo = (s.getHecho() != null) ? s.getHecho().getTitulo() : "Desconocido";

          item.put("titulo", titulo);

          // Validación del estado (por seguridad)
          item.put("estado", (s.getEstado() != null) ? s.getEstado().toString() : "DESCONOCIDO");

          item.put("tipo", "MODIFICACION");

          // ✅ CÓDIGO CORREGIDO (Null Safe)
          // 1. Borramos la línea que estaba fuera del if (la que causaba el error)
          // 2. Usamos item.put() en el else
          if (s.getFechaEstado() != null) {
            item.put("fecha_estado", s.getFechaEstado().toString());
          } else {
            item.put("fecha_estado", "-"); // Usamos put, no get
          }

          listaCombinada.add(item);
        }
      }

      // C. Guardar en el DTO
      perfilDTO.setUltimasSolicitudes(listaCombinada);
      System.out.println(">>> DEBUG PERFIL: Todo OK, devolviendo respuesta.");
      return ResponseEntity.ok(perfilDTO);

    } catch (RuntimeException e) {
      e.printStackTrace(); // Agrega esto para ver errores en consola si vuelven a pasar
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }


  @PutMapping("/hechos/{id}")
  public ResponseEntity<?> editarMiHecho(
      @PathVariable Long id,
      @RequestPart("hecho") HechoInputDTO hechoDTO,
      @RequestPart(value = "archivo", required = false) MultipartFile archivo,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    
    if (userDetails == null) {
      return new ResponseEntity<>("Acceso no autorizado. Token inválido o ausente.", HttpStatus.UNAUTHORIZED);
    }

    try {
      String username = userDetails.getUsername();
      Hecho hechoEditado = hechoService.editarHecho(id, hechoDTO, archivo, username);
      return new ResponseEntity<>(hechoEditado, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IllegalStateException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (IOException e) {
      System.err.println("Error de I/O al guardar archivo: " + e.getMessage());
      return new ResponseEntity<>("Error interno al procesar el archivo.", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      return new ResponseEntity<>("Ocurrió un error inesperado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private UsuarioOutputDTO convertirAUsuarioOutputDTO(Usuario usuario) {
    if (usuario == null) return null;
    UsuarioOutputDTO dto = new UsuarioOutputDTO();
    dto.setId(usuario.getId());
    dto.setNombre(usuario.getNombre());
    dto.setApellido(usuario.getApellido());
    dto.setNombreUsuario(usuario.getNombreUsuario());
    dto.setMail(usuario.getMail());
    dto.setUbicacion(usuario.getUbicacion());
    dto.setRol(usuario.getRol().toString());
    return dto;
  }

}