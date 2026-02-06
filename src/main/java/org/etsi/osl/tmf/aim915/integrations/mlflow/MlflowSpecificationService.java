package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.*;
import org.mlflow.api.proto.ModelRegistry.ModelVersion;
import org.mlflow.api.proto.ModelRegistry.ModelVersionTag;
import org.mlflow.api.proto.ModelRegistry.RegisteredModel;
import org.mlflow.api.proto.ModelRegistry.RegisteredModelTag;
import org.mlflow.api.proto.Service.Run;
import org.mlflow.api.proto.Service.RunData;
import org.mlflow.api.proto.Service.Param;
import org.mlflow.api.proto.Service.RunTag;
import org.mlflow.api.proto.Service.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for converting MLflow models to TMF 915 AiModelSpecification.
 * 
 * In this design:
 * - AiModelSpecification describes a model saved in MLflow (the blueprint/template)
 * - Each registered model version in MLflow maps to one AiModelSpecification
 * - The specification captures the model's metadata, capabilities, and artifact locations
 * 
 * When a model is deployed/served, it becomes an AiModel instance.
 */
@Service
public class MlflowSpecificationService {

    private static final Logger log = LoggerFactory.getLogger(MlflowSpecificationService.class);

    private static final String[] DATASHEET_PATHS = {"model_data_sheet", "datasheet", "model_card", "model_datasheet", "README.md"};
    private static final String[] DEPLOYMENT_PATHS = {"deployment_record", "deployment", "deployment.yaml", "deployment.json", "deployment_config"};
    private static final String[] INHERITED_MODEL_PATHS = {"base_model", "inherited_model", "parent_model", "foundation_model"};
    private static final String[] TRAINING_DATA_PATHS = {"training_data", "train_data", "data/train"};
    private static final String[] EVALUATION_DATA_PATHS = {"evaluation_data", "eval_data", "test_data", "data/test"};

    private final MlflowClientService mlflowClient;

    @Value("${aimodel.specification.default-lifecycle-status:Active}")
    private String defaultLifecycleStatus;

    public MlflowSpecificationService(MlflowClientService mlflowClient) {
        this.mlflowClient = mlflowClient;
    }

    /**
     * Creates an AiModelSpecificationCreate from an MLflow registered model version.
     * 
     * This specification describes a model stored in MLflow, including:
     * - Model identity (name, version, description)
     * - Framework and type information
     * - Artifact URLs (model files, data, documentation)
     * - Training and evaluation metrics
     * 
     * @param modelName The MLflow registered model name
     * @param version The model version (null for latest)
     * @param baseUrl The base URL for the TMF API (for artifact endpoints)
     * @return AiModelSpecificationCreate ready to be persisted
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelSpecificationCreate createSpecificationFromMlflow(String modelName, String version, String baseUrl) throws IOException {
        log.info("Creating AiModelSpecification from MLflow model: {} v{}", modelName, version);

        RegisteredModel registeredModel = mlflowClient.getRegisteredModel(modelName);
        ModelVersion modelVersion;
        
        String resolvedVersion = version;
        if (resolvedVersion != null) {
            List<org.mlflow.api.proto.ModelRegistry.ModelVersion> versions = mlflowClient.getModelVersions(modelName);
            final String versionToFind = resolvedVersion;
            modelVersion = versions.stream()
                    .filter(v -> v.getVersion().equals(versionToFind))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Version " + versionToFind + " not found for model " + modelName));
        } else {
            modelVersion = mlflowClient.getLatestModelVersion(modelName);
            if (modelVersion == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = modelVersion.getVersion();
        }

        return buildSpecificationCreate(modelVersion, registeredModel, baseUrl);
    }

    /**
     * Builds the AiModelSpecificationCreate object from MLflow data.
     */
    private AiModelSpecificationCreate buildSpecificationCreate(ModelVersion modelVersion, RegisteredModel registeredModel, String baseUrl) {
        
        AiModelSpecificationCreate spec = new AiModelSpecificationCreate();

        String modelName = modelVersion.getName();
        String version = modelVersion.getVersion();
        String modelId = modelName + "_v" + version;
        Run run = null;
        if (modelVersion.getRunId() != null && !modelVersion.getRunId().isEmpty()) {
            run = mlflowClient.getRun(modelVersion.getRunId());
        }
        String description = null;
        if (modelVersion.getDescription() != null && !modelVersion.getDescription().isEmpty()) {
            description = modelVersion.getDescription();
        }
        else if (registeredModel.getDescription() != null && !registeredModel.getDescription().isEmpty()) {
            description = registeredModel.getDescription();
        }
        else {
            description = "MLflow model: " + modelVersion.getName() + " v" + modelVersion.getVersion();
        }

        // Basic identity
        spec.setName(modelName);
        spec.setVersion(version);
        spec.setDescription(description);

        // Add specification characteristics
        addMlFlowCharacteristics(spec, modelVersion);
        addFrameworkCharacteristics(spec, modelVersion, run);
        addMetricsCharacteristics(spec, run);
        addTagsCharacteristics(spec, modelVersion, registeredModel);

        // Set artifact URLs (TMF 915 standard fields)
        setArtifactUrls(spec, modelVersion, run, baseUrl, modelId);

        log.info("Created AiModelSpecification for {} v{}", modelName, version);
        return spec;
    }

