package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * PERSONA C: RF06. Combina PostgreSQL (A y B) y MongoDB (C) en una única respuesta. Funciona en
 * cuanto A y B terminen sus métodos: aquí solo se ensambla.
 */
@Service
public class DetalleService {
  private final ActividadesService actividades;
  private final PersonasService personas;
  private final SeguimientoService seguimientos;

  public DetalleService(
      ActividadesService actividades,
      PersonasService personas,
      SeguimientoService seguimientos) {
    this.actividades = actividades;
    this.personas = personas;
    this.seguimientos = seguimientos;
  }

  public ActividadDetalleResponse detalle(Long id) {
    ActividadResponse actividad = actividades.obtenerActividad(id); // 404 si no existe
    DocenteResponse responsable = personas.obtenerDocente(actividad.docenteId());
    int ocupadas = actividades.plazasOcupadas(id);
    int disponibles = actividad.maxParticipantes() - ocupadas;
    List<EstudianteResponse> estudiantes = actividades.participantes(id);
    List<EtiquetaResponse> etiquetas = actividades.etiquetasDe(id);
    List<SeguimientoResponse> registros = seguimientos.seguimientosDe(id);
    // TODO(C): comprobad que ninguna lista sale null; añadid (opcional) la caché con Redis aquí.
    return new ActividadDetalleResponse(
        actividad, responsable, ocupadas, disponibles, estudiantes, etiquetas, registros);
  }
}
