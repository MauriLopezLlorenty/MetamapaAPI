package ar.utn.ba.ddsi.Metamapa.Datos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ar.utn.ba.ddsi.Metamapa.Fuente.FuenteEstatica;
import ar.utn.ba.ddsi.Metamapa.Fuente.TipoFuente;
import ar.utn.ba.ddsi.Metamapa.converters.AlgoritmoConsensoAttributeConverter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ar.utn.ba.ddsi.Metamapa.Datos.Consenso.AlgoritmoBasico;
import ar.utn.ba.ddsi.Metamapa.Datos.Consenso.AlgoritmoConsenso;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltrarPorVisible;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroCompuesto;
import ar.utn.ba.ddsi.Metamapa.Datos.Filtros.FiltroHecho;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "coleccion")
@Setter
@Getter
@NoArgsConstructor
public class Coleccion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String nombreColeccion;

  // Configuración: ¿Qué fuentes acepta esta colección? (Dinamica, Estatica o Ambas)
  @ElementCollection(targetClass = TipoFuente.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "coleccion_config_fuentes", joinColumns = @JoinColumn(name = "coleccion_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_fuente")
  private Set<TipoFuente> fuentesPermitidas = new HashSet<>();

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  // --- FALTABA ESTA DECLARACIÓN EN TU CÓDIGO ---
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "coleccion_id")
  private List<FuenteEstatica> fuentesEstaticas = new ArrayList<>();
  @OneToMany(mappedBy = "coleccion")
  @JsonManagedReference
  @org.hibernate.annotations.Where(clause = "visible = true")
  private List<Hecho> hechos;
  @Embedded
  private Criterio criterio;

  @Column(name = "algoritmo_consenso")
  @Convert(converter = AlgoritmoConsensoAttributeConverter.class)
  private AlgoritmoConsenso consenso;

  @Column(unique = true)
  private String handle;

  public Coleccion(String nombreColeccion, String descripcion) {
    this.nombreColeccion = nombreColeccion;
    this.descripcion = descripcion;
    this.hechos = new ArrayList<>();
    this.criterio = null;
    this.consenso = new AlgoritmoBasico();
    this.fuentesEstaticas = new ArrayList<>();

    // Por defecto aceptamos ambas, o puedes dejarlo vacío para obligar a configurar
    this.fuentesPermitidas.add(TipoFuente.DINAMICA);
    this.fuentesPermitidas.add(TipoFuente.ESTATICA);
  }

  // --- NUEVO: Método helper para validar fuente ---
  public boolean aceptaFuente(TipoFuente tipo) {
    return this.fuentesPermitidas.contains(tipo);
  }

  public void agregarFuenteEstatica(FuenteEstatica fuente) {
    // Solo agregamos la fuente si la colección permite datos ESTÁTICOS
    if (aceptaFuente(TipoFuente.ESTATICA)) {
      if (!this.fuentesEstaticas.contains(fuente)) {
        this.fuentesEstaticas.add(fuente);
      }
    } else {
      System.out.println("Advertencia: Esta colección no acepta fuentes estáticas.");
    }
  }

  public void cargarHechosDesdeFuente() {
    // Validación de seguridad: Si la colección se configuró solo como DINAMICA,
    // no debería cargar archivos CSV.
    if (!aceptaFuente(TipoFuente.ESTATICA)) {
      return;
    }

    if (this.fuentesEstaticas != null) {
      for (FuenteEstatica fe : this.fuentesEstaticas) {
        List<Hecho> hechosCargados = fe.obtenerHechos();
        // Usamos agregarHechoConCriterio para validar duplicados y criterios
        //hechosCargados.forEach(this::agregarHechoConCriterio);
      }
    }
  }

  public void agregarHechoConCriterio(Hecho h) {
    boolean cumple = (criterio == null) || criterio.cumpleCondicion(h);
    if (cumple && h.getVisible().equals(true)) {
      hechos.add(h);
    }
  }

  public void agregarHecho(Hecho h) {
    if(h.getVisible().equals(true)) {
      var idHechos = this.hechos.stream().map(hecho->hecho.getId()).toList();
      if(!idHechos.contains(h.getId())) {
        hechos.add(h);
      }
    }
  }

  // Eliminar hecho
  public void eliminarHecho(Hecho h) {
    hechos.remove(h);
  }

  // Verifica si un hecho pertenece a esta colección
  public boolean contiene(Hecho h) {
    return hechos.contains(h);
  }

  private void imprimirHechos(List<Hecho> lista) {
    for (Hecho h : lista) {
      System.out.println(h);
    }
  }

  public List<Hecho> filtrarHechos(FiltroHecho filtro) {
    return this.hechos.stream().filter(filtro::aplicaA).collect(Collectors.toList());
  }


  public List<Fuente> getFuentes() {
    return new ArrayList<>(this.fuentesEstaticas);
  }

  public void visualizarHechos() {
    imprimirHechos(filtrarHechos(new FiltrarPorVisible()));
  }

  public void visualizarFiltrarHechos(List<FiltroHecho> filtros) {
    List<FiltroHecho> filtrosAplicar = new ArrayList<>();
    filtrosAplicar.add(new FiltrarPorVisible());
    filtrosAplicar.addAll(filtros);
    FiltroHecho filtroCompuesto = new FiltroCompuesto(filtrosAplicar);
    imprimirHechos(filtrarHechos(filtroCompuesto));
  }

  public boolean validarConsenso(Hecho h, List<Fuente> fuentesExternas) {
    if (this.consenso == null) return true;
    // Solo validamos consenso si el hecho cumple con el tipo de fuente permitido
    if (!aceptaFuente(h.getTipoDeFuente())) return false;

    return this.consenso.esConsensuado(h, fuentesExternas);
  }

  public List<Hecho> getHechos() {
    return null;
  }
}