# MLflow Platform Integration

Platform-based integration of MLflow with TMF 915 AI Model Management API.

## Overview

This integration uses a **platform-based approach** where:
- **AiModelSpecification** represents the MLflow platform itself (single, static specification)
- **AiModel** instances represent individual registered models in MLflow

## MLflow Platform

[MLflow](https://mlflow.org/) is an open-source platform for managing the complete machine learning lifecycle, including:
- **Experiment Tracking**: Log parameters, metrics, and artifacts
- **Model Registry**: Centralized model store with versioning and stage transitions
- **Model Deployment**: Deploy models to various targets
- **Projects**: Packaging format for reproducible runs

## Architecture

### Components

1. **MLflowPlatformService** - Main service for MLflow integration
   - Creates/manages the platform specification
   - Converts MLflow registered models to TMF AiModel format
   - Provides model search and validation
   - Interfaces with MLflow REST API

2. **MLflowIntegrationExample** - Usage examples
   - Platform specification creation
   - Model import workflows
   - Listing and validation examples

### Key Features

- **Platform Specification**: 7 characteristics defining MLflow platform capabilities
- **Model Characteristics**: Up to 10+ characteristics per model (varies by metadata)
- **Model Versioning**: Supports specific versions or latest version
- **Stage Management**: Tracks model lifecycle stages (None, Staging, Production, Archived)
- **TMF Compliance**: Full alignment with TMF 915 specification fields

## Configuration

Add to `application.properties`:

```properties
# MLflow tracking server URI
mlflow.tracking.uri=http://localhost:5000
```

Default: `http://localhost:5000`

## Platform Specification Characteristics

| Characteristic | Type | Description |
|---------------|------|-------------|
| trackingUri | string | MLflow tracking server URI |
| apiVersion | string | MLflow REST API version |
| supportedFrameworks | array | ML frameworks (TensorFlow, PyTorch, etc.) |
| modelFlavors | array | Model serialization formats |
| deploymentTargets | array | Deployment options (local, SageMaker, etc.) |
| modelStages | array | Lifecycle stages |
| artifactStorage | array | Supported storage backends |

## Model Service Characteristics

| Characteristic | TMF Field | Description |
|---------------|-----------|-------------|
| modelName | - | Registered model name |
| modelVersion | - | Model version number |
| modelUri | - | Artifact storage location |
| runId | - | MLflow experiment run ID |
| experimentUrl | - | Link to experiment in MLflow UI |
| currentStage | - | Lifecycle stage (None/Staging/Production/Archived) |
| creationTimestamp | - | Model creation time |
| lastUpdatedTimestamp | - | Last update time |
| userId | - | User who created the model |
| tags | - | Model tags (key:value pairs) |
| modelDataSheet | modelDataSheet | MLflow model page URL |
| deploymentRecord | deploymentRecord | MLflow serving endpoint |

## Usage Examples

### 1. Create Platform Specification

```java
@Autowired
private MLflowPlatformService mlflowService;

AiModelSpecification platformSpec = mlflowService.getOrCreatePlatformSpecification();
```

### 2. Import a Specific Model Version

```java
String modelName = "fraud-detection-model";
String version = "3";
String baseUrl = "http://localhost:13082";

AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(modelName, version, baseUrl);
```

### 3. Import Latest Version

```java
// Pass null for version to get latest
AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(modelName, null, baseUrl);
```

### 4. List All Registered Models

```java
List<String> modelNames = mlflowService.listRegisteredModels();
modelNames.forEach(name -> System.out.println(name));
```

### 5. Validate Model Existence

```java
boolean exists = mlflowService.validateModelExists("my-model");
```

## MLflow REST API Endpoints Used

```
GET /api/2.0/mlflow/registered-models/list
GET /api/2.0/mlflow/registered-models/get?name={name}
GET /api/2.0/mlflow/model-versions/get?name={name}&version={version}
```

## Model Lifecycle Stages

MLflow supports four lifecycle stages:

1. **None** - Newly registered model
2. **Staging** - Model being tested/validated
3. **Production** - Model deployed to production
4. **Archived** - Model retired from active use

## Example Workflow

```java
@Autowired
private MLflowPlatformService mlflowService;

// 1. Create platform specification
AiModelSpecification platformSpec = mlflowService.getOrCreatePlatformSpecification();

// 2. List all models
List<String> models = mlflowService.listRegisteredModels();

// 3. Import a model
AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(
    "my-model", 
    "1",  // version
    "http://localhost:13082"
);

// 4. Access model characteristics
aiModel.getServiceCharacteristic().forEach(characteristic -> {
    System.out.println(characteristic.getName() + ": " + characteristic.getValue());
});
```

## MLflow UI Integration

Models in TMF API link to MLflow UI:

- **Model Page**: `{trackingUri}/#/models/{modelName}/versions/{version}`
- **Experiment**: `{trackingUri}/#/experiments/{runId}`

## Comparison with HuggingFace Integration

| Aspect | MLflow | HuggingFace |
|--------|--------|-------------|
| Focus | Internal ML lifecycle | External model hub |
| Models | Organization's models | Public/private community models |
| Versioning | Built-in versioning | Git-based versioning |
| Stages | None/Staging/Production/Archived | N/A |
| Artifacts | Custom storage backends | HuggingFace CDN |
| Deployment | Multiple targets | Inference API |

## Deployment Options

MLflow models can be deployed to:

- **Local**: Flask server on local machine
- **SageMaker**: AWS SageMaker endpoints
- **Azure ML**: Azure Machine Learning service
- **Kubernetes**: Kubernetes clusters
- **Databricks**: Databricks serving
- **MLflow Serving**: Built-in serving infrastructure

## Model Flavors

MLflow supports multiple model formats (flavors):

- `python_function` - Generic Python function
- `sklearn` - Scikit-learn models
- `tensorflow` - TensorFlow models
- `pytorch` - PyTorch models
- `keras` - Keras models
- `xgboost` - XGBoost models
- `lightgbm` - LightGBM models
- `spark` - Apache Spark MLlib models
- `onnx` - ONNX format
- `h2o` - H2O.ai models
- `fastai` - FastAI models

## Troubleshooting

### Connection Issues

If you get connection errors:
1. Verify MLflow tracking server is running: `mlflow server --host 0.0.0.0 --port 5000`
2. Check `mlflow.tracking.uri` configuration
3. Ensure network connectivity to tracking server

### Model Not Found

If a model is not found:
1. Verify model is registered: `mlflow models list`
2. Check model name spelling
3. Ensure you have access to the model

### No Models Listed

If no models appear:
1. Register a model first in MLflow
2. Check tracking server URI
3. Verify API version compatibility

## Future Enhancements

- Model artifact download support
- Experiment tracking integration
- Model comparison capabilities
- Stage transition tracking
- Metric history visualization
- Model signature integration

## References

- [MLflow Documentation](https://mlflow.org/docs/latest/index.html)
- [MLflow REST API](https://mlflow.org/docs/latest/rest-api.html)
- [MLflow Model Registry](https://mlflow.org/docs/latest/model-registry.html)
- [TMF 915 Specification](https://www.tmforum.org/resources/specification/tmf915-ai-ml-model-management-api/)
