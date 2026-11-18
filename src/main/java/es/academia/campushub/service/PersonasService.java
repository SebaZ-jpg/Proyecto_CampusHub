package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.*;
import es.academia.campushub.mapper.PersonasMapper;
import es.academia.campushub.repository.*;
import es.academia.campushub.util.Parseo;
import es.academia.campushub.util.exception.BusinessConflictException;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de departamentos, docentes, grupos, estudiantes y contactos. Valida entradas,
 * verifica referencias, delega persistencia en repositorios y convierte entidades en respuestas.
 */

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

  /** copia este patrón (validar -> comprobar 404/409 -> guardar -> mapper). */
  @Transactional
  public DepartamentoResponse crearDepartamento(CreateDepartamentoRequest r) {
    String nombre = Parseo.texto(r == null ? null : r.nombre(), "nombre"); // 400 si falta
    validarLongitud(nombre, 100, "nombre");
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

    // Validar nombre, apellidos, email (Parseo.texto) -> 400
    // departamentos.findById(r.departamentoId()) -> 404 si no existe
    // docentes.existsByEmail(email) -> 409
    // Crear Docente, guardar y devolver PersonasMapper.toResponse(...)
    if (r == null || r.departamentoId() == null) {
      throw new InvalidInputException("departamentoId es obligatorio");
    }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String apellidos = Parseo.texto(r.apellidos(), "apellidos");
    String email = validarEmail(r.email());
    validarLongitud(nombre, 100, "nombre");
    validarLongitud(apellidos, 100, "apellidos");
    Departamento departamento =
        departamentos
            .findById(r.departamentoId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Departamento " + r.departamentoId() + " no existe"));
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

    // Validar nombre y nivel -> 400
    // docentes.findById(r.tutorDocenteId()) -> 404 si no existe
    // Guardar y devolver el mapper
    // El tutor debe existir antes de crear el grupo para no guardar referencias inválidas.
    if (r == null || r.tutorDocenteId() == null) {
      throw new InvalidInputException("tutorDocenteId es obligatorio");
    }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String nivel = Parseo.texto(r.nivel(), "nivel");
    validarLongitud(nombre, 50, "nombre");
    validarLongitud(nivel, 50, "nivel");
    Docente tutor =
        docentes
            .findById(r.tutorDocenteId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Docente " + r.tutorDocenteId() + " no existe"));
    Grupo g = new Grupo();
    g.setNombre(nombre);
    g.setNivel(nivel);
    g.setTutor(tutor);
    return PersonasMapper.toResponse(grupos.save(g));
  }

  @Transactional
  public EstudianteResponse crearEstudiante(CreateEstudianteRequest r) {
    // Validar campos; fecha con Parseo.fecha(r.fechaNacimiento(), "fechaNacimiento")
    // Fecha de nacimiento anterior a hoy -> 400 (y CHECK en la BD)
    // grupos.findById(r.grupoId()) -> 404 si no existe
    // estudiantes.existsByEmail(email) -> 409
    // Guardar y devolver el mapper

    // La fecha se valida aquí para dar un error legible; el trigger de PostgreSQL repite
    // la regla en la base de datos para proteger también escrituras que no pasen por esta API.
    if (r == null || r.grupoId() == null) {
      throw new InvalidInputException("grupoId es obligatorio");
    }
    String nombre = Parseo.texto(r.nombre(), "nombre");
    String apellidos = Parseo.texto(r.apellidos(), "apellidos");
    String email = validarEmail(r.email());
    validarLongitud(nombre, 100, "nombre");
    validarLongitud(apellidos, 150, "apellidos");
    LocalDate nacimiento = Parseo.fecha(r.fechaNacimiento(), "fechaNacimiento");
    if (!nacimiento.isBefore(LocalDate.now())) {
      throw new InvalidInputException("fechaNacimiento debe ser anterior a hoy");
    }
    Grupo grupo =
        grupos
            .findById(r.grupoId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Grupo " + r.grupoId() + " no existe"));
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
   // Optional.map transforma el resultado; si no existe, se responde 404 mediante la excepción.
    return estudiantes
        .findById(id)
        .map(PersonasMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Estudiante " + id + " no existe"));
  }

  // throw new PersistenciaPendienteException("obtenerEstudiante");

  @Transactional
  public ContactoResponse guardarContacto(Long estudianteId, UpsertContactoRequest r) {
    // estudiantes.findById(estudianteId) -> 404 si no existe
    // contactos.findByEstudianteId(...) -> si existe se ACTUALIZA, si no se crea
    // (upsert)
    // Validar tutorLegal/telefono y Parseo.fechaOpcional(r.fechaAutorizacion(), ...)
    // Guardar y devolver el mapper (nunca debe quedar una segunda ficha)
    // throw new PersistenciaPendienteException("guardarContacto");
    if (r == null) {
      throw new InvalidInputException("Cuerpo obligatorio");
    }
    Estudiante estudiante =
        estudiantes
            .findById(estudianteId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Estudiante " + estudianteId + " no existe"));
    String tutor = Parseo.texto(r.tutorLegal(), "tutorLegal");
    String telefono = Parseo.texto(r.telefono(), "telefono");
    validarLongitud(tutor, 150, "tutorLegal");
    validarLongitud(telefono, 20, "telefono");
    if (r.autorizado() == null) {
      throw new InvalidInputException("autorizado es obligatorio");
    }
    String email = r.email() == null || r.email().isBlank() ? null : validarEmail(r.email());
    LocalDate fechaAutorizacion = Parseo.fechaOpcional(r.fechaAutorizacion(), "fechaAutorizacion");
      // La restricción UNIQUE de estudiante_id es la garantía final de una sola ficha por alumno.
    ContactoEstudiante c =
        contactos
            .findByEstudianteId(estudianteId)
            .orElseGet(
                () -> {
                  ContactoEstudiante nuevo = new ContactoEstudiante();
                  nuevo.setEstudiante(estudiante);
                  return nuevo;
                });
    c.setTutorLegal(tutor);
    c.setTelefono(telefono);
    c.setEmail(email);
    c.setAutorizado(r.autorizado());
    c.setFechaAutorizacion(fechaAutorizacion);
    return PersonasMapper.toResponse(contactos.save(c));
  }

  @Transactional(readOnly = true)
  public ContactoResponse contacto(Long estudianteId) {
   // Se distinguen los dos casos de ausencia para devolver un mensaje 404 comprensible.
    if (!estudiantes.existsById(estudianteId)) {
      throw new ResourceNotFoundException("Estudiante " + estudianteId + " no existe");
    }
    return contactos
        .findByEstudianteId(estudianteId)
        .map(PersonasMapper::toResponse)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("El estudiante " + estudianteId + " no tiene ficha"));
  }

  private void validarLongitud(String valor, int maximo, String campo) {
    if (valor.length() > maximo) {
      throw new InvalidInputException(campo + " no puede superar " + maximo + " caracteres");
    }
  }

  private String validarEmail(String valor) {
    String email = Parseo.texto(valor, "email");
    validarLongitud(email, 254, "email");
    int arroba = email.indexOf('@');
    if (arroba <= 0 || arroba != email.lastIndexOf('@') || arroba == email.length() - 1) {
      throw new InvalidInputException("email no es válido");
    }
    return email;
  }
}
