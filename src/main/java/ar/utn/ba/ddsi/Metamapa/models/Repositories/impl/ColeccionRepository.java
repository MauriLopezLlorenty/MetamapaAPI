package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;

import ar.utn.ba.ddsi.Metamapa.Datos.Coleccion;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {

  // Busca por handle a la coleccion ;)
  //Optional<Coleccion> findByIdentificadorIgnoreCase(String identificador);
  Coleccion findByHandle(String handle);


}