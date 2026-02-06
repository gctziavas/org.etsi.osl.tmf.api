# MLflow Integration for TMF 915 AI Model Management

This module provides integration between MLflow and the TMF 915 AI Model Management API.

## Architecture Overview

The integration follows a clear separation between **model specifications** (blueprints) and **model instances** (deployments):

```
┌─────────────────────────────────────────────────────────────────┐
│                        MLflow Registry                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Model A v1  │  │ Model A v2  │  │ Model B v1  │  ...         │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Import
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   AiModelSpecification                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Spec A v1   │  │ Spec A v2   │  │ Spec B v1   │  ...         │
│  │ (Blueprint) │  │ (Blueprint) │  │ (Blueprint) │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Instantiate/Deploy
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        AiModel                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Instance 1  │  │ Instance 2  │  │ Instance 3  │  ...         │
│  │ (Running)   │  │ (Running)   │  │ (Designed)  │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```


| Concept                 | TMF Entity             | Description                                                |
| ----------------------- | ---------------------- | ---------------------------------------------------------- |
| **Model in MLflow**     | -                      | A registered model with versions, artifacts, and metadata  |
| **Model Specification** | `AiModelSpecification` | Blueprint describing a model's capabilities and attributes |
| **Model Instance**      | `AiModel`              | A running/deployed instance of a model                     |

---

## REST API Endpoints

### Import & Sync Operations


| Method | Endpoint                                  | Description                                 |
| ------ | ----------------------------------------- | ------------------------------------------- |
| `POST` | `/AiM/v4/mlflow/import/{modelName}`       | Import MLflow model as AiModelSpecification |
| `POST` | `/AiM/v4/mlflow/sync`                     | Sync all MLflow models to specifications    |

### Deployment Operations


| Method | Endpoint                                      | Description                                |
| ------ | --------------------------------------------- | ------------------------------------------ |
| `POST` | `/AiM/v4/mlflow/deploy/{modelName}`           | Deploy using MLflow built-in serving       |
| `POST` | `/AiM/v4/mlflow/instantiate/{specificationId}`| Create AiModel with external inference URL |

### Query Operations


| Method | Endpoint                                       | Description                          |
| ------ | ---------------------------------------------- | ------------------------------------ |
| `GET`  | `/AiM/v4/mlflow/models`                        | List all registered models in MLflow |
| `GET`  | `/AiM/v4/mlflow/models/{modelName}/exists`     | Check if model exists                |
| `GET`  | `/AiM/v4/mlflow/models/{modelName}/artifacts`  | List model artifacts                 |
| `GET`  | `/AiM/v4/mlflow/info`                          | Get MLflow connection info           |

### Artifact Operations


| Method | Endpoint                                                  | Description                                 |
| ------ | --------------------------------------------------------- | ------------------------------------------- |
| `GET`  | `/AiM/v4/aiModel/{modelId}/artifacts/mlflow/{artifactType}` | Download artifact (model, training_data, etc.) |

### Example Requests

**Import a model:**

```bash
# Import specific version
curl -X POST "http://localhost:13082/AiM/v4/mlflow/import/fraud-detector?version=3"

# Import latest version
curl -X POST "http://localhost:13082/AiM/v4/mlflow/import/fraud-detector"
```

**Deploy a model using MLflow built-in serving:**

```bash
# Deploy with endpoint
curl -X POST "http://localhost:13082/AiM/v4/mlflow/deploy/fraud-detector?version=3&endpoint=http://localhost:5001/invocations"

# Deploy without endpoint
curl -X POST "http://localhost:13082/AiM/v4/mlflow/deploy/fraud-detector?version=3"
```

Response:
```json
{
  "id": "abc123",
  "name": "fraud-detector v3",
  "state": "ACTIVE",
  "serviceCharacteristic": [
    {"name": "inferenceUrl", "value": "http://localhost:5001/invocations"}
  ]
}
```

**Create instance with external inference URL:**

```bash
curl -X POST "http://localhost:13082/AiM/v4/mlflow/instantiate/{specificationId}?name=fraud-detector-prod&endpoint=http://fraud-detector.ml.svc.cluster.local:8080/v1/models/fraud-detector:predict"
```

**Call predictions on deployed model:**

```bash
curl -X POST "http://localhost:5001/invocations" \
  -H "Content-Type: application/json" \
  -d '{"inputs": [[1.0, 2.0, 3.0, 4.0]]}'
```

---

## MLflow to TMF 915 Mapping