    /**
     * Adds ML Flow model characteristics.
     */
    private void addMlFlowCharacteristics(AiModelSpecificationCreate spec, ModelVersion modelVersion) {
        
        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelName", "MLflow registered model name", modelVersion.getName()));
        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelVersion", "MLflow model version number", modelVersion.getVersion()));
        
        if (modelVersion.getStatus() != null) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "lifecycleStatus", 
                "MLflow model version status", 
                modelVersion.getStatus().name()
            ));
        }

        if (modelVersion.getCurrentStage() != null && !modelVersion.getCurrentStage().isEmpty()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "stage",
                "MLflow model stage (None/Staging/Production/Archived)",
                modelVersion.getCurrentStage()
            ));
        }

        if (modelVersion.getRunId() != null && !modelVersion.getRunId().isEmpty()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "runId",
                "MLflow experiment run ID used to create this model version",
                modelVersion.getRunId()
            ));
        }

        if (modelVersion.getSource() != null && !modelVersion.getSource().isEmpty()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "artifactUri",
                "MLflow artifact storage location",
                modelVersion.getSource()
            ));
        }

        if (modelVersion.getUserId() != null && !modelVersion.getUserId().isEmpty()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "createdBy",
                "User who created this model version",
                modelVersion.getUserId()
            ));
        }

        if (modelVersion.getCreationTimestamp() > 0) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "creationTimestamp",
                "Model version creation timestamp",
                String.valueOf(modelVersion.getCreationTimestamp())
            ));
        }

        if (modelVersion.getLastUpdatedTimestamp() > 0) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "lastUpdatedTimestamp",
                "Model version last update timestamp",
                String.valueOf(modelVersion.getLastUpdatedTimestamp())
            ));
        }

        String mlflowUrl = mlflowClient.buildModelUrl(modelVersion.getName(), modelVersion.getVersion());
        spec.addSpecCharacteristicItem(createCharacteristicSpec(
            "mlflowUrl",
            "URL to view model in MLflow UI",
            mlflowUrl
        ));
    }

    /**
     * Adds framework and model type characteristics.
     */
    private void addFrameworkCharacteristics(AiModelSpecificationCreate spec, ModelVersion modelVersion, Run run) {
        // Try to determine model flavor/type from run tags or artifacts
        if (run != null && run.getData() != null) {
            RunData data = run.getData();
            
            // Check for framework in tags
            for (RunTag tag : data.getTagsList()) {
                String key = tag.getKey();
                String value = tag.getValue();
                
                if ("mlflow.runName".equals(key)) {
                    spec.addSpecCharacteristicItem(createCharacteristicSpec("runName", "MLflow run name", value));
                } else if (key.startsWith("mlflow.log-model")) {
                    // Extract model flavor from log-model tag
                    if (key.contains("sklearn")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "sklearn"));
                    } else if (key.contains("tensorflow") || key.contains("keras")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "tensorflow"));
                    } else if (key.contains("pytorch")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "pytorch"));
                    } else if (key.contains("xgboost")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "xgboost"));
                    } else if (key.contains("lightgbm")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "lightgbm"));
                    } else if (key.contains("spark")) {
                        spec.addSpecCharacteristicItem(createCharacteristicSpec("modelType", "ML framework/flavor", "spark"));
                    }
                }
            }

            // Check for model parameters
            for (Param param : data.getParamsList()) {
                // Include important hyperparameters as characteristics
                if (isImportantParam(param.getKey())) {
                    spec.addSpecCharacteristicItem(createCharacteristicSpec(
                        "param_" + param.getKey(),
                        "Model training parameter",
                        param.getValue()
                    ));
                }
            }
        }
    }

    /**
     * Adds metrics from the training run.
     */
    private void addMetricsCharacteristics(AiModelSpecificationCreate spec, Run run) {
        if (run == null || run.getData() == null) {
            return;
        }

        List<Metric> metrics = run.getData().getMetricsList();
        for (Metric metric : metrics) {
            // Include common ML metrics
            String name = metric.getKey();
            if (isImportantMetric(name)) {
                spec.addSpecCharacteristicItem(createCharacteristicSpec(
                    "metric_" + name,
                    "Model evaluation metric",
                    String.valueOf(metric.getValue())
                ));
            }
        }
    }

    /**
     * Adds tags from the model version and registered model.
     */
    private void addTagsCharacteristics(AiModelSpecificationCreate spec, ModelVersion modelVersion, RegisteredModel registeredModel) {
        // Model version tags
        List<String> tags = new ArrayList<>();
        for (ModelVersionTag tag : modelVersion.getTagsList()) {
            tags.add(tag.getKey() + "=" + tag.getValue());
        }

        // Registered model tags
        for (RegisteredModelTag tag : registeredModel.getTagsList()) {
            tags.add(tag.getKey() + "=" + tag.getValue());
        }

        if (!tags.isEmpty()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "tags",
                "MLflow model tags",
                String.join(", ", tags)
            ));
        }
    }

    /**
     * Sets the artifact URLs for TMF 915 standard fields.
     */
    private void setArtifactUrls(AiModelSpecificationCreate spec, ModelVersion modelVersion, 
            Run run, String baseUrl, String modelId) {
        
        String artifactBaseUrl = baseUrl + "/tmf-api/aim/v4/aiModel/" + modelId + "/artifacts/mlflow/";
        String runId = modelVersion.getRunId();

        // Model data sheet
        if (run != null && runId != null) {
            String datasheetPath = mlflowClient.findArtifactPath(runId, DATASHEET_PATHS);
            if (datasheetPath != null) {
                spec.setModelDataSheet(artifactBaseUrl + datasheetPath);
            } else {
                // Fallback to model page URL
                spec.setModelDataSheet(mlflowClient.buildModelUrl(modelVersion.getName(), modelVersion.getVersion()));
            }
        }

        // Deployment record
        if (run != null && runId != null) {
            String deploymentPath = mlflowClient.findArtifactPath(runId, DEPLOYMENT_PATHS);
            if (deploymentPath != null) {
                spec.setDeploymentRecord(artifactBaseUrl + deploymentPath);
            }
        }
        
        // Check model version tags for deployment URL
        for (ModelVersionTag tag : modelVersion.getTagsList()) {
            if ("deployment.record.url".equals(tag.getKey())) {
                spec.setDeploymentRecord(tag.getValue());
                break;
            }
        }

        // Inherited model (base model)
        if (run != null && runId != null) {
            // First check run parameters and tags
            String inheritedModelUrl = findInheritedModelFromRun(run);
            if (inheritedModelUrl == null) {
                // Check artifacts
                String inheritedPath = mlflowClient.findArtifactPath(runId, INHERITED_MODEL_PATHS);
                if (inheritedPath != null) {
                    inheritedModelUrl = artifactBaseUrl + inheritedPath;
                }
            }
            if (inheritedModelUrl != null) {
                spec.setInheritedModel(inheritedModelUrl);
            }
        }

        // Model training data
        if (run != null && runId != null) {
            String trainingPath = mlflowClient.findArtifactPath(runId, TRAINING_DATA_PATHS);
            if (trainingPath != null) {
                spec.setModelTrainingData(artifactBaseUrl + trainingPath);
            }
        }

        // Model evaluation data
        if (run != null && runId != null) {
            String evalPath = mlflowClient.findArtifactPath(runId, EVALUATION_DATA_PATHS);
            if (evalPath != null) {
                spec.setModelEvaluationData(artifactBaseUrl + evalPath);
            }
        }
    }

    /**
     * Looks for inherited model reference in run parameters or tags.
     */
    private String findInheritedModelFromRun(Run run) {
        if (run.getData() == null) {
            return null;
        }

        // Check parameters
        for (Param param : run.getData().getParamsList()) {
            if ("base_model_uri".equals(param.getKey()) || "parent_model".equals(param.getKey()) ||
                "foundation_model".equals(param.getKey()) || "pretrained_model".equals(param.getKey())) {
                return param.getValue();
            }
        }

        // Check tags
        for (RunTag tag : run.getData().getTagsList()) {
            if ("base_model_uri".equals(tag.getKey()) || "parent_model".equals(tag.getKey()) ||
                "foundation_model".equals(tag.getKey()) || "pretrained_model".equals(tag.getKey())) {
                return tag.getValue();
            }
        }

        return null;
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Creates a characteristic specification with a default value.
     */
    private CharacteristicSpecification createCharacteristicSpec(String name, String description, String value) {
        CharacteristicSpecification charSpec = new CharacteristicSpecification();
        charSpec.setName(name);
        charSpec.setDescription(description);

        if (value != null) {
            CharacteristicValueSpecification valueSpec = new CharacteristicValueSpecification();
            valueSpec.setValueType("string");
            valueSpec.setValue(value);
            valueSpec.setIsDefault(true);
            charSpec.addCharacteristicValueSpecificationItem(valueSpec);
        }

        return charSpec;
    }

    /**
     * Determines if a parameter is important enough to include.
     */
    private boolean isImportantParam(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("learning_rate") || lower.contains("lr") ||
               lower.contains("epochs") || lower.contains("batch") ||
               lower.contains("layers") || lower.contains("hidden") ||
               lower.contains("optimizer") || lower.contains("loss") ||
               lower.contains("regularization") || lower.contains("dropout");
    }

    /**
     * Determines if a metric is important enough to include.
     */
    private boolean isImportantMetric(String metricName) {
        String lower = metricName.toLowerCase();
        return lower.contains("accuracy") || lower.contains("precision") ||
               lower.contains("recall") || lower.contains("f1") ||
               lower.contains("auc") || lower.contains("loss") ||
               lower.contains("mse") || lower.contains("rmse") ||
               lower.contains("mae") || lower.contains("r2") ||
               lower.contains("score");
    }
}
