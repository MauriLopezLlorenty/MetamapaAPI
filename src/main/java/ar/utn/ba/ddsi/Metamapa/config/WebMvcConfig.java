package ar.utn.ba.ddsi.Metamapa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // La URL que usará el frontend para pedir los archivos (ej. /uploads/media/mi-foto.jpg)
    String resourceHandler = "/uploads/media/**";

    // La ubicación REAL en tu disco duro donde están los archivos
    String resourceLocations = "file:" + Paths.get("uploads/media").toAbsolutePath().toString() + "/";

    registry.addResourceHandler(resourceHandler)
        .addResourceLocations(resourceLocations);

    registry.addResourceHandler("/uploadsEstaticas/**")
            .addResourceLocations("file:" + Paths.get("uploadsEstaticas").toAbsolutePath().toString() + "/");
  }
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**") // Aplica la configuración a todas las rutas que empiecen con /api/
        .allowedOrigins("http://127.0.0.1:5500") // Permite peticiones DESDE tu frontend
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
        .allowedHeaders("*") // Permite cualquier header en la petición
        .allowCredentials(true); // Permite el envío de credenciales (como cookies)
  }
}