### MLflow → AiModelSpecification

When importing a model from MLflow, the following mapping is applied:


| MLflow Source                                              | AiModelSpecification Field                 | Notes                               |
| ---------------------------------------------------------- | ------------------------------------------ | ----------------------------------- |
| `RegisteredModel.name`                                     | `name`                                     | Model name                          |
| `ModelVersion.version`                                     | `version`                                  | Version number                      |
| `RegisteredModel.description` / `ModelVersion.description` | `description`                              | Falls back to version description   |
| `ModelVersion.status`                                      | `specCharacteristic[lifecycleStatus]`      | PENDING_REGISTRATION, READY, etc.   |
| `ModelVersion.current_stage`                               | `specCharacteristic[stage]`                | None, Staging, Production, Archived |
| `ModelVersion.run_id`                                      | `specCharacteristic[runId]`                | Link to training run                |
| `ModelVersion.source`                                      | `specCharacteristic[artifactUri]`          | Model artifact location             |
| `ModelVersion.user_id`                                     | `specCharacteristic[createdBy]`            | Creator                             |
| `ModelVersion.creation_timestamp`                          | `specCharacteristic[creationTimestamp]`    | Creation time                       |
| `ModelVersion.last_updated_timestamp`                      | `specCharacteristic[lastUpdatedTimestamp]` | Last update time                    |
| `ModelVersion.tags[]`                                      | `specCharacteristic[tags]`                 | Concatenated key=value pairs        |
| `RegisteredModel.tags[]`                                   | `specCharacteristic[tags]`                 | Merged with version tags            |
| MLflow UI URL                                              | `specCharacteristic[mlflowUrl]`            | Link to MLflow UI                   |

**From Run Data (if available):**


| MLflow Source                         | AiModelSpecification Field      | Notes                              |
| ------------------------------------- | ------------------------------- | ---------------------------------- |
| `Run.data.tags["mlflow.runName"]`     | `specCharacteristic[runName]`   | Run name                           |
| `Run.data.tags["mlflow.log-model.*"]` | `specCharacteristic[modelType]` | sklearn, pytorch, tensorflow, etc. |
| `Run.data.params[]`                   | `specCharacteristic[param_*]`   | Important hyperparameters          |
| `Run.data.metrics[]`                  | `specCharacteristic[metric_*]`  | Training/eval metrics              |

**Artifact URLs (TMF 915 Standard Fields):**


| Artifact Path                                | AiModelSpecification Field | Description                  |
| -------------------------------------------- | -------------------------- | ---------------------------- |
| `model_data_sheet`, `datasheet`, `README.md` | `modelDataSheet`           | Model card/documentation     |
| `deployment_record`, `deployment.yaml`       | `deploymentRecord`         | Deployment configuration     |
| `base_model`, `inherited_model`              | `inheritedModel`           | Parent/foundation model      |
| `training_data`, `data/train`                | `modelTrainingData`        | Training dataset reference   |
| `evaluation_data`, `data/test`               | `modelEvaluationData`      | Evaluation dataset reference |

### MLflow → AiModel

When deploying/instantiating a model, the following mapping is applied:


| Source                     | AiModel Field          | Notes                                           |
| -------------------------- | ---------------------- | ----------------------------------------------- |
| AiModelSpecification       | `aiModelSpecification` | Reference to blueprint (contains all metadata)  |
| `{modelName} v{version}`   | `name`                 | Instance name with auto minor version (v1, v1.1)|
| `ModelVersion.description` | `description`          | Instance description                            |
| Always                     | `state`                | Always ACTIVE (AiModel = live deployment)       |

**Characteristics:**

AiModel stores deployment configuration. All model metadata (metrics, hyperparameters, 
artifact URLs, etc.) is accessed through the referenced `AiModelSpecification`.

| Category            | Characteristic     | Description                                    |
| ------------------- | ------------------ | ---------------------------------------------- |
| **Core**            | `platform`         | Always "mlflow"                                |
|                     | `endpoint`         | Inference API URL                              |
|                     | `deploymentTarget` | LOCAL or REMOTE                                |
|                     | `deployedAt`       | ISO timestamp of deployment                    |
| **Resources**       | `resources`        | JSON object with requests/limits (from config) |
| **Environment**     | `environment`      | JSON object with env vars (from config)        |
| **Remote Only**     | `remoteUrl`        | Remote MLflow server URL                       |
|                     | `endpointName`     | Endpoint name on remote server                 |

**Example AiModel characteristics:**

