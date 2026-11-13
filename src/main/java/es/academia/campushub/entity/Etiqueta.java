package es.academia.campushub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "etiqueta")
@Getter
@Setter
@NoArgsConstructor
public class Etiqueta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String nombre;
}

// Restricción de unicidad y longitud (RF10): Hemos aplicado @Column(nullable = false, unique =
// true, length = 50) al campo nombre.
// La longitud de 50 caracteres es más que suficiente para una etiqueta descriptiva (ej: "Desarrollo
// Backend"), y la restricción unique = true crea un índice único en PostgreSQL.
// Esto garantiza que la relación con Actividad (que configuramos en Actividad.java mediante
// @ManyToMany y un Set) reutilice siempre la misma etiqueta física en la BD, evitando anomalías e
// inconsistencias.
