package ar.utn.ba.ddsi.Metamapa.Datos.Spam;
import java.util.*;
import java.util.stream.Collectors;

public class DetectorTFIDF implements DetectorDeSpam {
  //TF-IDF (Term Frequency – Inverse Document Frequency).
  private List<String> guiasLegitimidad;
  private double umbral; //Separa Spam de Valido

  public DetectorTFIDF(List<String> textosLegitimos, double umbral) {
    this.guiasLegitimidad = textosLegitimos;
    this.umbral = umbral; // Ej: 0.15
  }

  @Override
  public boolean esSpam(String texto) {
    String motivo = texto.toLowerCase();
    Map<String, Double> tfidfTexto = calcularTFIDF(texto);

    double promedio = tfidfTexto.values().stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

    return promedio < umbral; // Cuanto más bajo, más sospechoso
  }

  private Map<String, Double> calcularTFIDF(String texto) {
    List<String> palabras = Arrays.asList(texto.split("\\W+"));
    Map<String, Double> tfidf = new HashMap<>();

    for (String palabra : palabras) {
      double tf = calcularTF(palabra, palabras);
      double idf = calcularIDF(palabra);
      tfidf.put(palabra, tf * idf);
    }
    return tfidf;
  }

  private double calcularTF(String palabra, List<String> documento) {
    long count = documento.stream().filter(p -> p.equals(palabra)).count();
    return (double) count / documento.size();
  }

  private double calcularIDF(String palabra) {
    long documentosConPalabra = guiasLegitimidad.stream()
        .map(doc -> doc.toLowerCase().split("\\W+"))
        .filter(palabras -> Arrays.asList(palabras).contains(palabra))
        .count();

    if (documentosConPalabra == 0) {
      documentosConPalabra = 1; // Para evitar división por cero
    }

    return Math.log((double) guiasLegitimidad.size() / documentosConPalabra);
  }
}