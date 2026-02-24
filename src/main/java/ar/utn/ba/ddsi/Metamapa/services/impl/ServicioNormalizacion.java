package ar.utn.ba.ddsi.Metamapa.services.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Categoria;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.CategoriaRepository;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ServicioNormalizacion {

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;
    /**
     * Busca un Lugar por su nombre. Si no existe, lo crea y lo guarda.
     * Devuelve siempre una entidad Lugar persistid
     */
    public Lugar buscarOCrearLugar(double latitud, double longitud) {

        // 1. Buscamos si ya existe
        Optional<Lugar> lugarExistente = lugarRepository.findByLatitudAndLongitud(latitud, longitud);

        if (lugarExistente.isPresent()) {
            return lugarExistente.get();
        } else {
            // 2. Si no existe, creamos uno nuevo con nombre generado
            Lugar nuevoLugar = new Lugar();
            nuevoLugar.setLatitud(latitud);
            nuevoLugar.setLongitud(longitud);

            // Generamos el nombre: "Lugar: -34.6037, -58.3816"
            String nombreGenerado = "Lugar: " + latitud + ", " + longitud;
            nuevoLugar.setNombreNormalizado(nombreGenerado);

            return lugarRepository.save(nuevoLugar);
        }
    }


    private static final Map<String, String> equivalenciasLugares = Map.of(
        "caba", "Ciudad Autónoma de Buenos Aires",
        "capital federal", "Ciudad Autónoma de Buenos Aires",
        "bs as", "Buenos Aires"
    );

    private String estandarizarNombreLugar(String nombre) {
        if (nombre == null) return "Desconocido";
        String nombreLimpio = nombre.trim();
        // Ejemplo de la consigna (pa pruebas)
        if ("CABA".equalsIgnoreCase(nombreLimpio)) {
            return "Ciudad Autónoma de Buenos Aires";
        }
        return equivalenciasLugares.getOrDefault(nombreLimpio,nombreLimpio);
    }


    private static final Map<String, String> equivalenciasCategorias = new HashMap<>();

    static {
        // Incendios
        equivalenciasCategorias.put("fuego forestal", "Incendio Forestal");
        equivalenciasCategorias.put("incendio forestal", "Incendio Forestal");
        equivalenciasCategorias.put("incendios", "Incendio Forestal"); // Plural
        equivalenciasCategorias.put("wildfire", "Incendio Forestal");

        // Accidentes
        equivalenciasCategorias.put("caida de aeronave", "Accidente Aéreo");
        equivalenciasCategorias.put("avion caido", "Accidente Aéreo");

        // Seguridad (Tu caso específico)
        equivalenciasCategorias.put("delito", "Delitos"); // Singular
        equivalenciasCategorias.put("delitos", "Delitos"); // Plural
        equivalenciasCategorias.put("robo", "Robo");
        equivalenciasCategorias.put("robos", "Robo");
        equivalenciasCategorias.put("hurto", "Robo");
        equivalenciasCategorias.put("asesinato", "Homicidio");
        equivalenciasCategorias.put("crimen", "Delitos");
    }

    public Categoria buscarOCrearCategoria(String nombreCategoria) {
        // 1. Estandarizamos el nombre (Ej: "  Delito  " -> "Delitos")
        String nombreFinal = estandarizarNombreCategoria(nombreCategoria);

        // 2. Buscamos en BD
        Optional<Categoria> categoriaExistente = categoriaRepository.findByNombreIgnoreCase(nombreFinal);

        if (categoriaExistente.isPresent()) {
            return categoriaExistente.get();
        } else {
            // 3. Si no existe, creamos la nueva con el nombre YA CORREGIDO
            Categoria nuevaCategoria = new Categoria();
            nuevaCategoria.setNombre(nombreFinal); // Guardará "Delitos", no "delito"
            return categoriaRepository.save(nuevaCategoria);
        }
    }

    private String estandarizarNombreCategoria(String nombreInput) {
        if (nombreInput == null) return "Sin Categoría";

        // A. Limpieza básica: Minúsculas, trim y quitar acentos
        // Ej: "Delítos " -> "delitos"
        String nombreLimpio = limpiarTexto(nombreInput);

        // B. Buscar coincidencia exacta en el diccionario
        if (equivalenciasCategorias.containsKey(nombreLimpio)) {
            return equivalenciasCategorias.get(nombreLimpio);
        }

        // C. Intentar singularizar (Lógica simple: quitar 's' o 'es' al final)
        // Esto ayuda si escriben "Cocheras" y no lo tenías mapeado, lo convertirá a "Cochera" si sirve
        if (nombreLimpio.endsWith("s")) {
            String singular = nombreLimpio.substring(0, nombreLimpio.length() - 1); // Quita la 's'
            if (equivalenciasCategorias.containsKey(singular)) {
                return equivalenciasCategorias.get(singular);
            }
        }

        // D. Si no encontramos nada, devolvemos el texto original pero Capitalizado
        // Ej: "delito informatico" -> "Delito Informatico"
        return capitalizarPalabras(nombreInput.trim());
    }

    // --- Métodos Auxiliares ---

    private String limpiarTexto(String texto) {
        String textoLower = texto.trim().toLowerCase();
        // Quitar acentos (Normalizer NFD separa la letra de la tilde, luego regex borra la tilde)
        String normalizado = Normalizer.normalize(textoLower, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalizado).replaceAll("");
    }

    private String capitalizarPalabras(String texto) {
        String[] palabras = texto.split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return resultado.toString().trim();
    }
}
