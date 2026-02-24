package ar.utn.ba.ddsi.Metamapa.Usuario;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuario")
// ✅ 1. Implementamos UserDetails para que Spring Security entienda esta clase
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String apellido;

    @Column(unique = true, nullable = false)
    private String nombreUsuario;

    @Column(unique = true, nullable = false)
    private String mail;

    @Column(nullable = false)
    private String contrasena;

    @Column
    private String ubicacion;


    @Enumerated(EnumType.STRING)
    private Rol rol; // ADMIN o CONTRIBUYENTE

    // =================================================================
    // MÉTODOS OBLIGATORIOS DE USER DETAILS (Spring Security)
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ Convertimos tu Enum 'Rol' en una autoridad que Spring entienda.
        // Spring espera que los roles empiecen con "ROLE_" (ej: ROLE_ADMIN).
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.name()));
    }

    @Override
    public String getPassword() {
        // Mapeamos tu campo 'contrasena' al método estándar 'getPassword'
        return this.contrasena;
    }

    @Override
    public String getUsername() {
        // Mapeamos tu campo 'nombreUsuario' al método estándar 'getUsername'
        return this.nombreUsuario;
    }

    // ✅ Configuraciones de cuenta (por ahora devolvemos 'true' para que estén siempre activas)

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}