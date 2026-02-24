package ar.utn.ba.ddsi.Metamapa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de control de acceso por IP.
 * Bloquea o permite solicitudes HTTP basándose en listas blancas y negras de direcciones IP.
 * 
 * Orden de verificación:
 * 1. Si la IP está en la blacklist -> BLOQUEAR
 * 2. Si whitelist está habilitada y la IP NO está en la whitelist -> BLOQUEAR
 * 3. Si whitelist NO está habilitada -> PERMITIR (a menos que esté en blacklist)
 */
public class IPAccessFilter extends OncePerRequestFilter {

    private final IPAccessConfig ipAccessConfig;

    public IPAccessFilter(IPAccessConfig ipAccessConfig) {
        this.ipAccessConfig = ipAccessConfig;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);

        // Verificar si la IP tiene acceso permitido
        if (!ipAccessConfig.isAccessAllowed(clientIp)) {
            handleAccessDenied(response, clientIp);
            return;
        }

        // IP permitida, continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene la dirección IP real del cliente, considerando proxies y load balancers.
     * Prioridad:
     * 1. X-Forwarded-For (primera IP de la lista)
     * 2. X-Real-IP
     * 3. RemoteAddr
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // X-Forwarded-For puede contener múltiples IPs separadas por coma
        // La primera IP es generalmente la IP real del cliente
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String[] ips = xForwardedFor.split(",");
            String firstIp = ips[0].trim();
            // Limpiar puerto si está presente (ej: "192.168.1.1:8080" -> "192.168.1.1")
            if (firstIp.contains(":")) {
                firstIp = firstIp.split(":")[0].trim();
            }
            return firstIp;
        }

        // X-Real-IP es otra cabecera común usada por proxies
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            // Limpiar puerto si está presente
            if (xRealIp.contains(":")) {
                xRealIp = xRealIp.split(":")[0].trim();
            }
            return xRealIp;
        }

        // Si no hay cabeceras de proxy, usar la IP remota directa
        String remoteAddr = request.getRemoteAddr();
        // Limpiar puerto si está presente
        if (remoteAddr != null && remoteAddr.contains(":")) {
            remoteAddr = remoteAddr.split(":")[0].trim();
        }
        return remoteAddr;
    }

    /**
     * Maneja la respuesta cuando se deniega el acceso por IP
     */
    private void handleAccessDenied(HttpServletResponse response, String clientIp) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403 Forbidden
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Determinar el motivo del bloqueo
        String reason;
        if (ipAccessConfig.isBlacklisted(clientIp)) {
            reason = "Tu dirección IP está en la lista negra y no tiene permiso para acceder a este recurso.";
        } else if (ipAccessConfig.isWhitelistEnabled() && !ipAccessConfig.isWhitelisted(clientIp)) {
            reason = "Tu dirección IP no está autorizada para acceder a este recurso.";
        } else {
            reason = "Acceso denegado por política de seguridad.";
        }
        
        String errorMessage = String.format(
                "{\"error\":\"Access denied\",\"message\":\"%s\",\"ip\":\"%s\"}",
                reason, clientIp
        );
        
        response.getWriter().write(errorMessage);
    }
}

