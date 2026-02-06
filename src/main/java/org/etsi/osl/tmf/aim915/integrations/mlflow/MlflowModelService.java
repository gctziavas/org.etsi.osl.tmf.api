package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.mlflow.api.proto.ModelRegistry.ModelVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for creating AiModel instances from MLflow models.
 * 
 * An AiModel represents a live deployment of a model - it links an
 * AiModelSpecification (the blueprint) to an inference endpoint.
 * 
 * AiModel instances are always ACTIVE (serving predictions).
 * Deployment configuration/records belong in the AiModelSpecification.
 */
@Service
public class MlflowModelService {

    private static final Logger log = LoggerFactory.getLogger(MlflowModelService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Pattern to extract minor version: "modelName v1_2" -> extracts "2"
    private static final Pattern MINOR_VERSION_PATTERN = Pattern.compile("_([0-9]+)$");

    private final MlflowClientService mlflowClient;
    private final MlflowDeploymentService deploymentService;
    private final AiModelRepositoryService aiModelRepository;

    // Resource configuration from application.yaml
    @Value("${mlflow.deployment.resources.requests.cpu:N/A}")
    private String requestsCpu;

    @Value("${mlflow.deployment.resources.requests.memory:N/A}")
    private String requestsMemory;

    @Value("${mlflow.deployment.resources.limits.cpu:N/A}")
    private String limitsCpu;

    @Value("${mlflow.deployment.resources.limits.memory:N/A}")
    private String limitsMemory;

    @Value("${mlflow.deployment.resources.limits.gpu:N/A}")
    private String limitsGpu;

    // Environment configuration from application.yaml (comma-separated key=value pairs)
    @Value("${mlflow.deployment.environment:}")
    private String environmentConfig;

    public MlflowModelService(MlflowClientService mlflowClient, 
            MlflowDeploymentService deploymentService,
            AiModelRepositoryService aiModelRepository) {
        this.mlflowClient = mlflowClient;
        this.deploymentService = deploymentService;
        this.aiModelRepository = aiModelRepository;
    }

    /**
     * Deploys an MLflow model locally and creates an AiModel.
     * 
     * This method:
     * 1. Starts an MLflow model server using `mlflow models serve`
     * 2. Creates an AiModel record with the inference endpoint
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param port The port to serve on (null for auto-assign)
     * @return AiModelCreate ready to be persisted
     * @throws IOException if deployment or model retrieval fails
     */
    public AiModelCreate deployAndCreateModel(AiModelSpecification specification,
            String modelName, String version, Integer port) throws IOException {
        
        // Resolve version if not specified
        String resolvedVersion = version;
        if (resolvedVersion == null) {
            ModelVersion latest = mlflowClient.getLatestModelVersion(modelName);
            if (latest == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = latest.getVersion();
        }

        log.info("Deploying MLflow model {} v{} locally...", modelName, resolvedVersion);

        // Deploy locally
        MlflowDeploymentService.DeploymentInfo deployment;
        if (port != null) {
            deployment = deploymentService.deployModel(modelName, resolvedVersion, port);
        } else {
            deployment = deploymentService.deployModel(modelName, resolvedVersion);
        }

        log.info("Model deployed at: {}", deployment.getInferenceUrl());

        // Create the AiModel with the inference URL
        return createModelFromDeployment(specification, modelName, resolvedVersion, deployment.getInferenceUrl());
    }

    /**
     * Deploys an MLflow model to a remote MLflow server and creates an AiModel.
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param remoteUrl The remote MLflow server URL (e.g., https://mlflow.example.com)
     * @param authToken Authentication token (optional)
     * @return AiModelCreate ready to be persisted
     * @throws IOException if deployment fails
     */
    public AiModelCreate deployToRemote(AiModelSpecification specification,
            String modelName, String version, String remoteUrl, String authToken) throws IOException {
        
        // Resolve version if not specified
        String resolvedVersion = version;
        if (resolvedVersion == null) {
            ModelVersion latest = mlflowClient.getLatestModelVersion(modelName);
            if (latest == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = latest.getVersion();
        }

        log.info("Deploying MLflow model {} v{} to remote: {}", modelName, resolvedVersion, remoteUrl);

        // Deploy to remote MLflow server
        MlflowDeploymentService.DeploymentInfo deployment = 
            deploymentService.deployToRemote(modelName, resolvedVersion, remoteUrl, authToken);

        log.info("Model deployed at: {}", deployment.getInferenceUrl());

        // Create the AiModel with the inference URL
        return createModelFromDeployment(specification, modelName, resolvedVersion, deployment.getInferenceUrl());
    }

    /**
     * Deploys an MLflow model to a remote MLflow server with custom endpoint name.
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param remoteUrl The remote MLflow server URL
     * @param authToken Authentication token (optional)
     * @param endpointName Custom endpoint name
     * @return AiModelCreate ready to be persisted
     * @throws IOException if deployment fails
     */
    public AiModelCreate deployToRemote(AiModelSpecification specification,
            String modelName, String version, String remoteUrl, 
            String authToken, String endpointName) throws IOException {
        
        // Resolve version if not specified
        String resolvedVersion = version;
        if (resolvedVersion == null) {
            ModelVersion latest = mlflowClient.getLatestModelVersion(modelName);
            if (latest == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = latest.getVersion();
        }

        log.info("Deploying MLflow model {} v{} to remote: {} as {}", 
            modelName, resolvedVersion, remoteUrl, endpointName);

        // Deploy to remote MLflow server with custom endpoint name
        MlflowDeploymentService.DeploymentInfo deployment = 
            deploymentService.deployToRemote(modelName, resolvedVersion, remoteUrl, authToken, endpointName);

        log.info("Model deployed at: {}", deployment.getInferenceUrl());

        // Create the AiModel with the inference URL
        return createModelFromDeployment(specification, modelName, resolvedVersion, deployment.getInferenceUrl());
    }

    /**
     * Deploys an MLflow model on an auto-assigned port (local deployment).
     */
    public AiModelCreate deployAndCreateModel(AiModelSpecification specification,
            String modelName, String version) throws IOException {
        return deployAndCreateModel(specification, modelName, version, (Integer) null);
    }

    /**
     * Creates an AiModelCreate from an MLflow model deployment.
     * 
     * Only deployment-specific information is stored here - all model metadata
     * is accessed through the referenced AiModelSpecification.
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version
     * @param inferenceUrl The inference endpoint URL (required)
     * @return AiModelCreate ready to be persisted
     * @throws IOException if model information cannot be retrieved
     * @throws IllegalArgumentException if inferenceUrl is null or empty
     */
    public AiModelCreate createModelFromDeployment(AiModelSpecification specification, 
            String modelName, String version, String inferenceUrl) throws IOException {
        
        if (inferenceUrl == null || inferenceUrl.isEmpty()) {
            throw new IllegalArgumentException("inferenceUrl is required - AiModel represents a live deployment");
        }
        
        log.info("Creating AiModel from deployed MLflow model: {} v{}", modelName, version);

        // Get model version info for description
        ModelVersion modelVersion;
        String resolvedVersion = version;
        if (resolvedVersion != null) {
            List<ModelVersion> versions = mlflowClient.getModelVersions(modelName);
            final String versionToFind = resolvedVersion;
            modelVersion = versions.stream()
                    .filter(v -> v.getVersion().equals(versionToFind))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Version " + versionToFind + " not found"));
        } else {
            modelVersion = mlflowClient.getLatestModelVersion(modelName);
            if (modelVersion == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = modelVersion.getVersion();
        }

        return buildAiModelCreate(specification, modelName, resolvedVersion, modelVersion, inferenceUrl);
    }

    /**
     * Stops a running MLflow model deployment.
     * 
     * @param modelName The model name
     * @param version The model version
     * @throws IOException if stopping the deployment fails
     */
    public void stopDeployment(String modelName, String version) throws IOException {
        deploymentService.stopDeployment(modelName, version);
    }

    /**
     * Checks if a model is currently deployed and serving.
     */
    public boolean isDeployed(String modelName, String version) {
        return deploymentService.isDeployed(modelName, version);
    }

    /**
     * Gets the inference URL for a deployed model.
     * 
     * @return The inference URL, or null if not deployed
     */
    public String getInferenceUrl(String modelName, String version) {
        MlflowDeploymentService.DeploymentInfo info = deploymentService.getDeployment(modelName, version);
        return info != null ? info.getInferenceUrl() : null;
    }

    /**
     * Creates an AiModelCreate for a model being instantiated from a specification.
     * 
     * Stores deployment configuration - all model metadata
     * is accessed through the referenced AiModelSpecification.
     * 
     * If an AiModel with the same name exists, a minor version suffix is added.
     * 
     * @param specification The AiModelSpecification to instantiate
     * @param instanceName Optional custom name for the instance
     * @param inferenceUrl The inference endpoint URL (required)
     * @return AiModelCreate ready to be persisted
     * @throws IllegalArgumentException if inferenceUrl is null or empty
     */
    public AiModelCreate createModelInstance(AiModelSpecification specification, 
            String instanceName, String inferenceUrl) {
        
        if (inferenceUrl == null || inferenceUrl.isEmpty()) {
            throw new IllegalArgumentException("inferenceUrl is required - AiModel represents a live deployment");
        }
        
        log.info("Creating AiModel instance from specification: {}", specification.getName());

        AiModelCreate model = new AiModelCreate();

        // Link to specification - all model metadata is accessed through this reference
        model.setAiModelSpecification(specification);

        // Generate unique name
        String baseName = instanceName != null ? instanceName : specification.getName() + " Instance";
        String uniqueName = generateUniqueName(baseName);
        model.setName(uniqueName);
        model.setDescription("Instance of " + specification.getName() + " v" + specification.getVersion());

        // AiModel is always ACTIVE (it's a live deployment)
        model.setState(ServiceStateType.ACTIVE);
        
        // Add deployment configuration (no deployment info available for external instances)
        addDeploymentConfiguration(model, inferenceUrl, null);

        log.info("Created AiModel instance: {}", uniqueName);
        return model;
    }

    /**
     * Builds the AiModelCreate from MLflow deployment data.
     * 
     * Stores the inference URL and deployment configuration as characteristics.
     * All model metadata is accessed through the referenced AiModelSpecification.
     * 
     * If an AiModel with the same name exists, a minor version suffix is added:
     * - First instance: "modelName v1"
     * - Second instance: "modelName v1.1"
     */
    private AiModelCreate buildAiModelCreate(AiModelSpecification specification, String modelName, 
            String version, ModelVersion modelVersion, String inferenceUrl) {
        
        AiModelCreate model = new AiModelCreate();

        model.setAiModelSpecification(specification);
        
        // Generate unique name with minor version if needed
        String baseName = modelName + " v" + version;
        String uniqueName = generateUniqueName(baseName);
        model.setName(uniqueName);
        model.setDescription(buildDescription(modelVersion));

        // AiModel is always ACTIVE (it's a live deployment)
        model.setState(ServiceStateType.ACTIVE);
        
        // Get deployment info for target type
        MlflowDeploymentService.DeploymentInfo deployment = 
            deploymentService.getDeployment(modelName, version);
        
        // Add deployment configuration
        addDeploymentConfiguration(model, inferenceUrl, deployment);

        log.info("Created AiModel '{}' at {}", uniqueName, inferenceUrl);
        return model;
    }

    /**
     * Adds deployment configuration characteristics to the AiModel.
     * 
     * Configuration structure:
     * - platform: Always "mlflow"
     * - endpoint: The inference URL
     * - deploymentTarget: LOCAL or REMOTE
     * - deployedAt: ISO timestamp
     * - resources: From application.yaml (requests/limits for cpu, memory, gpu)
     * - environment: From application.yaml (runtime env vars)
     * - remoteUrl/endpointName: For remote deployments
     */
    private void addDeploymentConfiguration(AiModelCreate model, String inferenceUrl,
            MlflowDeploymentService.DeploymentInfo deployment) {
        
        // Core deployment info
        addCharacteristic(model, "platform", "mlflow", "string");
        addCharacteristic(model, "endpoint", inferenceUrl, "string");
        addCharacteristic(model, "deployedAt", Instant.now().toString(), "string");
        
        // Deployment target (LOCAL or REMOTE)
        if (deployment != null) {
            addCharacteristic(model, "deploymentTarget", deployment.getTarget().name(), "string");
            
            if (deployment.isRemote()) {
                if (deployment.getRemoteUrl() != null) {
                    addCharacteristic(model, "remoteUrl", deployment.getRemoteUrl(), "string");
                }
                if (deployment.getEndpointName() != null) {
                    addCharacteristic(model, "endpointName", deployment.getEndpointName(), "string");
                }
            }
        } else {
            addCharacteristic(model, "deploymentTarget", "LOCAL", "string");
        }
        
        // Resources configuration (from application.yaml)
        String resourcesJson = buildResourcesJson();
        addCharacteristic(model, "resources", resourcesJson, "object");
        
        // Environment configuration (from application.yaml)
        String environmentJson = buildEnvironmentJson();
        if (environmentJson != null && !environmentJson.equals("{}")) {
            addCharacteristic(model, "environment", environmentJson, "object");
        }
    }

    /**
     * Builds the resources JSON from application.yaml configuration.
     * Returns N/A values if not configured.
     */
    private String buildResourcesJson() {
        Map<String, Object> resources = new HashMap<>();
        
        Map<String, String> requests = new HashMap<>();
        requests.put("cpu", requestsCpu);
        requests.put("memory", requestsMemory);
        resources.put("requests", requests);
        
        Map<String, String> limits = new HashMap<>();
        limits.put("cpu", limitsCpu);
        limits.put("memory", limitsMemory);
        if (!"N/A".equals(limitsGpu)) {
            limits.put("nvidia.com/gpu", limitsGpu);
        }
        resources.put("limits", limits);
        
        try {
            return objectMapper.writeValueAsString(resources);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize resources: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Builds the environment JSON from application.yaml configuration.
     * Expects comma-separated key=value pairs: "KEY1=value1,KEY2=value2"
     */
    private String buildEnvironmentJson() {
        if (environmentConfig == null || environmentConfig.isEmpty()) {
            return "{}";
        }
        
        Map<String, String> environment = new HashMap<>();
        String[] pairs = environmentConfig.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2) {
                environment.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        
        try {
            return objectMapper.writeValueAsString(environment);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize environment: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Generates a unique name for the AiModel.
     * If an AiModel with the base name exists, appends a minor version suffix.
     * 
     * @param baseName The base name (e.g., "fraud-detector v1")
     * @return Unique name (e.g., "fraud-detector v1" or "fraud-detector v1.1")
     */
    private String generateUniqueName(String baseName) {
        List<AiModel> existingModels = aiModelRepository.findByNameStartingWith(baseName);
        
        if (existingModels.isEmpty()) {
            return baseName;
        }
        
        // Find the highest minor version
        int maxMinorVersion = 0;
        for (AiModel existing : existingModels) {
            String existingName = existing.getName();
            
            // Check if it's exactly the base name (no minor version)
            if (existingName.equals(baseName)) {
                maxMinorVersion = Math.max(maxMinorVersion, 0);
                continue;
            }
            
            // Extract minor version from names like "modelName v1.2"
            Matcher matcher = MINOR_VERSION_PATTERN.matcher(existingName);
            if (matcher.find()) {
                int minorVersion = Integer.parseInt(matcher.group(1));
                maxMinorVersion = Math.max(maxMinorVersion, minorVersion);
            }
        }
        
        // Next minor version
        int nextMinorVersion = maxMinorVersion + 1;
        return baseName + "." + nextMinorVersion;
    }

    /**
     * Builds a description from model version.
     */
    private String buildDescription(ModelVersion modelVersion) {
        if (modelVersion.getDescription() != null && !modelVersion.getDescription().isEmpty()) {
            return modelVersion.getDescription();
        }
        return "Deployed instance of " + modelVersion.getName() + " v" + modelVersion.getVersion();
    }

    /**
     * Helper to add a characteristic to an AiModelCreate.
     */
    private void addCharacteristic(AiModelCreate model, String name, String value, String valueType) {
        Characteristic characteristic = new Characteristic();
        characteristic.setName(name);
        characteristic.setValue(new Any(value));
        characteristic.setValueType(valueType != null ? valueType : "string");
        model.addServiceCharacteristicItem(characteristic);
    }
}
