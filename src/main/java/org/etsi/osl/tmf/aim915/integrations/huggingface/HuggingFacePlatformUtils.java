package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.CharacteristicSpecification;
import org.etsi.osl.tmf.aim915.model.CharacteristicValueSpecification;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.Any;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for HuggingFace Platform integration
 * Contains helper methods for creating characteristics, parsing JSON, and formatting data
 */
public class HuggingFacePlatformUtils {

    // HuggingFace API constants
    public static final String HF_API_BASE_URL = "https://huggingface.co/api";
    public static final String HF_HUB_URL = "https://huggingface.co";
    
    /**
     * Creates a CharacteristicSpecification for platform specification
     * 
     * @param name Characteristic name
     * @param description Characteristic description
     * @param valueType Value type (string, array, etc.)
     * @param value Default value
     * @return CharacteristicSpecification object
     */
    public static CharacteristicSpecification createCharacteristicSpec(String name, String description, 
                                                                        String valueType, String value) {
        CharacteristicSpecification spec = new CharacteristicSpecification();
        spec.setName(name);
        spec.setDescription(description);
        
        CharacteristicValueSpecification valueSpec = new CharacteristicValueSpecification();
        valueSpec.setValueType(valueType);
        valueSpec.setValue(value);
        valueSpec.setIsDefault(true);
        
        spec.setCharacteristicValueSpecification(List.of(valueSpec));
        return spec;
    }
    
    /**
     * Adds a service characteristic to an AiModelCreate
     * 
     * @param aiModelCreate The AiModelCreate object
     * @param name Characteristic name
     * @param value Characteristic value
     * @param valueType Characteristic value type (string, integer, array, etc.)
     */
    public static void addCharacteristic(AiModelCreate aiModelCreate, String name, String value, String valueType) {
        Characteristic characteristic = new Characteristic();
        characteristic.setName(name);
        characteristic.setValue(new Any(value));
        characteristic.setValueType(valueType);
        aiModelCreate.addServiceCharacteristicItem(characteristic);
    }
    
    /**
     * Extracts text value from JsonNode
     * 
     * @param node JsonNode to extract from
     * @param fieldName Field name to extract
     * @return Text value or null if not found
     */
    public static String getTextValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asText();
        }
        return null;
    }
    
    /**
     * Extracts nested text value from JsonNode
     * 
     * @param node JsonNode to extract from
     * @param path Nested path (e.g., "cardData", "base_model")
     * @return Text value or null if not found
     */
    public static String getNestedTextValue(JsonNode node, String... path) {
        JsonNode current = node;
        for (String fieldName : path) {
            if (current == null || !current.has(fieldName)) {
                return null;
            }
            current = current.get(fieldName);
        }
        return current != null ? current.asText() : null;
    }
    
    /**
     * Formats byte size to human-readable format
     * 
     * @param bytes Size in bytes
     * @return Human-readable size string (e.g., "1.5 GB")
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Converts model ID to URL-safe format
     * Replaces slashes with underscores
     * 
     * @param modelId The HuggingFace model ID (e.g., "mistralai/Mistral-7B-v0.1")
     * @return URL-safe model ID (e.g., "mistralai_Mistral-7B-v0.1")
     */
    public static String toUrlSafeModelId(String modelId) {
        return modelId.replace("/", "_");
    }
    
    /**
     * List of essential deployment files for HuggingFace models
     * These are the core files needed to deploy and run a model
     * 
     * @return List of essential file names
     */
    public static List<String> getEssentialDeploymentFiles() {
        return List.of(
            "config.json",
            "pytorch_model.bin",
            "model.safetensors",
            "tokenizer.json",
            "tokenizer_config.json",
            "vocab.json",
            "merges.txt",
            "special_tokens_map.json"
        );
    }
    
    /**
     * Extracts model description from HuggingFace API response
     * Looks for description in cardData.model-index[0].name
     * 
     * @param modelInfo JsonNode containing model information from HF API
     * @param modelId Fallback model ID if description not found
     * @return Model description
     */
    public static String extractModelDescription(JsonNode modelInfo, String modelId) {
        String description = "Hugging Face model: " + modelId;
        if (modelInfo.has("cardData")) {
            JsonNode cardData = modelInfo.get("cardData");
            if (cardData.has("model-index")) {
                JsonNode modelIndex = cardData.get("model-index");
                if (modelIndex.isArray() && modelIndex.size() > 0) {
                    JsonNode firstModel = modelIndex.get(0);
                    if (firstModel.has("name")) {
                        description = firstModel.get("name").asText();
                    }
                }
            }
        }
        return description;
    }
    
    /**
     * Extracts tags from HuggingFace model info
     * 
     * @param modelInfo JsonNode containing model information
     * @return Comma-separated string of tags, or null if no tags found
     */
    public static String extractTags(JsonNode modelInfo) {
        if (modelInfo.has("tags")) {
            JsonNode tags = modelInfo.get("tags");
            if (tags.isArray()) {
                java.util.List<String> tagList = new java.util.ArrayList<>();
                for (JsonNode tag : tags) {
                    tagList.add(tag.asText());
                }
                return String.join(",", tagList);
            }
        }
        return null;
    }
    
    /**
     * Calculates total size of model from siblings array
     * 
     * @param siblings JsonNode containing siblings array from HF API
     * @return Total size in bytes, or 0 if no size information
     */
    public static long calculateTotalSize(JsonNode siblings) {
        long totalSize = 0;
        if (siblings != null && siblings.isArray()) {
            for (JsonNode sibling : siblings) {
                if (sibling.has("size")) {
                    totalSize += sibling.get("size").asLong();
                }
            }
        }
        return totalSize;
    }
    
    /**
     * Extracts languages from model card data
     * 
     * @param modelInfo JsonNode containing model information
     * @return Comma-separated string of languages, or null if not found
     */
    public static String extractLanguages(JsonNode modelInfo) {
        if (modelInfo.has("cardData") && modelInfo.get("cardData").has("language")) {
            JsonNode language = modelInfo.get("cardData").get("language");
            if (language.isArray()) {
                java.util.List<String> languages = new java.util.ArrayList<>();
                for (JsonNode lang : language) {
                    languages.add(lang.asText());
                }
                return String.join(",", languages);
            } else if (language.isTextual()) {
                return language.asText();
            }
        }
        return null;
    }
    
    /**
     * Builds HuggingFace model repository URL
     * 
     * @param modelId The model ID
     * @return Full repository URL
     */
    public static String buildRepositoryUrl(String modelId) {
        return HF_HUB_URL + "/" + modelId;
    }
    
    /**
     * Builds HuggingFace dataset URL
     * 
     * @param datasetName The dataset name
     * @return Full dataset URL
     */
    public static String buildDatasetUrl(String datasetName) {
        return HF_HUB_URL + "/datasets/" + datasetName;
    }
    
    /**
     * Builds HuggingFace inference API URL
     * 
     * @param modelId The model ID
     * @return Inference API endpoint URL
     */
    public static String buildInferenceApiUrl(String modelId) {
        return HF_API_BASE_URL + "/models/" + modelId;
    }
    
    /**
     * Builds deployment artifacts URL
     * 
     * @param baseUrl The base TMF API URL
     * @param modelId The model ID
     * @return Deployment artifacts download URL
     */
    public static String buildDeploymentArtifactsUrl(String baseUrl, String modelId) {
        String urlSafeModelId = toUrlSafeModelId(modelId);
        return baseUrl + "/tmf-api/aim/v1/aiModel/artifacts/huggingface/" + urlSafeModelId + "/deployment.tar.gz";
    }
}
