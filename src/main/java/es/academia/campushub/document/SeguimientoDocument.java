package es.academia.campushub.document;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Registro de seguimiento de una actividad (RF05). La estructura de {@code datos} es libre: taller,
 * visita, charla o tipos futuros. Se guarda tal cual llega.
 *
 * <p>actividadId es una referencia "blanda" a una fila de PostgreSQL: Mongo NO comprueba que
 * exista. Ahí aparece el riesgo de inconsistencia entre motores que hay que explicar (RF07).
 */
@Document(collection = "seguimientos")
@Getter
@Setter
@NoArgsConstructor
public class SeguimientoDocument {
  @Id private String id;

  @Indexed(name = "idx_seguimiento_actividad")
  private Long actividadId;

  private String tipo;

  private Map<String, Object> datos;

  private Instant creadoEn;
}
