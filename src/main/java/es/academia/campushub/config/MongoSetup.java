package es.academia.campushub.config;

import es.academia.campushub.document.SeguimientoDocument;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Component;

/**
 * PERSONA C: crea al arrancar la colección con validación mínima ($jsonSchema) y sus índices.
 * Spring Data NO crea los índices por defecto, por eso se hace aquí de forma explícita.
 */
@Component
public class MongoSetup implements ApplicationRunner {
  private final MongoTemplate mongo;

  public MongoSetup(MongoTemplate mongo) {
    this.mongo = mongo;
  }

  @Override
  public void run(ApplicationArguments args) {
    Document schema =
        Document.parse(
            """
            { "$jsonSchema": {
                "bsonType": "object",
                "required": ["actividadId", "creadoEn"],
                "properties": {
                  "actividadId": { "bsonType": ["int", "long"] },
                  "tipo":        { "bsonType": "string", "minLength": 1 },
                  "datos":       { "bsonType": "object" },
                  "creadoEn":    { "bsonType": "date" }
                },
                "additionalProperties": true
              } }
            """);
    if (!mongo.collectionExists(SeguimientoDocument.class)) {
      mongo.createCollection(
          SeguimientoDocument.class,
          CollectionOptions.empty().validator(Validator.document(schema)));
    } else {
      mongo.executeCommand(
          new Document("collMod", "seguimientos")
              .append("validator", schema)
              .append("validationLevel", "moderate")
              .append("validationAction", "error"));
    }
    // Consulta típica: "seguimientos de una actividad, en orden cronológico".
    mongo
        .indexOps(SeguimientoDocument.class)
        .createIndex(
            new Index()
                .on("actividadId", Sort.Direction.ASC)
                .on("creadoEn", Sort.Direction.ASC)
                .named("idx_actividad_fecha"));
  }
}
