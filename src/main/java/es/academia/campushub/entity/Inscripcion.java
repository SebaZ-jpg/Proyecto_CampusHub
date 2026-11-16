package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "inscripcion",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_inscripcion_actividad_estudiante",
            columnNames = {"actividad_id", "estudiante_id"}),
    indexes = {
      @Index(name = "idx_inscripcion_estudiante", columnList = "estudiante_id"),
      @Index(name = "idx_inscripcion_actividad", columnList = "actividad_id")
    })
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actividad_id", nullable = false)
  private Actividad actividad;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "estudiante_id", nullable = false)
  private Estudiante estudiante;

  @Column(name = "fecha_inscripcion", nullable = false)
  private LocalDateTime fechaInscripcion;
}

// Prevención de duplicados (RF04): Hemos añadido una restricción UNIQUE multicomlumna
// (actividad_id, estudiante_id). Esta es la verdadera red de seguridad.
// Si dos hilos en Java intentan inscribir al mismo estudiante a la vez y logran pasar el if
// (yaInscrito), la base de datos lanzará una DataIntegrityViolationException (que tu controlador
// convertirá en un HTTP 409).
//
// Diseño de índices y la respuesta al TODO:
// No hace falta un índice extra para actividad_id. PostgreSQL, al crear la restricción UNIQUE
// (actividad_id, estudiante_id), crea automáticamente un índice B-Tree subyacente.
// Como los índices B-Tree se leen de izquierda a derecha, cualquier búsqueda que filtre por
// actividad_id (para sacar la lista de participantes) usará ese índice gratis.
// Sin embargo, si buscamos el historial de un alumno (filtrando solo por estudiante_id), el índice
// compuesto no sirve.
// Por eso, hemos creado explícitamente el índice secundario idx_inscripcion_estudiante.
