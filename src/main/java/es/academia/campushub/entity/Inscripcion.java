package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO(B): la pareja (actividad, estudiante) debe ser ÚNICA en la BD:
//   @Table(name = "inscripcion",
//          uniqueConstraints = @UniqueConstraint(name = "uk_inscripcion_actividad_estudiante",
//                                                columnNames = {"actividad_id", "estudiante_id"}),
//          indexes = @Index(name = "idx_inscripcion_estudiante", columnList = "estudiante_id"))
// ¿Hace falta un índice en actividad_id además? (pista: el UNIQUE ya empieza por esa columna).
@Entity
@Table(name = "inscripcion")
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

  // Se conserva la fecha/hora de la inscripción (RF04.5).
  @Column(name = "fecha_inscripcion", nullable = false)
  private LocalDateTime fechaInscripcion;
}
