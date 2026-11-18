package es.academia.campushub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ficha de contacto: como máximo UNA por estudiante y no puede existir sin él (RF09). */
@Entity
@Table(
    name = "contacto_estudiante",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_contacto_estudiante_estudiante",
            columnNames = "estudiante_id"))
@Getter
@Setter
@NoArgsConstructor
public class ContactoEstudiante {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // unique = true garantiza "como máximo una ficha por estudiante" en la BD.
  //   ¿Qué pasa si se borra el estudiante? (cascada vs. impedirlo). Razonadlo.
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
  private Estudiante estudiante;

  // longitudes máximas; tutorLegal y telefono obligatorios (decidid el resto).
  @Column(name = "tutor_legal", nullable = false, length = 150)
  private String tutorLegal;

  @Column(nullable = false, length = 20)
  private String telefono;

  @Column(length = 254)
  private String email;

  @Column(nullable = false)
  private Boolean autorizado;

  @Column(name = "fecha_autorizacion")
  private LocalDate fechaAutorizacion;
}
