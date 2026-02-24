package ar.utn.ba.ddsi.Metamapa.Datos;
import ar.utn.ba.ddsi.Metamapa.Fuente.TipoFuente;
import ar.utn.ba.ddsi.Metamapa.Usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*Cada hecho representa una pieza de información,
la cual debe contener mínimamente:
 título, descripción, categoría, contenido multimedia opcional,
  lugar y fecha del acontecimiento, fecha de carga y
  su origen (carga manual, proveniente de un dataset o provisto por un contribuyente).
 */
@Setter
@Getter

@NoArgsConstructor
@Entity
@Table(name = "hecho")
public class Hecho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "coleccion_id")
    @JsonBackReference
    private Coleccion coleccion;

    @Column(name = "archivo_multimedia")
    private String archivoMultimedia;

    @Column(length = 2000)
    private String descripcion;


    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "categoria_id", referencedColumnName = "id")
    private Categoria categoria;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "lugar_id", referencedColumnName = "id")
    private Lugar lugar;

    @Column(name = "fecha_del_hecho")
    private LocalDateTime fechaDelHecho;

    @Column(name = "fecha_de_carga", nullable = false)
    private LocalDateTime fechaDeCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false)
    private Origen origen;

    @Column(name = "visible", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean visible=true;

    @Column(name = "editable", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean editable = false;

    @ManyToOne
    @JoinColumn(name = "contribuyente_id", referencedColumnName = "id")
    private Usuario contribuyente;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }) // <-- AÑADÍ ESTO
    @JoinTable(
            name = "hecho_etiquetas",
            joinColumns = @JoinColumn(name = "hecho_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private List<Etiqueta> etiquetas;

    public Hecho(String titulo, String descripcion,String archivoMultimedia, String categoria,double latitud, double longitud, LocalDateTime fechaDelHecho, Origen origen, Usuario contribuyente) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.archivoMultimedia = archivoMultimedia;
        this.categoria = new Categoria(categoria);
        this.lugar = new Lugar(latitud,longitud);
        this.fechaDelHecho = fechaDelHecho;
        this.fechaDeCarga = LocalDateTime.now();
        this.origen = origen;
        this.visible = true;
        this.editable = false;
        this.contribuyente = contribuyente;
        this.etiquetas = new ArrayList<>();
    }
    public void agregarEtiqueta(Etiqueta etiqueta) {

        if(!etiquetas.contains(etiqueta)) {
            etiquetas.add(etiqueta);
        }
    }
    public TipoFuente getTipoDeFuente() {
        if (this.origen == Origen.DATASET) {
            return TipoFuente.ESTATICA;
        } else {
            // Asumimos que MANUAL, CONTRIBUYENTE, etc. son Dinámicas
            return TipoFuente.DINAMICA;
        }
    }
}
