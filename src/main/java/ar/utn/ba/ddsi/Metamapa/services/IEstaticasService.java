package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface IEstaticasService {
    String leerArchivo(InputStream inputStream);
    Page<HechoOutputDTO> obtenerHechosEstaticos(Pageable pageable);
    HechoDTO modificarHechoEstatico(HechoInputDTO hechoDTO, MultipartFile archivo, Long id, Long idColeccion);
    Page<HechoDTO> obtenerHechosPorColeccionPaginado(Long idColeccion, Pageable pageable);
    HechoDTO obtenerHechoInd(Long id);

}
