package es.academia.campushub.mapper;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.*;

/** Mapper manual (Persona A). Ya está hecho: lo usan también B y C. */
public final class PersonasMapper {
  private PersonasMapper() {}

  public static DepartamentoResponse toResponse(Departamento d) {
    return new DepartamentoResponse(d.getId(), d.getNombre());
  }

  public static DocenteResponse toResponse(Docente d) {
    return new DocenteResponse(
        d.getId(), d.getNombre(), d.getApellidos(), d.getEmail(), d.getDepartamento().getId());
  }

  public static GrupoResponse toResponse(Grupo g) {
    return new GrupoResponse(g.getId(), g.getNombre(), g.getNivel(), g.getTutor().getId());
  }

  public static EstudianteResponse toResponse(Estudiante e) {
    return new EstudianteResponse(
        e.getId(),
        e.getNombre(),
        e.getApellidos(),
        e.getEmail(),
        e.getFechaNacimiento().toString(),
        e.getGrupo().getId());
  }

  public static ContactoResponse toResponse(ContactoEstudiante c) {
    return new ContactoResponse(
        c.getEstudiante().getId(),
        c.getTutorLegal(),
        c.getTelefono(),
        c.getEmail(),
        c.getAutorizado(),
        c.getFechaAutorizacion() == null ? null : c.getFechaAutorizacion().toString());
  }
}
