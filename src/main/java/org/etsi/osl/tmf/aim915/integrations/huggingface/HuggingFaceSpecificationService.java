package org.etsi.osl.tmf.aim915.integrations.huggingface;

import com.fasterxml.jackson.databind.JsonNode;
import org.etsi.osl.tmf.aim915.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for converting Hugging Face models to TMF 915 AiModelSpecification.
 * 
 * In this design:
 * - AiModelSpecification describes a model on Hugging Face (the blueprint/template)
 * - Each Hugging Face model maps to one AiModelSpecification
 * - The specification captures the model's metadata, capabilities, and artifact locations
 * 
 * When a model is used/deployed, it becomes an AiModel instance.
 */
@Service
public class HuggingFaceSpecificationService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceSpecificationService.class);

    private final HuggingFaceClientService hfClient;

    @Value("${aimodel.specification.default-lifecycle-status:Active}")
    private String defaultLifecycleStatus;

    public HuggingFaceSpecificationService(HuggingFaceClientService hfClient) {
        this.hfClient = hfClient;
    }

    /**
     * Creates an AiModelSpecificationCreate from a Hugging Face model.
     * 
     * This specification describes a model on Hugging Face, including:
     * - Model identity (ID, description)
     * - Framework and type information (pipeline tag, library)
     * - Artifact URLs (model files, model card)
     * - Provider/author information
     * 
     * @param modelId The Hugging Face model ID (e.g., "bert-base-uncased", "openai/whisper-large")
     * @param baseUrl The base URL for the TMF API (for artifact endpoints)
     * @return AiModelSpecificationCreate ready to be persisted
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelSpecificationCreate createSpecificationFromHuggingFace(String modelId, String baseUrl) throws IOException {
        log.info("Creating AiModelSpecification from Hugging Face model: {}", modelId);

        JsonNode modelInfo = hfClient.getModelInfo(modelId);
        return buildSpecificationCreate(modelId, modelInfo, baseUrl);
    }

    /**
     * Builds the AiModelSpecificationCreate object from Hugging Face data.
     */
    private AiModelSpecificationCreate buildSpecificationCreate(String modelId, JsonNode modelInfo, String baseUrl) {
        
        AiModelSpecificationCreate spec = new AiModelSpecificationCreate();

        // Basic identity
        spec.setName(modelId);
        spec.setVersion(extractVersion(modelInfo));
        spec.setDescription(extractDescription(modelInfo, modelId));
        spec.setLifecycleStatus(defaultLifecycleStatus);

        // Set last update time
        String lastModified = getTextValue(modelInfo, "lastModified");
        if (lastModified != null) {
            try {
                spec.setLastUpdate(OffsetDateTime.parse(lastModified));
            } catch (Exception e) {
                log.debug("Could not parse lastModified date: {}", lastModified);
            }
        }

        // TMF 915 standard artifact URLs
        setArtifactUrls(spec, modelId, modelInfo, baseUrl);

        // Add specification characteristics
        addCoreCharacteristics(spec, modelId, modelInfo);
        addFrameworkCharacteristics(spec, modelInfo);
        addProviderCharacteristics(spec, modelInfo);
        addSizeCharacteristics(spec, modelInfo);
        addTagsCharacteristics(spec, modelInfo);

        log.info("Created AiModelSpecification for Hugging Face model: {}", modelId);
        return spec;
    }

    /**
     * Sets the TMF 915 standard artifact URLs.
     */
    private void setArtifactUrls(AiModelSpecificationCreate spec, String modelId, 
                                  JsonNode modelInfo, String baseUrl) {
        
        // Model Data Sheet - link to the model card
        spec.setModelDataSheet(hfClient.getModelHubUrl(modelId));

        // Deployment Record - link to the API for artifacts download
        String urlSafeModelId = modelId.replace("/", "_");
        spec.setDeploymentRecord(baseUrl + "/huggingface/models/" + urlSafeModelId + "/artifacts/deployment.tar.gz");

        // Check for base model information (inherited model)
        String baseModel = getNestedTextValue(modelInfo, "cardData", "base_model");
        if (baseModel != null && !baseModel.isEmpty()) {
            spec.setInheritedModel(hfClient.getModelHubUrl(baseModel));
        }

        // Training data - datasets used
        JsonNode datasets = getNestedNode(modelInfo, "cardData", "datasets");
        if (datasets != null && datasets.isArray() && datasets.size() > 0) {
            String firstDataset = datasets.get(0).asText();
            spec.setModelTrainingData("https://huggingface.co/datasets/" + firstDataset);
        }

        // Evaluation data - if available
        JsonNode evalDatasets = getNestedNode(modelInfo, "cardData", "eval_datasets");
        if (evalDatasets != null && evalDatasets.isArray() && evalDatasets.size() > 0) {
            String firstEvalDataset = evalDatasets.get(0).asText();
            spec.setModelEvaluationData("https://huggingface.co/datasets/" + firstEvalDataset);
        }
    }

    /**
     * Adds core model characteristics.
     */
    private void addCoreCharacteristics(AiModelSpecificationCreate spec, String modelId, JsonNode modelInfo) {
        
        // Model ID
        spec.addSpecCharacteristicItem(createCharacteristicSpec(
            "modelId", 
            "Hugging Face model identifier", 
            modelId
        ));
        
        // Repository URL
        spec.addSpecCharacteristicItem(createCharacteristicSpec(
            "repositoryUrl", 
            "URL to the model repository on Hugging Face Hub", 
            hfClient.getModelHubUrl(modelId)
        ));

        // API URL
        spec.addSpecCharacteristicItem(createCharacteristicSpec(
            "apiUrl", 
            "URL to the model API endpoint", 
            hfClient.getModelApiUrl(modelId)
        ));

        // Pipeline tag (model type/task)
        String pipelineTag = getTextValue(modelInfo, "pipeline_tag");
        if (pipelineTag != null) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "pipelineTag", 
                "The task this model is designed for (e.g., text-generation, image-classification)", 
                pipelineTag
            ));
        }

        // Downloads count
        if (modelInfo.has("downloads")) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "downloads", 
                "Number of downloads from Hugging Face Hub", 
                String.valueOf(modelInfo.get("downloads").asLong())
            ));
        }

        // Likes count
        if (modelInfo.has("likes")) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "likes", 
                "Number of likes on Hugging Face Hub", 
                String.valueOf(modelInfo.get("likes").asInt())
            ));
        }

        // Private status
        if (modelInfo.has("private")) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "isPrivate", 
                "Whether the model is private", 
                String.valueOf(modelInfo.get("private").asBoolean())
            ));
        }

        // Gated access
        if (modelInfo.has("gated")) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "isGated", 
                "Whether the model has gated access", 
                String.valueOf(modelInfo.get("gated").asBoolean())
            ));
        }
    }

    /**
     * Adds framework-related characteristics.
     */
    private void addFrameworkCharacteristics(AiModelSpecificationCreate spec, JsonNode modelInfo) {
        
        // Library name (transformers, diffusers, etc.)
        String libraryName = getTextValue(modelInfo, "library_name");
        if (libraryName != null) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "libraryName", 
                "ML library/framework used (e.g., transformers, diffusers, timm)", 
                libraryName
            ));
        }

        // Model type from config
        String modelType = getNestedTextValue(modelInfo, "config", "model_type");
        if (modelType != null) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "modelType", 
                "Model architecture type", 
                modelType
            ));
        }

        // Architecture info
        JsonNode architectures = getNestedNode(modelInfo, "config", "architectures");
        if (architectures != null && architectures.isArray() && architectures.size() > 0) {
            List<String> archList = new ArrayList<>();
            for (JsonNode arch : architectures) {
                archList.add(arch.asText());
            }
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "architectures", 
                "Model architectures", 
                String.join(",", archList)
            ));
        }
    }

    /**
     * Adds provider/author characteristics.
     */
    private void addProviderCharacteristics(AiModelSpecificationCreate spec, JsonNode modelInfo) {
        
        // Author
        String author = getTextValue(modelInfo, "author");
        if (author != null) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "author", 
                "Model author/organization", 
                author
            ));
        }

        // License
        JsonNode cardData = modelInfo.get("cardData");
        if (cardData != null && cardData.has("license")) {
            String license = cardData.get("license").asText();
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "license", 
                "Model license", 
                license
            ));
        }

        // Languages (for NLP models)
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
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "languages", 
                "Supported languages", 
                langValue
            ));
        }
    }

    /**
     * Adds size-related characteristics.
     */
    private void addSizeCharacteristics(AiModelSpecificationCreate spec, JsonNode modelInfo) {
        
        // Calculate total size
        long totalSize = hfClient.calculateTotalSize(modelInfo);
        if (totalSize > 0) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "totalSizeBytes", 
                "Total size of model files in bytes", 
                String.valueOf(totalSize)
            ));
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "totalSizeHuman", 
                "Total size of model files (human readable)", 
                formatBytes(totalSize)
            ));
        }

        // Number of files
        JsonNode siblings = modelInfo.get("siblings");
        if (siblings != null && siblings.isArray()) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "fileCount", 
                "Number of files in the repository", 
                String.valueOf(siblings.size())
            ));
        }

        // Safetensors info
        JsonNode safetensors = modelInfo.get("safetensors");
        if (safetensors != null && safetensors.has("total")) {
            spec.addSpecCharacteristicItem(createCharacteristicSpec(
                "safetensorsParameters", 
                "Total number of parameters (from safetensors)", 
                String.valueOf(safetensors.get("total").asLong())
            ));
        }
    }

    /**
     * Adds tags characteristics.
     */
    private void addTagsCharacteristics(AiModelSpecificationCreate spec, JsonNode modelInfo) {
        
        if (modelInfo.has("tags")) {
            JsonNode tags = modelInfo.get("tags");
            if (tags.isArray()) {
                List<String> tagList = new ArrayList<>();
                for (JsonNode tag : tags) {
                    tagList.add(tag.asText());
                }
                spec.addSpecCharacteristicItem(createCharacteristicSpec(
                    "tags", 
                    "Model tags from Hugging Face Hub", 
                    String.join(",", tagList)
                ));
            }
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String extractVersion(JsonNode modelInfo) {
        // Try to get SHA for versioning
        String sha = getTextValue(modelInfo, "sha");
        if (sha != null && sha.length() > 7) {
            return sha.substring(0, 7); // Short SHA as version
        }
        return "1.0"; // Default version
    }

    private String extractDescription(JsonNode modelInfo, String modelId) {
        // Try model card data first
        JsonNode cardData = modelInfo.get("cardData");
        if (cardData != null) {
            // Try model-index name
            JsonNode modelIndex = cardData.get("model-index");
            if (modelIndex != null && modelIndex.isArray() && modelIndex.size() > 0) {
                JsonNode firstModel = modelIndex.get(0);
                if (firstModel.has("name")) {
                    return firstModel.get("name").asText();
                }
            }
        }

        // Try pipeline tag as description
        String pipelineTag = getTextValue(modelInfo, "pipeline_tag");
        String library = getTextValue(modelInfo, "library_name");
        
        if (pipelineTag != null && library != null) {
            return String.format("%s model for %s", library, pipelineTag);
        } else if (pipelineTag != null) {
            return "Model for " + pipelineTag;
        }
        
        return "Hugging Face model: " + modelId;
    }

    private CharacteristicSpecification createCharacteristicSpec(String name, String description, String value) {
        CharacteristicSpecification spec = new CharacteristicSpecification();
        spec.setName(name);
        spec.setDescription(description);
        
        if (value != null) {
            CharacteristicValueSpecification valueSpec = new CharacteristicValueSpecification();
            valueSpec.setValueType("string");
            valueSpec.setValue(value);
            valueSpec.setIsDefault(true);
            spec.setCharacteristicValueSpecification(List.of(valueSpec));
        }
        
        return spec;
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
