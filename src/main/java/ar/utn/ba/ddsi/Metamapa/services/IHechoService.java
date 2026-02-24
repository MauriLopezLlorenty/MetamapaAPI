package ar.utn.ba.ddsi.Metamapa.services;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IHechoService {
  /**
   * Toma los datos del formulario (DTO) y un archivo, crea las entidades
   * correspondientes y guarda el nuevo Hecho en la base de datos.
   * @param hechoDTO Los datos del hecho enviados desde el frontend.
   * @param archivo El archivo multimedia opcional.
   * @return El objeto Hecho que fue guardado.
   */
  Hecho crearHechoDesdeDTO(HechoInputDTO hechoDTO, MultipartFile archivo,String username) throws IOException;
  Page<Hecho> obtenerTodosPaginados(Pageable pageable);
  Hecho buscarPorId(Long id);
  List<Hecho> buscarPorUsuario(String nombreUsuario);
  long contarHechosDeUsuario(String nombreUsuario);
  Hecho editarHecho(Long id, HechoInputDTO hechoDTO, MultipartFile archivo, String username) throws IOException;
}
