package es.academia.campushub.util;

import es.academia.campushub.util.exception.InvalidInputException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Utilidad compartida: convierte textos ISO (YYYY-MM-DD) a fechas y lanza 400 si no son válidos.
 */
public final class Parseo {
  private Parseo() {}

  public static LocalDate fecha(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidInputException("Falta el campo obligatorio: " + campo);
    }
    try {
      return LocalDate.parse(valor.strip());
    } catch (DateTimeParseException e) {
      throw new InvalidInputException("Fecha inválida en " + campo + " (formato YYYY-MM-DD)");
    }
  }

  public static LocalDate fechaOpcional(String valor, String campo) {
    return (valor == null || valor.isBlank()) ? null : fecha(valor, campo);
  }

  public static String texto(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidInputException("Falta el campo obligatorio: " + campo);
    }
    return valor.strip();
  }
}
