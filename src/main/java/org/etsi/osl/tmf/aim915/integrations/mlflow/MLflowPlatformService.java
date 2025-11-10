package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.etsi.osl.tmf.aim915.model.CharacteristicValueSpecification;
import org.etsi.osl.tmf.aim915.model.CharacteristicSpecification;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for integrating with MLflow as a platform
 * 
 * In this approach:
 * - AiModelSpecification represents the MLflow platform itself (single, static specification)
 * - AiModel instances represent individual models in MLflow registry
 * 
 * MLflow is an open-source platform for managing the ML lifecycle, including:
 * - Experiment tracking
 * - Model registry
 * - Model deployment
 * - Model versioning
 * 
 * Benefits of this platform approach:
 * - Single specification for the entire MLflow platform
 * - Individual models are instances of the platform specification
 * - Scalable for organizations with many models
 * - Clear separation between platform capabilities and model-specific attributes
 */
@Service
public class MLflowPlatformService {

    private static final Logger log = LoggerFactory.getLogger(MLflowPlatformService.class);
    private static final String PLATFORM_SPEC_NAME = "MLflow Platform";
    private static final String PLATFORM_SPEC_VERSION = "1.0";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String mlflowTrackingUri;
    private final AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService;

    public MLflowPlatformService(@Value("${mlflow.tracking.uri:http://localhost:5000}") String mlflowTrackingUri,
                                  AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.mlflowTrackingUri = mlflowTrackingUri;
        this.aiModelSpecificationRepositoryService = aiModelSpecificationRepositoryService;
        
        log.info("Initializing MLflowPlatformService with tracking URI: {}", mlflowTrackingUri);
    }
    