```json
{
  "serviceCharacteristic": [
    {"name": "platform", "value": "mlflow", "valueType": "string"},
    {"name": "endpoint", "value": "http://localhost:5001/invocations", "valueType": "string"},
    {"name": "deploymentTarget", "value": "LOCAL", "valueType": "string"},
    {"name": "deployedAt", "value": "2026-01-23T10:30:00Z", "valueType": "string"},
    {"name": "resources", "valueType": "object", "value": {
      "requests": {"cpu": "500m", "memory": "1Gi"},
      "limits": {"cpu": "2", "memory": "4Gi", "nvidia.com/gpu": "1"}
    }},
    {"name": "environment", "valueType": "object", "value": {
      "MODEL_BATCH_SIZE": "16",
      "INFERENCE_TIMEOUT_MS": "5000"
    }}
  ]
}
```

**Design Rationale:**

- **AiModelSpecification** = Blueprint describing what a model can do (immutable metadata)
- **AiModel** = Live deployment with configuration (platform, resources, environment)

This separation ensures:
1. No duplicate metadata between specification and instance
2. Multiple instances can share the same specification
3. Clear distinction between "what" (specification) and "where/how" (deployment)

---

## Deployment Configuration

Resources and environment are configured in `application.yaml`:

```yaml
mlflow:
  deployment:
    # Resource requests and limits (optional, defaults to N/A)
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"
      limits:
        cpu: "2"
        memory: "4Gi"
        gpu: "1"                           # nvidia.com/gpu
    
    # Environment variables for serving (optional)
    # Format: comma-separated KEY=value pairs
    environment: "MODEL_BATCH_SIZE=16,INFERENCE_TIMEOUT_MS=5000"
```

**Note:** MLflow does not return runtime environment variables from its API. 
Environment configuration is read from `application.yaml` and stored in the AiModel 
for documentation/reference purposes.


| Field                       | Description                                      |
| --------------------------- | ------------------------------------------------ |
| `resources.requests.cpu`    | Minimum CPU guaranteed (default: N/A)            |
| `resources.requests.memory` | Minimum memory guaranteed (default: N/A)         |
| `resources.limits.cpu`      | Maximum CPU limit (default: N/A)                 |
| `resources.limits.memory`   | Maximum memory limit (default: N/A)              |
| `resources.limits.gpu`      | GPU count (default: N/A, omitted if not set)     |
| `environment`               | Runtime env vars as KEY=value,KEY=value          |

---

## Model Lifecycle

```
MLflow Model Registration
         │
         ▼
   ┌─────────────┐
   │   Import    │  POST /mlflow/import/{name}
   └──────┬──────┘
          │
          ▼
┌───────────────────┐
│ AiModelSpec       │  (Blueprint - describes capabilities)
│ state: n/a        │
└─────────┬─────────┘
          │
          ▼
   ┌─────────────┐
   │   Deploy    │  POST /mlflow/deploy/{name}
   └──────┬──────┘  (MLflow built-in serving)
          │
          ▼
┌───────────────────┐
│ AiModel           │
│ state: ACTIVE     │
│ inferenceUrl set  │
└───────────────────┘
```

---

## Configuration

Add to `application.yml`:

```yaml
mlflow:
  enabled: true
  host: "127.0.0.1"
  port: 5000
  tracking-uri: ""  # Optional, defaults to http://{host}:{port}
  deployment:
    host: "0.0.0.0"                    # Host to bind serving
    startup-timeout-seconds: 60        # Max wait for server startup
    health-check-interval-ms: 1000     # Health check polling interval

aimodel:
  specification:
    default-lifecycle-status: Active
```

---

## Built-in Model Serving

The integration supports deploying models using MLflow's built-in `mlflow models serve` command.

### How it works

1. **Deploy** - Starts an MLflow model server process
2. **Serve** - Model is available at `http://localhost:{port}/invocations`
3. **Record** - Creates an `AiModel` with the `inferenceUrl`
4. **Stop** - Terminates the serving process

### Requirements

- MLflow CLI must be installed (`pip install mlflow`)
- Python environment with model dependencies
- MLflow tracking server accessible

### Inference API

Once deployed, the model accepts predictions at:

```
POST http://localhost:{port}/invocations
Content-Type: application/json

{
  "inputs": [[1.0, 2.0, 3.0, 4.0]]
}
```

Or for DataFrame-oriented input:

```json
{
  "dataframe_split": {
    "columns": ["feature1", "feature2", "feature3"],
    "data": [[1.0, 2.0, 3.0], [4.0, 5.0, 6.0]]
  }
}
```

