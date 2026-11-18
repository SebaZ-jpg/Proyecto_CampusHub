package es.academia.campushub.controller.rest;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.service.CampusHubService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EstudianteController {
  private final CampusHubService uc;

  public EstudianteController(CampusHubService uc) {
    this.uc = uc;
  }

    // Crea el estudiante; el servicio valida campos y referencias antes de guardar.
  @PostMapping("/api/estudiantes")
  public ResponseEntity<EstudianteResponse> crear(@RequestBody CreateEstudianteRequest r) {
    return ResponseEntity.status(201).body(uc.crearEstudiante(r));
  }

    // Recupera un estudiante por su clave primaria.
  @GetMapping("/api/estudiantes/{id}")
  public EstudianteResponse obtener(@PathVariable Long id) {
    return uc.obtenerEstudiante(id);
  }

    // Historial calculado desde las inscripciones persistidas en PostgreSQL.
  @GetMapping("/api/estudiantes/{id}/actividades")
  public List<ActividadHistorialResponse> historial(@PathVariable Long id) {
    return uc.historial(id);
  }

    // PUT funciona como upsert: crea la ficha si no existe o sustituye sus datos si ya existe.
  @PutMapping("/api/estudiantes/{id}/contacto")
  public ContactoResponse contactoGuardar(
      @PathVariable Long id, @RequestBody UpsertContactoRequest r) {
    return uc.guardarContacto(id, r);
  }

    // Consulta la ficha única asociada al estudiante.
  @GetMapping("/api/estudiantes/{id}/contacto")
  public ContactoResponse contacto(@PathVariable Long id) {
    return uc.contacto(id);
  }
}
