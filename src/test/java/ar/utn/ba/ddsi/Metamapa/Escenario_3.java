package ar.utn.ba.ddsi.Metamapa;


import ar.utn.ba.ddsi.Metamapa.Datos.*;
import ar.utn.ba.ddsi.Metamapa.Fuente.DatasetCSV;
import ar.utn.ba.ddsi.Metamapa.Fuente.Fuente;
import ar.utn.ba.ddsi.Metamapa.Fuente.FuenteEstatica;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Escenario_3 {
    public String crearDescripcion() {
        return "a".repeat(500);
    }

    public String crearDescripcion2() {
        return "b".repeat(200);
    }
    Fuente fuente = new FuenteEstatica(new DatasetCSV(),"manual");

    Hecho unhecho = new Hecho("Brote de enfermedad contagiosa causa estragos en San Lorenzo, Santa Fe",
            "Grave brote de enfermedad contagiosa ocurrió en las inmediaciones de San Lorenzo, Santa Fe. " +
                    "El incidente dejó varios heridos y daños materiales. " +
                    "Se ha declarado estado de emergencia en la región para facilitar la asistencia.",null , "Evento sanitario",
            -32.786098, -60.741543, LocalDateTime.of(2005, 7, 5,0,0), Origen.MANUAL, null);

    Coleccion coleccion = new Coleccion("Colección prueba",fuente, "Esto es una prueba");




    @Test
    @DisplayName("Se crea una solicitud y su estado inicial es pendiente")
    void crearSolicitudEliminacion() throws URISyntaxException {
        SolicitudDeEliminacion unaSolicitud = new SolicitudDeEliminacion(unhecho, crearDescripcion());
        System.out.println("Estado actual de la solicitud "+ unaSolicitud.getEstado());
        Assertions.assertTrue(unaSolicitud.getHecho().equals(unhecho) && unaSolicitud.getEstado()== EstadoSolicitud.PENDIENTE );
    }
    @Test
    @DisplayName("Las solicitudes siempre inician su estado como pendiente")
    void siempreEmpiezanPendientes()throws URISyntaxException{
        SolicitudDeEliminacion otraSolicitud = new SolicitudDeEliminacion(unhecho, crearDescripcion());
        System.out.println("Estado actual de la solicitud "+ otraSolicitud.getEstado());
        Assertions.assertNotSame(EstadoSolicitud.ACEPTADA, otraSolicitud.getEstado());
    }
    @Test
    @DisplayName("Se rechaza la solicitud y se puede agregar a una coleccion ")
    void rechazaSolicitud()throws URISyntaxException{
        SolicitudDeEliminacion solicitudPrueba = new SolicitudDeEliminacion(unhecho,crearDescripcion());
        solicitudPrueba.rechazar();
        System.out.println("Estado actual de la solicitud "+ solicitudPrueba.getEstado());

        System.out.println("Se tenían " + coleccion.getHechos().size() + " hechos.");
        coleccion.agregarHecho(unhecho);
        System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");
        Assertions.assertFalse(coleccion.getHechos().isEmpty());
    }

    @Test
    @DisplayName("Se acepta la solicitud y no se puede agregar a una coleccion")
    void aceptaSolicitud()throws URISyntaxException {
        SolicitudDeEliminacion solicitudPrueba = new SolicitudDeEliminacion(unhecho, crearDescripcion());
        solicitudPrueba.aceptar();
        System.out.println("Estado actual de la solicitud " + solicitudPrueba.getEstado());
        System.out.println("Se tenían " + coleccion.getHechos().size() + " hechos.");
        Coleccion coleccion = new Coleccion("Colección prueba", fuente, "Esto es una prueba");
        coleccion.agregarHecho(unhecho);
        System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");
        Assertions.assertTrue((coleccion.getHechos().isEmpty()));



    }
    @Test
    @DisplayName("Se rechaza la solicitud y se puede agregar a una coleccion con fechas  ")
    void rechazaSolicitudConFechas() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        SolicitudDeEliminacion solicitudPrueba = new SolicitudDeEliminacion(unhecho, crearDescripcion());
        Field field = SolicitudDeEliminacion.class.getDeclaredField("fechaSolicitud");
        field.setAccessible(true);
        LocalDateTime fechaEsperada = LocalDateTime.of(2025, 4, 20, 10, 0);
        field.set(solicitudPrueba, fechaEsperada);
        System.out.println("La fecha de creación de la solicitud es: " + solicitudPrueba.getFechaSolicitud());
        solicitudPrueba.rechazar();
        System.out.println("Estado actual de la solicitud: " + solicitudPrueba.getEstado() + ". La fecha de cambio de estado es: " + solicitudPrueba.getFechaEstado());
        System.out.println("Se tenían " + coleccion.getHechos().size() + " hechos.");
        coleccion.agregarHecho(unhecho);
        System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");
        Assertions.assertFalse(coleccion.getHechos().isEmpty());
    }
    @Test
    @DisplayName("Se acepta la solicitud y no se puede agregar a una coleccion con fechas ")
    void solicitudAceptadaConFechas() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        SolicitudDeEliminacion solicitudPrueba = new SolicitudDeEliminacion(unhecho, crearDescripcion());
        Field field = SolicitudDeEliminacion.class.getDeclaredField("fechaSolicitud");
        field.setAccessible(true);
        LocalDateTime fechaEsperada = LocalDateTime.of(2025, 4, 20, 10, 0);
        field.set(solicitudPrueba, fechaEsperada);
        System.out.println("La fecha de creación de la solicitud es: " + solicitudPrueba.getFechaSolicitud());
        solicitudPrueba.aceptar();
        System.out.println("Estado actual de la solicitud: " + solicitudPrueba.getEstado() + ". La fecha de cambio de estado es: " + solicitudPrueba.getFechaEstado());
        coleccion.agregarHecho(unhecho);
        System.out.println("Se cargaron " + coleccion.getHechos().size() + " hechos.");
        Assertions.assertTrue(coleccion.getHechos().isEmpty());
    }
}





