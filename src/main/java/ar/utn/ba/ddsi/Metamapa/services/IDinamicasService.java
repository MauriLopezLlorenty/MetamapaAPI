package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IDinamicasService  {
    Hecho crearHecho(HechoInputDTO hechoInputDTO, MultipartFile archivo) throws IOException;
    void modificarHecho(Long id, HechoInputDTO hechoInputDTO, MultipartFile archivo) throws IOException;
}
