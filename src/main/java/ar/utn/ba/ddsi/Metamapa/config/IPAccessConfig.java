package ar.utn.ba.ddsi.Metamapa.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuración de control de acceso por IP.
 * Maneja listas blancas (whitelist) y listas negras (blacklist) de direcciones IP.
 */
@Component
public class IPAccessConfig {

    // Lista blanca: IPs autorizadas (si está habilitada, solo estas IPs pueden acceder)
    @Value("${ip.access.whitelist.enabled:false}")
    private boolean whitelistEnabled;

    @Value("${ip.access.whitelist.ips:}")
    private String whitelistIps;

    // Lista negra: IPs bloqueadas (siempre bloqueadas, independientemente de la whitelist)
    @Value("${ip.access.blacklist.enabled:true}")
    private boolean blacklistEnabled;

    @Value("${ip.access.blacklist.ips:}")
    private String blacklistIps;

    // IPs locales que siempre están permitidas (localhost, 127.0.0.1, etc.)
    @Value("${ip.access.allow-localhost:true}")
    private boolean allowLocalhost;

    private Set<String> whitelistSet;
    private Set<String> blacklistSet;
    private Set<String> localhostIps;

    @PostConstruct
    public void initialize() {
        // Inicializar lista blanca
        whitelistSet = parseIpList(whitelistIps);
        
        // Inicializar lista negra
        blacklistSet = parseIpList(blacklistIps);
        
        // IPs locales permitidas por defecto
        localhostIps = Set.of(
            "127.0.0.1",
            "localhost",
            "0:0:0:0:0:0:0:1", // IPv6 localhost
            "::1" // IPv6 localhost comprimido
        );
    }

    /**
     * Parsea una lista de IPs separadas por comas en un Set
     */
    private Set<String> parseIpList(String ipList) {
        if (ipList == null || ipList.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        return Stream.of(ipList.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Verifica si una IP está en la lista negra
     */
    public boolean isBlacklisted(String ip) {
        if (!blacklistEnabled) {
            return false;
        }
        
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // Verificar coincidencia exacta
        if (blacklistSet.contains(ip)) {
            return true;
        }
        
        // Verificar rangos CIDR si están soportados (por ahora solo IPs exactas)
        return false;
    }

    /**
     * Verifica si una IP está en la lista blanca
     */
    public boolean isWhitelisted(String ip) {
        if (!whitelistEnabled) {
            return true; // Si la whitelist no está habilitada, todas las IPs están permitidas
        }
        
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // Si está en la whitelist, está permitida
        if (whitelistSet.contains(ip)) {
            return true;
        }
        
        // Si allowLocalhost está habilitado y es una IP local, está permitida
        if (allowLocalhost && isLocalhost(ip)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica si una IP es localhost
     */
    private boolean isLocalhost(String ip) {
        return localhostIps.contains(ip) || 
               ip.startsWith("127.") || 
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") ||
               ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") ||
               ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") ||
               ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") ||
               ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") ||
               ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.");
    }

    /**
     * Verifica si una IP tiene acceso permitido
     * Retorna true si:
     * - No está en la blacklist Y
     * - Está en la whitelist (si whitelist está habilitada) O whitelist no está habilitada
     */
    public boolean isAccessAllowed(String ip) {
        // Primero verificar blacklist (tiene prioridad)
        if (isBlacklisted(ip)) {
            return false;
        }
        
        // Luego verificar whitelist
        return isWhitelisted(ip);
    }

    /**
     * Agrega una IP a la lista negra (dinámicamente)
     */
    public void addToBlacklist(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            blacklistSet.add(ip.trim());
        }
    }

    /**
     * Agrega una IP a la lista blanca (dinámicamente)
     */
    public void addToWhitelist(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            whitelistSet.add(ip.trim());
        }
    }

    /**
     * Remueve una IP de la lista negra
     */
    public void removeFromBlacklist(String ip) {
        blacklistSet.remove(ip);
    }

    /**
     * Remueve una IP de la lista blanca
     */
    public void removeFromWhitelist(String ip) {
        whitelistSet.remove(ip);
    }

    /**
     * Obtiene todas las IPs en la lista negra
     */
    public Set<String> getBlacklist() {
        return new HashSet<>(blacklistSet);
    }

    /**
     * Obtiene todas las IPs en la lista blanca
     */
    public Set<String> getWhitelist() {
        return new HashSet<>(whitelistSet);
    }

    // Getters para verificar el estado de las listas
    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public boolean isBlacklistEnabled() {
        return blacklistEnabled;
    }
}

