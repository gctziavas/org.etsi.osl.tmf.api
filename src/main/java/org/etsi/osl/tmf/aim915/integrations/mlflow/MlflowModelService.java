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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for creating AiModel instances from MLflow models.
 * 
 * An AiModel represents a live deployment of a model - it links an
 * AiModelSpecification (the blueprint) to an inference endpoint.
 * 
 * Deployments are done via Docker containers on a remote Docker host.
 * AiModel instances are always ACTIVE (serving predictions).
 */
@Service
public class MlflowModelService {

    private static final Logger log = LoggerFactory.getLogger(MlflowModelService.class);
    
    // Pattern to extract minor version: "modelName v1.2" -> extracts "2"
    private static final Pattern MINOR_VERSION_PATTERN = Pattern.compile("\\.([0-9]+)$");

    private final MlflowClientService mlflowClient;
    private final MlflowDeploymentService deploymentService;
    private final AiModelRepositoryService aiModelRepository;

    public MlflowModelService(MlflowClientService mlflowClient, 
            MlflowDeploymentService deploymentService,
            AiModelRepositoryService aiModelRepository) {
        this.mlflowClient = mlflowClient;
        this.deploymentService = deploymentService;
        this.aiModelRepository = aiModelRepository;
    }

    /**
     * Deploys an MLflow model to Docker and creates an AiModel.
     * 
     * This method:
     * 1. Deploys an MLflow model container on the remote Docker host
     * 2. Creates an AiModel record with the inference endpoint
     * 
     * Port resolution:
     * - If port is null: automatically finds an available port
     * - If port is specified but in use: automatically finds next available port
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param port The preferred host port to expose (null for auto-assign, falls back if in use)
     * @return AiModelCreate ready to be persisted
     * @throws IOException if deployment or model retrieval fails
     */
    public AiModelCreate deployAndCreateTmfModel(AiModelSpecification specification,
            String modelName, String version, Integer port) throws IOException {
        
        if (!deploymentService.isEnabled()) {
            throw new IllegalStateException(
                "Docker deployment is not enabled. Configure mlflow.docker.enabled=true and mlflow.docker.host");
        }

        // Resolve version if not specified
        String resolvedVersion = version;
        if (resolvedVersion == null) {
            ModelVersion latest = mlflowClient.getLatestModelVersion(modelName);
            if (latest == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = latest.getVersion();
        }

        log.info("Deploying MLflow model {} v{} to Docker...", modelName, resolvedVersion);

        // Deploy to Docker
        MlflowDeploymentService.DeploymentResult deployment;
        if (port != null) {
            deployment = deploymentService.deployModel(modelName, resolvedVersion, port);
        } else {
            deployment = deploymentService.deployModel(modelName, resolvedVersion);
        }

        log.info("Model deployed at: {}", deployment.getInferenceUrl());

        // Create the AiModel with the deployment info
        return createModelFromDeployment(specification, modelName, resolvedVersion, deployment);
    }

    /**
     * Deploys an MLflow model to Docker on an auto-assigned port.
     */
    public AiModelCreate deployAndCreateTmfModel(AiModelSpecification specification,
            String modelName, String version) throws IOException {
        return deployAndCreateTmfModel(specification, modelName, version, (Integer) null);
    }

    /**
     * Deploys an MLflow model to a CUSTOM Docker host and creates an AiModel.
     * 
     * This method:
     * 1. Deploys an MLflow model container on the specified Docker host
     * 2. Creates an AiModel record with the inference endpoint
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param customDockerHost The Docker host IP/hostname to deploy to
     * @param customDockerPort The Docker API port on the custom host
     * @param port The preferred host port to expose (null for auto-assign)
     * @return AiModelCreate ready to be persisted
     * @throws IOException if deployment or model retrieval fails
     */
    public AiModelCreate deployAndCreateTmfModelToHost(AiModelSpecification specification,
            String modelName, String version, 
            String customDockerHost, int customDockerPort, Integer port) throws IOException {
        
        // Resolve version if not specified
        String resolvedVersion = version;
        if (resolvedVersion == null) {
            ModelVersion latest = mlflowClient.getLatestModelVersion(modelName);
            if (latest == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            resolvedVersion = latest.getVersion();
        }

        log.info("Deploying MLflow model {} v{} to custom Docker host {}:{}...", 
                modelName, resolvedVersion, customDockerHost, customDockerPort);

        // Deploy to custom Docker host
        MlflowDeploymentService.DeploymentResult deployment = 
            deploymentService.deployModelToHost(modelName, resolvedVersion, 
                    customDockerHost, customDockerPort, port);

        log.info("Model deployed at: {}", deployment.getInferenceUrl());

        // Create the AiModel with the deployment info
        return createModelFromDeployment(specification, modelName, resolvedVersion, deployment);
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
     * @param deployment The deployment result with container info
     * @return AiModelCreate ready to be persisted
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelCreate createModelFromDeployment(AiModelSpecification specification, 
            String modelName, String version, 
            MlflowDeploymentService.DeploymentResult deployment) throws IOException {
        
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

        return buildAiModelCreate(specification, modelName, resolvedVersion, modelVersion, deployment);
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
     * Stops a running MLflow model deployment on a CUSTOM Docker host.
     * 
     * @param modelName The model name
     * @param version The model version
     * @param customDockerHost The Docker host
     * @param customDockerPort The Docker API port
     */
    public void stopDeploymentOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        deploymentService.stopDeploymentOnHost(modelName, version, 
                customDockerHost, customDockerPort);
    }

    /**
     * Checks if a model is currently deployed and serving.
     */
    public boolean isDeployed(String modelName, String version) {
        return deploymentService.isDeployed(modelName, version);
    }

    /**
     * Checks if a model is currently deployed on a CUSTOM Docker host.
     */
    public boolean isDeployedOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        return deploymentService.isDeployedOnHost(modelName, version, 
                customDockerHost, customDockerPort);
    }

    /**
     * Gets the inference URL for a deployed model.
     * 
     * @return The inference URL, or null if not deployed
     */
    public String getInferenceUrl(String modelName, String version) {
        return deploymentService.getInferenceUrl(modelName, version);
    }

    /**
     * Gets the inference URL for a deployed model on a CUSTOM Docker host.
     * 
     * @return The inference URL, or null if not deployed
     */
    public String getInferenceUrlOnHost(String modelName, String version, String customDockerHost) {
        return deploymentService.getInferenceUrlOnHost(modelName, version, customDockerHost);
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
        
        // Add deployment configuration for external/manual deployment
        addDeploymentConfiguration(model, inferenceUrl);

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
            String version, ModelVersion modelVersion, 
            MlflowDeploymentService.DeploymentResult deployment) {
        
        AiModelCreate model = new AiModelCreate();

        model.setAiModelSpecification(specification);
        
        // Generate unique name with minor version if needed
        String baseName = modelName + " v" + version;
        String uniqueName = generateUniqueName(baseName);

        model.setName(uniqueName);
        if (modelVersion.getDescription() != null && !modelVersion.getDescription().isEmpty()) {
            model.setDescription(modelVersion.getDescription());
        } else {
            model.setDescription("Deployed instance of " + modelVersion.getName() + " v" + modelVersion.getVersion());
        }

        // AiModel is always ACTIVE (it's a live deployment)
        model.setState(ServiceStateType.ACTIVE);
        
        // Add deployment configuration
        addDeploymentConfiguration(model, deployment);

        log.info("Created AiModel '{}' at {}", uniqueName, deployment.getInferenceUrl());
        return model;
    }

    /**
     * Adds deployment configuration characteristics to the AiModel.
     * 
     * Configuration structure for Docker deployments:
     * - platform: Always "mlflow"
     * - endpoint: The inference URL
     * - deploymentTarget: Always "DOCKER"
     * - deployedAt: ISO timestamp
     * - dockerHost: The Docker host address
     * - containerId: The Docker container ID
     * - containerName: The Docker container name
     * - hostPort: The port on the Docker host
     */
    private void addDeploymentConfiguration(AiModelCreate model, 
            MlflowDeploymentService.DeploymentResult deployment) {
        
        // Core deployment info
        addCharacteristic(model, "platform", "mlflow", "string");
        addCharacteristic(model, "endpoint", deployment.getInferenceUrl(), "string");
        addCharacteristic(model, "deployedAt", Instant.now().toString(), "string");
        addCharacteristic(model, "deploymentTarget", "DOCKER", "string");
        
        // Docker-specific info
        if (deployment.getDockerHost() != null) {
            addCharacteristic(model, "dockerHost", deployment.getDockerHost(), "string");
        }
        if (deployment.getContainerId() != null) {
            addCharacteristic(model, "containerId", deployment.getContainerId(), "string");
        }
        if (deployment.getContainerName() != null) {
            addCharacteristic(model, "containerName", deployment.getContainerName(), "string");
        }
        if (deployment.getHostPort() > 0) {
            addCharacteristic(model, "hostPort", String.valueOf(deployment.getHostPort()), "integer");
        }
    }

    /**
     * Adds deployment configuration for external/manual deployments (no Docker info).
     */
    private void addDeploymentConfiguration(AiModelCreate model, String inferenceUrl) {
        addCharacteristic(model, "platform", "mlflow", "string");
        addCharacteristic(model, "endpoint", inferenceUrl, "string");
        addCharacteristic(model, "deployedAt", Instant.now().toString(), "string");
        addCharacteristic(model, "deploymentTarget", "EXTERNAL", "string");
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
