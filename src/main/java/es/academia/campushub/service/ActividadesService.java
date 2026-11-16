package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.Actividad;
import es.academia.campushub.entity.Docente;
import es.academia.campushub.entity.Estudiante;
import es.academia.campushub.entity.Etiqueta;
import es.academia.campushub.entity.Inscripcion;
import es.academia.campushub.mapper.ActividadesMapper;
import es.academia.campushub.mapper.PersonasMapper;
import es.academia.campushub.repository.*;
import es.academia.campushub.util.exception.BusinessConflictException;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Locale;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PERSONA B: actividades, inscripciones, historial y etiquetas (RF03, RF04, RF08, RF10). */
@Service
public class ActividadesService {
  private final ActividadRepository actividades;
  private final InscripcionRepository inscripciones;
  private final EtiquetaRepository etiquetas;
  private final DocenteRepository docentes; // repositorio de A: solo lectura (findById)
  private final EstudianteRepository estudiantes; // repositorio de A: solo lectura (findById)

  public ActividadesService(
      ActividadRepository actividades,
      InscripcionRepository inscripciones,
      EtiquetaRepository etiquetas,
      DocenteRepository docentes,
      EstudianteRepository estudiantes) {
    this.actividades = actividades;
    this.inscripciones = inscripciones;
    this.etiquetas = etiquetas;
    this.docentes = docentes;
    this.estudiantes = estudiantes;
  }

