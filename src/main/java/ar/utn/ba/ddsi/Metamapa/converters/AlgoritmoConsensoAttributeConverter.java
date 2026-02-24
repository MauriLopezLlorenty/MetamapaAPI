package ar.utn.ba.ddsi.Metamapa.converters;

import ar.utn.ba.ddsi.Metamapa.Datos.Consenso.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AlgoritmoConsensoAttributeConverter implements AttributeConverter<AlgoritmoConsenso, String> {

  @Override
  public String convertToDatabaseColumn(AlgoritmoConsenso attribute) {
    if (attribute == null) {
      return "BASICO"; // Valor por defecto
    }
    return attribute.getNombre();
  }

  @Override
  public AlgoritmoConsenso convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return new AlgoritmoBasico();
    }

    switch (dbData) {
      case "ABSOLUTO":
        return new ConsensoAbsoluto();
      case "MAYORIA":
        return new MayoriaSimple();
      case "MULTIPLES":
        return new MultiplesMenciones();
      case "BASICO":
      default:
        return new AlgoritmoBasico();
    }
  }
}