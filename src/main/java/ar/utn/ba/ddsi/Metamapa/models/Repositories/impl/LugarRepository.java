package ar.utn.ba.ddsi.Metamapa.models.Repositories.impl;
import ar.utn.ba.ddsi.Metamapa.Datos.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    Optional<Lugar> findByNombreNormalizadoIgnoreCase(String nombreNormalizado);
    Optional<Lugar> findByLatitudAndLongitud(double latitud, double longitud);
}