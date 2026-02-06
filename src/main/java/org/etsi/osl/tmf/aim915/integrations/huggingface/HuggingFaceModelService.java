package org.etsi.osl.tmf.aim915.integrations.huggingface;

import com.fasterxml.jackson.databind.JsonNode;
import org.etsi.osl.tmf.aim915.model.*;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for creating AiModel instances from Hugging Face deployed/used models.
 * 
 * In this design:
 * - AiModel represents a deployed/used model instance
 * - It references an AiModelSpecification (the model blueprint from Hugging Face)
 * - Deployment-specific information (endpoints, state) is captured here
 * 
 * A model becomes an AiModel when:
 * - It's deployed to a serving endpoint
 * - It's instantiated for inference
 * - A user creates an instance from a specification
 */
@Service
public class HuggingFaceModelService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceModelService.class);

    private final HuggingFaceClientService hfClient;

    @Value("${aimodel.deployment.default-platform:huggingface}")
    private String defaultDeploymentPlatform;

    @Value("${aimodel.deployment.serving-base-url:}")
    private String servingBaseUrl;

    public HuggingFaceModelService(HuggingFaceClientService hfClient) {
        this.hfClient = hfClient;
    }

    /**
     * Creates an AiModelCreate from a Hugging Face model that is being deployed/used.
     * 
     * The AiModel represents the actual running/deployed instance of a model.
     * 
     * @param specification The AiModelSpecification this model instantiates
     * @param modelId The Hugging Face model ID
     * @param deploymentEndpoint The endpoint where the model is served (optional)
     * @param baseUrl The base URL for the TMF API
     * @return AiModelCreate ready to be persisted
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelCreate createModelFromDeployment(AiModelSpecification specification, 
            String modelId, String deploymentEndpoint, String baseUrl) throws IOException {
        
        log.info("Creating AiModel from Hugging Face model: {}", modelId);

        JsonNode modelInfo = hfClient.getModelInfo(modelId);
        
        return createModelInstance(specification, modelId, deploymentEndpoint, baseUrl, modelInfo);
    }

    /**
     * Creates an AiModelCreate without fetching model info (when already available).
     */
    public AiModelCreate createModelInstance(AiModelSpecification specification, 
            String modelId, String deploymentEndpoint, String baseUrl, JsonNode modelInfo) {
        
        AiModelCreate model = new AiModelCreate();
        
        // Link to specification
        model.setAiModelSpecification(specification);
        
        // Basic identity
        model.setName(modelId);
        model.setDescription(buildDescription(modelInfo, modelId, deploymentEndpoint));
        
        // Set state to ACTIVE (deployed)
        model.setState(ServiceStateType.ACTIVE);
        
        // Add service characteristics
        addCoreCharacteristics(model, modelId, modelInfo);
        addDeploymentCharacteristics(model, modelId, deploymentEndpoint, baseUrl);
        addMetadataCharacteristics(model, modelInfo);
        
        log.info("Created AiModel for Hugging Face model: {}", modelId);
        return model;
    }

    /**
     * Adds core model characteristics.
     */
    private void addCoreCharacteristics(AiModelCreate model, String modelId, JsonNode modelInfo) {
        
        // Hugging Face model ID
        addCharacteristic(model, "huggingFaceModelId", modelId, "string");
        
        // Repository URL
        addCharacteristic(model, "repositoryUrl", hfClient.getModelHubUrl(modelId), "string");
        
        // Model card URL
        addCharacteristic(model, "modelCardUrl", hfClient.getModelHubUrl(modelId), "string");
        
        // Pipeline tag (model type/task)
        String pipelineTag = getTextValue(modelInfo, "pipeline_tag");
        if (pipelineTag != null) {
            addCharacteristic(model, "pipelineTag", pipelineTag, "string");
        }
        
        // Library name
        String libraryName = getTextValue(modelInfo, "library_name");
        if (libraryName != null) {
            addCharacteristic(model, "libraryName", libraryName, "string");
        }
        
        // Author
        String author = getTextValue(modelInfo, "author");
        if (author != null) {
            addCharacteristic(model, "author", author, "string");
        }
    }

    /**
     * Adds deployment-specific characteristics.
     */
    private void addDeploymentCharacteristics(AiModelCreate model, String modelId, 
            String deploymentEndpoint, String baseUrl) {
        
        // Deployment platform
        addCharacteristic(model, "deploymentPlatform", defaultDeploymentPlatform, "string");
        
        // Deployment endpoint (if specified)
        if (deploymentEndpoint != null && !deploymentEndpoint.isEmpty()) {
            addCharacteristic(model, "deploymentEndpoint", deploymentEndpoint, "string");
        } else if (servingBaseUrl != null && !servingBaseUrl.isEmpty()) {
            // Use default serving base URL
            addCharacteristic(model, "deploymentEndpoint", 
                servingBaseUrl + "/huggingface/" + modelId.replace("/", "_"), "string");
        }
        
        // Inference API URL (Hugging Face hosted inference)
        addCharacteristic(model, "inferenceApiUrl", 
            "https://api-inference.huggingface.co/models/" + modelId, "string");
        
        // Download URL for artifacts
        String urlSafeModelId = modelId.replace("/", "_");
        addCharacteristic(model, "artifactsDownloadUrl", 
            baseUrl + "/huggingface/models/" + urlSafeModelId + "/artifacts/deployment.tar.gz", "string");
        
        // Individual artifact download URL pattern
        addCharacteristic(model, "artifactDownloadPattern", 
            baseUrl + "/huggingface/models/" + urlSafeModelId + "/artifacts/{filename}", "string");
    }

    /**
     * Adds metadata characteristics.
     */
    private void addMetadataCharacteristics(AiModelCreate model, JsonNode modelInfo) {
        
        // Downloads count
        if (modelInfo.has("downloads")) {
            addCharacteristic(model, "downloadCount", 
                String.valueOf(modelInfo.get("downloads").asLong()), "integer");
        }
        
        // Likes count
        if (modelInfo.has("likes")) {
            addCharacteristic(model, "likesCount", 
                String.valueOf(modelInfo.get("likes").asInt()), "integer");
        }
        
        // Last modified
        String lastModified = getTextValue(modelInfo, "lastModified");
        if (lastModified != null) {
            addCharacteristic(model, "lastModified", lastModified, "datetime");
        }
        
        // SHA (version)
        String sha = getTextValue(modelInfo, "sha");
        if (sha != null) {
            addCharacteristic(model, "commitSha", sha, "string");
        }
        
        // Tags
        if (modelInfo.has("tags")) {
            JsonNode tags = modelInfo.get("tags");
            if (tags.isArray()) {
                List<String> tagList = new ArrayList<>();
                for (JsonNode tag : tags) {
                    tagList.add(tag.asText());
                }
                addCharacteristic(model, "tags", String.join(",", tagList), "array");
            }
        }
        
        // Total size
        long totalSize = hfClient.calculateTotalSize(modelInfo);
        if (totalSize > 0) {
            addCharacteristic(model, "totalSizeBytes", String.valueOf(totalSize), "integer");
            addCharacteristic(model, "totalSizeHuman", formatBytes(totalSize), "string");
        }
        
        // Private status
        if (modelInfo.has("private")) {
            addCharacteristic(model, "isPrivate", 
                String.valueOf(modelInfo.get("private").asBoolean()), "boolean");
        }
        
        // Languages
        JsonNode languages = getNestedNode(modelInfo, "cardData", "language");
        if (languages != null) {
            String langValue;
            if (languages.isArray()) {
                List<String> langList = new ArrayList<>();
                for (JsonNode lang : languages) {
                    langList.add(lang.asText());
                }
                langValue = String.join(",", langList);
            } else {
                langValue = languages.asText();
            }
            addCharacteristic(model, "languages", langValue, "array");
        }
        
        // Base model (if fine-tuned)
        String baseModel = getNestedTextValue(modelInfo, "cardData", "base_model");
        if (baseModel != null) {
            addCharacteristic(model, "baseModel", baseModel, "string");
            addCharacteristic(model, "baseModelUrl", hfClient.getModelHubUrl(baseModel), "string");
        }
        
        // Datasets used
        JsonNode datasets = getNestedNode(modelInfo, "cardData", "datasets");
        if (datasets != null && datasets.isArray() && datasets.size() > 0) {
            List<String> datasetList = new ArrayList<>();
            for (JsonNode dataset : datasets) {
                datasetList.add(dataset.asText());
            }
            addCharacteristic(model, "trainingDatasets", String.join(",", datasetList), "array");
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String buildDescription(JsonNode modelInfo, String modelId, String deploymentEndpoint) {
        StringBuilder desc = new StringBuilder();
        desc.append("Deployed Hugging Face model: ").append(modelId);
        
        String pipelineTag = getTextValue(modelInfo, "pipeline_tag");
        if (pipelineTag != null) {
            desc.append(" (").append(pipelineTag).append(")");
        }
        
        if (deploymentEndpoint != null && !deploymentEndpoint.isEmpty()) {
            desc.append(" at ").append(deploymentEndpoint);
        }
        
        return desc.toString();
    }

    private void addCharacteristic(AiModelCreate model, String name, String value, String valueType) {
        Characteristic characteristic = new Characteristic();
        characteristic.setName(name);
        characteristic.setValue(new Any(value));
        characteristic.setValueType(valueType);
        model.addServiceCharacteristicItem(characteristic);
    }

    private String getTextValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    private String getNestedTextValue(JsonNode node, String... path) {
        JsonNode current = node;
        for (String fieldName : path) {
            if (current == null || !current.has(fieldName)) {
                return null;
            }
            current = current.get(fieldName);
        }
        return current != null && !current.isNull() ? current.asText() : null;
    }

    private JsonNode getNestedNode(JsonNode node, String... path) {
        JsonNode current = node;
        for (String fieldName : path) {
            if (current == null || !current.has(fieldName)) {
                return null;
            }
            current = current.get(fieldName);
        }
        return current;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
