package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.Datos.Origen;
import ar.utn.ba.ddsi.Metamapa.Fuente.DatasetCSV;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.ColeccionRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.HechosRepository;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoInputDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoOutputDTO;
import ar.utn.ba.ddsi.Metamapa.services.IEstaticasService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service
public class EstaticasService implements IEstaticasService {

    private final HechosRepository hechosRepository;
    private final ColeccionRepository coleccionRepository;
    private final CategoriaRepository categoriaRepository;

    private DatasetCSV datasetCSV=new DatasetCSV();
    private final Path rootLocation = Paths.get("uploadsEstaticas");
    private static final Logger log = LoggerFactory.getLogger(EstaticasService.class);

    public EstaticasService(ColeccionRepository coleccionRepository, HechosRepository hechosRepository,CategoriaRepository categoriaRepository) throws IOException {
        Files.createDirectories(rootLocation);
        this.coleccionRepository=coleccionRepository;
        this.hechosRepository=hechosRepository;
        this.categoriaRepository=categoriaRepository;
    }

    @Async
    public void leerArchivoAsincronico(InputStream inputStream) {
        long inicio = System.currentTimeMillis();

        try {
            List<Hecho> hechos = new ArrayList<>();

            try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {

                reader.readNext(); // saltar encabezado
                String[] linea;

                while ((linea = reader.readNext()) != null) {
                    Hecho nuevoHecho = this.inicializarHecho(linea);
                    datasetCSV.gestionarRepetidos(nuevoHecho, hechos);
                }

            } catch (IOException | CsvValidationException e) {
                log.error("Error leyendo CSV: {}", e.getMessage(), e);
            }

            hechosRepository.saveAll(hechos);  // <<== BATCH

            long fin = System.currentTimeMillis();
            log.info("CSV procesado: {} hechos, tiempo: {} ms", hechos.size(), (fin - inicio));

        } catch (Exception e) {
            log.error("ERROR async: {}", e.getMessage(), e);
        }
    }
    private Categoria asegurarCategoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Categoria nueva = new Categoria();
                    nueva.setNombre(nombre); // <- IMPORTANTE
                    return categoriaRepository.save(nueva);
                });
    }
    @Override
    public String leerArchivo(InputStream inputStream) {
        byte[] data=null;
        try {
            // Copia el stream a memoria (seguro y reutilizable)
            data = inputStream.readAllBytes();

        } catch (IOException e) {
            System.out.println( "Error copiando el archivo: " + e.getMessage());
        }

        // Llama ASYNC
        this.leerArchivoAsincronico(new ByteArrayInputStream(data));

        return "Cargando hechos ...";
    }

    private Hecho inicializarHecho(String[] linea) {
        String titulo = linea[0];
        String descripcion = linea[1];
        String categoria = linea[2];
        double latitud = Double.parseDouble(linea[3]); // String -> double
        double longitud = Double.parseDouble(linea[4]);
/*
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime fechaDelHecho = LocalDateTime.parse(linea[5], formatter); // String -> LocalDate
*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate fechaDelHechoDate = LocalDate.parse(linea[5], formatter);
        LocalDateTime fechaDelHecho = fechaDelHechoDate.atStartOfDay();

        Origen origen = Origen.DATASET;

        //return new Hecho(titulo,descripcion,null,categoria,latitud,longitud,fechaDelHecho,origen,null );
        Hecho hechoE = new Hecho();
        hechoE.setTitulo(titulo);
        hechoE.setDescripcion(descripcion);
        hechoE.setCategoria(asegurarCategoria(categoria));
        hechoE.setLugar(new Lugar(latitud,longitud));
        hechoE.setFechaDelHecho(fechaDelHecho);
        hechoE.setOrigen(origen);
        hechoE.setFechaDeCarga(LocalDateTime.now());
        hechoE.setVisible(true);
        hechoE.setEditable(false);
        hechoE.setEtiquetas(new ArrayList<>());
        return hechoE;
    }
    private HechoOutputDTO hechoEntityADTO(Hecho hecho){
        HechoOutputDTO hechoOutputDTO = new HechoOutputDTO();
        hechoOutputDTO.setId(hecho.getId());
        hechoOutputDTO.setTitulo(hecho.getTitulo());
        hechoOutputDTO.setCategoria(hecho.getCategoria().getNombre());
        if (hecho.getEtiquetas() != null) {
            List<String> nombresEtiquetas = hecho.getEtiquetas().stream()
                    .map(Etiqueta::getNombre) // For each Etiqueta, get its name
                    .collect(Collectors.toList());
            hechoOutputDTO.setEtiquetas(nombresEtiquetas);
        }
        hechoOutputDTO.setFechaDelHecho(hecho.getFechaDelHecho());
        hechoOutputDTO.setDescripcion(hecho.getDescripcion());
        hechoOutputDTO.setLongitud(hecho.getLugar().getLongitud());
        hechoOutputDTO.setLatitud(hecho.getLugar().getLatitud());
        hechoOutputDTO.setFechaDeCarga(hecho.getFechaDeCarga());
        hechoOutputDTO.setOrigen(hecho.getOrigen().name());
        return hechoOutputDTO;
    }

    @Override
    public HechoDTO obtenerHechoInd(Long id){
        HechoDTO hechoDTO = hechoADTO(hechosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la colección con id: " + id)));
        return hechoDTO;
    }
    @Override
    public Page<HechoDTO> obtenerHechosPorColeccionPaginado(Long idColeccion, Pageable pageable) {
        Page<Hecho> pageHechos = hechosRepository.findByColeccion_Id(idColeccion, pageable);

        return pageHechos.map(this::hechoADTO);
    }

    @Override
    public Page<HechoOutputDTO> obtenerHechosEstaticos(Pageable pageable) {

        Page<Hecho> page = hechosRepository.findByOrigen(Origen.DATASET, pageable);

        return page.map(this::hechoEntityADTO);
    }

    @Override
    public HechoDTO modificarHechoEstatico(HechoInputDTO hechoDTO, MultipartFile archivo, Long idHecho, Long idColeccion){
        Hecho hecho = this.hechosRepository.findById(idHecho).orElseThrow(() -> new IllegalArgumentException("No se encontró la colección con id: " + idHecho));
        hecho.setCategoria(asegurarCategoria(hechoDTO.getCategoriaNombre()));
        hecho.setDescripcion(hechoDTO.getDescripcion());
        hecho.setTitulo(hechoDTO.getTitulo());
        hecho.setLugar(new Lugar(hechoDTO.getLatitud(),hechoDTO.getLongitud()));
        hecho.setFechaDelHecho(hechoDTO.getFechaDelHecho());
        try {
            if (archivo != null && !archivo.isEmpty()) {
                hecho.setArchivoMultimedia(guardarArchivo(archivo));
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo multimedia",e);
        }
        if(idColeccion != null) {
            Coleccion coleccion = coleccionRepository.findById(idColeccion)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el coleccion con id: " + idColeccion));;
            coleccion.agregarHecho(hecho);
            coleccionRepository.save(coleccion);
            hecho.setColeccion(coleccion);
        }
        this.hechosRepository.save(hecho);

        return hechoADTO(hecho);

    }


    //para guardar archivomultimedia
    public String guardarArchivo(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) return null;

        String nombreUnico = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
        Path destino = rootLocation.resolve(nombreUnico);

        Files.copy(archivo.getInputStream(), destino);

        return "uploadsEstaticas/" + nombreUnico;
    }
    private HechoDTO hechoADTO(Hecho hecho){
        HechoDTO hechoOutputDTO = new HechoDTO();
        hechoOutputDTO.setId(hecho.getId());
        hechoOutputDTO.setTitulo(hecho.getTitulo());
        hechoOutputDTO.setCategoria(hecho.getCategoria().getNombre());
        if (hecho.getEtiquetas() != null) {
            List<String> nombresEtiquetas = hecho.getEtiquetas().stream()
                    .map(Etiqueta::getNombre) // For each Etiqueta, get its name
                    .collect(Collectors.toList());
            hechoOutputDTO.setEtiquetas(nombresEtiquetas);
        }
        hechoOutputDTO.setFechaDelHecho(hecho.getFechaDelHecho());
        hechoOutputDTO.setDescripcion(hecho.getDescripcion());
        hechoOutputDTO.setLongitud(hecho.getLugar().getLongitud());
        hechoOutputDTO.setLatitud(hecho.getLugar().getLatitud());
        hechoOutputDTO.setFechaDeCarga(hecho.getFechaDeCarga());
        hechoOutputDTO.setOrigen(hecho.getOrigen().name());
        if(hecho.getColeccion()!=null){
            hechoOutputDTO.setIdColeccion(hecho.getColeccion().getId());
        }
        hechoOutputDTO.setArchivoMultimedia(hecho.getArchivoMultimedia());
        return hechoOutputDTO;
    }
}
