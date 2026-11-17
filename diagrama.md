// Diagrama

    DEPARTAMENTO {
    bigint id PK
    varchar nombre UK
    }

    DOCENTE {
        id PK
        varchar nombre
        varchar apellidos
        varchar email UK
        long departamento_id FK
    }

    GRUPO {
        id PK
        varchar nombre
        varchar nivel
        long tutor_docente_id FK
    }

    ESTUDIANTE {
        id PK
        varchar nombre
        varchar apellidos
        varchar email UK
        date fecha_nacimiento
        long grupo_id FK
    }

    CONTACTO_ESTUDIANTE {
        id PK
        long estudiante_id FK_UK
        varchar tutor_legal
        varchar telefono
        varchar email
        boolean autorizado
        date fecha_autorizacion
    }

    ACTIVIDAD {
        id PK
        varchar titulo
        text descripcion
        varchar tipo
        date fecha_prevista
        int max_participantes
        long docente_id FK
    }

    INSCRIPCION {
        long id PK
        long actividad_id FK
        long estudiante_id FK
        datetime fecha_inscripcion
    }

    ETIQUETA {
        id PK
        varchar nombre UK
    }

    ACTIVIDAD_ETIQUETA {
        long actividad_id PK, FK
        long etiqueta_id PK, FK
    }

    DEPARTAMENTO 1 -- M DOCENTE : agrupa
    DOCENTE 1--M GRUPO : tutoriza
    DOCENTE 1--M ACTIVIDAD : organiza
    GRUPO 1--M ESTUDIANTE : contiene
    ESTUDIANTE 1--M CONTACTO_ESTUDIANTE : tiene
    ESTUDIANTE 1--M INSCRIPCION : realiza
    ACTIVIDAD 1--M INSCRIPCION : recibe
    ACTIVIDAD 1--M ACTIVIDAD_ETIQUETA : clasifica
    ETIQUETA 1--M ACTIVIDAD_ETIQUETA : asignada