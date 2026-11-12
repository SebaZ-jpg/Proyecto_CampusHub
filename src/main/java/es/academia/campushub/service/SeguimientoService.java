package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.mapper.SeguimientoMapper;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.PersistenciaPendienteException;
import java.util.List;
import org.springframework.stereotype.Service;

/** PERSONA C: seguimientos de actividades en MongoDB (RF05). */
@Service
public class SeguimientoService {
  private final SeguimientoRepository seguimientos;
  private final ActividadRepository actividades; // repositorio de B: solo existsById

  public SeguimientoService(
      SeguimientoRepository seguimientos, ActividadRepository actividades) {
    this.seguimientos = seguimientos;
    this.actividades = actividades;
  }

  public SeguimientoResponse registrarSeguimiento(Long actividadId, CreateSeguimientoRequest r) {
    // TODO(C) 1. actividades.existsById(actividadId) -> 404 si no existe (comprobación en PostgreSQL
    //            ANTES de escribir en Mongo: así se reduce la inconsistencia)
    // TODO(C) 2. Validar tipo no vacío -> 400. ¿Qué hacéis si datos es null? (400 o mapa vacío)
    // TODO(C) 3. Crear SeguimientoDocument con creadoEn = Instant.now() y datos TAL CUAL
    // TODO(C) 4. seguimientos.save(...) y devolver SeguimientoMapper.toResponse(...)
    // Cuidado: las claves de un Map en Mongo no deben contener '.' ni empezar por '$'.
    throw new PersistenciaPendienteException("registrarSeguimiento");
  }

  /** Lista vacía (nunca null) si la actividad no tiene seguimientos. Ya está hecho. */
  public List<SeguimientoResponse> seguimientosDe(Long actividadId) {
    return seguimientos.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(SeguimientoMapper::toResponse)
        .toList();
  }
}
