package es.academia.campushub.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresIntegritySetup implements ApplicationRunner {
  private final JdbcTemplate jdbc;

  public PostgresIntegritySetup(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(ApplicationArguments args) {
    jdbc.execute(
        """
        CREATE OR REPLACE FUNCTION campushub_validate_estudiante_fecha_nacimiento()
        RETURNS trigger AS $$
        BEGIN
          IF NEW.fecha_nacimiento >= CURRENT_DATE THEN
            RAISE EXCEPTION 'fecha_nacimiento debe ser anterior a hoy'
              USING ERRCODE = '23514';
          END IF;
          RETURN NEW;
        END;
        $$ LANGUAGE plpgsql
        """);
    jdbc.execute("DROP TRIGGER IF EXISTS trg_estudiante_fecha_nacimiento ON estudiante");
    jdbc.execute(
        """
        CREATE TRIGGER trg_estudiante_fecha_nacimiento
        BEFORE INSERT OR UPDATE OF fecha_nacimiento ON estudiante
        FOR EACH ROW
        EXECUTE FUNCTION campushub_validate_estudiante_fecha_nacimiento()
        """);
  }
}
