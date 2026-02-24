package ar.utn.ba.ddsi.Metamapa.Datos.Spam;

import ar.utn.ba.ddsi.Metamapa.Datos.Spam.DetectorDeSpam;


import java.util.List;

public class DetectorBasico implements DetectorDeSpam {
  private List<String> palabrasSpam = List.of("Odio","Trucho","promocion","vendo","Gamba");

  @Override
  public boolean esSpam(String texto) {
    String motivo = texto.toLowerCase();
    return palabrasSpam.stream().anyMatch(motivo::contains);
  }
}