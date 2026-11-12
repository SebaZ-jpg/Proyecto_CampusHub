package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO(A): para el CHECK de la fecha de nacimiento, podéis usar la anotación de Hibernate:
//   @org.hibernate.annotations.Check(constraints = "fecha_nacimiento < CURRENT_DATE")
//   ...o un script SQL / trigger. Explicad en el documento qué elegís y por qué
//   (CURRENT_DATE en un CHECK es "estable", no "inmutable": comentad ese inconveniente).
@Entity
@Table(name = "estudiante")
@Getter
@Setter
@NoArgsConstructor
public class Estudiante {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(A): nombre y apellidos obligatorios con longitud máxima razonada.
  private String nombre;

  private String apellidos;

  // TODO(A): email obligatorio, longitud máxima y ÚNICO.
  private String email;

  // TODO(A): obligatoria y anterior a hoy.
  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  // TODO(A): índice en grupo_id (consultas por grupo) y decidid el borrado de grupos con alumnos.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grupo_id", nullable = false)
  private Grupo grupo;
}
