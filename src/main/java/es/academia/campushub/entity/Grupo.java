package es.academia.campushub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grupo")
@Getter
@Setter
@NoArgsConstructor
public class Grupo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // TODO(A): nombre y nivel obligatorios con longitud máxima.
  //   ¿Debe ser único el nombre del grupo? Decidid y justificadlo (la rúbrica no lo exige).
  private String nombre;

  private String nivel;

  // TODO(A): decidid si el tutor es obligatorio (la rúbrica dice que cada grupo "dispone de" docente tutor).
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tutor_docente_id", nullable = false)
  private Docente tutor;
}
