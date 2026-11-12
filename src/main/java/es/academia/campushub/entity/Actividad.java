package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO(B): capacidad máxima > 0 también en la BD. Con Hibernate:
//   @org.hibernate.annotations.Check(constraints = "max_participantes > 0")
// (con ddl-auto=update solo se aplica al CREAR la tabla: usad "docker compose down -v" para probar).
@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(B): obligatorio, longitud máxima razonada.
  private String titulo;

  // TODO(B): texto largo. Pista: @Column(columnDefinition = "text"). Evitad @Lob con PostgreSQL.
  private String descripcion;

  // TODO(B): TALLER, CHARLA, VISITA o JORNADA (los tests usan estos valores). Podéis dejarlo como
  // String y validarlo en el servicio (400), o usar un enum con @Enumerated(EnumType.STRING).
  // Decidid y justificad. Añadid también un CHECK si lo dejáis como String.
  private String tipo;

  @Column(name = "fecha_prevista")
  private LocalDate fechaPrevista;

  @Column(name = "max_participantes")
  private Integer maxParticipantes;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "docente_id", nullable = false)
  private Docente docente;

  // Relación N:M con Etiqueta. Un Set evita duplicados; la tabla intermedia tiene clave
  // primaria compuesta (actividad_id, etiqueta_id), que es la protección real en la BD.
  @ManyToMany
  @JoinTable(
      name = "actividad_etiqueta",
      joinColumns = @JoinColumn(name = "actividad_id"),
      inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
  private Set<Etiqueta> etiquetas = new HashSet<>();
}
