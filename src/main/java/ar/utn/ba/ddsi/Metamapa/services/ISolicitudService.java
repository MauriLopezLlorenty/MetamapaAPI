package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudModOutputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudOutputDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ISolicitudService {

    void procesarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud);

    SolicitudOutputDTO crearSolicitudDeEliminacion(SolicitudInputDTO solicitud,String username);

    List<SolicitudOutputDTO> obtenerSolicitudesDeEliminacion();

    SolicitudDeEliminacion obtenerSolicitudDeEliminacion(Long id);

    void acpetarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud);

    void rechazarSolicitudDeEliminacion(SolicitudDeEliminacion solicitud);

    SolicitudModOutputDTO crearSolicitudDeModificacion(SolicitudModInputDTO solicitud,String username );
    List<SolicitudModOutputDTO> obtenerSolicitudesDeModificacion();

    void aceptarSolicitudDeModificacion(SolicitudDeModificacion solicitud);

    void rechazarSolicitudDeModificacion(SolicitudDeModificacion solicitud);

    SolicitudDeModificacion obtenerSolicitudDeMod(Long id);
    List<SolicitudDeEliminacion> buscarEliminacionesPorUsuario(String username);
    List<SolicitudDeModificacion> buscarModificacionesPorUsuario(String username);


}
