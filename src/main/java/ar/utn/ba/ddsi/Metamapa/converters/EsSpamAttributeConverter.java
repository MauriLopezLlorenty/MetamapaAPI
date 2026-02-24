package ar.utn.ba.ddsi.Metamapa.converters;

import jakarta.persistence.AttributeConverter;

public class EsSpamAttributeConverter implements AttributeConverter<Boolean,String> {
  @Override
  public String convertToDatabaseColumn(Boolean aBoolean) {
    if (aBoolean == null) return null;
    return aBoolean ? "SI" : "NO";
  }

  @Override
  public Boolean convertToEntityAttribute(String s) {
    if (s == null) return null;
    switch (s) {
      case "SI": return true;
      case "NO": return false;
      default: return null;
    }
  }
}
