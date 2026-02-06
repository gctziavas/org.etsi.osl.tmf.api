# TMF 915 AI Model Management API

Spring Boot implementation of the TMF 915 AI Model Management API specification.

## Overview

This implementation provides a comprehensive API for managing AI models according to TMF (TeleManagement Forum) standards. It includes:

- **TMF 915 AI Model Management** - Core API implementation
- **Platform Integrations** - MLflow integration
- **Model Lifecycle Management** - Creation, retrieval, update, and deletion of AI models
- **Artifact Management** - On-demand deployment artifact generation and download

## Documentation

### MLflow Integration

- See [integrations/mlflow/README.md](integrations/mlflow/README.md) for comprehensive MLflow integration guide
- See [docs/mlflow_to_tmf915_flow.puml](docs/mlflow_to_tmf915_flow.puml) for integration flow diagram
- See [docs/mlflow_to_tmf915_class_entity_mapping.puml](docs/mlflow_to_tmf915_class_entity_mapping.puml) for entity mapping diagram
- See [docs/mlflow_to_tmf915_field_mapping.puml](docs/mlflow_to_tmf915_field_mapping.puml) for field mapping details

## API Endpoints

### Base Path: `/AiM/v4/`


| Resource             | Endpoint                       | Description                       |
| -------------------- | ------------------------------ | --------------------------------- |
| AiModel              | `/AiM/v4/aiModel`              | AI Model instance management      |
| AiModelSpecification | `/AiM/v4/aiModelSpecification` | AI Model specification management |
| MLflow               | `/AiM/v4/mlflow/*`             | MLflow integration endpoints      |

## API Documentation

The underlying library integrating OpenAPI to Spring Boot is [springdoc](https://springdoc.org).

You can view the API documentation in swagger-ui:

- Swagger UI: `http://localhost:13082/swagger-ui.html`
- OpenAPI Spec: `http://localhost:13082/v3/api-docs/`

## Quick Start

### Configuration

Add to `application.yml`:

```yaml
# MLflow configuration
mlflow:
  enabled: true
  host: "127.0.0.1"
  port: 5000
```

### Running the Server

Start the server as a Spring Boot application:

```bash
mvn spring-boot:run
```
