# Guía de reparto CampusHub (3 personas)

Este repositorio es el esqueleto oficial **más** una estructura ya repartida en tres bloques.
Cada persona trabaja en **sus propios ficheros**, así no hay conflictos de Git.

> Importante: el esqueleto está escrito para Spring Boot 4.1.2 / Java 21, pero no se ha podido
> compilar al prepararlo. El primer paso de todos (Fase 0) es `mvn compile`. Si algo no compila,
> avisad al grupo antes de seguir: es un fallo del esqueleto, no vuestro.

## Quién hace qué

| | Persona A | Persona B | Persona C |
|---|---|---|---|
| Requisitos | RF01, RF02, RF09 | RF03, RF04, RF08, RF10 | RF05, RF06, RF07 (+ Redis opcional) |
| Motor | PostgreSQL | PostgreSQL | MongoDB + ensamblar ambos |
| Entidades / documentos | Departamento, Docente, Grupo, Estudiante, ContactoEstudiante | Actividad, Inscripcion, Etiqueta | SeguimientoDocument |
| Repositorios | Departamento, Docente, Grupo, Estudiante, ContactoEstudiante | Actividad, Inscripcion, Etiqueta | Seguimiento |
| Servicio | `PersonasService` | `ActividadesService` | `SeguimientoService`, `DetalleService`, `config/MongoSetup` |
| Documento | Diagrama relacional | Restricciones + concurrencia | Modelo documental + riesgos de dos motores |

`CampusHubServiceImpl` solo delega en estos servicios. **Nadie lo modifica** después de la Fase 0.

## Reglas del grupo

1. Cada uno edita solo sus ficheros. Los repositorios de otra persona se pueden **usar**
   (`findById`, `existsById`), no cambiar. Si necesitáis algo nuevo, pedídselo a su dueño.
2. No se tocan controllers, DTO, endpoints ni tests (la rúbrica lo prohíbe).
3. Una rama por persona: `feature/a-personas`, `feature/b-actividades`, `feature/c-mongo`.
   Commits pequeños y pull request hacia `main` cuando algo funcione.
4. Antes de cada commit: `mvn spotless:apply` (si no, `mvn verify` falla por formato).
5. Con `ddl-auto: update`, las restricciones nuevas **solo se aplican al crear la tabla**. Si
   cambiáis una entidad y no veis el efecto en `psql`, reiniciad la base:
   `docker compose down -v`(solo usarlo con datos descartables) y volved a arrancar. (Los tests usan `create-drop` y no tienen este problema.)

## Contrato entre personas (no cambiar las firmas)

| Lo ofrece | Método | Lo usa |
|---|---|---|
| A | `PersonasService.obtenerDocente(Long)` (ya hecho) | C (detalle) |
| A | `PersonasMapper.toResponse(...)` (ya hecho) | B y C |
| B | `ActividadesService.obtenerActividad(Long)` | C |
| B | `ActividadesService.participantes(Long)` | C |
| B | `ActividadesService.plazasOcupadas(Long)` (ya hecho) | C |
| B | `ActividadesService.etiquetasDe(Long)` | C |
| C | `SeguimientoService.seguimientosDe(Long)` (ya hecho) | C |

## Fase 0 (los tres juntos, ~30 min)

1. Una persona crea el repositorio en GitHub, sube este proyecto a `main` y añade a las otras dos
   como colaboradoras. Las otras clonan.
2. Todos: `mvn compile` (necesita JDK 21 y Maven 3.9+).
3. Todos: arrancar con `mvn spring-boot:run` y abrir http://localhost:8080/swagger-ui.html.
   Verán 501 en todo: es lo esperado.
4. Todos: `mvn verify` para ver los tests fallar (necesita Docker). Es vuestro punto de partida.
5. Crear cada persona su rama y repasar juntos la tabla de arriba y el contrato.

## Persona A — base relacional (RF01, RF02, RF09)

1. Leed el ejemplo completo: `entity/Departamento.java`, `repository/DepartamentoRepository.java`
   y `PersonasService.crearDepartamento`. Es el patrón de todo lo demás.
2. Completad los `TODO(A)` de las entidades: `Docente`, `Grupo`, `Estudiante`, `ContactoEstudiante`
   (longitudes, NOT NULL, UNIQUE de emails, UNIQUE de `estudiante_id` en el contacto, CHECK de la fecha).
3. Implementad los métodos de `PersonasService` **en este orden**, probando cada uno en Swagger antes
   de pasar al siguiente: `crearDocente` → `crearGrupo` → `crearEstudiante` → `obtenerEstudiante`
   → `guardarContacto` → `contacto`.
