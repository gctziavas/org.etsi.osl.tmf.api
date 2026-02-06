# TMF 915 AI Model Management API

Spring Boot implementation of the TMF 915 AI Model Management API specification.

## Overview

This implementation provides a comprehensive API for managing AI models according to TMF (TeleManagement Forum) standards. It includes:

- **TMF 915 AI Model Management** - Core API implementation
- **Platform Integrations** - MLflow client services with Docker deployment
- **Model Lifecycle Management** - Creation, retrieval, update, and deletion of AI models
- **Artifact Management** - On-demand deployment artifact generation and download
- **Remote Docker Deployment** - Deploy MLflow models as Docker containers

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        MLflow Registry                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Model A v1  │  │ Model A v2  │  │ Model B v1  │  ...         │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Import (via MlflowIntegrationService)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   AiModelSpecification                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Spec A v1   │  │ Spec A v2   │  │ Spec B v1   │  ...         │
│  │ (Blueprint) │  │ (Blueprint) │  │ (Blueprint) │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Deploy (via Docker) or Register External
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        AiModel                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Instance 1  │  │ Instance 2  │  │ Instance 3  │  ...         │
│  │ (Docker)    │  │ (External)  │  │ (Docker)    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

## Documentation

### MLflow Integration

- See [integrations/mlflow/README.md](integrations/mlflow/README.md) for MLflow client services documentation
- See [docs/mlflow_to_tmf915_flow.puml](docs/mlflow_to_tmf915_flow.puml) for integration flow diagram
- See [docs/mlflow_to_tmf915_class_entity_mapping.puml](docs/mlflow_to_tmf915_class_entity_mapping.puml) for entity mapping diagram
- See [docs/mlflow_to_tmf915_field_mapping.puml](docs/mlflow_to_tmf915_field_mapping.puml) for field mapping details

## API Endpoints

### Base Path: `/AiM/v4/`

| Resource                  | Endpoint                            | Description                        |
| ------------------------- | ----------------------------------- | ---------------------------------- |
| AiModel                   | `/AiM/v4/aiModel`                   | AI Model instance management       |
| AiModelSpecification      | `/AiM/v4/aiModelSpecification`      | AI Model specification management  |
| AiContract                | `/AiM/v4/aiContract`                | AI Contract management             |
| AiContractSpecification   | `/AiM/v4/aiContractSpecification`   | AI Contract specification          |
| AiContractViolation       | `/AiM/v4/aiContractViolation`       | AI Contract violation tracking     |
| Alarm                     | `/AiM/v4/alarm`                     | Alarm management                   |
| Monitor                   | `/AiM/v4/monitor`                   | Monitoring endpoints               |
| Rule                      | `/AiM/v4/rule`                      | Rule management                    |
| Topic                     | `/AiM/v4/topic`                     | Topic management                   |
| Hub                       | `/AiM/v4/hub`                       | Event subscription hub             |

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
  
  # Docker deployment configuration (optional)
  docker:
    enabled: true
    host: "docker-host.example.com"    # Remote Docker host
    port: 2375                          # Docker API port
    tls-enabled: false                  # Enable for secure connections
    image: "ghcr.io/mlflow/mlflow:v2.10.0"
    container-port: 5001                # Starting port for containers
    startup-timeout-seconds: 120        # Max wait for container readiness

# AI Model defaults
aimodel:
  specification:
    default-lifecycle-status: Active
```

### Running the Server

Start the server as a Spring Boot application:

```bash
mvn spring-boot:run
```

## Key Concepts

| Concept                 | TMF Entity             | Description                                                |
| ----------------------- | ---------------------- | ---------------------------------------------------------- |
| **Model in MLflow**     | -                      | A registered model with versions, artifacts, and metadata  |
| **Model Specification** | `AiModelSpecification` | Blueprint describing a model's capabilities and attributes |
| **Model Instance**      | `AiModel`              | A running/deployed instance of a model                     |
