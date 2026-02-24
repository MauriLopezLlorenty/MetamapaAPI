package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.models.dtos.UsuarioDTO;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;

public interface IUsuarioService {
  Usuario registrarUsuario(UsuarioDTO usuarioDTO);
  Usuario buscarPorNombreUsuario(String nombreUsuario);
}