package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.etsi.osl.tmf.aim915.model.*;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Main orchestration service for Hugging Face integration with TMF 915.
 * 
 * This service provides the high-level API for:
 * - Importing Hugging Face models as AiModelSpecifications
 * - Creating AiModel instances from specifications
 * - Managing the lifecycle of AI models
 * - Downloading artifacts
 * 
 * Architecture:
 * - AiModelSpecification: Describes a model on Hugging Face (blueprint)
 * - AiModel: Represents a deployed/used model instance
 * 
 * When a model is imported from Hugging Face → Create AiModelSpecification
 * When a model is deployed/used → Create AiModel from specification
 */
@Service
public class HuggingFaceIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceIntegrationService.class);

    private final HuggingFaceClientService hfClient;
    private final HuggingFaceSpecificationService specificationService;
    private final HuggingFaceModelService modelService;
    private final AiModelSpecificationRepositoryService specificationRepository;
    private final AiModelRepositoryService modelRepository;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${server.port:13082}")
    private int serverPort;

    public HuggingFaceIntegrationService(
            HuggingFaceClientService hfClient,
            HuggingFaceSpecificationService specificationService,
            HuggingFaceModelService modelService,
            AiModelSpecificationRepositoryService specificationRepository,
            AiModelRepositoryService modelRepository) {
        this.hfClient = hfClient;
        this.specificationService = specificationService;
        this.modelService = modelService;
        this.specificationRepository = specificationRepository;
        this.modelRepository = modelRepository;
        log.info("HuggingFaceIntegrationService initialized");
    }

    // ========================================
    // Specification Operations
    // ========================================

    /**
     * Imports a Hugging Face model as an AiModelSpecification.
     * 
     * Creates a new specification or returns existing one if it already exists.
     * 
     * @param modelId The Hugging Face model ID (e.g., "bert-base-uncased", "openai/whisper-large")
     * @return The created or existing AiModelSpecification
     * @throws IOException if model cannot be accessed
     */
    public AiModelSpecification importModelAsSpecification(String modelId) throws IOException {
        log.info("Importing Hugging Face model as specification: {}", modelId);

        // Check if specification already exists
        // Use model ID as name and a derived version
        AiModelSpecification existing = specificationRepository.findAiModelSpecificationByName(modelId);
        if (existing != null) {
            log.info("Specification already exists for {}, returning existing (ID: {})", modelId, existing.getId());
            return existing;
        }

        // Create new specification
        String baseUrl = getBaseUrl();
        AiModelSpecificationCreate specCreate = specificationService.createSpecificationFromHuggingFace(modelId, baseUrl);
        AiModelSpecification spec = specificationRepository.createAiModelSpecification(specCreate);
        
        log.info("Created specification for {} with ID: {}", modelId, spec.getId());
        return spec;
    }

    /**
     * Imports multiple Hugging Face models as specifications.
     * 
     * @param modelIds List of model IDs to import
     * @return List of created/existing specifications
     */
    public List<AiModelSpecification> importModelsAsSpecifications(List<String> modelIds) {
        log.info("Importing {} Hugging Face models as specifications", modelIds.size());
        
        List<AiModelSpecification> specs = new ArrayList<>();
        
        for (String modelId : modelIds) {
            try {
                AiModelSpecification spec = importModelAsSpecification(modelId);
                specs.add(spec);
            } catch (Exception e) {
                log.warn("Failed to import model {}: {}", modelId, e.getMessage());
            }
        }
        
        log.info("Successfully imported {} specifications", specs.size());
        return specs;
    }

    /**
     * Searches for models on Hugging Face and imports them as specifications.
     * 
     * @param searchQuery The search query
     * @param limit Maximum number of models to import
     * @return List of created specifications
     * @throws IOException if search fails
     */
    public List<AiModelSpecification> searchAndImportModels(String searchQuery, int limit) throws IOException {
        log.info("Searching and importing Hugging Face models: query='{}', limit={}", searchQuery, limit);
        
        List<String> modelIds = hfClient.searchModels(searchQuery, limit);
        return importModelsAsSpecifications(modelIds);
    }

    // ========================================
    // Model Instance Operations
    // ========================================

    /**
     * Deploys a Hugging Face model, creating both specification and model instance.
     * 
     * @param modelId The Hugging Face model ID
     * @param deploymentEndpoint Optional deployment endpoint URL
     * @return The created AiModel instance
     * @throws IOException if model cannot be accessed
     */
    public AiModel deployModelFromHuggingFace(String modelId, String deploymentEndpoint) throws IOException {
        log.info("Deploying Hugging Face model: {} to {}", modelId, deploymentEndpoint);

        // First, ensure specification exists
        AiModelSpecification spec = importModelAsSpecification(modelId);

        // Create model instance
        String baseUrl = getBaseUrl();
        AiModelCreate modelCreate = modelService.createModelFromDeployment(spec, modelId, deploymentEndpoint, baseUrl);
        AiModel model = modelRepository.createAiModel(modelCreate);

        log.info("Deployed model {} with ID: {}", modelId, model.getId());
        return model;
    }

    /**
     * Creates an AiModel instance from an existing specification.
     * 
     * @param specificationId The ID of the specification
     * @param instanceName Optional name for the instance
     * @param deploymentEndpoint Optional deployment endpoint
     * @return The created AiModel
     */
    public AiModel createModelInstance(String specificationId, String instanceName, String deploymentEndpoint) {
        log.info("Creating model instance from specification: {}", specificationId);

        AiModelSpecification spec = specificationRepository.findByUuid(specificationId);
        if (spec == null) {
            throw new IllegalArgumentException("Specification not found: " + specificationId);
        }

        // Get model ID from specification
        String modelId = spec.getName();
        
        AiModelCreate modelCreate = new AiModelCreate();
        modelCreate.setAiModelSpecification(spec);
        modelCreate.setName(instanceName != null ? instanceName : modelId + "-instance");
        modelCreate.setDescription("Model instance of " + spec.getName());
        modelCreate.setState(org.etsi.osl.tmf.common.model.service.ServiceStateType.ACTIVE);

        // Add deployment endpoint if provided
        if (deploymentEndpoint != null && !deploymentEndpoint.isEmpty()) {
            org.etsi.osl.tmf.common.model.service.Characteristic endpointChar = 
                new org.etsi.osl.tmf.common.model.service.Characteristic();
            endpointChar.setName("deploymentEndpoint");
            endpointChar.setValue(new org.etsi.osl.tmf.common.model.Any(deploymentEndpoint));
            endpointChar.setValueType("string");
            modelCreate.addServiceCharacteristicItem(endpointChar);
        }

        AiModel model = modelRepository.createAiModel(modelCreate);
        log.info("Created model instance {} with ID: {}", model.getName(), model.getId());
        
        return model;
    }

    // ========================================
    // Query Operations
    // ========================================

    /**
     * Searches for models on Hugging Face.
     * 
     * @param query Search query
     * @param limit Maximum results
     * @return List of model IDs
     * @throws IOException if search fails
     */
    public List<String> searchModels(String query, int limit) throws IOException {
        return hfClient.searchModels(query, limit);
    }

    /**
     * Validates if a model exists on Hugging Face.
     * 
     * @param modelId The model ID to check
     * @return true if model exists
     */
    public boolean modelExists(String modelId) {
        return hfClient.validateModelExists(modelId);
    }

    /**
     * Finds similar models on Hugging Face.
     * 
     * @param partialModelId Partial or incorrect model ID
     * @param limit Maximum suggestions
     * @return List of similar model IDs
     */
    public List<String> findSimilarModels(String partialModelId, int limit) {
        return hfClient.findSimilarModels(partialModelId, limit);
    }

    /**
     * Lists files in a Hugging Face model.
     * 
     * @param modelId The model ID
     * @return List of file names
     * @throws IOException if model cannot be accessed
     */
    public List<String> listModelFiles(String modelId) throws IOException {
        return hfClient.listModelFiles(modelId);
    }

    // ========================================
    // Artifact Operations
    // ========================================

    /**
     * Downloads a specific file from a Hugging Face model.
     * 
     * @param modelId The model ID
     * @param filename The file to download
     * @return The downloaded file
     * @throws IOException if download fails
     */
    public java.io.File downloadArtifact(String modelId, String filename) throws IOException {
        return hfClient.downloadModelFile(modelId, filename);
    }

    /**
     * Streams a file from a Hugging Face model.
     * 
     * @param modelId The model ID
     * @param filename The file to stream
     * @return InputStream for the file
     * @throws IOException if streaming fails
     */
    public java.io.InputStream streamArtifact(String modelId, String filename) throws IOException {
        return hfClient.streamModelFile(modelId, filename);
    }

    /**
     * Gets the size of a file in a Hugging Face model.
     * 
     * @param modelId The model ID
     * @param filename The file name
     * @return File size in bytes, or -1 if unknown
     * @throws IOException if model cannot be accessed
     */
    public long getFileSize(String modelId, String filename) throws IOException {
        return hfClient.getFileSize(modelId, filename);
    }

    /**
     * Downloads deployment artifacts as tar.gz.
     * 
     * @param modelId The model ID
     * @return byte array containing tar.gz
     * @throws IOException if download fails
     */
    public byte[] downloadDeploymentArtifacts(String modelId) throws IOException {
        return hfClient.downloadDeploymentArtifactsAsTargz(modelId);
    }

    /**
     * Streams deployment artifacts as tar.gz to an output stream.
     * 
     * @param modelId The model ID
     * @param outputStream The output stream
     * @throws IOException if streaming fails
     */
    public void streamDeploymentArtifacts(String modelId, OutputStream outputStream) throws IOException {
        hfClient.streamDeploymentArtifactsAsTargz(modelId, outputStream);
    }

    // ========================================
    // Information Operations
    // ========================================

    /**
     * Gets the Hugging Face Hub URL.
     * 
     * @return The Hub URL
     */
    public String getHubUrl() {
        return hfClient.getHubUrl();
    }

    /**
     * Gets the Hub URL for a specific model.
     * 
     * @param modelId The model ID
     * @return The model's Hub URL
     */
    public String getModelHubUrl(String modelId) {
        return hfClient.getModelHubUrl(modelId);
    }

    /**
     * Checks if an API token is configured.
     * 
     * @return true if token is configured
     */
    public boolean hasApiToken() {
        return hfClient.hasApiToken();
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String getBaseUrl() {
        return "http://localhost:" + serverPort + contextPath;
    }

    /**
     * Converts a URL-safe model ID back to the original format.
     * 
     * @param urlSafeModelId Model ID with underscores
     * @return Original model ID with slashes
     */
    public String fromUrlSafeModelId(String urlSafeModelId) {
        return urlSafeModelId.replace("_", "/");
    }

    /**
     * Converts a model ID to URL-safe format.
     * 
     * @param modelId Original model ID
     * @return URL-safe model ID with underscores
     */
    public String toUrlSafeModelId(String modelId) {
        return modelId.replace("/", "_");
    }
}
