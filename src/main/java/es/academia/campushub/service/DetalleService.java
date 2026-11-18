package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Ensambla en una sola respuesta REST los datos de PostgreSQL y MongoDB y usa Redis como caché
 * temporal. Las listas se obtienen de los servicios, que devuelven listas vacías si no hay datos.
 */

@Service
public class DetalleService {
  private final ActividadesService actividades;
  private final PersonasService personas;
  private final SeguimientoService seguimientos;

  public DetalleService(
      ActividadesService actividades, PersonasService personas, SeguimientoService seguimientos) {
    this.actividades = actividades;
    this.personas = personas;
    this.seguimientos = seguimientos;
  }

  @Cacheable(cacheNames = "actividadDetalle", key = "'actividad:detalle:' + #id")
  public ActividadDetalleResponse detalle(Long id) {
      // Cada servicio consulta su motor; el DTO final oculta al cliente dónde se guarda cada pieza.
    ActividadResponse actividad = actividades.obtenerActividad(id); // 404 si no existe
    DocenteResponse responsable = personas.obtenerDocente(actividad.docenteId());
    int ocupadas = actividades.plazasOcupadas(id);
    int disponibles = actividad.maxParticipantes() - ocupadas;
    List<EstudianteResponse> estudiantes = actividades.participantes(id);
    List<EtiquetaResponse> etiquetas = actividades.etiquetasDe(id);
    List<SeguimientoResponse> registros = seguimientos.seguimientosDe(id);
    // TODO(C): comprobad que ninguna lista sale null; añadid (opcional) la caché con Redis aquí.
      // La clave y TTL de Redis están en RedisCacheConfig; los cambios relevantes expulsan esta entrada.
    return new ActividadDetalleResponse(
        actividad, responsable, ocupadas, disponibles, estudiantes, etiquetas, registros);
  }
}
