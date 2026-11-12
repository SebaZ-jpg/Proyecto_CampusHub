package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.mapper.ActividadesMapper;
import es.academia.campushub.repository.*;
import es.academia.campushub.util.exception.PersistenciaPendienteException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.util.List;
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
    // TODO(B) 1. Validar título, descripción, tipo (TALLER/CHARLA/VISITA/JORNADA) -> 400
    // TODO(B) 2. Fecha con Parseo.fecha(r.fechaPrevista(), "fechaPrevista")
    // TODO(B) 3. maxParticipantes > 0 -> 400 (y CHECK en la BD)
    // TODO(B) 4. docentes.findById(r.docenteId()) -> 404 si no existe
    // TODO(B) 5. Guardar y devolver ActividadesMapper.toResponse(...)
    throw new PersistenciaPendienteException("crearActividad");
  }

  @Transactional(readOnly = true)
  public ActividadResponse obtenerActividad(Long id) {
    // TODO(B) actividades.findById(id) -> 404 si no existe (lo usa también C en el detalle)
    throw new PersistenciaPendienteException("obtenerActividad");
  }

  /** Pieza más delicada del proyecto: aforo + duplicados + concurrencia. */
  @Transactional
  public InscripcionResponse inscribir(Long actividadId, CreateInscripcionRequest r) {
    // TODO(B) 1. actividades.findByIdForUpdate(actividadId) -> 404 (bloquea la fila de la actividad)
    // TODO(B) 2. estudiantes.findById(r.estudianteId()) -> 404 si no existe
    // TODO(B) 3. inscripciones.existsByActividadIdAndEstudianteId(...) -> 409 (duplicada)
    // TODO(B) 4. inscripciones.countByActividadId(...) >= maxParticipantes -> 409 (sin plazas)
    // TODO(B) 5. Crear Inscripcion con LocalDateTime.now(), inscripciones.saveAndFlush(...)
    // TODO(B) 6. Devolver ActividadesMapper.toResponse(...)
    // Nota: el UNIQUE(actividad, estudiante) de la BD es la red de seguridad si el paso 3 falla.
    throw new PersistenciaPendienteException("inscribir");
  }

  @Transactional(readOnly = true)
  public List<EstudianteResponse> participantes(Long actividadId) {
    // TODO(B) 404 si la actividad no existe; inscripciones.findByActividadId(...) y mapear cada
    //         i.getEstudiante() con PersonasMapper.toResponse (mapper de A)
    throw new PersistenciaPendienteException("participantes");
  }

  @Transactional(readOnly = true)
  public List<ActividadHistorialResponse> historial(Long estudianteId) {
    // TODO(B) 404 si el estudiante no existe; inscripciones.findByEstudianteId(...) y
    //         ActividadesMapper.toHistorial(...). Si no tiene actividades: lista vacía.
    throw new PersistenciaPendienteException("historial");
  }

  @Transactional
  public EtiquetaResponse crearEtiqueta(CreateEtiquetaRequest r) {
    // TODO(B) validar nombre -> 400; etiquetas.existsByNombre(...) -> 409; guardar; mapper
    throw new PersistenciaPendienteException("crearEtiqueta");
  }

  @Transactional
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    // TODO(B) 404 si no existe la actividad o la etiqueta. actividad.getEtiquetas().add(etiqueta).
    //         Al ser un Set (y PK compuesta en la tabla intermedia), repetirlo NO duplica y el
    //         endpoint sigue devolviendo 204.
    throw new PersistenciaPendienteException("asociarEtiqueta");
  }

  @Transactional(readOnly = true)
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    // TODO(B) 404 si la etiqueta no existe; actividades.findByEtiquetasId(etiquetaId); mapper
    throw new PersistenciaPendienteException("actividadesPorEtiqueta");
  }

  // ===================== CONTRATO CON LA PERSONA C (no cambiar firmas) =====================

  /** Nº de plazas ocupadas de una actividad. */
  @Transactional(readOnly = true)
  public int plazasOcupadas(Long actividadId) {
    return (int) inscripciones.countByActividadId(actividadId);
  }

  /** Etiquetas de una actividad (lista vacía si no tiene). 404 si la actividad no existe. */
  @Transactional(readOnly = true)
  public List<EtiquetaResponse> etiquetasDe(Long actividadId) {
    // TODO(B) actividades.findById(...) o 404; mapear actividad.getEtiquetas() con
    //         ActividadesMapper.toResponse(...)
    throw new PersistenciaPendienteException("etiquetasDe");
  }

  // Referencia útil para el 404 uniforme:
  @SuppressWarnings("unused")
  private ResourceNotFoundException noExiste(String que, Long id) {
    return new ResourceNotFoundException(que + " " + id + " no existe");
  }
}