4. **Subid pronto** `Docente`, `Grupo` y `Estudiante` funcionando: B y C dependen de ellos.
5. Tests que debéis ver en verde: `ContratoApiIT.registroDeDepartamentoYDocente` y
   `ContratoApiIT.estudianteSeRecupera`.
6. Comprobad la integridad a mano (como harán en la defensa):
   ```bash
   docker compose exec postgres psql -U campushub -d campushub
   \d estudiante
   INSERT INTO estudiante(nombre, apellidos, email, fecha_nacimiento, grupo_id)
     VALUES ('x','y','mismo@correo.es','2999-01-01',1);   -- debe fallar
   ```
7. Entregables: diagrama ER (Mermaid vale) de vuestras tablas y vuestra sección del documento de decisiones.

## Persona B — actividades, inscripciones y etiquetas (RF03, RF04, RF08, RF10)

1. Completad los `TODO(B)` de `Actividad`, `Inscripcion` y `Etiqueta` (CHECK `max_participantes > 0`,
   UNIQUE `(actividad_id, estudiante_id)`, nombre de etiqueta único, `descripcion` como texto largo).
2. Implementad en `ActividadesService`: `crearActividad` → `obtenerActividad` → `crearEtiqueta`
   → `asociarEtiqueta` → `actividadesPorEtiqueta` → `etiquetasDe`.
   (`obtenerActividad` y `etiquetasDe` los necesita C: hacedlos pronto.)
3. Luego lo difícil, `inscribir`: bloqueo de la fila de la actividad con `findByIdForUpdate`,
   comprobar duplicado (409), comprobar aforo (409), guardar con `saveAndFlush`.
4. Después `participantes` y `historial`.
5. Escribid vuestro propio test de concurrencia (por ejemplo, 10 hilos inscribiendo estudiantes
   distintos en una actividad de 3 plazas: deben quedar exactamente 3). Es lo que más impresiona en la defensa.
6. Tests en verde: `RelacionesApiIT` (ambos) y `ContratoHttpIT.recursoInexistenteEs404`.
7. Comprobad en `psql` que inserciones duplicadas, `max_participantes = 0` o FKs inválidas se rechazan.
8. Entregables: sección del documento sobre restricciones, índices y control de aforo.

## Persona C — MongoDB e integración (RF05, RF06, RF07)

1. Arrancad la app y mirad lo que crea `MongoSetup`:
   ```bash
   docker compose exec mongo mongosh campushub
   db.getCollectionInfos({name: "seguimientos"})
   db.seguimientos.getIndexes()
   ```
2. Implementad `SeguimientoService.registrarSeguimiento` (primero comprobad en PostgreSQL que la
   actividad existe, después escribid en Mongo).
3. Revisad `DetalleService.detalle`: ya está ensamblado, pero solo funcionará cuando A y B terminen.
   Comprobad que ninguna lista sale `null`.
4. Comprobad el validador: `db.seguimientos.insertOne({tipo: 5})` debe ser rechazado.
5. Tests en verde: `ContratoApiIT.unaActividadAdmiteSeguimientosVariables` y, con todo integrado,
   `mvn verify` completo.
6. Redactad el apartado RF07: cómo puede quedar un seguimiento huérfano (por ejemplo, si se
   elimina una actividad en PostgreSQL o falla algo entre las dos escrituras) y cómo reduciríais el riesgo.
7. Montad el documento de decisiones final juntando las secciones de A y B, y el README (arranque,
   tests, cómo consultar `psql` y `mongosh`, cómo parar).
8. Opcional (hasta +1 punto): Redis para el detalle. Solo si todo lo demás está terminado.

Trampas conocidas de Mongo: las claves de un `Map` no pueden contener `.` ni empezar por `$`;
y `Instant` se guarda con precisión de milisegundos.

## Fase final (los tres)

1. Integrad ramas en `main`, `mvn spotless:apply`, `mvn verify` todo en verde.
2. Reiniciad la aplicación y comprobad que los datos siguen ahí (RF07).
3. Ensayo de defensa: cada uno explica **su** parte y también el diseño general. Preguntas probables:
   - A: ¿qué pasa si dos peticiones registran el mismo email a la vez? ¿Por qué UNIQUE además del `existsBy`?
   - B: ¿cómo evitas que se supere el aforo con dos inscripciones simultáneas? ¿Qué hace `FOR UPDATE`?
   - C: ¿por qué los seguimientos en MongoDB y no en `jsonb`? ¿Qué inconsistencia puede haber entre motores?
