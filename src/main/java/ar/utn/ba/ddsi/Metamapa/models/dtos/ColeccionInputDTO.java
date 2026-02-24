package ar.utn.ba.ddsi.Metamapa.models.dtos;


import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import ar.utn.ba.ddsi.Metamapa.Datos.Criterio;
import ar.utn.ba.ddsi.Metamapa.Datos.Consenso.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ColeccionInputDTO {

  private Long id;
  private String nombreColeccion;
  private String handle;
  private String descripcion;

  private String algoritmo; // Ej: "MAYORIA", "ABSOLUTO"

  private Long criterioCategoriaId;

  private Double criterioLatitud;
  private Double criterioLongitud;

  private String criterioFechaInicio;
  private String criterioFechaFin;
  private List<String> fuentes;

  // ==========================================
  // 1. MÉTODO PARA CREAR (POST)
  // ==========================================
  public Coleccion aEntidad() {
    Coleccion col = new Coleccion();
    col.setId(this.id);
    this.actualizarEntidad(col);
    return col;
  }

  // ==========================================
  // 2. MÉTODO PARA ACTUALIZAR (PUT)
  // ==========================================
  public void actualizarEntidad(Coleccion col) {
    // Datos básicos
    col.setNombreColeccion(this.nombreColeccion);
    col.setHandle(this.handle);
    col.setDescripcion(this.descripcion);

    // --- MAPEO DE CRITERIOS (Embedded) ---
    Criterio c = col.getCriterio();
    if (c == null) c = new Criterio();

    // ✅ Asignamos los IDs en lugar de Strings
    c.setCategoriaId(this.criterioCategoriaId);

    c.setLatitud(this.criterioLatitud);
    c.setLongitud(this.criterioLongitud);
    if (this.criterioFechaInicio != null && !this.criterioFechaInicio.isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(this.criterioFechaInicio);
        c.setFechaInicio(LocalDate.from(fecha.atStartOfDay()));
      } catch (Exception e) { /* Ignorar o loggear */ }
    }

    if (this.criterioFechaFin != null && !this.criterioFechaFin.isEmpty()) {
      try {
        LocalDate fecha = LocalDate.parse(this.criterioFechaFin);
        c.setFechaFin(LocalDate.from(fecha.atTime(23, 59, 59)));
      } catch (Exception e) { /* Ignorar o loggear */ }
    }

    col.setCriterio(c);

    // --- MAPEO DE ALGORITMO (Strategy Pattern) ---
    // Transforma el String "MAYORIA" en new MayoriaSimple()
    col.setConsenso(convertirAlgoritmo(this.algoritmo));
  }

  // ==========================================
  // 3. FACTORY DE ALGORITMOS
  // ==========================================
  private AlgoritmoConsenso convertirAlgoritmo(String nombre) {
    if (nombre == null) return new AlgoritmoBasico();

    switch (nombre.toUpperCase()) {
      case "MAYORIA": return new MayoriaSimple();
      case "ABSOLUTO": return new ConsensoAbsoluto();
      case "MULTIPLES": return new MultiplesMenciones();
      case "BASICO":
      default: return new AlgoritmoBasico();
    }
  }
}