package es.academia.campushub.mapper;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.*;

/** Mapper manual (Persona B). Ya está hecho. Requiere estar dentro de una transacción (LAZY). */
public final class ActividadesMapper {
  private ActividadesMapper() {}

  public static ActividadResponse toResponse(Actividad a) {
    return new ActividadResponse(
            a.getId(),
            a.getTitulo(),
            a.getDescripcion(),
            a.getTipo().name(), // <-- FIX: Extraemos el String del Enum
            a.getFechaPrevista().toString(),
            a.getMaxParticipantes(),
            a.getDocente().getId());
  }

  public static InscripcionResponse toResponse(Inscripcion i) {
    return new InscripcionResponse(
            i.getId(),
            i.getActividad().getId(),
            i.getEstudiante().getId(),
            i.getFechaInscripcion().toString());
  }

  public static EtiquetaResponse toResponse(Etiqueta e) {
    return new EtiquetaResponse(e.getId(), e.getNombre());
  }

  public static ActividadHistorialResponse toHistorial(Inscripcion i) {
    Actividad a = i.getActividad();
    return new ActividadHistorialResponse(
            a.getId(),
            a.getTitulo(),
            a.getTipo().name(), // <-- FIX: Extraemos el String del Enum
            a.getFechaPrevista().toString(),
            i.getFechaInscripcion().toString(),
            PersonasMapper.toResponse(a.getDocente()));
  }
}