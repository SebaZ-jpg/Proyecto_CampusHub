package es.academia.campushub.repository;

import es.academia.campushub.entity.Actividad;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
  /**
   * Carga la actividad BLOQUEANDO su fila (SELECT ... FOR UPDATE) hasta que acabe la transacción.
   * Sirve para que dos inscripciones simultáneas no superen el aforo. Solo funciona dentro de un
   * método @Transactional.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Actividad a where a.id = :id")
  Optional<Actividad> findByIdForUpdate(@Param("id") Long id);

  List<Actividad> findByEtiquetasId(Long etiquetaId);
}
