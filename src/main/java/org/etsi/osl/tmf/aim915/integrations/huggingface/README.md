# Hugging Face Integration for TMF 915 AI Model Management

This module provides integration between Hugging Face Hub and the TMF 915 AI Model Management API.

## Architecture Overview

The integration follows a clear separation between **model specifications** (blueprints) and **model instances** (deployments):

```
┌─────────────────────────────────────────────────────────────────┐
│                      Hugging Face Hub                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ bert-base       │  │ openai/whisper  │  │ meta/llama      │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Import
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   AiModelSpecification                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Spec bert-base  │  │ Spec whisper    │  │ Spec llama      │  │
│  │ (Blueprint)     │  │ (Blueprint)     │  │ (Blueprint)     │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Instantiate/Deploy
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        AiModel                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Instance 1      │  │ Instance 2      │  │ Instance 3      │  │
│  │ (Running)       │  │ (Running)       │  │ (Designed)      │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

| Concept                 | TMF Entity             | Description                                                |
| ----------------------- | ---------------------- | ---------------------------------------------------------- |
| **Model on HF Hub**     | -                      | A model hosted on Hugging Face Hub                         |
| **Model Specification** | `AiModelSpecification` | Blueprint describing a model's capabilities and attributes |
| **Model Instance**      | `AiModel`              | A running/deployed instance of a model                     |

---

## REST API Endpoints

### Base Path: `/AiM/v4`

### Import Operations

| Method | Endpoint                                | Description                                 |
| ------ | --------------------------------------- | ------------------------------------------- |
| `POST` | `/huggingface/import/{modelId}`         | Import HF model as AiModelSpecification     |
| `POST` | `/huggingface/search-import`            | Search and import multiple models           |

### Deployment Operations

| Method | Endpoint                                    | Description                                |
| ------ | ------------------------------------------- | ------------------------------------------ |
| `POST` | `/huggingface/deploy/{modelId}`             | Deploy model (imports spec if needed)      |
| `POST` | `/huggingface/instantiate/{specificationId}`| Create AiModel from existing specification |

### Query Operations

| Method | Endpoint                                 | Description                          |
| ------ | ---------------------------------------- | ------------------------------------ |
| `GET`  | `/huggingface/search`                    | Search Hugging Face Hub              |
| `GET`  | `/huggingface/models/{modelId}/validate` | Check if model exists (with suggestions) |
| `GET`  | `/huggingface/models/{modelId}/files`    | List files in model repository       |
| `GET`  | `/huggingface/info`                      | Get connection info                  |

### Artifact Operations

| Method | Endpoint                                              | Description                       |
| ------ | ----------------------------------------------------- | --------------------------------- |
| `GET`  | `/huggingface/models/{modelId}/artifacts`             | Download specific file            |
| `GET`  | `/huggingface/models/{modelId}/artifacts/deployment.tar.gz` | Download deployment archive |
| `GET`  | `/aiModel/{modelId}/artifacts/huggingface`            | Download artifact by model ID     |
| `GET`  | `/aiModel/{modelId}/artifacts/huggingface/list`       | List available artifacts          |

### Example Requests

**Import a model:**

```bash
# Import a model (use underscore for slash in model IDs)
curl -X POST "http://localhost:13082/AiM/v4/huggingface/import/bert-base-uncased"

# Import a namespaced model
curl -X POST "http://localhost:13082/AiM/v4/huggingface/import/openai_whisper-large"
```

**Search and import models:**

```bash
curl -X POST "http://localhost:13082/AiM/v4/huggingface/search-import?query=text-classification&limit=5"
```

**Deploy a model:**

```bash
curl -X POST "http://localhost:13082/AiM/v4/huggingface/deploy/bert-base-uncased?endpoint=http://my-inference-server/predict"
```

**Validate model exists:**

```bash
curl "http://localhost:13082/AiM/v4/huggingface/models/bert-base-uncased/validate"
```

Response if found:
```json
{
  "modelId": "bert-base-uncased",
  "exists": true,
  "hubUrl": "https://huggingface.co/bert-base-uncased"
}
```

Response if not found (with suggestions):
```json
{
  "modelId": "bert-bse-uncased",
  "exists": false,
  "suggestions": ["bert-base-uncased", "bert-base-cased", "bert-large-uncased"],
  "message": "Model not found. See suggestions for similar models."
}
```

---

## Model ID Format

Hugging Face model IDs may contain slashes (e.g., `openai/whisper-large`). In URL paths, use underscore instead:

| Hugging Face ID | URL Path ID |
|-----------------|-------------|
| `bert-base-uncased` | `bert-base-uncased` |
| `openai/whisper-large` | `openai_whisper-large` |
| `meta-llama/Llama-2-7b` | `meta-llama_Llama-2-7b` |

---

## Configuration

Add to `application.yml`:

```yaml
huggingface:
  api:
    token: ""  # Optional: API token for private models
  download:
    timeout: 300000  # Download timeout in milliseconds (default: 5 min)
```

---

## Service Components

| Service                        | Responsibility                                                   |
| ------------------------------ | ---------------------------------------------------------------- |
| `HuggingFaceConfiguration`     | Spring beans and configuration                                   |
| `HuggingFaceClientService`     | Low-level Hugging Face Hub API operations                        |
| `HuggingFaceSpecificationService` | Converts HF models → AiModelSpecification                     |
| `HuggingFaceModelService`      | Creates AiModel instances                                        |
| `HuggingFaceIntegrationService`| High-level orchestration API                                     |
| `HuggingFaceApiController`     | REST API endpoints                                               |
| `HuggingFaceService`           | Legacy service for artifact downloads                            |

---

## References

- [Hugging Face Hub](https://huggingface.co/models)
- [Hugging Face Hub API](https://huggingface.co/docs/hub/api)
- [TMF 915 Specification](https://www.tmforum.org/resources/specification/tmf915-ai-ml-model-management-api/)
