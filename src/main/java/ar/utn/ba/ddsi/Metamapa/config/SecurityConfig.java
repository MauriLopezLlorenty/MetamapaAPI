package ar.utn.ba.ddsi.Metamapa.config;

import ar.utn.ba.ddsi.Metamapa.models.Repositories.impl.UsuarioRepository;
import ar.utn.ba.ddsi.Metamapa.services.impl.JwtService; // Import JwtService
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UsuarioRepository usuarioRepository;

  // The constructor no longer needs JwtAuthenticationFilter injected
  public SecurityConfig(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // ✅ FIX: Create the JwtAuthenticationFilter bean here
  // Spring will automatically provide JwtService and UserDetailsService to this method.
  @Bean
  public JwtAuthenticationFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    return new JwtAuthenticationFilter(jwtService, userDetailsService);
  }

  // Bean para el filtro de control de acceso por IP
  @Bean
  public IPAccessFilter ipAccessFilter(IPAccessConfig ipAccessConfig) {
    return new IPAccessFilter(ipAccessConfig);
  }

  // Bean para el filtro de Rate Limiting
  @Bean
  public RateLimitingFilter rateLimitingFilter(RateLimitConfig rateLimitConfig) {
    return new RateLimitingFilter(rateLimitConfig);
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> usuarioRepository.findByNombreUsuario(username).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  JwtAuthenticationFilter jwtAuthFilter,
                                                  RateLimitingFilter rateLimitingFilter,
                                                  IPAccessFilter ipAccessFilter) throws Exception {
    http
        .cors(withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(req -> req
            // 1. Accesos Públicos (Visualizador / Anónimo)
                .requestMatchers("/api/auth/**", "/api/public/**",
                        "/uploads/media/**","/api/usuarios/**",
                        "/estadisticas/**","/api/estaticas/**",
                        "/uploadsEstaticas/**",
                        "/api/public/solicitud-**",
                        "/api/public/solicitudes-**",
                        "/graphiql", "/graphiql/**",
                        "/graphql", "/graphql/**" // <-- Agregado para pruebas sin autenticación
                ).permitAll()

                //.requestMatchers("/graphql").authenticated() // <-- Comentado temporalmente

                // 2. Accesos solo para Administradores
            // (Crear colecciones, aprobar hechos, importar CSV, panel de control)
            .requestMatchers("/api/admin/**","/api/colecciones/**").hasRole("ADMIN")

            // 3. Accesos para Contribuyentes y Admins
            // (Crear hecho, editar propio hecho)
            .requestMatchers("/api/hechos/**").hasAnyRole("CONTRIBUYENTE", "ADMIN")

            // Todo lo demás requiere autenticación mínima
            .anyRequest().authenticated()
        )
        // Se añaden los filtros en orden inverso de ejecución, siempre antes de un filtro conocido
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class)
        .addFilterBefore(ipAccessFilter, RateLimitingFilter.class);


    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // A. Permitir el origen de tu Frontend (Live Server usa 127.0.0.1:5500)
    configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500"));

    // B. Permitir los métodos HTTP (¡Aquí faltaba PATCH!)
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    // C. Permitir cabeceras (para que pase el Token)
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
