package es.academia.campushub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "docente",
    indexes = @Index(name = "idx_docente_departamento", columnList = "departamento_id"))
@Getter
@Setter
@NoArgsConstructor
public class Docente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(A): nombre y apellidos obligatorios y con longitud máxima razonada.
  //   Pista: @Column(nullable = false, length = ...)
  // resulto:
  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 100)
  private String apellidos;

  // TODO(A): email obligatorio, longitud máxima (254 es el máximo de un correo) y ÚNICO.
  // resulto:
  @Column(nullable = false, length = 254, unique = true)
  private String email;

  // TODO(A): ¿por qué LAZY? ¿Qué debe pasar al borrar un departamento que tiene docentes?
  //   (sin cascada, la FK lo impide: razonadlo en el documento de decisiones).
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "departamento_id", nullable = false)
  private Departamento departamento;
}
