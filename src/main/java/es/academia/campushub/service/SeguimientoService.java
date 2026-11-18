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

/**
 * Casos de uso para guardar y recuperar seguimientos en MongoDB. Comprueba primero la actividad en
 * PostgreSQL; el vínculo entre ambos motores es lógico, no una clave foránea.
 */

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
      // Validar la actividad evita crear referencias huérfanas en el flujo normal de la API.
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

    /** Devuelve los seguimientos en orden de creación; una consulta sin resultados produce lista vacía. */
  public List<SeguimientoResponse> seguimientosDe(Long actividadId) {
    return seguimientos.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(SeguimientoMapper::toResponse)
        .toList();
  }
}