---

## Remote Deployment

The integration supports deploying models to local or remote MLflow servers:

| Target | Description |
|--------|-------------|
| `LOCAL` | Local `mlflow models serve` process |
| `REMOTE` | Remote MLflow server with serving capabilities |

### Local Deployment

Deploy locally using `mlflow models serve`:

```bash
# Deploy with endpoint
curl -X POST "http://localhost:13082/AiM/v4/mlflow/deploy/fraud-detector?version=3&endpoint=http://localhost:5001/invocations"

# Deploy without endpoint
curl -X POST "http://localhost:13082/AiM/v4/mlflow/deploy/fraud-detector?version=3"
```

### Remote MLflow Server

Deploy to a remote MLflow instance with model serving capabilities:

```bash
curl -X POST "http://localhost:13082/AiM/v4/mlflow/deploy/fraud-detector?version=3&endpoint=https://mlflow.example.com/invocations"
```

**Requirements for Remote Deployment:**
- Remote MLflow server must have model serving enabled
- Network access to the remote server
- Valid authentication token (if required by the server)

---

## Configuration

Add to `application.yml`:

```yaml
mlflow:
  enabled: true
  host: "127.0.0.1"                     # MLflow tracking server host
  port: 5000                            # MLflow tracking server port
  tracking-uri: ""                      # Full URI (overrides host:port)
  
  deployment:
    host: "0.0.0.0"                     # Local serving bind host
    startup-timeout-seconds: 60         # Max wait for server startup
    health-check-interval-ms: 1000      # Health check polling interval

aimodel:
  specification:
    default-lifecycle-status: Active
```

---

## Java API

```java
@Autowired
private MlflowIntegrationService mlflowService;

@Autowired
private MlflowModelService modelService;

@Autowired
private MlflowDeploymentService deploymentService;

// Import model as specification
AiModelSpecification spec = mlflowService.importModelAsSpecification("fraud-detector", "3");

// ===== LOCAL DEPLOYMENT =====

// Deploy using MLflow built-in serving (auto port)
AiModelCreate model = modelService.deployAndCreateModel(spec, "fraud-detector", "3");

// Deploy on specific port
AiModelCreate model = modelService.deployAndCreateModel(spec, "fraud-detector", "3", 5001);

// ===== REMOTE MLFLOW =====

// Deploy to remote MLflow server
AiModelCreate model = modelService.deployToRemote(
    spec, "fraud-detector", "3",
    "https://mlflow.example.com",
    "auth-token"
);

// Deploy with custom endpoint name
AiModelCreate model = modelService.deployToRemote(
    spec, "fraud-detector", "3",
    "https://mlflow.example.com",
    "auth-token",
    "fraud-detector-prod"
);

// ===== MANAGEMENT =====

// Check if deployed
boolean running = deploymentService.isDeployed("fraud-detector", "3");

// Get deployment info
DeploymentInfo info = deploymentService.getDeployment("fraud-detector", "3");
String url = info.getInferenceUrl();
boolean isLocal = info.isLocal();
boolean isRemote = info.isRemote();

// Stop deployment (local or remote)
modelService.stopDeployment("fraud-detector", "3");

// List all deployments
Map<String, DeploymentInfo> deployments = deploymentService.getRunningDeployments();
```

---

## Service Components


| Service                      | Responsibility                                                         |
| ---------------------------- | ---------------------------------------------------------------------- |
| `MlflowConfiguration`        | Spring beans for MLflow client                                         |
| `MlflowClientService`        | Low-level MLflow API operations (models, runs, experiments, artifacts) |
| `MlflowDeploymentService`    | Manages local and remote MLflow deployments                            |
| `MlflowSpecificationService` | Converts MLflow models → AiModelSpecification                          |
| `MlflowModelService`         | Creates AiModel instances, orchestrates deployments                    |
| `MlflowIntegrationService`   | High-level orchestration API                                           |
| `MlflowApiController`        | REST API endpoints                                                     |

---

## References

- [MLflow Documentation](https://mlflow.org/docs/latest/index.html)
- [MLflow Java API](https://mlflow.org/docs/latest/api_reference/java_api/org/mlflow/tracking/MlflowClient.html)
- [MLflow Model Registry](https://mlflow.org/docs/latest/model-registry.html)
- [TMF 915 Specification](https://www.tmforum.org/resources/specification/tmf915-ai-ml-model-management-api/)
