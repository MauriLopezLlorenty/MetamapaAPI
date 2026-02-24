package ar.utn.ba.ddsi.Metamapa.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de Rate Limiting que protege los endpoints contra abusos y sobrecarga.
 * Utiliza Bucket4j con algoritmo Token Bucket para limitar las peticiones por cliente.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> cache;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitingFilter(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
        // Cache con expiración automática para limpiar buckets inactivos
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientKey = getClientKey(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        // Determinar qué límite aplicar según el endpoint
        RateLimitConfig.LimitConfig limitConfig = rateLimitConfig.getLimitForEndpoint(endpoint, method);

        if (limitConfig == null) {
            // Si no hay límite configurado, permitir la petición
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener o crear el bucket para este cliente
        Bucket bucket = cache.get(clientKey, key -> createBucket(limitConfig));

        // Intentar consumir un token
        if (bucket.tryConsume(1)) {
            // Hay tokens disponibles, permitir la petición
            addRateLimitHeaders(response, bucket, limitConfig);
            filterChain.doFilter(request, response);
        } else {
            // No hay tokens disponibles, rechazar la petición
            handleRateLimitExceeded(response, limitConfig);
        }
    }

    /**
     * Obtiene la clave única del cliente (IP o username si está autenticado)
     */
    private String getClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si el usuario está autenticado, usar su username como clave
        if (authentication != null && authentication.isAuthenticated() 
            && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return "user:" + userDetails.getUsername();
        }

        // Si no está autenticado, usar la IP del cliente
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }

    /**
     * Obtiene la dirección IP real del cliente, considerando proxies y load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs separadas por coma
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Crea un nuevo bucket con la configuración de límites especificada
     */
    private Bucket createBucket(RateLimitConfig.LimitConfig limitConfig) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(limitConfig.getCapacity())
                .refillIntervally(limitConfig.getRefillTokens(), 
                                 Duration.ofSeconds(limitConfig.getRefillPeriodSeconds()))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Agrega headers informativos sobre el rate limit en la respuesta
     */
    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket, 
                                     RateLimitConfig.LimitConfig limitConfig) {
        long availableTokens = bucket.getAvailableTokens();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limitConfig.getCapacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(
                System.currentTimeMillis() / 1000 + limitConfig.getRefillPeriodSeconds()));
    }

    /**
     * Maneja la respuesta cuando se excede el límite de rate
     */
    private void handleRateLimitExceeded(HttpServletResponse response, 
                                         RateLimitConfig.LimitConfig limitConfig) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.setHeader("X-RateLimit-Limit", String.valueOf(limitConfig.getCapacity()));
        response.setHeader("Retry-After", String.valueOf(limitConfig.getRefillPeriodSeconds()));
        
        String errorMessage = String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Has excedido el límite de %d peticiones por %d segundos. Por favor, intenta de nuevo más tarde.\"}",
                limitConfig.getCapacity(), limitConfig.getRefillPeriodSeconds()
        );
        response.getWriter().write(errorMessage);
    }
}

