package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.document.SeguimientoDocument;
import es.academia.campushub.mapper.SeguimientoMapper;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PERSONA C: seguimientos de actividades en MongoDB (RF05). */
@Service
public class SeguimientoService {
  private final SeguimientoRepository seguimientos;
  private final ActividadRepository actividades; // repositorio de B: solo existsById

  public SeguimientoService(SeguimientoRepository seguimientos, ActividadRepository actividades) {
    this.seguimientos = seguimientos;
    this.actividades = actividades;
  }

  @CacheEvict(cacheNames = "actividadDetalle", key = "'actividad:detalle:' + #actividadId")
  @Transactional
  public SeguimientoResponse registrarSeguimiento(Long actividadId, CreateSeguimientoRequest r) {
    if (!actividades.existsById(actividadId)) {
      throw new ResourceNotFoundException("Actividad " + actividadId + " no existe");
    }
    if (r == null || r.tipo() == null || r.tipo().isBlank()) {
      throw new InvalidInputException("tipo es obligatorio");
    }
    if (r.datos() == null) {
      throw new InvalidInputException("datos es obligatorio y debe ser un objeto");
    }

    SeguimientoDocument document = new SeguimientoDocument();
    document.setActividadId(actividadId);
    document.setTipo(r.tipo().trim());
    document.setDatos(r.datos());
    document.setCreadoEn(java.time.Instant.now());
    return SeguimientoMapper.toResponse(seguimientos.save(document));
  }

  /** Lista vacía (nunca null) si la actividad no tiene seguimientos. Ya está hecho. */
  public List<SeguimientoResponse> seguimientosDe(Long actividadId) {
    return seguimientos.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(SeguimientoMapper::toResponse)
        .toList();
  }
}
