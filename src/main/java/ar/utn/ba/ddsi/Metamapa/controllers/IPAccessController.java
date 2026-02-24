package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.config.IPAccessConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controlador para gestionar las listas de control de acceso por IP.
 * Solo accesible para administradores.
 */
@RestController
@RequestMapping("/api/admin/ip-access")
@PreAuthorize("hasRole('ADMIN')")
public class IPAccessController {

    private final IPAccessConfig ipAccessConfig;

    public IPAccessController(IPAccessConfig ipAccessConfig) {
        this.ipAccessConfig = ipAccessConfig;
    }

    /**
     * Obtiene el estado actual de las listas de IPs
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("whitelistEnabled", ipAccessConfig.isWhitelistEnabled());
        status.put("blacklistEnabled", ipAccessConfig.isBlacklistEnabled());
        status.put("whitelist", ipAccessConfig.getWhitelist());
        status.put("blacklist", ipAccessConfig.getBlacklist());
        return ResponseEntity.ok(status);
    }

    /**
     * Agrega una IP a la lista negra
     */
    @PostMapping("/blacklist/add")
    public ResponseEntity<Map<String, String>> addToBlacklist(@RequestBody Map<String, String> request) {
        String ip = request.get("ip");
        if (ip == null || ip.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "IP no proporcionada");
            return ResponseEntity.badRequest().body(error);
        }

        ipAccessConfig.addToBlacklist(ip.trim());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "IP agregada a la lista negra: " + ip.trim());
        response.put("ip", ip.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * Remueve una IP de la lista negra
     */
    @DeleteMapping("/blacklist/{ip}")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@PathVariable String ip) {
        ipAccessConfig.removeFromBlacklist(ip);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "IP removida de la lista negra: " + ip);
        response.put("ip", ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Agrega una IP a la lista blanca
     */
    @PostMapping("/whitelist/add")
    public ResponseEntity<Map<String, String>> addToWhitelist(@RequestBody Map<String, String> request) {
        String ip = request.get("ip");
        if (ip == null || ip.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "IP no proporcionada");
            return ResponseEntity.badRequest().body(error);
        }

        ipAccessConfig.addToWhitelist(ip.trim());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "IP agregada a la lista blanca: " + ip.trim());
        response.put("ip", ip.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * Remueve una IP de la lista blanca
     */
    @DeleteMapping("/whitelist/{ip}")
    public ResponseEntity<Map<String, String>> removeFromWhitelist(@PathVariable String ip) {
        ipAccessConfig.removeFromWhitelist(ip);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "IP removida de la lista blanca: " + ip);
        response.put("ip", ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las IPs en la lista negra
     */
    @GetMapping("/blacklist")
    public ResponseEntity<Set<String>> getBlacklist() {
        return ResponseEntity.ok(ipAccessConfig.getBlacklist());
    }

    /**
     * Obtiene todas las IPs en la lista blanca
     */
    @GetMapping("/whitelist")
    public ResponseEntity<Set<String>> getWhitelist() {
        return ResponseEntity.ok(ipAccessConfig.getWhitelist());
    }
}

