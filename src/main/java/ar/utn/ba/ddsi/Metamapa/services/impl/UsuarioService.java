package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.UsuarioRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.UsuarioDTO;
import ar.utn.ba.ddsi.Metamapa.services.IUsuarioService;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.Usuario.Rol;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService{

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Usuario registrarUsuario(UsuarioDTO dto) {
    // 1. Validar que el usuario o mail no existan
    if (usuarioRepository.findByNombreUsuario(dto.getNombreUsuario()).isPresent()) {
      throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
    }
    if (usuarioRepository.findByMail(dto.getMail()).isPresent()) {
      throw new IllegalArgumentException("El correo electrónico ya está registrado.");
    }

    // 2. Crear la nueva entidad de usuario
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(dto.getNombre());
    nuevoUsuario.setApellido(dto.getApellido());
    nuevoUsuario.setNombreUsuario(dto.getNombreUsuario());
    nuevoUsuario.setMail(dto.getMail());

    // 3. Encriptar la contraseña ANTES de guardarla
    nuevoUsuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));

    nuevoUsuario.setUbicacion(dto.getUbicacion());
    nuevoUsuario.setRol(Rol.CONTRIBUYENTE);

    // 4. Guardar el usuario en la base de datos
    return usuarioRepository.save(nuevoUsuario);
  }
  @Override
  public Usuario buscarPorNombreUsuario(String nombreUsuario) {

    Optional<Usuario> usuarioOptional = usuarioRepository.findByNombreUsuario(nombreUsuario);

    return usuarioOptional.orElseThrow(() ->
            new RuntimeException("Usuario no encontrado con nombre de usuario: " + nombreUsuario)
    );
  }
}