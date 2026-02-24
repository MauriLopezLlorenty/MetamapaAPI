package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.UsuarioRepository;
import ar.utn.ba.ddsi.Metamapa.services.impl.JwtService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;

  public AuthenticationController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
    this.authenticationManager = authenticationManager;
    this.usuarioRepository = usuarioRepository;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    // 1. Autenticar (Verificar usuario y contraseña)
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    Usuario usuario = this.usuarioRepository.findByNombreUsuario(request.getUsername())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado")); // Manejo básico de error

    // 3. Generar el token usando el usuario completo
    final String jwt = jwtService.generateToken(usuario);

    return ResponseEntity.ok(new LoginResponse(jwt));
  }
}

// Clases DTO para la petición y respuesta
@Data
class LoginRequest {
  private String username;
  private String password;
}

@Data
class LoginResponse {
  private final String token;
}