package ar.utn.ba.ddsi.Metamapa.Datos.Estadisticas;


import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Estadisticas {
    protected List<Registro> registros;


    public Estadisticas() {
        this.registros=new ArrayList<>();
    }

    public abstract void generarEstadisticas();

    public List<Registro> normalizarRegistros(List<Object[]> registros){
        List<Registro> registrosNormalizados = new ArrayList<>();

        for (Object[] fila : registros) {
            String atributoX = (fila[0] == null || fila[0].toString().isBlank())
                    ? "Sin datos"
                    : fila[0].toString();

            String atributoY = (fila[1] == null)
                    ? "0"
                    : fila[1].toString();

            registrosNormalizados.add(new Registro(atributoX, atributoY));
        }

        return registrosNormalizados;
    }

}
