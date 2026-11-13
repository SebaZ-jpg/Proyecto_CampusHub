package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.Departamento;
import es.academia.campushub.entity.Docente;
import es.academia.campushub.entity.Estudiante;
import es.academia.campushub.entity.Grupo;
import es.academia.campushub.mapper.PersonasMapper;
import es.academia.campushub.repository.*;
import es.academia.campushub.util.Parseo;
import es.academia.campushub.util.exception.BusinessConflictException;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.PersistenciaPendienteException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** PERSONA A: departamentos, docentes, grupos, estudiantes y ficha de contacto (RF01, RF02, RF09). */
@Service
public class PersonasService {
  private final DepartamentoRepository departamentos;
  private final DocenteRepository docentes;
  private final GrupoRepository grupos;
  private final EstudianteRepository estudiantes;
  private final ContactoEstudianteRepository contactos;

  public PersonasService(
      DepartamentoRepository departamentos,
      DocenteRepository docentes,
      GrupoRepository grupos,
      EstudianteRepository estudiantes,
      ContactoEstudianteRepository contactos) {
    this.departamentos = departamentos;
    this.docentes = docentes;
    this.grupos = grupos;
    this.estudiantes = estudiantes;
    this.contactos = contactos;
  }

  /** EJEMPLO COMPLETO: copiad este patrón (validar -> comprobar 404/409 -> guardar -> mapper). */
  @Transactional
  public DepartamentoResponse crearDepartamento(CreateDepartamentoRequest r) {
    String nombre = Parseo.texto(r == null ? null : r.nombre(), "nombre"); // 400 si falta
    if (departamentos.existsByNombre(nombre)) { // mensaje claro (409)...
      throw new BusinessConflictException("Ya existe un departamento con ese nombre");
    }
    Departamento d = new Departamento();
    d.setNombre(nombre);
    // ...pero la protección REAL es el UNIQUE de la columna (carreras entre dos peticiones).
    return PersonasMapper.toResponse(departamentos.save(d));
  }

  /** Lo usan B y C (responsable de una actividad). Ya está hecho. */
  @Transactional(readOnly = true)
  public DocenteResponse obtenerDocente(Long id) {
    return docentes
        .findById(id)
        .map(PersonasMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Docente " + id + " no existe"));
  }

  @Transactional
  public DocenteResponse crearDocente(CreateDocenteRequest r) {
    // TODO(A) 1. Validar nombre, apellidos, email (Parseo.texto) -> 400
    // TODO(A) 2. departamentos.findById(r.departamentoId()) -> 404 si no existe
    // TODO(A) 3. docentes.existsByEmail(email) -> 409
    // TODO(A) 4. Crear Docente, guardar y devolver PersonasMapper.toResponse(...)
    if (r == null || r.departamentoId() == null) {
      throw new InvalidInputException("departamentoId es obligatorio");
    }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String apellidos = Parseo.texto(r.apellidos(), "apellidos");
    String email = Parseo.texto(r.email(), "email");
    Departamento departamento =
            departamentos
                    .findById(r.departamentoId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Departamento " + r.departamentoId() + " no existe"));
    if (docentes.existsByEmail(email)) {
      throw new BusinessConflictException("Ya existe un docente con ese correo");
    }
    Docente d = new Docente();
    d.setNombre(nombre);
    d.setApellidos(apellidos);
    d.setEmail(email);
    d.setDepartamento(departamento);
    return PersonasMapper.toResponse(docentes.save(d));
  }

  @Transactional
  public GrupoResponse crearGrupo(CreateGrupoRequest r) {
    // TODO(A) 1. Validar nombre y nivel -> 400
    // TODO(A) 2. docentes.findById(r.tutorDocenteId()) -> 404 si no existe
    // TODO(A) 3. Guardar y devolver el mapper
    if (r == null || r.tutorDocenteId() == null) {
      throw new PersistenciaPendienteException("crearGrupo");
  }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String nivel = Parseo.texto(r.nivel(), "nivel");
    Docente tutor =
            docentes
                    .findById(r.tutorDocenteId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Docente " + r.tutorDocenteId() + " no existe"));
    Grupo g = new Grupo();
    g.setNombre(nombre);
    g.setNivel(nivel);
    g.setTutor(tutor);
    return PersonasMapper.toResponse(grupos.save(g));
  }


  @Transactional
  public EstudianteResponse crearEstudiante(CreateEstudianteRequest r) {
    // TODO(A) 1. Validar campos; fecha con Parseo.fecha(r.fechaNacimiento(), "fechaNacimiento")
    // TODO(A) 2. Fecha de nacimiento anterior a hoy -> 400 (y CHECK en la BD)
    // TODO(A) 3. grupos.findById(r.grupoId()) -> 404 si no existe
    // TODO(A) 4. estudiantes.existsByEmail(email) -> 409
    // TODO(A) 5. Guardar y devolver el mapper
    if (r == null || r.grupoId() == null) {
      throw new InvalidInputException("grupoId es obligatorio");
    }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String apellidos = Parseo.texto(r.apellidos(), "apellidos");
    String email = Parseo.texto(r.email(), "email");
    LocalDate nacimiento = Parseo.fecha(r.fechaNacimiento(), "fechaNacimiento");
    if (!nacimiento.isBefore(LocalDate.now())) {
    throw new PersistenciaPendienteException("crearEstudiante");
    }
    Grupo grupo =
            grupos
                    .findById(r.grupoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo " + r.grupoId() + " no existe"));
    if (estudiantes.existsByEmail(email)) {
      throw new BusinessConflictException("Ya existe un estudiante con ese correo");
    }
    Estudiante e = new Estudiante();
    e.setNombre(nombre);
    e.setApellidos(apellidos);
    e.setEmail(email);
    e.setFechaNacimiento(nacimiento);
    e.setGrupo(grupo);
    return PersonasMapper.toResponse(estudiantes.save(e));
  }



  @Transactional(readOnly = true)
  public EstudianteResponse obtenerEstudiante(Long id) {
    // TODO(A) estudiantes.findById(id) -> 404 si no existe; mapper
    return estudiantes
            .findById(id)
            .map(PersonasMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Estudiante " + id + " no existe"));
  }
    //throw new PersistenciaPendienteException("obtenerEstudiante");


  @Transactional
  public ContactoResponse guardarContacto(Long estudianteId, UpsertContactoRequest r) {
    // TODO(A) 1. estudiantes.findById(estudianteId) -> 404 si no existe
    // TODO(A) 2. contactos.findByEstudianteId(...) -> si existe se ACTUALIZA, si no se crea (upsert)
    // TODO(A) 3. Validar tutorLegal/telefono y Parseo.fechaOpcional(r.fechaAutorizacion(), ...)
    // TODO(A) 4. Guardar y devolver el mapper (nunca debe quedar una segunda ficha)
    throw new PersistenciaPendienteException("guardarContacto");
  }

  @Transactional(readOnly = true)
  public ContactoResponse contacto(Long estudianteId) {
    // TODO(A) 404 si el estudiante no existe O si no tiene ficha
    throw new PersistenciaPendienteException("contacto");
  }
}
