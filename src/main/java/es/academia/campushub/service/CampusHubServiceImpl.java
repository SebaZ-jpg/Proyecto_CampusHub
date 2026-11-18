package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementación fachada de CampusHubService. Distribuye cada operación entre los servicios de
 * personas, actividades, seguimientos y detalle.
 */

@Service
public class CampusHubServiceImpl implements CampusHubService {
  private final PersonasService personas;
  private final ActividadesService actividades;
  private final SeguimientoService seguimientos;
  private final DetalleService detalle;

  public CampusHubServiceImpl(
      PersonasService personas,
      ActividadesService actividades,
      SeguimientoService seguimientos,
      DetalleService detalle) {
    this.personas = personas;
    this.actividades = actividades;
    this.seguimientos = seguimientos;
    this.detalle = detalle;
  }

  @Override
  public GrupoResponse crearGrupo(CreateGrupoRequest request) {
    return personas.crearGrupo(request);
  }

  @Override
  public DepartamentoResponse crearDepartamento(CreateDepartamentoRequest request) {
    return personas.crearDepartamento(request);
  }

  @Override
  public EstudianteResponse crearEstudiante(CreateEstudianteRequest request) {
    return personas.crearEstudiante(request);
  }

  @Override
  public EstudianteResponse obtenerEstudiante(Long id) {
    return personas.obtenerEstudiante(id);
  }

  @Override
  public DocenteResponse crearDocente(CreateDocenteRequest request) {
    return personas.crearDocente(request);
  }

  @Override
  public ContactoResponse guardarContacto(Long estudianteId, UpsertContactoRequest request) {
    return personas.guardarContacto(estudianteId, request);
  }

  @Override
  public ContactoResponse contacto(Long estudianteId) {
    return personas.contacto(estudianteId);
  }

  @Override
  public ActividadResponse crearActividad(CreateActividadRequest request) {
    return actividades.crearActividad(request);
  }

  @Override
  public ActividadResponse obtenerActividad(Long id) {
    return actividades.obtenerActividad(id);
  }

  @Override
  public InscripcionResponse inscribir(Long actividadId, CreateInscripcionRequest request) {
    return actividades.inscribir(actividadId, request);
  }

  @Override
  public List<EstudianteResponse> participantes(Long actividadId) {
    return actividades.participantes(actividadId);
  }

  @Override
  public List<ActividadHistorialResponse> historial(Long estudianteId) {
    return actividades.historial(estudianteId);
  }

  @Override
  public EtiquetaResponse crearEtiqueta(CreateEtiquetaRequest request) {
    return actividades.crearEtiqueta(request);
  }

  @Override
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    actividades.asociarEtiqueta(actividadId, etiquetaId);
  }

  @Override
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    return actividades.actividadesPorEtiqueta(etiquetaId);
  }

  @Override
  public SeguimientoResponse registrarSeguimiento(
      Long actividadId, CreateSeguimientoRequest request) {
    return seguimientos.registrarSeguimiento(actividadId, request);
  }

  @Override
  public ActividadDetalleResponse detalle(Long id) {
    return detalle.detalle(id);
  }
}
