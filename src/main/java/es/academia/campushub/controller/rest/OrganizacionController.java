package es.academia.campushub.controller.rest;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.service.CampusHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizacionController {
  private final CampusHubService service;

  public OrganizacionController(CampusHubService service) {
    this.service = service;
  }

    // La clave y TTL de Redis están en RedisCacheConfig; los cambios relevantes expulsan esta entrada.
  @PostMapping("/api/grupos")
  public ResponseEntity<GrupoResponse> grupo(@RequestBody CreateGrupoRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crearGrupo(r));
  }

    // Da de alta un departamento antes de registrar docentes asociados.
  @PostMapping("/api/departamentos")
  public ResponseEntity<DepartamentoResponse> departamento(
      @RequestBody CreateDepartamentoRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crearDepartamento(r));
  }

    // Da de alta un docente que pertenece a un departamento existente.
  @PostMapping("/api/docentes")
  public ResponseEntity<DocenteResponse> docente(@RequestBody CreateDocenteRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crearDocente(r));
  }
}
