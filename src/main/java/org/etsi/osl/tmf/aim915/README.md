# TMF 915 AI Model Management API

Spring Boot implementation of the TMF 915 AI Model Management API specification.

## Overview

This implementation provides a comprehensive API for managing AI models according to TMF (TeleManagement Forum) standards. It includes:

- **TMF 915 AI Model Management** - Core API implementation
- **Platform Integrations** - HuggingFace Hub integration with platform-based approach
- **Model Lifecycle Management** - Creation, retrieval, update, and deletion of AI models
- **Artifact Management** - On-demand deployment artifact generation and download

## Documentation

For detailed documentation on the HuggingFace integration:
- See [docs/README.md](docs/README.md) for comprehensive integration guide
- See [docs/huggingface-mapping.puml](docs/huggingface-mapping.puml) for architecture diagram
- See [docs/platform-specification.json](docs/platform-specification.json) for platform spec structure
- See [docs/example-aimodel.json](docs/example-aimodel.json) for example model instance

## API Documentation

The underlying library integrating OpenAPI to Spring Boot is [springdoc](https://springdoc.org).

You can view the API documentation in swagger-ui:
- Swagger UI: `http://localhost:13082/swagger-ui.html`
- OpenAPI Spec: `http://localhost:13082/v3/api-docs/`

## Quick Start

### Configuration

Add to `application.properties`:
```properties
# Optional: HuggingFace API token for private models
huggingface.api.token=your_token_here
```

### Running the Server

Start the server as a Spring Boot application:
```bash
mvn spring-boot:run
```

Default port: 13082 (configurable in application.properties)