package ar.utn.ba.ddsi.Metamapa.Datos;

import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_de_modificacion")
@Getter
@Setter
@NoArgsConstructor

public class SolicitudDeModificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(optional = false)
    @JoinColumn(name = "hecho_id", referencedColumnName = "id")
    private Hecho hecho;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Getter
    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Getter
    @Column(name = "fecha_estado")
    private LocalDateTime fechaEstado;

    //Propuesta de Cambios
    @Column(name = "propuesta_titulo")
    private String propuestaTitulo;

    @Column(name = "propuesta_descripcion", length = 2000)
    private String propuestaDescripcion;

    @Column(name = "propuesta_archivo_multimedia")
    private String propuestaArchivoMultimedia;

    @Column(name = "propuesta_fecha_del_hecho")
    private LocalDateTime propuestaFechaDelHecho;

    @ManyToOne
    @JoinColumn(name = "propuesta_categoria_id")
    private Categoria propuestaCategoria;

    @ManyToOne
    @JoinColumn(name = "propuesta_lugar_id")
    private Lugar propuestaLugar;

    public SolicitudDeModificacion(Hecho hecho,Usuario usuario) {
        this.hecho = hecho;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
        this.usuario = usuario;
        this.fechaEstado = LocalDateTime.now();
    }
    public void rechazar () {
        this.hecho.setEditable(false);
        this.estado = EstadoSolicitud.RECHAZADA;
        this.fechaEstado = LocalDateTime.now();
    }
    public void aceptar () {
        this.estado = EstadoSolicitud.ACEPTADA;
        this.fechaEstado = LocalDateTime.now();
    }

}
