package es.academia.campushub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EJEMPLO COMPLETO (Persona A): fijaos en cómo cada regla de la rúbrica se expresa en la propia
 * columna (NOT NULL, longitud máxima, UNIQUE) y no solo en el código Java. Copiad este patrón.
 */
@Entity
@Table(name = "departamento")
@Getter
@Setter
@NoArgsConstructor
public class Departamento {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100, unique = true)
  private String nombre;
}
