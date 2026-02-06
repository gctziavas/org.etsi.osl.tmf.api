# TMF 915 AI Model Management API - HuggingFace Integration

This documentation covers the integration of HuggingFace Hub with the TMF 915 AI Model Management API.

## Overview

The HuggingFace integration uses a **platform-based approach** where:
- **AiModelSpecification** represents the HuggingFace Hub platform itself (single, static specification)
- **AiModel** instances represent individual models from HuggingFace (e.g., `mistralai/Mistral-7B-v0.1`, `gpt2`, `bert-base-uncased`)

This approach provides:
- ✅ Single specification for the entire platform - easier to manage
- ✅ Individual models as instances of the platform specification
- ✅ Scalable for thousands of models
- ✅ Clear separation between platform capabilities and model-specific attributes

## Architecture

### Components

1. **HuggingFacePlatformService** - Main service for HuggingFace integration
   - Creates/manages the platform specification
   - Converts HuggingFace models to TMF AiModel format
   - Provides model search and validation

2. **HuggingFacePlatformUtils** - Static utility class
   - Helper methods for characteristic management
   - JSON extraction utilities
   - URL builders for HuggingFace resources
   - File handling for deployment artifacts

3. **ArtifactController** - REST endpoint for deployment artifacts
   - Downloads deployment files on-demand from HuggingFace
   - Creates tar.gz archives in-memory (no disk storage)
   - Supports streaming for large models (13GB+)

### Key Features

- **Platform Specification**: 7 characteristics defining HuggingFace Hub capabilities
- **Model Characteristics**: 15-19 characteristics per model (varies by model metadata)
- **Deployment Artifacts**: On-demand tar.gz creation with essential files
- **TMF Compliance**: Full alignment with TMF 915 specification fields

## Documentation Files

- [README.md](README.md) - This file, overview and architecture
- [huggingface-mapping.puml](huggingface-mapping.puml) - PlantUML diagram of the mapping
- [platform-specification.json](platform-specification.json) - HuggingFace platform specification structure
- [example-aimodel.json](example-aimodel.json) - Example AI model instance (Mistral-7B)

## Quick Start

### 1. Configure API Token (Optional)

Add to `application.properties`:
```properties
huggingface.api.token=your_token_here
```

### 2. Platform Specification

The platform specification is created automatically on first use:

```java
AiModelSpecification platformSpec = huggingFacePlatformService.getOrCreatePlatformSpecification();
```

### 3. Import a Model

```java
String modelId = "mistralai/Mistral-7B-v0.1";
AiModelCreate aiModel = huggingFacePlatformService.huggingFaceModelToAiModelCreate(modelId, baseUrl);
```

### 4. Download Deployment Artifacts

```
GET /tmf-api/aim/v1/aiModel/artifacts/huggingface/{model-id-safe}/deployment.tar.gz
```

Example: `/tmf-api/aim/v1/aiModel/artifacts/huggingface/mistralai_Mistral-7B-v0.1/deployment.tar.gz`

## Field Mappings

### Platform Specification Characteristics (7)

| Characteristic | Type | Description |
|---------------|------|-------------|
| platformUrl | string | Base URL for HuggingFace Hub |
| apiUrl | string | API endpoint base URL |
| supportedModelTypes | array | List of supported model tasks |
| supportedLibraries | array | ML frameworks supported |
| licenseTypes | array | Available license types |
| accessTypes | array | Public/private/gated access |
| deploymentArtifactsInfo | string | Description of artifact downloads |

### Model Service Characteristics (15-19)

| Characteristic | TMF Field | Description |
|---------------|-----------|-------------|
| modelId | - | HuggingFace model identifier |
| repositoryUrl | - | Link to model repository |
| modelType | - | Task/pipeline tag |
| library | - | Framework used |
| author | - | Model creator/organization |
| lastModified | - | Last update timestamp |
| tags | - | Model tags/categories |
| totalSize | - | Size in bytes |
| totalSizeHuman | - | Human-readable size |
| languages | - | Supported languages (NLP) |
| modelDataSheet | modelDataSheet | Repository URL |
| inheritedModel | inheritedModel | Base model name |
| inheritedModelUrl | - | Base model URL |
| trainingData | trainingData | Training datasets |
| trainingDataUrl | - | Dataset URL |
| evaluationData | evaluationData | Evaluation datasets |
| deploymentRecord | deploymentRecord | Inference API URL |
| deploymentArtifactsUrl | - | Download URL for tar.gz |

## Deployment Artifacts

The deployment artifacts endpoint provides all essential files needed to deploy a model:

**Essential Files Included:**
- `config.json` - Model configuration
- `tokenizer_config.json` - Tokenizer settings
- `tokenizer.json` - Tokenizer vocabulary
- `special_tokens_map.json` - Special tokens
- `vocab.txt` / `vocab.json` - Vocabulary files
- `merges.txt` - BPE merges (if applicable)
- `*.safetensors` - Model weights
- `*.bin` - PyTorch model files
- `*.onnx` - ONNX format files
- `*.msgpack` - TensorFlow.js files

**Archive Format:**
- Proper POSIX tar format (Apache Commons Compress)
- GZIP compression
- Long filename support enabled
- Created in-memory, no disk storage

## API Examples

### Search Models

```bash
curl "http://localhost:13082/tmf-api/aim/v1/huggingface/models?search=mistral&limit=10"
```

### Get Model Info

```bash
curl "http://localhost:13082/tmf-api/aim/v1/huggingface/models/mistralai/Mistral-7B-v0.1"
```

### Validate Model

```bash
curl "http://localhost:13082/tmf-api/aim/v1/huggingface/validate/gpt2"
```

### Download Deployment Artifacts

```bash
curl -O "http://localhost:13082/tmf-api/aim/v1/aiModel/artifacts/huggingface/mistralai_Mistral-7B-v0.1/deployment.tar.gz"
```

## Code Structure

```
org.etsi.osl.tmf.aim915.integrations.huggingface/
├── HuggingFacePlatformService.java      # Main service
├── HuggingFacePlatformUtils.java        # Utility helpers
└── HuggingFaceIntegrationExample.java   # Usage examples

org.etsi.osl.tmf.aim915.api/
└── ArtifactController.java              # Artifact download endpoint

org.etsi.osl.tmf.aim915.docs/
├── README.md                            # This file
├── huggingface-mapping.puml             # Architecture diagram
├── platform-specification.json          # Platform spec structure
└── example-aimodel.json                 # Example model instance
```

## Notes

- **No Disk Storage**: Artifacts are created on-demand in memory
- **Streaming Support**: Large models use streaming to avoid memory issues
- **Static Utilities**: All helpers in `HuggingFacePlatformUtils` for reusability
- **TMF Compliance**: Adheres to TMF 915 specification terminology

## Future Enhancements

- Model versioning support
- Caching for frequently accessed models
- Batch import capabilities
- Model metrics and statistics
- Advanced search filters
