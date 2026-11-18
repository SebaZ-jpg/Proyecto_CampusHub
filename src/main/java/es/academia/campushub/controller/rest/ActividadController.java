package es.academia.campushub.controller.rest;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.service.CampusHubService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ActividadController {
  private final CampusHubService service;

  public ActividadController(CampusHubService service) {
    this.service = service;
  }

    // POST crea un recurso nuevo; por contrato responde 201 Created con el DTO generado.
  @PostMapping("/api/actividades")
  public ResponseEntity<ActividadResponse> crear(@RequestBody CreateActividadRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crearActividad(r));
  }

    // GET consulta una actividad existente; si no existe, el servicio lanza un error 404.
  @GetMapping("/api/actividades/{id}")
  public ActividadResponse obtener(@PathVariable Long id) {
    return service.obtenerActividad(id);
  }

    // El id de ruta identifica la actividad y el cuerpo aporta el estudiante que se quiere inscribir.
  @PostMapping("/api/actividades/{id}/inscripciones")
  public ResponseEntity<InscripcionResponse> inscribir(
      @PathVariable Long id, @RequestBody CreateInscripcionRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.inscribir(id, r));
  }

    // Devuelve la lista de estudiantes inscritos; si no hay inscripciones, será una lista vacía.
  @GetMapping("/api/actividades/{id}/inscripciones")
  public List<EstudianteResponse> participantes(@PathVariable Long id) {
    return service.participantes(id);
  }

    // Guarda en MongoDB el objeto de seguimiento flexible asociado a la actividad.
  @PostMapping("/api/actividades/{id}/seguimientos")
  public ResponseEntity<SeguimientoResponse> seguimiento(
      @PathVariable Long id, @RequestBody CreateSeguimientoRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarSeguimiento(id, r));
  }

    // Ensambla los datos de actividad, docente, plazas, estudiantes, etiquetas y seguimientos.
  @GetMapping("/api/actividades/{id}/detalle")
  public ActividadDetalleResponse detalle(@PathVariable Long id) {
    return service.detalle(id);
  }
}
