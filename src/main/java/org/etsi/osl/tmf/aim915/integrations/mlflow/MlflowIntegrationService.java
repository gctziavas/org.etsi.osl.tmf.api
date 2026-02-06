package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.*;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main orchestration service for MLflow integration with TMF 915.
 * 
 * This service provides the high-level API for:
 * - Synchronizing MLflow models as AiModelSpecifications
 * - Creating AiModel instances from specifications
 * - Managing the lifecycle of AI models
 * 
 * Architecture:
 * - AiModelSpecification: Describes a model stored in MLflow (blueprint)
 * - AiModel: Represents a deployed/served model instance
 * 
 * When a model is registered in MLflow → Create AiModelSpecification
 * When a model is deployed/served → Create AiModel from specification
 */
@Service
public class MlflowIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(MlflowIntegrationService.class);

    private final MlflowClientService mlflowClient;
    private final MlflowSpecificationService specificationService;
    private final MlflowModelService modelService;
    private final AiModelSpecificationRepositoryService specificationRepository;
    private final AiModelRepositoryService modelRepository;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${server.port:13082}")
    private int serverPort;

    public MlflowIntegrationService(
            MlflowClientService mlflowClient,
            MlflowSpecificationService specificationService,
            MlflowModelService modelService,
            AiModelSpecificationRepositoryService specificationRepository,
            AiModelRepositoryService modelRepository) {
        this.mlflowClient = mlflowClient;
        this.specificationService = specificationService;
        this.modelService = modelService;
        this.specificationRepository = specificationRepository;
        this.modelRepository = modelRepository;
        log.info("MlflowIntegrationService initialized");
    }

    // ========================================
    // Specification Operations
    // ========================================

    /**
     * Imports an MLflow model as an AiModelSpecification.
     * 
     * Creates a new specification or updates existing one if it already exists.
     * 
     * @param modelName The MLflow registered model name
     * @param version The model version (null for latest)
     * @return The created or updated AiModelSpecification
     * @throws IOException if model cannot be accessed
     */
    public AiModelSpecification importModelAsSpecification(String modelName, String version) throws IOException {
        log.info("Importing MLflow model as specification: {} v{}", modelName, version != null ? version : "latest");

        // Resolve version if not specified
        if (version == null) {
            var latestVersion = mlflowClient.getLatestModelVersion(modelName);
            if (latestVersion == null) {
                throw new IOException("No versions found for model: " + modelName);
            }
            version = latestVersion.getVersion();
        }

        // Check if specification already exists
        AiModelSpecification existing = specificationRepository.findAiModelSpecificationByNameAndVersion(modelName, version);
        if (existing != null) {
            log.info("Specification already exists for {} v{}, returning existing", modelName, version);
            return existing;
        }

        // Create new specification
        String baseUrl = getBaseUrl();
        AiModelSpecificationCreate specCreate = specificationService.createSpecificationFromMlflow(modelName, version, baseUrl);
        AiModelSpecification spec = specificationRepository.createAiModelSpecification(specCreate);
        
        log.info("Created specification for {} v{} with ID: {}", modelName, version, spec.getId());
        return spec;
    }

    /**
     * Imports all versions of an MLflow model as specifications.
     * 
     * @param modelName The MLflow registered model name
     * @return List of created specifications
     * @throws IOException if model cannot be accessed
     */
    public List<AiModelSpecification> importAllVersionsAsSpecifications(String modelName) throws IOException {
        log.info("Importing all versions of model: {}", modelName);
        
        var versions = mlflowClient.getModelVersions(modelName);
        List<AiModelSpecification> specs = new ArrayList<>();
        
        for (var version : versions) {
            try {
                AiModelSpecification spec = importModelAsSpecification(modelName, version.getVersion());
                specs.add(spec);
            } catch (Exception e) {
                log.warn("Failed to import version {} of {}: {}", version.getVersion(), modelName, e.getMessage());
            }
        }
        
        log.info("Imported {} specifications for model {}", specs.size(), modelName);
        return specs;
    }

    /**
     * Synchronizes all MLflow models with the specification repository.
     * 
     * Creates specifications for new models, skips existing ones.
     * 
     * @return List of newly created specifications
     * @throws IOException if MLflow cannot be accessed
     */
    public List<AiModelSpecification> syncAllModels() throws IOException {
        log.info("Synchronizing all MLflow models");
        
        List<String> modelNames = mlflowClient.listRegisteredModels();
        List<AiModelSpecification> newSpecs = new ArrayList<>();
        
        for (String modelName : modelNames) {
            try {
                // Import latest version only for sync
                AiModelSpecification spec = importModelAsSpecification(modelName, null);
                newSpecs.add(spec);
            } catch (Exception e) {
                log.warn("Failed to sync model {}: {}", modelName, e.getMessage());
            }
        }
        
        log.info("Synchronized {} models from MLflow", newSpecs.size());
        return newSpecs;
    }

    /**
     * Finds a specification by model name and version.
     * 
     * @param modelName The model name
     * @param version The version (null for any)
     * @return Optional containing the specification if found
     */
    public Optional<AiModelSpecification> findSpecification(String modelName, String version) {
        AiModelSpecification spec = specificationRepository.findAiModelSpecificationByNameAndVersion(modelName, version);
        return Optional.ofNullable(spec);
    }

    // ========================================
    // Model Instance Operations
    // ========================================

    /**
     * Creates an AiModel instance from an existing specification for an external deployment.
     * 
     * Use this when registering an externally deployed/hosted model that is not
     * managed by this system's Docker deployment.
     * 
     * @param specificationId The ID of the specification to instantiate
     * @param instanceName Optional name for the instance
     * @param deploymentEndpoint The deployment endpoint URL (required)
     * @return The created AiModel
     * @throws IllegalArgumentException if specification not found or endpoint is null
     */
    public AiModel createModelInstance(String specificationId, String instanceName, String deploymentEndpoint) {
        log.info("Creating model instance from specification: {}", specificationId);
        
        AiModelSpecification spec = specificationRepository.findAiModelSpecificationByUuid(specificationId);
        if (spec == null) {
            throw new IllegalArgumentException("Specification not found: " + specificationId);
        }

        AiModelCreate modelCreate = modelService.createModelInstance(spec, instanceName, deploymentEndpoint);
        AiModel model = modelRepository.createAiModel(modelCreate);
        
        log.info("Created model instance: {} with ID: {}", model.getName(), model.getId());
        return model;
    }

    /**
     * Deploys an MLflow model to Docker and creates an AiModel.
     * 
     * This method:
     * 1. Imports/finds the specification
     * 2. Deploys the model to Docker
     * 3. Creates an AiModel with the inference endpoint
     * 
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @return The created AiModel
     * @throws IOException if deployment fails
     */
    public AiModel deployModelFromMlflow(String modelName, String version) throws IOException {
        log.info("Deploying model from MLflow: {} v{}", modelName, version != null ? version : "latest");
        
        // Ensure specification exists
        AiModelSpecification spec = importModelAsSpecification(modelName, version);
        
        // Deploy to Docker and create AiModel
        AiModelCreate modelCreate = modelService.deployAndCreateTmfModel(
            spec, modelName, version != null ? version : spec.getVersion());
        
        AiModel model = modelRepository.createAiModel(modelCreate);
        
        log.info("Deployed model {} v{} with ID: {}", modelName, version, model.getId());
        return model;
    }

    /**
     * Deploys an MLflow model to Docker on a specific port.
     * 
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param port The host port to expose
     * @return The created AiModel
     * @throws IOException if deployment fails
     */
    public AiModel deployModelFromMlflow(String modelName, String version, int port) throws IOException {
        log.info("Deploying model from MLflow: {} v{} on port {}", modelName, version != null ? version : "latest", port);
        
        // Ensure specification exists
        AiModelSpecification spec = importModelAsSpecification(modelName, version);
        
        // Deploy to Docker on specific port and create AiModel
        AiModelCreate modelCreate = modelService.deployAndCreateTmfModel(
            spec, modelName, version != null ? version : spec.getVersion(), port);
        
        AiModel model = modelRepository.createAiModel(modelCreate);
        
        log.info("Deployed model {} v{} on port {} with ID: {}", modelName, version, port, model.getId());
        return model;
    }

    /**
     * Deploys an MLflow model to a CUSTOM Docker host.
     * 
     * Use this to deploy models to Docker hosts other than the configured one.
     * 
     * @param modelName The MLflow model name
     * @param version The model version (null for latest)
     * @param customDockerHost The Docker host IP/hostname to deploy to
     * @param customDockerPort The Docker API port on the custom host
     * @param port The host port to expose (null for auto-assign)
     * @return The created AiModel
     * @throws IOException if deployment fails
     */
    public AiModel deployModelToHost(String modelName, String version, 
            String customDockerHost, int customDockerPort, Integer port) throws IOException {
        log.info("Deploying model from MLflow: {} v{} to custom Docker host {}:{}", 
                modelName, version != null ? version : "latest", customDockerHost, customDockerPort);
        
        // Ensure specification exists
        AiModelSpecification spec = importModelAsSpecification(modelName, version);
        
        // Deploy to custom Docker host and create AiModel
        AiModelCreate modelCreate = modelService.deployAndCreateTmfModelToHost(
            spec, modelName, version != null ? version : spec.getVersion(), 
            customDockerHost, customDockerPort, port);
        
        AiModel model = modelRepository.createAiModel(modelCreate);
        
        log.info("Deployed model {} v{} to host {} with ID: {}", 
                modelName, version, customDockerHost, model.getId());
        return model;
    }

    /**
     * Checks if a model is currently deployed.
     * 
     * @param modelName The model name
     * @param version The model version
     * @return true if deployed and serving
     */
    public boolean isModelDeployed(String modelName, String version) {
        return modelService.isDeployed(modelName, version);
    }

    /**
     * Checks if a model is currently deployed on a CUSTOM Docker host.
     * 
     * @param modelName The model name
     * @param version The model version
     * @param customDockerHost The Docker host
     * @param customDockerPort The Docker API port
     * @return true if deployed and serving
     */
    public boolean isModelDeployedOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        return modelService.isDeployedOnHost(modelName, version, customDockerHost, customDockerPort);
    }

    /**
     * Stops a deployed model.
     * 
     * @param modelName The model name
     * @param version The model version
     * @throws IOException if stopping fails
     */
    public void stopDeployment(String modelName, String version) throws IOException {
        log.info("Stopping deployment: {} v{}", modelName, version);
        modelService.stopDeployment(modelName, version);
        log.info("Deployment stopped: {} v{}", modelName, version);
    }

    /**
     * Stops a deployed model on a CUSTOM Docker host.
     * 
     * @param modelName The model name
     * @param version The model version
     * @param customDockerHost The Docker host
     * @param customDockerPort The Docker API port
     */
    public void stopDeploymentOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        log.info("Stopping deployment on host {}: {} v{}", customDockerHost, modelName, version);
        modelService.stopDeploymentOnHost(modelName, version, customDockerHost, customDockerPort);
        log.info("Deployment stopped on host {}: {} v{}", customDockerHost, modelName, version);
    }

    /**
     * Gets the inference URL for a deployed model.
     * 
     * @param modelName The model name
     * @param version The model version
     * @return The inference URL, or null if not deployed
     */
    public String getDeploymentUrl(String modelName, String version) {
        return modelService.getInferenceUrl(modelName, version);
    }

    /**
     * Gets the inference URL for a model deployed on a CUSTOM Docker host.
     * 
     * @param modelName The model name
     * @param version The model version
     * @param customDockerHost The Docker host
     * @return The inference URL, or null if not deployed
     */
    public String getDeploymentUrlOnHost(String modelName, String version, String customDockerHost) {
        return modelService.getInferenceUrlOnHost(modelName, version, customDockerHost);
    }

    // ========================================
    // Query Operations
    // ========================================

    /**
     * Lists all registered models in MLflow.
     * 
     * @return List of model names
     * @throws IOException if MLflow cannot be accessed
     */
    public List<String> listMlflowModels() throws IOException {
        return mlflowClient.listRegisteredModels();
    }

    /**
     * Checks if a model exists in MLflow.
     * 
     * @param modelName The model name
     * @return true if model exists
     */
    public boolean modelExistsInMlflow(String modelName) {
        return mlflowClient.modelExists(modelName);
    }

    /**
     * Gets the MLflow tracking URI.
     * 
     * @return The tracking URI
     */
    public String getMlflowTrackingUri() {
        return mlflowClient.getTrackingUri();
    }

    /**
     * Gets the MLflow UI URL for a model.
     * 
     * @param modelName The model name
     * @param version Optional version
     * @return URL to view in MLflow UI
     */
    public String getMlflowModelUrl(String modelName, String version) {
        return mlflowClient.buildModelUrl(modelName, version);
    }

    // ========================================
    // Artifact Operations
    // ========================================

    /**
     * Downloads an artifact for a model.
     * 
     * @param modelName The model name
     * @param artifactPath The artifact path
     * @return File pointing to the artifact, or null if not found
     */
    public java.io.File downloadArtifact(String modelName, String artifactPath) {
        return mlflowClient.downloadArtifactForModel(modelName, artifactPath);
    }

    /**
     * Lists available artifacts for a model.
     * 
     * @param modelName The model name
     * @return List of artifact paths
     */
    public List<String> listArtifacts(String modelName) {
        var version = mlflowClient.getLatestModelVersion(modelName);
        if (version == null || version.getRunId() == null) {
            return new ArrayList<>();
        }
        return mlflowClient.listArtifacts(version.getRunId());
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Builds the base URL for API endpoints.
     */
    private String getBaseUrl() {
        return "https://api.example.com/:" + serverPort + contextPath;
    }
}
