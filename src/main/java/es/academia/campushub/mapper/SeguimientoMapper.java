package es.academia.campushub.mapper;

import es.academia.campushub.controller.dto.SeguimientoResponse;
import es.academia.campushub.document.SeguimientoDocument;

/** Mapper manual (Persona C). Ya está hecho. */
public final class SeguimientoMapper {
  private SeguimientoMapper() {}

  public static SeguimientoResponse toResponse(SeguimientoDocument d) {
    return new SeguimientoResponse(
        d.getId(),
        d.getActividadId(),
        d.getTipo(),
        d.getDatos(),
        d.getCreadoEn() == null ? null : d.getCreadoEn().toString());
  }
}
