package ar.utn.ba.ddsi.Metamapa.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Configuración de Rate Limiting para diferentes tipos de endpoints.
 * Define límites específicos según la criticidad y el tipo de operación.
 */
@Component
public class RateLimitConfig {

    // Límites configurables desde application.properties
    @Value("${rate.limit.public.capacity:100}")
    private int publicCapacity;

    @Value("${rate.limit.public.refill-period:60}")
    private int publicRefillPeriod;

    @Value("${rate.limit.authenticated.capacity:200}")
    private int authenticatedCapacity;

    @Value("${rate.limit.authenticated.refill-period:60}")
    private int authenticatedRefillPeriod;

    @Value("${rate.limit.admin.capacity:500}")
    private int adminCapacity;

    @Value("${rate.limit.admin.refill-period:60}")
    private int adminRefillPeriod;

    @Value("${rate.limit.auth.capacity:5}")
    private int authCapacity;

    @Value("${rate.limit.auth.refill-period:300}")
    private int authRefillPeriod;

    @Value("${rate.limit.create-hecho.capacity:10}")
    private int createHechoCapacity;

    @Value("${rate.limit.create-hecho.refill-period:60}")
    private int createHechoRefillPeriod;

    // Mapa de patrones de endpoints a sus configuraciones de límite
    private final Map<EndpointPattern, LimitConfig> endpointLimits = new HashMap<>();

    /**
     * Inicializa los límites de rate para diferentes endpoints después de que Spring inyecte los valores
     */
    @PostConstruct
    public void initialize() {
        // Endpoints de autenticación: límite muy estricto para prevenir ataques de fuerza bruta
        endpointLimits.put(new EndpointPattern("/api/auth/login", "POST"), 
                new LimitConfig(authCapacity, authRefillPeriod));

        // Endpoints públicos de lectura: límite moderado
        endpointLimits.put(new EndpointPattern("/api/public/hechos", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/hechoindv/.*", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/hechos/filtrar", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/colecciones", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/coleccion/.*", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/categorias", "GET"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));

        // Endpoint de creación de hechos: límite más estricto para prevenir spam
        endpointLimits.put(new EndpointPattern("/api/public/crearhecho", "POST"), 
                new LimitConfig(createHechoCapacity, createHechoRefillPeriod));

        // Endpoints de solicitudes: límite moderado
        endpointLimits.put(new EndpointPattern("/api/public/solicitud-eliminacion", "POST"), 
                new LimitConfig(createHechoCapacity, createHechoRefillPeriod));

        // Endpoints autenticados: límite más generoso
        endpointLimits.put(new EndpointPattern("/api/hechos/.*", "GET"), 
                new LimitConfig(authenticatedCapacity, authenticatedRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/public/mis-hechos", "GET"), 
                new LimitConfig(authenticatedCapacity, authenticatedRefillPeriod));

        // Endpoints de administración: límite muy generoso
        endpointLimits.put(new EndpointPattern("/api/admin/.*", "GET"), 
                new LimitConfig(adminCapacity, adminRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/admin/.*", "POST"), 
                new LimitConfig(adminCapacity, adminRefillPeriod));
        endpointLimits.put(new EndpointPattern("/api/admin/.*", "PATCH"), 
                new LimitConfig(adminCapacity, adminRefillPeriod));

        // GraphQL: límite moderado
        endpointLimits.put(new EndpointPattern("/graphql", "POST"), 
                new LimitConfig(publicCapacity, publicRefillPeriod));
    }

    /**
     * Obtiene la configuración de límite para un endpoint específico
     */
    public LimitConfig getLimitForEndpoint(String endpoint, String method) {
        // Buscar coincidencia exacta primero
        EndpointPattern exactPattern = new EndpointPattern(endpoint, method);
        LimitConfig exactMatch = endpointLimits.get(exactPattern);
        if (exactMatch != null) {
            return exactMatch;
        }

        // Buscar coincidencia por patrón regex
        for (Map.Entry<EndpointPattern, LimitConfig> entry : endpointLimits.entrySet()) {
            if (entry.getKey().matches(endpoint, method)) {
                return entry.getValue();
            }
        }

        // Si no hay configuración específica, retornar null (sin límite)
        return null;
    }

    /**
     * Clase interna que representa un patrón de endpoint con su método HTTP
     */
    private static class EndpointPattern {
        private final Pattern pattern;
        private final String method;

        public EndpointPattern(String endpointPattern, String method) {
            // Convertir el patrón a regex (reemplazar .* por .* en regex)
            String regex = endpointPattern.replace(".*", ".*");
            this.pattern = Pattern.compile("^" + regex + "$");
            this.method = method.toUpperCase();
        }

        public boolean matches(String endpoint, String method) {
            return this.method.equals(method.toUpperCase()) && 
                   this.pattern.matcher(endpoint).matches();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EndpointPattern that = (EndpointPattern) o;
            return pattern.pattern().equals(that.pattern.pattern()) && 
                   method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return pattern.pattern().hashCode() * 31 + method.hashCode();
        }
    }

    /**
     * Clase que representa la configuración de límites para un endpoint
     */
    public static class LimitConfig {
        private final int capacity;              // Capacidad máxima del bucket
        private final int refillTokens;         // Tokens a reponer por período
        private final int refillPeriodSeconds;   // Período de reposición en segundos

        public LimitConfig(int capacity, int refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillTokens = capacity; // Por defecto, reponer la capacidad completa
            this.refillPeriodSeconds = refillPeriodSeconds;
        }

        public LimitConfig(int capacity, int refillTokens, int refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriodSeconds = refillPeriodSeconds;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public int getRefillPeriodSeconds() {
            return refillPeriodSeconds;
        }
    }
}

