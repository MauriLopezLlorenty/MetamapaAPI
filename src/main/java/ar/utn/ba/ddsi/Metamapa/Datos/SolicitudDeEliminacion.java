package ar.utn.ba.ddsi.Metamapa.Datos;

import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import ar.utn.ba.ddsi.Metamapa.converters.EsSpamAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_de_eliminacion")
@Getter
@Setter
@NoArgsConstructor

public class SolicitudDeEliminacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hecho_id", referencedColumnName = "id")
    private Hecho hecho;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(length = 2000, nullable = false)
    private String motivo;

    @Column(length = 2000, nullable = true)
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = true)
    private LocalDateTime fechaEstado;

    @Convert(converter = EsSpamAttributeConverter.class)
    @Column(name="es_spam")
    private Boolean esSpam;

    public SolicitudDeEliminacion(Hecho hecho, String motivo,Usuario usuario) {
        this.hecho = hecho;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
        this.validarMotivo(motivo);
        this.motivo = motivo;
        this.esSpam=false;
        this.usuario = usuario;
        this.fechaEstado = LocalDateTime.now();
    }

    private void validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().length() < 500 ) {
            throw new IllegalArgumentException("El motivo no puede ser nulo y debe tener al menos 500 caracteres.");
        }
    }

    public void aceptar() {
        this.estado = EstadoSolicitud.ACEPTADA;
        this.fechaEstado = LocalDateTime.now();
        hecho.setVisible(false);
    }

    public void rechazar() {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.fechaEstado = LocalDateTime.now();
        hecho.setVisible(true); // es necesario este metodo?
    }
}
