package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.api.proto.ModelRegistry.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.mlflow.api.proto.Service.Run;
import org.mlflow.api.proto.Service.Experiment;
import org.mlflow.api.proto.Service.RunInfo;
import org.mlflow.api.proto.Service.RunData;
import org.mlflow.api.proto.Service.Param;
import org.mlflow.api.proto.Service.Metric;
import org.mlflow.api.proto.Service.RunTag;
import org.mlflow.entities.model_registry.ModelVersion;
import org.mlflow.entities.model_registry.ModelVersionTag;
import org.mlflow.paginator.ModelVersionsPage;

import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.common.model.Any;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class MlflowService {

    private static final Logger log = LoggerFactory.getLogger(MlflowService.class);
    private final MlflowClient client;

    public MlflowService(@Value("${mlflow.host:127.0.0.1}") String host,
                         @Value("${mlflow.port:5000}") int port) {
        String url = String.format("http://%s:%d", host, port);
        log.info("Initializing MlflowService with URL: {}", url);
        this.client = new MlflowClient(url);
    }

    public Run getMlFlowRun(String runId) {
        log.debug("Fetching MLflow run: {}", runId);
        return client.getRun(runId);
    }

    public Experiment getMlFlowExperiment(String experimentId) {
        log.debug("Fetching MLflow experiment: {}", experimentId);
        return client.getExperiment(experimentId);
    }

    public AiModelSpecification mlFlowToAiModelSpecification(ModelVersion modelVersion, Run run) {
        log.debug("Converting MLflow ModelVersion to AiModelSpecification: name={}, version={}", 
            modelVersion.getName(), modelVersion.getVersion());
        
        AiModelSpecification spec = new AiModelSpecification();

        spec.setName(modelVersion.getName());
        spec.setVersion(modelVersion.getVersion());
        spec.setLifecycleStatus(modelVersion.getStatus().name());
        if (modelVersion.getLastUpdatedTimestamp() > 0) {
            spec.setLastUpdate(java.time.OffsetDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(modelVersion.getLastUpdatedTimestamp()),
                    java.time.ZoneId.systemDefault()));
        }

        // Model data sheet from model version description
        spec.setModelDataSheet(modelVersion.getDescription());
        log.debug("Set model data sheet from description");

        // Deployment record from model version tags
        for (ModelVersionTag tag : modelVersion.getTagsList()) {
            if ("deployment.record.url".equals(tag.getKey())) {
                spec.setDeploymentRecord(tag.getValue());
                log.debug("Found deployment record URL: {}", tag.getValue());
                break;
            }
        }

        if (run != null) {
            RunData runData = run.getData();

            // Inherited model from run parameters or tags
            for (Param param : runData.getParamsList()) {
                if ("base_model_uri".equals(param.getKey())) {
                    spec.setInheritedModel(param.getValue());
                    log.debug("Found inherited model in params: {}", param.getValue());
                    break;
                }
            }
            if (spec.getInheritedModel() == null) {
                for (RunTag tag : runData.getTagsList()) {
                    if ("base_model_uri".equals(tag.getKey())) {
                        spec.setInheritedModel(tag.getValue());
                        log.debug("Found inherited model in tags: {}", tag.getValue());
                        break;
                    }
                }
            }
        }

        log.info("Successfully converted ModelVersion {} v{} to AiModelSpecification", 
            spec.getName(), spec.getVersion());
        return spec;
    }

    public AiModel mlFlowToAiModel(ModelVersion modelVersion, Run run, String baseUrl) {
        log.debug("Converting MLflow ModelVersion to AiModel: name={}, version={}, baseUrl={}", 
            modelVersion.getName(), modelVersion.getVersion(), baseUrl);
        
        AiModel aiModel = new AiModel();
        
        // Set basic properties
        aiModel.setName(modelVersion.getName() + " v" + modelVersion.getVersion());
        // Map MLflow status to ServiceStateType - adjust mappings as needed
        try {
            aiModel.setState(ServiceStateType.fromValue(modelVersion.getStatus().name()));
            log.debug("Mapped MLflow status {} to ServiceStateType", modelVersion.getStatus().name());
        } catch (IllegalArgumentException e) {
            // Default to FEASIBILITYCHECKED if status doesn't map directly
            aiModel.setState(ServiceStateType.FEASIBILITYCHECKED);
            log.warn("Could not map MLflow status {}, defaulting to FEASIBILITYCHECKED", 
                modelVersion.getStatus().name());
        }
        
        // Create and attach the AiModelSpecification
        AiModelSpecification spec = mlFlowToAiModelSpecification(modelVersion, run);
        aiModel.setAiModelSpecification(spec);
        log.debug("Attached AiModelSpecification to AiModel");
        
        // Create service characteristics to hold artifact information
        Set<Characteristic> characteristics = new HashSet<>();
        
        // 1. Model artifact URL
        Characteristic modelArtifactChar = new Characteristic();
        modelArtifactChar.setName("modelArtifactUrl");
        modelArtifactChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/model"));
        modelArtifactChar.setValueType("string");
        characteristics.add(modelArtifactChar);
        log.debug("Added modelArtifactUrl characteristic");
        
        if (run != null) {
            // 2. Training data URL
            Characteristic trainingDataChar = new Characteristic();
            trainingDataChar.setName("trainingDataUrl");
            trainingDataChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/training_data"));
            trainingDataChar.setValueType("string");
            characteristics.add(trainingDataChar);
            
            // 3. Evaluation data URL
            Characteristic evaluationDataChar = new Characteristic();
            evaluationDataChar.setName("evaluationDataUrl");
            evaluationDataChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/evaluation_data"));
            evaluationDataChar.setValueType("string");
            characteristics.add(evaluationDataChar);
            
            log.debug("Added training and evaluation data URL characteristics");
            
            RunData runData = run.getData();
            
            // 4. Deployment record from model version tags
            for (ModelVersionTag tag : modelVersion.getTagsList()) {
                if ("deployment.record.url".equals(tag.getKey())) {
                    Characteristic deploymentRecordChar = new Characteristic();
                    deploymentRecordChar.setName("deploymentRecordUrl");
                    deploymentRecordChar.setValue(new Any(tag.getValue()));
                    deploymentRecordChar.setValueType("string");
                    characteristics.add(deploymentRecordChar);
                    log.debug("Added deploymentRecordUrl characteristic: {}", tag.getValue());
                    break;
                }
            }
            
            // 5. Inherited model from run parameters or tags
            for (Param param : runData.getParamsList()) {
                if ("base_model_uri".equals(param.getKey())) {
                    Characteristic inheritedModelChar = new Characteristic();
                    inheritedModelChar.setName("inheritedModelUri");
                    inheritedModelChar.setValue(new Any(param.getValue()));
                    inheritedModelChar.setValueType("string");
                    characteristics.add(inheritedModelChar);
                    log.debug("Added inheritedModelUri characteristic from params: {}", param.getValue());
                    break;
                }
            }
            
            // Check tags if not found in params
            if (characteristics.stream().noneMatch(c -> "inheritedModelUri".equals(c.getName()))) {
                for (RunTag tag : runData.getTagsList()) {
                    if ("base_model_uri".equals(tag.getKey())) {
                        Characteristic inheritedModelChar = new Characteristic();
                        inheritedModelChar.setName("inheritedModelUri");
                        inheritedModelChar.setValue(new Any(tag.getValue()));
                        inheritedModelChar.setValueType("string");
                        characteristics.add(inheritedModelChar);
                        log.debug("Added inheritedModelUri characteristic from tags: {}", tag.getValue());
                        break;
                    }
                }
            }
            
            // 6. Model data sheet from model version description
            if (modelVersion.getDescription() != null && !modelVersion.getDescription().isEmpty()) {
                Characteristic modelDataSheetChar = new Characteristic();
                modelDataSheetChar.setName("modelDataSheet");
                modelDataSheetChar.setValue(new Any(modelVersion.getDescription()));
                modelDataSheetChar.setValueType("string");
                characteristics.add(modelDataSheetChar);
                log.debug("Added modelDataSheet characteristic from description");
            }
            
            // 7. Store MLflow Run ID for artifact retrieval
            Characteristic runIdChar = new Characteristic();
            runIdChar.setName("mlflowRunId");
            runIdChar.setValue(new Any(run.getInfo().getRunId()));
            runIdChar.setValueType("string");
            characteristics.add(runIdChar);
            log.debug("Added mlflowRunId characteristic: {}", run.getInfo().getRunId());
        } else {
            log.debug("No Run provided, skipping run-based characteristics");
        }
        
        aiModel.setServiceCharacteristic(characteristics);
        
        log.info("Successfully converted ModelVersion {} v{} to AiModel with {} characteristics", 
            modelVersion.getName(), modelVersion.getVersion(), characteristics.size());
        return aiModel;
    }

    public File downloadArtifact(String modelName, String artifactPath) {
        log.info("Downloading artifact {} for model: {}", artifactPath, modelName);
        try {
            // Search for model versions - returns a paginated result
            ModelVersionsPage versionsPage = client.searchModelVersions("name='" + modelName + "'");
            List<ModelVersion> versions = versionsPage.getItems();
            
            if (versions == null || versions.isEmpty()) {
                log.warn("No versions found for model: {}", modelName);
                return null; // Or throw an exception
            }
            
            // Get the latest version's run ID
            String runId = versions.get(0).getRunId();
            log.debug("Using run ID {} for model {}", runId, modelName);
            
            File artifact = client.downloadArtifacts(runId, artifactPath);
            log.info("Successfully downloaded artifact {} from model {}", artifactPath, modelName);
            return artifact;
            
        } catch (Exception e) {
            log.error("Error downloading artifact {} for model {}: {}", 
                artifactPath, modelName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lists all available artifacts for a given model.
     * This is useful to discover what artifacts (model files, datasets, etc.) are available.
     * 
     * @param modelName The name of the model
     * @return List of artifact paths available for the model's run
     */
    public List<String> listAvailableArtifacts(String modelName) {
        log.debug("Listing available artifacts for model: {}", modelName);
        try {
            // Search for model versions
            ModelVersionsPage versionsPage = client.searchModelVersions("name='" + modelName + "'");
            List<ModelVersion> versions = versionsPage.getItems();
            
            if (versions == null || versions.isEmpty()) {
                log.warn("No versions found for model: {}", modelName);
                return new ArrayList<>();
            }
            
            // Get the latest version's run ID
            String runId = versions.get(0).getRunId();
            log.debug("Listing artifacts for run ID: {}", runId);
            
            // List all artifacts for this run
            List<String> artifacts = client.listArtifacts(runId).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(java.util.stream.Collectors.toList());
                    
            log.info("Found {} artifacts for model {}", artifacts.size(), modelName);
            return artifacts;
            
        } catch (Exception e) {
            log.error("Error listing artifacts for model {}: {}", modelName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts for a specific run ID.
     * 
     * @param runId The MLflow run ID
     * @return List of artifact paths
     */
    public List<String> listArtifactsByRunId(String runId) {
        log.debug("Listing artifacts for run ID: {}", runId);
        try {
            List<String> artifacts = client.listArtifacts(runId).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(java.util.stream.Collectors.toList());
            log.info("Found {} artifacts for run {}", artifacts.size(), runId);
            return artifacts;
        } catch (Exception e) {
            log.error("Error listing artifacts for run {}: {}", runId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts under a specific path in a run.
     * 
     * @param runId The MLflow run ID
     * @param path The path within the artifacts directory (e.g., "training_data")
     * @return List of artifact paths under the specified path
     */
    public List<String> listArtifactsByPath(String runId, String path) {
        log.debug("Listing artifacts by path: runId={}, path={}", runId, path);
        try {
            List<String> artifacts = client.listArtifacts(runId, path).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(java.util.stream.Collectors.toList());
            log.info("Found {} artifacts for run {} at path {}", artifacts.size(), runId, path);
            return artifacts;
        } catch (Exception e) {
            log.error("Error listing artifacts by path for run {} at {}: {}", runId, path, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

}