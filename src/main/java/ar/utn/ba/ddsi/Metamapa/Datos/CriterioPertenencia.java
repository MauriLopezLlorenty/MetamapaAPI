package ar.utn.ba.ddsi.Metamapa.Datos;
import java.util.function.Predicate;

public interface CriterioPertenencia extends Predicate<Hecho> {
    boolean cumpleCondicion(Hecho h);
}
