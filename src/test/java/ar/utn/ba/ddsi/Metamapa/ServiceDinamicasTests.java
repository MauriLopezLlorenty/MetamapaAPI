package ar.utn.ba.ddsi.Metamapa;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.SolicitudDeModificacion;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.services.impl.DinamicasService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class ServiceDinamicasTests {

    @Autowired
    HechosRepository repositorio;
    @Autowired
    DinamicasService service;
    @Test
    @DisplayName("Llega un Hecho y se lo registra")
    public void seAgregaHechoDeFuenteDinamica() throws IOException {

        HechoInputDTO dto = new HechoInputDTO(
                "Incendio en Córdoba",
                null,
                "Incendio forestal de gran magnitud",
                new Categoria("Incendio"),
                new Lugar(-26.7800, -60.4587),
                LocalDateTime.of(2025, 5, 10,0,0),
                null,
                null,
                null,
                null);
        Hecho hechoUno=service.crearHecho(dto);
        HechoInputDTO otroDto = new HechoInputDTO(
                "Inundación en Buenos Aires",            // título
                null,                                    // archivo multimedia (null si no hay)
                "Inundaciones graves por tormentas intensas", // descripción
                new Categoria("Desastre Natural"),       // categoría
                new Lugar(-34.6037, -58.3816),           // lugar (latitud, longitud)
                LocalDateTime.of(2025, 3, 15,0,0),               // fecha del hecho
                null,                                   // contribuyente (puede ser null)
                null,
                null,
                null
        );

        Hecho hechoDos=service.crearHecho(otroDto);
        System.out.println("Se tiene " + repositorio.findAll().size() + " Hechos");
        repositorio.findAll().forEach(hecho -> {
            System.out.println("Título: " + hecho.getTitulo() + " | ID: " + hecho.getId());
        });
        HechoInputDTO hechoActualizado = new HechoInputDTO(
                "Terremoto en San Juan",                       // título nuevo
                null,                                         // archivo multimedia
                "Fuerte terremoto que causó daños estructurales", // descripción nueva
                new Categoria("Desastre Natural"),            // categoría
                new Lugar(-31.5375, -68.5364),                 // lugar nuevo (latitud, longitud)
                LocalDateTime.of(2025, 6, 5,0,0),                      // nueva fecha del hecho
                null,
                null,
                null,
                null
        );

        SolicitudDeModificacion solicitudMod = new SolicitudDeModificacion(repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")));
        System.out.println("El estado actual de la solicitud es: " + solicitudMod.getEstado());
        solicitudMod.aceptar(repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")));
        System.out.println("El estado actual de la solicitud es: " + solicitudMod.getEstado() + " por lo que se modifica el hecho con ID: "+hechoUno.getId());
        service.modificarHecho(hechoUno.getId(), hechoActualizado);
        repositorio.findAll().forEach(hecho -> {
            System.out.println("Título: " + hecho.getTitulo() + " | ID: " + hecho.getId());
        });


    }
    @Test
    @DisplayName("No se puede modificar un hecho viejo")
    public void hechoNoModificable() throws IOException{
        HechoInputDTO dto = new HechoInputDTO(
                "Incendio en Córdoba",
                null,
                "Incendio forestal de gran magnitud",
                new Categoria("Incendio"),
                new Lugar(-26.7800, -60.4587),
                LocalDateTime.of(2025, 5, 10,0,0),
                null,
                null,
                null,
                null);
        Hecho hechoUno=service.crearHecho (dto) ;
        repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).setFechaDeCarga(LocalDateTime.now().minusDays(12));
        System.out.println("Se tiene el hecho con el titulo: " + repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getTitulo());
        System.out.println("El hecho tiene como fecha de carga: " + repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getFechaDeCarga());
        System.out.println("Ya pasaron: "+ ChronoUnit.DAYS.between(repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getFechaDeCarga(), LocalDateTime.now()) + " Días");
        HechoInputDTO hechoActualizado = new HechoInputDTO(
                "Terremoto en San Juan",                       // título nuevo
                null,                                         // archivo multimedia
                "Fuerte terremoto que causó daños estructurales", // descripción nueva
                new Categoria("Desastre Natural"),            // categoría
                new Lugar(-31.5375, -68.5364),                 // lugar nuevo (latitud, longitud)
                LocalDateTime.of(2025, 6, 5,0,0),                      // nueva fecha del hecho
                null,
                null,
                null,
                null
        );
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            service.modificarHecho(hechoUno.getId(), hechoActualizado);
        });

        System.out.println("El hecho no se actualizo y sigue conservando su titulo original: " + repositorio.findById(hechoUno.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getTitulo());

        Assertions.assertEquals("El hecho ya no es editable pasados los 7 días.", exception.getMessage());
    }
    @Test
    @DisplayName("Hecho no editable por rechazo de solicitud")
    public void hechoNoEditable() throws IOException{
        HechoInputDTO dto = new HechoInputDTO(
                "Incendio en Córdoba",
                null,
                "Incendio forestal de gran magnitud",
                new Categoria("Incendio"),
                new Lugar(-26.7800, -60.4587),
                LocalDateTime.of(2025, 5, 10,0,0),
                null,
                null,
                null,
                null);
        Hecho hechoCuatro=service.crearHecho(dto);
        System.out.println("Se tiene el hecho con el titulo: " + repositorio.findById(hechoCuatro.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getTitulo());
        SolicitudDeModificacion solicitudMod = new SolicitudDeModificacion(repositorio.findById(hechoCuatro.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")));
        solicitudMod.rechazar(repositorio.findById(hechoCuatro.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")));
        HechoInputDTO hechoActualizado = new HechoInputDTO(
                "Terremoto en San Juan",                       // título nuevo
                null,                                         // archivo multimedia
                "Fuerte terremoto que causó daños estructurales", // descripción nueva
                new Categoria("Desastre Natural"),            // categoría
                new Lugar(-31.5375, -68.5364),                 // lugar nuevo (latitud, longitud)
                LocalDateTime.of(2025, 6, 5,0,0),                      // nueva fecha del hecho
                null,
                null,
                null,
                null
        );
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            service.modificarHecho(hechoCuatro.getId(), hechoActualizado);
        });
        System.out.println(solicitudMod.getEstado());
        System.out.println("El hecho no se actualizo y sigue conservando su titulo original: " + repositorio.findById(hechoCuatro.getId()).orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id")).getTitulo());
        Assertions.assertEquals("El hecho no es editable.", exception.getMessage());




    }


}