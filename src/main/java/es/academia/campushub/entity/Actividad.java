package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@org.hibernate.annotations.Check(constraints = "max_participantes > 0")
@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String titulo;

  @Column(columnDefinition = "text", nullable = false)
  private String descripcion;

  public enum TipoActividad {
    TALLER,
    CHARLA,
    VISITA,
    JORNADA
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoActividad tipo;

  @Column(name = "fecha_prevista")
  private LocalDate fechaPrevista;

  @Column(name = "max_participantes", nullable = false)
  private Integer maxParticipantes;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "docente_id", nullable = false)
  private Docente docente;

  @ManyToMany
  @JoinTable(
          name = "actividad_etiqueta",
          joinColumns = @JoinColumn(name = "actividad_id"),
          inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
  private Set<Etiqueta> etiquetas = new HashSet<>();
}
// Restricción de aforo (RF03): Hemos aplicado @org.hibernate.annotations.Check(constraints =
// "max_participantes > 0") a nivel de clase.
// Esto garantiza en el motor de base de datos que ninguna vía (ni siquiera inserciones manuales en
// SQL) pueda crear actividades con aforo cero o negativo.

// Textos y longitudes: título se ha limitado a 150 caracteres, mientras que para descripción hemos
// usado columnDefinition = "text" para saltarnos el límite de 255 de los VARCHAR tradicionales,
// descartando @Lob porque en PostgreSQL el tipo nativo text es más eficiente y fácil de consultar.

// Tipo de Actividad: Aunque se podía dejar como String y validarlo a mano, hemos optado por crear
// el enumerado interno TipoActividad y mapearlo a la BD con @Enumerated(EnumType.STRING).
// Motivo: Garantiza la seguridad de tipos en Java y MapStruct hace la conversión automática a
// String en los DTOs sin tener que tocar nada más.
