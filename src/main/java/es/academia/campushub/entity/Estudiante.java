package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA del estudiante. Su grupo es obligatorio; el correo es único y los índices ayudan a
 * consultar estudiantes por grupo.
 */
@Entity
@Table(
    name = "estudiante",
    indexes = @Index(name = "idx_estudiante_grupo", columnList = "grupo_id"))
@Getter
@Setter
@NoArgsConstructor
public class Estudiante {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(A): nombre y apellidos obligatorios con longitud máxima razonada.
  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 150)
  private String apellidos;

  // TODO(A): email obligatorio, longitud máxima y ÚNICO.
  @Column(nullable = false, length = 254, unique = true)
  private String email;

  // TODO(A): obligatoria y anterior a hoy.
  @Column(name = "fecha_nacimiento", nullable = false)
  private LocalDate fechaNacimiento;

  // TODO(A): índice en grupo_id (consultas por grupo) y decidid el borrado de grupos con alumnos.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grupo_id", nullable = false)
  private Grupo grupo;
}