  @Transactional
  public ActividadResponse crearActividad(CreateActividadRequest r) {
    if (r == null) {
      throw new InvalidInputException("Faltan los datos de la actividad");
    }
    String titulo = textoObligatorio(r.titulo(), "El título");
    if (titulo.length() > 150) {
      throw new InvalidInputException("El título no puede superar los 150 caracteres");
    }
    String descripcion = textoObligatorio(r.descripcion(), "La descripción");

    Actividad.TipoActividad tipoActividad;
    try {
      tipoActividad = Actividad.TipoActividad.valueOf(r.tipo().trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidInputException(
          "Tipo de actividad inválido. Debe ser TALLER, CHARLA, VISITA o JORNADA");
    }

    if (r.maxParticipantes() == null || r.maxParticipantes() <= 0) {
      throw new InvalidInputException("maxParticipantes debe ser > 0");
    }
    if (r.docenteId() == null) {
      throw new InvalidInputException("El docente responsable es obligatorio");
    }

    Docente docente =
        docentes.findById(r.docenteId()).orElseThrow(() -> noExiste("Docente", r.docenteId()));

    Actividad actividad = new Actividad();
    actividad.setTitulo(titulo);
    actividad.setDescripcion(descripcion);
    actividad.setTipo(tipoActividad);
    actividad.setFechaPrevista(
        es.academia.campushub.util.Parseo.fecha(r.fechaPrevista(), "fechaPrevista"));
    actividad.setMaxParticipantes(r.maxParticipantes());
    actividad.setDocente(docente);

    return ActividadesMapper.toResponse(actividades.save(actividad));
  }

  @Transactional(readOnly = true)
  public ActividadResponse obtenerActividad(Long id) {
    Actividad actividad = actividades.findById(id).orElseThrow(() -> noExiste("Actividad", id));
    return ActividadesMapper.toResponse(actividad);
  }

  /** Pieza más delicada del proyecto: aforo + duplicados + concurrencia. */
  @CacheEvict(cacheNames = "actividadDetalle", key = "'actividad:detalle:' + #actividadId")
  @Transactional
  public InscripcionResponse inscribir(Long actividadId, CreateInscripcionRequest r) {
    if (r == null || r.estudianteId() == null) {
      throw new InvalidInputException("El estudiante es obligatorio");
    }

    // 1. Bloqueo pesimista de la actividad (FOR UPDATE)
    Actividad actividad =
        actividades
            .findByIdForUpdate(actividadId)
            .orElseThrow(() -> noExiste("Actividad", actividadId));

    // 2. Comprobar que el estudiante existe
    Estudiante estudiante =
        estudiantes
            .findById(r.estudianteId())
            .orElseThrow(() -> noExiste("Estudiante", r.estudianteId()));

    // 3. Comprobar si ya está inscrito
    if (inscripciones.existsByActividadIdAndEstudianteId(actividadId, r.estudianteId())) {
      throw new BusinessConflictException("El estudiante ya está inscrito en esta actividad");
    }

    // 4. Comprobar aforo máximo con la fila bloqueada
    long ocupadas = inscripciones.countByActividadId(actividadId);
    if (ocupadas >= actividad.getMaxParticipantes()) {
      throw new BusinessConflictException("La actividad ha alcanzado el aforo máximo");
    }

    // 5. Crear inscripción y forzar flush para validar integridad inmediata
    Inscripcion inscripcion = new Inscripcion();
    inscripcion.setActividad(actividad);
    inscripcion.setEstudiante(estudiante);
    inscripcion.setFechaInscripcion(java.time.LocalDateTime.now());

    inscripcion = inscripciones.saveAndFlush(inscripcion);

    return ActividadesMapper.toResponse(inscripcion);
  }

  @Transactional(readOnly = true)
  public List<EstudianteResponse> participantes(Long actividadId) {
    if (!actividades.existsById(actividadId)) {
      throw noExiste("Actividad", actividadId);
    }
    return inscripciones.findByActividadId(actividadId).stream()
        .map(i -> PersonasMapper.toResponse(i.getEstudiante()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ActividadHistorialResponse> historial(Long estudianteId) {
    if (!estudiantes.existsById(estudianteId)) {
      throw noExiste("Estudiante", estudianteId);
    }
    return inscripciones.findByEstudianteId(estudianteId).stream()
        .map(ActividadesMapper::toHistorial)
        .toList();
  }

  @Transactional
  public EtiquetaResponse crearEtiqueta(CreateEtiquetaRequest r) {
    if (r == null) {
      throw new InvalidInputException("Faltan los datos de la etiqueta");
    }
    String nombreLimpio = textoObligatorio(r.nombre(), "El nombre de la etiqueta");
    if (nombreLimpio.length() > 50) {
      throw new InvalidInputException("El nombre de la etiqueta no puede superar 50 caracteres");
    }

    if (etiquetas.existsByNombre(nombreLimpio)) {
      throw new BusinessConflictException("La etiqueta ya existe");
    }

    Etiqueta etiqueta = new Etiqueta();
    etiqueta.setNombre(nombreLimpio);
    return ActividadesMapper.toResponse(etiquetas.save(etiqueta));
  }

  @Transactional
  @CacheEvict(cacheNames = "actividadDetalle", key = "'actividad:detalle:' + #actividadId")
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    Actividad actividad =
        actividades
            .findByIdForUpdate(actividadId)
            .orElseThrow(() -> noExiste("Actividad", actividadId));
    Etiqueta etiqueta =
        etiquetas.findById(etiquetaId).orElseThrow(() -> noExiste("Etiqueta", etiquetaId));

    // The Set makes this idempotent in the transaction; the database unique constraint
    // also protects against concurrent association requests.
    actividad.getEtiquetas().add(etiqueta);
  }

  @Transactional(readOnly = true)
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    if (!etiquetas.existsById(etiquetaId)) {
      throw noExiste("Etiqueta", etiquetaId);
    }
    return actividades.findByEtiquetasId(etiquetaId).stream()
        .map(ActividadesMapper::toResponse)
        .toList();
  }

  // ===================== CONTRATO CON LA PERSONA C (no cambiar firmas) =====================

  @Transactional(readOnly = true)
  public int plazasOcupadas(Long actividadId) {
    return (int) inscripciones.countByActividadId(actividadId);
  }

  @Transactional(readOnly = true)
  public List<EtiquetaResponse> etiquetasDe(Long actividadId) {
    Actividad actividad =
        actividades.findById(actividadId).orElseThrow(() -> noExiste("Actividad", actividadId));

    return actividad.getEtiquetas().stream().map(ActividadesMapper::toResponse).toList();
  }

  @SuppressWarnings("unused")
  private ResourceNotFoundException noExiste(String que, Long id) {
    return new ResourceNotFoundException(que + " " + id + " no existe");
  }

  private String textoObligatorio(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidInputException(campo + " es obligatorio");
    }
    return valor.trim();
  }
}

// Decisiones Técnicas - Persona B (Actividades, Inscripciones, Etiquetas)
// Se han implementado restricciones nativas en PostgreSQL para garantizar la integridad por debajo
// de la capa Java. En la tabla actividad, se ha añadido un @Check para evitar aforos nulos o
// negativos, y se ha mapeado el TipoActividad como EnumType.STRING para evitar inconsistencias de
// texto. La descripción usa columnDefinition = "text" superando el límite de VARCHAR.
// Para etiqueta, se usa UNIQUE(nombre) evitando duplicados físicos en la relación N:M. En
// inscripcion, la red de seguridad principal es un UNIQUE(actividad_id, estudiante_id) respaldado
// por un índice sobre estudiante_id para optimizar el historial.
// El problema de concurrencia en la inscripción (condición de carrera al comprobar el aforo) se ha
// resuelto aplicando un bloqueo pesimista en base de datos mediante findByIdForUpdate (ejecuta
// SELECT ... FOR UPDATE). Esto serializa las transacciones simultáneas para una misma actividad,
// impidiendo sobrepasar el límite de plazas sin introducir bloqueos globales en la aplicación, y
// rematando con un saveAndFlush para validar la inserción inmediatamente.
