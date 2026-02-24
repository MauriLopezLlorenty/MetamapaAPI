package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
  Optional<Etiqueta> findByNombre(String nombre);
}