package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IDinamicasService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class DinamicasService implements IDinamicasService {

    private final HechosRepository hechosRepository;
    // Asumo que tienes un servicio de normalización, lo inyectamos por constructor
    // private final ServicioNormalizacion servicioNormalizacion;
    private static final String MULTIMEDIA_DIR = "uploads/hechos/";

    // Best practice: Inject all dependencies through the constructor.
    public DinamicasService(HechosRepository hechosRepository) {
        this.hechosRepository = hechosRepository;
        // this.servicioNormalizacion = servicioNormalizacion;
    }

    // FIX: The method now accepts the file as a separate parameter.
    public Hecho crearHecho(HechoInputDTO hechoInputDTO, MultipartFile archivo) throws IOException {
        Hecho nuevoHecho = new Hecho();

        // 1. Map simple fields from the DTO to the new Hecho entity
        nuevoHecho.setTitulo(hechoInputDTO.getTitulo());
        nuevoHecho.setDescripcion(hechoInputDTO.getDescripcion());
        nuevoHecho.setFechaDelHecho(hechoInputDTO.getFechaDelHecho());
        nuevoHecho.setOrigen(Origen.valueOf(hechoInputDTO.getOrigen())); // Convert String from DTO to Enum
        nuevoHecho.setFechaDeCarga(LocalDateTime.now());
        nuevoHecho.setEditable(true); // Assuming new facts are editable

        // 2. Build complex objects using the simple data from the DTO
        // FIX: Use getCategoriaNombre() from DTO to create a Categoria object
        Categoria categoria = new Categoria(hechoInputDTO.getCategoriaNombre());
        nuevoHecho.setCategoria(categoria);

        // FIX: Use getLatitud() and getLongitud() from DTO to create a Lugar object
        Lugar lugar = new Lugar(hechoInputDTO.getLatitud(), hechoInputDTO.getLongitud());
        nuevoHecho.setLugar(lugar);

        // 3. Handle the file, which is now a separate parameter
        if (archivo != null && !archivo.isEmpty()) {
            String nombreArchivo = guardarArchivo(archivo);
            nuevoHecho.setArchivoMultimedia(nombreArchivo);
        }

        // FIX: The 'contribuyente' should be determined from security context, not the DTO.
        // For now, we leave it null or assign a default user if needed.
        // nuevoHecho.setContribuyente(someDefaultUser);

        return this.hechosRepository.save(nuevoHecho);
    }

    // FIX: The method now accepts the file as a separate parameter.
    public void modificarHecho(Long id, HechoInputDTO hechoInputDTO, MultipartFile archivo) throws IOException {
        Hecho hechoModificado = this.hechosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el hecho con id: " + id));

        // ... your validation logic remains the same ...

        hechoModificado.setTitulo(hechoInputDTO.getTitulo());
        hechoModificado.setDescripcion(hechoInputDTO.getDescripcion());
        hechoModificado.setFechaDelHecho(hechoInputDTO.getFechaDelHecho());

        // FIX: Use getCategoriaNombre() from the DTO
        if (hechoInputDTO.getCategoriaNombre() != null && !hechoInputDTO.getCategoriaNombre().isEmpty()) {
            // Categoria categoriaNormalizada = servicioNormalizacion.buscarOCrearCategoria(hechoInputDTO.getCategoriaNombre());
            // hechoModificado.setCategoria(categoriaNormalizada);
            hechoModificado.setCategoria(new Categoria(hechoInputDTO.getCategoriaNombre())); // Simplified version
        }

        // FIX: Use getLatitud() and getLongitud() from the DTO
        // Note: The logic for Lugar update might need refinement based on your business rules.
        hechoModificado.setLugar(new Lugar(hechoInputDTO.getLatitud(), hechoInputDTO.getLongitud()));

        // FIX: Handle the file from the separate 'archivo' parameter
        if (archivo != null && !archivo.isEmpty()) {
            String nuevoArchivo = guardarArchivo(archivo);
            hechoModificado.setArchivoMultimedia(nuevoArchivo);
        }

        this.hechosRepository.save(hechoModificado);
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        Path dir = Paths.get(MULTIMEDIA_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String nombreFinal = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = dir.resolve(nombreFinal);
        archivo.transferTo(destino.toFile());
        return nombreFinal;
    }
}