    /**
     * Creates or retrieves the single MLflow platform specification
     * This specification models the platform itself, not individual models
     * 
     * @return The MLflow platform AiModelSpecification
     */
    public AiModelSpecification getOrCreatePlatformSpecification() {
        log.debug("Looking for existing MLflow platform specification");
        
        // Try to find existing specification
        AiModelSpecification existingSpec = aiModelSpecificationRepositoryService
            .findAiModelSpecificationByNameAndVersion(PLATFORM_SPEC_NAME, PLATFORM_SPEC_VERSION);
        
        if (existingSpec != null) {
            log.debug("Found existing MLflow platform specification with ID: {}", existingSpec.getId());
            return existingSpec;
        }
        
        log.info("Creating new MLflow platform specification");
        
        // Create new platform specification
        AiModelSpecificationCreate specCreate = new AiModelSpecificationCreate();
        specCreate.setName(PLATFORM_SPEC_NAME);
        specCreate.setVersion(PLATFORM_SPEC_VERSION);
        specCreate.setDescription("MLflow - An open source platform for the machine learning lifecycle, including "
            + "experimentation, reproducibility, deployment, and a central model registry. "
            + "Supports various ML frameworks including TensorFlow, PyTorch, Scikit-learn, and more.");
        
        // Model characteristics template - describes what MLflow models have
        
        // 1. platformUrl - Model registry URL
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "platformUrl",
                "MLflow model registry URL where model is registered"
            )
        );
        
        // 2. apiUrl - Model version API endpoint
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "apiUrl", 
                "MLflow REST API endpoint for model version operations"
            )
        );
        
        // 3. modelType - Model flavor/framework
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "modelType",
                "MLflow model flavor (e.g., sklearn, tensorflow, pytorch, xgboost, etc.)"
            )
        );
        
        // 4. supportedLibraries - Framework/library used
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "supportedLibraries",
                "Machine learning framework or library used to create the model"
            )
        );
        
        // 5. provider - Model creator/owner
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "provider",
                "User or organization that registered the model in MLflow"
            )
        );
        
        // 6. deploymentArtifactsUrl - Model artifacts download URL
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "deploymentArtifactsUrl",
                "URL to download model artifacts in tar.gz format"
            )
        );
        
        // 7. totalSize - Model artifacts size
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "totalSize (in bytes)",
                "Total size of model artifacts in bytes"
            )
        );
        
        // 8. tags - Model tags and metadata
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "tags",
                "Tags and metadata associated with the model"
            )
        );        // Set TMF 915 field descriptions for model template
        specCreate.setModelDataSheet("The URL to the location of the model data sheet (model card)");
        specCreate.setDeploymentRecord("The URL to the artifact or the location of the deployment record");
        specCreate.setInheritedModel("The URL to the artifact or the location of the inherited model");
        specCreate.setModelEvaluationData("The URL to the artifact or the location of the model evaluation data");
        specCreate.setModelTrainingData("The URL to the artifact or the location of the model training data");
        
        log.debug("Creating platform specification in repository");
        AiModelSpecification createdSpec = aiModelSpecificationRepositoryService.createAiModelSpecification(specCreate);
        log.info("Successfully created MLflow platform specification with ID: {}", createdSpec.getId());
        
        return createdSpec;
    }
    
    /**
     * Helper method to create a characteristic specification with default value
     */
    private CharacteristicSpecification createCharacteristicSpec(String name, String description, 
                                                                  String valueType, String defaultValue) {
        CharacteristicSpecification charSpec = new CharacteristicSpecification();
        charSpec.setName(name);
        charSpec.setDescription(description);
        
        CharacteristicValueSpecification valueSpec = new CharacteristicValueSpecification();
        valueSpec.setValueType(valueType);
        valueSpec.setValue(defaultValue);
        valueSpec.setIsDefault(true);
        
        charSpec.addCharacteristicValueSpecificationItem(valueSpec);
        
        return charSpec;
    }
    
    /**
     * Helper method to create a characteristic specification template (no default value)
     */
    private CharacteristicSpecification createCharacteristicSpec(String name, String description) {
        CharacteristicSpecification charSpec = new CharacteristicSpecification();
        charSpec.setName(name);
        charSpec.setDescription(description);
        
        return charSpec;
    }
    
    /**
     * Retrieves registered model information from MLflow
     * 
     * @param modelName The registered model name in MLflow
     * @return JsonNode containing model metadata
     * @throws IOException if model cannot be accessed
     */
    public JsonNode getRegisteredModel(String modelName) throws IOException {
        log.debug("Fetching registered model: {}", modelName);
        String url = mlflowTrackingUri + "/api/2.0/mlflow/registered-models/get?name=" + modelName;
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            log.debug("Successfully retrieved registered model: {}", modelName);
            return objectMapper.readTree(response.getBody()).get("registered_model");
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND) {
                log.warn("Registered model not found: {}", modelName);
                throw new IOException("Registered model '" + modelName + "' not found in MLflow registry", e);
                
            } else {
                log.error("HTTP error accessing model {}: {} - {}", modelName, e.getStatusCode(), e.getMessage());
                throw new IOException("Error accessing model '" + modelName + "': " + 
                                     e.getStatusCode() + " - " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error accessing model {}: {}", modelName, e.getMessage(), e);
            throw new IOException("Unexpected error accessing model '" + modelName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves a specific model version from MLflow
     * 
     * @param modelName The registered model name
     * @param version The model version number
     * @return JsonNode containing model version metadata
     * @throws IOException if model version cannot be accessed
     */
    public JsonNode getModelVersion(String modelName, String version) throws IOException {
        log.debug("Fetching model version: {} v{}", modelName, version);
        String url = mlflowTrackingUri + "/api/2.0/mlflow/model-versions/get?name=" + modelName + "&version=" + version;
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            log.debug("Successfully retrieved model version: {} v{}", modelName, version);
            return objectMapper.readTree(response.getBody()).get("model_version");
            
        } catch (Exception e) {
            log.error("Error accessing model version {} v{}: {}", modelName, version, e.getMessage());
            throw new IOException("Error accessing model version: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lists all registered models in MLflow
     * 
     * @return List of registered model names
     * @throws IOException if listing fails
     */
    public List<String> listRegisteredModels() throws IOException {
        log.debug("Listing all registered models");
        String url = mlflowTrackingUri + "/api/2.0/mlflow/registered-models/list";
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode models = root.get("registered_models");
            
            List<String> modelNames = new ArrayList<>();
            if (models != null && models.isArray()) {
                for (JsonNode model : models) {
                    if (model.has("name")) {
                        modelNames.add(model.get("name").asText());
                    }
                }
            }
            
            log.debug("Found {} registered models", modelNames.size());
            return modelNames;
            
        } catch (Exception e) {
            log.error("Error listing registered models: {}", e.getMessage());
            throw new IOException("Error listing registered models: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts an MLflow registered model to an AiModelCreate
     * The model will be associated with the platform specification
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version (optional, uses latest if null)
     * @param baseUrl The base URL for the TMF API
     * @return AiModelCreate object representing the specific model
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelCreate mlflowModelToAiModelCreate(String modelName, String version, String baseUrl) throws IOException {
        log.debug("Converting MLflow model to AiModelCreate: {} v{}", modelName, version);
        
        // Get or create the platform specification
        AiModelSpecification platformSpec = getOrCreatePlatformSpecification();
        
        // Get model information
        JsonNode registeredModel = getRegisteredModel(modelName);
        JsonNode modelVersion = null;
        
        if (version != null) {
            modelVersion = getModelVersion(modelName, version);
        } else {
            // Get latest version
            JsonNode latestVersions = registeredModel.get("latest_versions");
            if (latestVersions != null && latestVersions.isArray() && latestVersions.size() > 0) {
                modelVersion = latestVersions.get(0);
                version = modelVersion.get("version").asText();
            }
        }
        
        AiModelCreate aiModelCreate = new AiModelCreate();
        
        // Associate with platform specification
        aiModelCreate.setAiModelSpecification(platformSpec);
        
        // Set model name and description
        String fullName = modelName + (version != null ? " v" + version : "");
        aiModelCreate.setName(fullName);
        
        String description = "MLflow model: " + modelName;
        if (registeredModel.has("description") && !registeredModel.get("description").isNull()) {
            description = registeredModel.get("description").asText();
        }
        aiModelCreate.setDescription(description);
        
        // Service characteristics - model-specific attributes matching specification
        
        // 1. platformUrl - Model registry URL
        String modelUrl = mlflowTrackingUri + "/#/models/" + modelName;
        if (version != null) {
            modelUrl += "/versions/" + version;
        }
        addCharacteristic(aiModelCreate, "platformUrl", modelUrl, "string");
        
        // 2. apiUrl - Model version API endpoint  
        String apiUrl = mlflowTrackingUri + "/api/2.0/mlflow/registered-models/get?name=" + modelName;
        if (version != null) {
            apiUrl = mlflowTrackingUri + "/api/2.0/mlflow/model-versions/get?name=" + modelName + "&version=" + version;
        }
        addCharacteristic(aiModelCreate, "apiUrl", apiUrl, "string");
        
        // 3. modelType - Model flavor/framework (from model version)
        if (modelVersion != null && modelVersion.has("flavors")) {
            JsonNode flavors = modelVersion.get("flavors");
            if (flavors.isObject()) {
                String[] flavorNames = {"sklearn", "tensorflow", "pytorch", "keras", "xgboost", "lightgbm", "spark", "python_function"};
                for (String flavorName : flavorNames) {
                    if (flavors.has(flavorName)) {
                        addCharacteristic(aiModelCreate, "modelType", flavorName, "string");
                        break;
                    }
                }
            }
        }
        
        // 4. supportedLibraries - Framework/library (inferred from model type)
        if (modelVersion != null && modelVersion.has("flavors")) {
            JsonNode flavors = modelVersion.get("flavors");
            if (flavors.isObject()) {
                if (flavors.has("sklearn")) {
                    addCharacteristic(aiModelCreate, "supportedLibraries", "scikit-learn", "string");
                } else if (flavors.has("tensorflow")) {
                    addCharacteristic(aiModelCreate, "supportedLibraries", "tensorflow", "string");
                } else if (flavors.has("pytorch")) {
                    addCharacteristic(aiModelCreate, "supportedLibraries", "pytorch", "string");
                } else if (flavors.has("xgboost")) {
                    addCharacteristic(aiModelCreate, "supportedLibraries", "xgboost", "string");
                } else {
                    addCharacteristic(aiModelCreate, "supportedLibraries", "mlflow", "string");
                }
            }
        }
        
        // 5. provider - Model creator/owner
        if (modelVersion != null && modelVersion.has("user_id")) {
            String userId = modelVersion.get("user_id").asText();
            addCharacteristic(aiModelCreate, "provider", userId, "string");
        }
        
        // 6. deploymentArtifactsUrl - Model artifacts download URL
        if (modelVersion != null && modelVersion.has("source")) {
            String source = modelVersion.get("source").asText();
            addCharacteristic(aiModelCreate, "deploymentArtifactsUrl", source + ".tar.gz", "string");
        }
        
        // 7. totalSize (in bytes) - Model artifacts size (if available)
        // Note: MLflow doesn't provide size directly, would need to inspect artifacts
        addCharacteristic(aiModelCreate, "totalSize (in bytes)", "0", "integer");
        
        // 8. tags - Model tags and metadata
        if (registeredModel.has("tags")) {
            JsonNode tags = registeredModel.get("tags");
            if (tags.isArray()) {
                List<String> tagList = new ArrayList<>();
                for (JsonNode tag : tags) {
                    if (tag.has("key") && tag.has("value")) {
                        tagList.add(tag.get("key").asText() + ":" + tag.get("value").asText());
                    }
                }
                if (!tagList.isEmpty()) {
                    addCharacteristic(aiModelCreate, "tags", String.join(",", tagList), "array");
                }
            }
        }
        
        // Additional TMF 915 standard fields
        
        // Model Data Sheet - MLflow model page
        addCharacteristic(aiModelCreate, "modelDataSheet", modelUrl, "string");
        
        // Deployment Record - MLflow serving endpoint
        String servingUri = mlflowTrackingUri.replace(":5000", ":5001") + "/invocations";
        addCharacteristic(aiModelCreate, "deploymentRecord", servingUri, "string");
        
        // Inherited Model (base model) - if available from model metadata
        // Note: MLflow doesn't have direct parent model concept, could be derived from run lineage
        
        // Model Training Data - experiment run URL
        if (modelVersion != null && modelVersion.has("run_id")) {
            String runId = modelVersion.get("run_id").asText();
            String runUrl = mlflowTrackingUri + "/#/experiments/0/runs/" + runId;
            addCharacteristic(aiModelCreate, "modelTrainingData", runUrl, "string");
        }
        
        // Model Evaluation Data - metrics from the run
        if (modelVersion != null && modelVersion.has("run_id")) {
            String runId = modelVersion.get("run_id").asText();
            String metricsUrl = mlflowTrackingUri + "/api/2.0/mlflow/runs/get?run_id=" + runId;
            addCharacteristic(aiModelCreate, "modelEvaluationData", metricsUrl, "string");
        }
        
        // Additional model-specific metadata (not in specification)
        
        // Model name for reference
        addCharacteristic(aiModelCreate, "modelName", modelName, "string");
        
        // Model version
        if (version != null) {
            addCharacteristic(aiModelCreate, "modelVersion", version, "string");
        }
        
        // Current stage
        if (modelVersion != null && modelVersion.has("current_stage")) {
            String stage = modelVersion.get("current_stage").asText();
            addCharacteristic(aiModelCreate, "currentStage", stage, "string");
        }
        
        // Run ID
        if (modelVersion != null && modelVersion.has("run_id")) {
            String runId = modelVersion.get("run_id").asText();
            addCharacteristic(aiModelCreate, "runId", runId, "string");
        }
        
        log.info("Successfully converted MLflow model {} to AiModelCreate with platform specification", fullName);
        return aiModelCreate;
    }
    
    /**
     * Helper method to add a characteristic to an AiModelCreate
     */
    private void addCharacteristic(AiModelCreate aiModel, String name, String value, String valueType) {
        Characteristic characteristic = new Characteristic();
        characteristic.setName(name);
        characteristic.setValue(new Any(value));
        characteristic.setValueType(valueType);
        aiModel.addServiceCharacteristicItem(characteristic);
    }
    
    /**
     * Validates if a model exists in MLflow registry
     * 
     * @param modelName The registered model name
     * @return true if model exists and is accessible
     */
    public boolean validateModelExists(String modelName) {
        try {
            log.debug("Validating model existence: {}", modelName);
            getRegisteredModel(modelName);
            log.debug("Model validated successfully: {}", modelName);
            return true;
        } catch (IOException e) {
            log.debug("Model validation failed for {}: {}", modelName, e.getMessage());
            return false;
        }
    }
}
