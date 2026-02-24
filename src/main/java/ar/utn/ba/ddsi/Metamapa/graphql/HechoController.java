package ar.utn.ba.ddsi.Metamapa.graphql;

import ar.utn.ba.ddsi.Metamapa.Datos.Hecho;
import ar.utn.ba.ddsi.Metamapa.graphql.dto.HechoFilterInput;
import ar.utn.ba.ddsi.Metamapa.graphql.specs.HechoSpecifications;
import ar.utn.ba.ddsi.Metamapa.models.Repositories.IHechosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HechoController {

    @Autowired
    private IHechosRepository hechoRepository;

    @QueryMapping
    public List<Hecho> hechos(@Argument HechoFilterInput filter, @Argument int page, @Argument int size) {
        Specification<Hecho> spec = HechoSpecifications.withFilter(filter);
        Pageable pageable = PageRequest.of(page, size);
        return hechoRepository.findAll(spec, pageable).getContent();
    }
}
