package ar.utn.ba.ddsi.Metamapa.models.Repositories;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IHechosRepository extends JpaRepository<Hecho, Long>, JpaSpecificationExecutor<Hecho> {
    boolean existsByFechaDeCargaAfter(LocalDate fecha);
    List<Hecho> findByFechaDeCargaAfter(LocalDate fecha);
}
