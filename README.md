## Laboratorio #4 – REST API Blueprints (Java 21 / Spring Boot 3.3.x)
# Escuela Colombiana de Ingeniería – Arquitecturas de Software  

---

## 📋 Requisitos
- Java 21
- Maven 3.9+

## ▶️ Ejecución del proyecto
```bash
mvn clean install
mvn spring-boot:run
```
Por defecto la app corre sin perfil activo, usando persistencia **en memoria**. Todas las respuestas
vienen envueltas en `ApiResponse<T>` (`{ "code", "message", "data" }`) y la ruta base es `/api/v1/blueprints`.

Probar con `curl`:
```bash
curl -s http://localhost:8080/api/v1/blueprints | jq
curl -s http://localhost:8080/api/v1/blueprints/john | jq
curl -s http://localhost:8080/api/v1/blueprints/john/house | jq
curl -i -X POST http://localhost:8080/api/v1/blueprints -H 'Content-Type: application/json' -d '{ "author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}] }'
curl -i -X PUT  http://localhost:8080/api/v1/blueprints/john/kitchen/points -H 'Content-Type: application/json' -d '{ "x":3,"y":3 }'
```

> Para activar filtros de puntos usa los perfiles de Spring `redundancy` o `undersampling`
> (ver sección [Filtros de Blueprints](#5-filtros-de-blueprints)).
---

## 🐘 Persistencia en PostgreSQL

Por defecto la app usa `InMemoryBlueprintPersistence`. Para usar `PostgresBlueprintPersistence`
(misma interfaz `BlueprintPersistence`, sin cambios en servicios/controladores):

1. Levanta un PostgreSQL local con Docker Compose (incluido en el repo):
   ```bash
   docker compose up -d
   ```
   Esto crea una base `blueprints` con usuario/clave `blueprints`/`blueprints` en el puerto 5432.

2. Corre la aplicación con el perfil `postgres` activo:
   ```powershell
   # PowerShell
   mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
   ```
   ```bash
   # bash / Linux / macOS
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

   Las tablas (`blueprints`, `blueprint_points`) se crean automáticamente desde
   `src/main/resources/schema.sql` en cada arranque (sentencias `IF NOT EXISTS`, idempotentes).

3. Si tu base de datos no corre en `localhost:5432` con esas credenciales, sobreescribe con
   variables de entorno antes de arrancar: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   (ver `application-postgres.properties`).

4. Verifica los datos directamente en la base:
   ```bash
   docker exec -it blueprints-postgres psql -U blueprints -d blueprints -c "SELECT * FROM blueprints;"
   docker exec -it blueprints-postgres psql -U blueprints -d blueprints -c "SELECT * FROM blueprint_points;"
   ```
---

## 📦 Formato de respuesta uniforme (`ApiResponse<T>`)

Todos los endpoints devuelven el mismo sobre de respuesta:
```json
{ "code": 200, "message": "execute ok", "data": { "author": "john", "name": "house", "points": [...] } }
```

Códigos usados:
- `200 OK` — consultas exitosas (GET).
- `201 Created` — creación de blueprint (POST).
- `202 Accepted` — actualización de puntos (PUT).
- `400 Bad Request` — validación fallida o blueprint duplicado.
- `404 Not Found` — autor/blueprint inexistente.

Estos casos se manejan de forma centralizada en `ApiExceptionHandler`
(`@RestControllerAdvice`), evitando repetir `try/catch` en cada endpoint del controller.
---

Abrir en navegador:  
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)  

---

## 🗂️ Estructura de carpetas (arquitectura)

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         # Entidades de dominio: Blueprint, Point
  ├── dto/           # DTOs de entrada/salida: NewBlueprintRequest, ApiResponse<T>
  ├── persistence/   # Interfaz + excepciones (BlueprintPersistence, ...)
  │    └── impl/     # Implementaciones concretas: InMemory, Postgres
  ├── services/      # Lógica de negocio y orquestación
  ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
  ├── controllers/   # REST Controllers (BlueprintsAPIController)
  ├── web/           # Manejo centralizado de errores (ApiExceptionHandler)
  └── config/        # Configuración (Swagger/OpenAPI, etc.)
```

> Esta separación sigue el patrón **capas lógicas** (modelo, persistencia, servicios, controladores), facilitando la extensión hacia nuevas tecnologías o fuentes de datos.

---

## 📖 Actividades del laboratorio

### 1. Familiarización con el código base
- Revisa el paquete `model` con las clases `Blueprint` y `Point`.  
- Entiende la capa `persistence` con `InMemoryBlueprintPersistence`.  
- Analiza la capa `services` (`BlueprintsServices`) y el controlador `BlueprintsAPIController`.

### 2. Migración a persistencia en PostgreSQL
- Configura una base de datos PostgreSQL (puedes usar Docker).  
- Implementa un nuevo repositorio `PostgresBlueprintPersistence` que reemplace la versión en memoria.  
- Mantén el contrato de la interfaz `BlueprintPersistence`.  

### 3. Buenas prácticas de API REST
- Cambia el path base de los controladores a `/api/v1/blueprints`.  
- Usa **códigos HTTP** correctos:  
  - `200 OK` (consultas exitosas).  
  - `201 Created` (creación).  
  - `202 Accepted` (actualizaciones).  
  - `400 Bad Request` (datos inválidos).  
  - `404 Not Found` (recurso inexistente).  
- Implementa una clase genérica de respuesta uniforme:
  ```java
  public record ApiResponse<T>(int code, String message, T data) {}
  ```
  Ejemplo JSON:
  ```json
  {
    "code": 200,
    "message": "execute ok",
    "data": { "author": "john", "name": "house", "points": [...] }
  }
  ```

### 4. OpenAPI / Swagger
- Configura `springdoc-openapi` en el proyecto.  
- Expón documentación automática en `/swagger-ui.html`.  
- Anota endpoints con `@Operation` y `@ApiResponse`.

### 5. Filtros de *Blueprints*
- Implementa filtros:
  - **RedundancyFilter**: elimina puntos duplicados consecutivos.  
  - **UndersamplingFilter**: conserva 1 de cada 2 puntos.  
- Activa los filtros mediante perfiles de Spring (`redundancy`, `undersampling`).  

---

## ✅ Entregables

1. Repositorio en GitHub con:  
   - Código fuente actualizado.  
   - Configuración PostgreSQL (`application.yml` o script SQL).  
   - Swagger/OpenAPI habilitado.  
   - Clase `ApiResponse<T>` implementada.  

2. Documentación:  
   - Informe de laboratorio con instrucciones claras.  
   - Evidencia de consultas en Swagger UI y evidencia de mensajes en la base de datos.  
   - Breve explicación de buenas prácticas aplicadas.  

---

## 🖼️ Evidencias

| Evidencia | Captura |
|---|---|
| Swagger UI con los endpoints versionados (`/api/v1/blueprints`) y los schemas de `ApiResponse<T>` generados (`ApiResponseBlueprint`, `ApiResponseSetBlueprint`, `ApiResponseVoid`) | ![Swagger UI](docs/evidence/swagger-ui.png) |
| PostgreSQL levantado con `docker compose up -d` (contenedor `blueprints-postgres`) | ![docker compose up](docs/evidence/docker-compose-up.png) |

> Faltan por agregar: capturas de `curl -i` mostrando los códigos HTTP (200/201/202/400/404) y
> del `SELECT * FROM blueprints;` / `SELECT * FROM blueprint_points;` corriendo con el perfil
> `postgres` activo — guárdalas también en `docs/evidence/` y agrégalas a esta tabla.

---

## 📊 Criterios de evaluación

| Criterio | Peso |
|----------|------|
| Diseño de API (versionamiento, DTOs, ApiResponse) | 25% |
| Migración a PostgreSQL (repositorio y persistencia correcta) | 25% |
| Uso correcto de códigos HTTP y control de errores | 20% |
| Documentación con OpenAPI/Swagger + README | 15% |
| Pruebas básicas (unitarias o de integración) | 15% |

**Bonus**:  

- Imagen de contenedor (`spring-boot:build-image`) — ver Dockerfile incluido, o el plugin de Spring Boot.
- Métricas con Actuator — ya habilitado (`/actuator/health`, `/actuator/info`, `/actuator/metrics`).  