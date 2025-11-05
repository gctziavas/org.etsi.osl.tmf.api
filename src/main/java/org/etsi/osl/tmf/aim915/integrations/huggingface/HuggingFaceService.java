package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.common.model.Any;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for integrating with Hugging Face Hub API
 * Provides methods to convert Hugging Face models to TMF AI Model structures
 */
@Service
public class HuggingFaceService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceService.class);
    private static final String HF_API_BASE_URL = "https://huggingface.co/api";
    private static final String HF_HUB_URL = "https://huggingface.co";
    private static final int BUFFER_SIZE = 65536; // 64KB buffer for streaming
    
    private final RestTemplate restTemplate;
    private final RestTemplate streamingRestTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;

    public HuggingFaceService(@Value("${huggingface.api.token:}") String apiToken,
                               @Value("${huggingface.download.timeout:300000}") int downloadTimeout) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiToken = apiToken;
        
        log.info("Initializing HuggingFaceService with timeout: {}ms", downloadTimeout);
        
        // Create a separate RestTemplate with extended timeout for large file downloads
        this.streamingRestTemplate = createStreamingRestTemplate(downloadTimeout);
    }
    
    /**
     * Creates a RestTemplate configured for streaming large files
     */
    private RestTemplate createStreamingRestTemplate(int timeout) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        factory.setBufferRequestBody(false); // Don't buffer the entire response in memory
        
        return new RestTemplate(factory);
    }
    
    /**
     * Custom exception for model not found scenarios with helpful suggestions
     */
    public static class ModelNotFoundException extends IOException {
        private final String modelId;
        private final List<String> suggestions;
        private final boolean isPrivate;
        
        public ModelNotFoundException(String modelId, List<String> suggestions, boolean isPrivate) {
            super(buildMessage(modelId, suggestions, isPrivate));
            this.modelId = modelId;
            this.suggestions = suggestions;
            this.isPrivate = isPrivate;
        }
        
        private static String buildMessage(String modelId, List<String> suggestions, boolean isPrivate) {
            StringBuilder msg = new StringBuilder("Model '").append(modelId).append("' not found on Hugging Face Hub.");
            
            if (isPrivate) {
                msg.append("\n\nThis model may be private. Please ensure:");
                msg.append("\n- You have set 'huggingface.api.token' in application.properties");
                msg.append("\n- Your token has access to this model");
                msg.append("\n- Visit: https://huggingface.co/").append(modelId).append(" to verify access");
            }
            
            if (suggestions != null && !suggestions.isEmpty()) {
                msg.append("\n\nDid you mean one of these?");
                for (String suggestion : suggestions) {
                    msg.append("\n- ").append(suggestion);
                }
            }
            
            msg.append("\n\nNote: Model IDs are case-sensitive!");
            msg.append("\nSearch for models at: https://huggingface.co/models");
            
            return msg.toString();
        }
        
        public String getModelId() { return modelId; }
        public List<String> getSuggestions() { return suggestions; }
        public boolean isPrivate() { return isPrivate; }
    }

    /**
     * Validates if a model exists and is accessible
     * 
     * @param modelId The Hugging Face model ID
     * @return true if model exists and is accessible
     */
    public boolean validateModelExists(String modelId) {
        try {
            log.debug("Validating model existence: {}", modelId);
            getModelInfo(modelId);
            log.debug("Model validated successfully: {}", modelId);
            return true;
        } catch (IOException e) {
            log.debug("Model validation failed for {}: {}", modelId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Finds similar model IDs based on partial match
     * 
     * @param partialModelId Partial or possibly incorrect model ID
     * @param limit Maximum number of suggestions
     * @return List of similar model IDs
     */
    public List<String> findSimilarModels(String partialModelId, int limit) {
        try {
            log.debug("Searching for similar models to: {}", partialModelId);
            List<String> results = searchModels(partialModelId, limit);
            log.debug("Found {} similar models for {}", results.size(), partialModelId);
            return results;
        } catch (IOException e) {
            log.error("Error searching for similar models to {}: {}", partialModelId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves model information from Hugging Face Hub
     * 
     * @param modelId The Hugging Face model ID (e.g., "bert-base-uncased", "openai/whisper-large")
     * @return JsonNode containing model metadata
     * @throws ModelNotFoundException if model is not found with helpful suggestions
     */
    public JsonNode getModelInfo(String modelId) throws IOException {
        log.debug("Fetching model info for: {}", modelId);
        String url = HF_API_BASE_URL + "/models/" + modelId;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
            log.trace("Using authentication token for model: {}", modelId);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Successfully retrieved model info for: {}", modelId);
            return objectMapper.readTree(response.getBody());
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Handle different HTTP error codes with helpful messages
            if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND) {
                log.warn("Model not found: {}. Searching for similar models...", modelId);
                // Try to find similar models
                List<String> suggestions = findSimilarModels(modelId, 5);
                log.info("Found {} suggestions for model {}", suggestions.size(), modelId);
                throw new ModelNotFoundException(modelId, suggestions, false);
                
            } else if (e.getStatusCode() == org.springframework.http.HttpStatus.UNAUTHORIZED || 
                       e.getStatusCode() == org.springframework.http.HttpStatus.FORBIDDEN) {
                log.warn("Access denied to model: {}. Model may be private.", modelId);
                // Model might be private
                List<String> suggestions = new ArrayList<>();
                throw new ModelNotFoundException(modelId, suggestions, true);
                
            } else {
                log.error("HTTP error accessing model {}: {} - {}", modelId, e.getStatusCode(), e.getMessage());
                throw new IOException("Error accessing Hugging Face model '" + modelId + "': " + 
                                     e.getStatusCode() + " - " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error accessing model {}: {}", modelId, e.getMessage(), e);
            throw new IOException("Unexpected error accessing model '" + modelId + "': " + e.getMessage(), e);
        }
    }

    /**
     * Lists all files in a Hugging Face model repository
     * 
     * @param modelId The Hugging Face model ID
     * @return List of file paths in the repository
     */
    public List<String> listModelFiles(String modelId) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        List<String> files = new ArrayList<>();
        if (siblings != null && siblings.isArray()) {
            for (JsonNode sibling : siblings) {
                String filename = sibling.get("rfilename").asText();
                files.add(filename);
            }
        }
        
        return files;
    }

    /**
     * Converts a Hugging Face model to an AiModelSpecification
     * 
     * @param modelId The Hugging Face model ID
     * @return AiModelSpecification object
     */
    public AiModelSpecification huggingFaceToAiModelSpecification(String modelId) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        
        AiModelSpecification spec = new AiModelSpecification();
        
        // Basic information
        spec.setName(modelId);
        
        // Extract model card information if available
        if (modelInfo.has("cardData")) {
            JsonNode cardData = modelInfo.get("cardData");
            if (cardData.has("model-index")) {
                JsonNode modelIndex = cardData.get("model-index");
                if (modelIndex.isArray() && modelIndex.size() > 0) {
                    JsonNode firstModel = modelIndex.get(0);
                    if (firstModel.has("name")) {
                        spec.setDescription(firstModel.get("name").asText());
                    }
                }
            }
        }
        
        // Set lifecycle status based on model tags
        if (modelInfo.has("tags")) {
            JsonNode tags = modelInfo.get("tags");
            if (tags.isArray()) {
                for (JsonNode tag : tags) {
                    String tagValue = tag.asText();
                    if (tagValue.equals("arxiv")) {
                        spec.setLifecycleStatus("PUBLISHED");
                        break;
                    }
                }
            }
        }
        
        if (spec.getLifecycleStatus() == null) {
            spec.setLifecycleStatus("ACTIVE");
        }
        
        // Set last update time
        if (modelInfo.has("lastModified")) {
            String lastModified = modelInfo.get("lastModified").asText();
            spec.setLastUpdate(java.time.OffsetDateTime.parse(lastModified));
        }
        
        // Model data sheet - link to the model card
        String modelCardUrl = HF_HUB_URL + "/" + modelId;
        spec.setModelDataSheet(modelCardUrl);
        
        // Check for base model information
        if (modelInfo.has("cardData") && modelInfo.get("cardData").has("base_model")) {
            String baseModel = modelInfo.get("cardData").get("base_model").asText();
            spec.setInheritedModel(HF_HUB_URL + "/" + baseModel);
        }
        
        return spec;
    }

    /**
     * Converts a Hugging Face model to an AiModel with service characteristics
     * 
     * @param modelId The Hugging Face model ID
     * @param baseUrl The base URL for the TMF API
     * @return AiModel object with artifact URLs in service characteristics
     */
    public AiModel huggingFaceToAiModel(String modelId, String baseUrl) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        
        AiModel aiModel = new AiModel();
        
        // Set basic properties
        aiModel.setName(modelId);
        aiModel.setState(ServiceStateType.ACTIVE);
        
        // Create and attach the AiModelSpecification
        AiModelSpecification spec = huggingFaceToAiModelSpecification(modelId);
        aiModel.setAiModelSpecification(spec);
        
        // Create service characteristics to hold artifact information
        Set<Characteristic> characteristics = new HashSet<>();
        
        // 1. Model repository URL
        Characteristic repoUrlChar = new Characteristic();
        repoUrlChar.setName("repositoryUrl");
        repoUrlChar.setValue(new Any(HF_HUB_URL + "/" + modelId));
        repoUrlChar.setValueType("string");
        characteristics.add(repoUrlChar);
        
        // 2. Model card URL (documentation)
        Characteristic modelCardChar = new Characteristic();
        modelCardChar.setName("modelCardUrl");
        modelCardChar.setValue(new Any(HF_HUB_URL + "/" + modelId));
        modelCardChar.setValueType("string");
        characteristics.add(modelCardChar);
        
        // 3. Download URL for model files
        Characteristic downloadUrlChar = new Characteristic();
        downloadUrlChar.setName("downloadUrl");
        downloadUrlChar.setValue(new Any(baseUrl + "/aiModel/" + modelId.replace("/", "_") + "/artifacts/model"));
        downloadUrlChar.setValueType("string");
        characteristics.add(downloadUrlChar);
        
        // 4. Model type/task
        if (modelInfo.has("pipeline_tag")) {
            Characteristic taskChar = new Characteristic();
            taskChar.setName("pipelineTag");
            taskChar.setValue(new Any(modelInfo.get("pipeline_tag").asText()));
            taskChar.setValueType("string");
            characteristics.add(taskChar);
        }
        
        // 5. Model downloads count
        if (modelInfo.has("downloads")) {
            Characteristic downloadsChar = new Characteristic();
            downloadsChar.setName("downloadCount");
            downloadsChar.setValue(new Any(String.valueOf(modelInfo.get("downloads").asLong())));
            downloadsChar.setValueType("integer");
            characteristics.add(downloadsChar);
        }
        
        // 6. Model likes count
        if (modelInfo.has("likes")) {
            Characteristic likesChar = new Characteristic();
            likesChar.setName("likesCount");
            likesChar.setValue(new Any(String.valueOf(modelInfo.get("likes").asInt())));
            likesChar.setValueType("integer");
            characteristics.add(likesChar);
        }
        
        // 7. Library name (transformers, diffusers, etc.)
        if (modelInfo.has("library_name")) {
            Characteristic libraryChar = new Characteristic();
            libraryChar.setName("libraryName");
            libraryChar.setValue(new Any(modelInfo.get("library_name").asText()));
            libraryChar.setValueType("string");
            characteristics.add(libraryChar);
        }
        
        // 8. Tags
        if (modelInfo.has("tags")) {
            JsonNode tags = modelInfo.get("tags");
            if (tags.isArray()) {
                StringBuilder tagsList = new StringBuilder();
                for (JsonNode tag : tags) {
                    if (tagsList.length() > 0) tagsList.append(",");
                    tagsList.append(tag.asText());
                }
                Characteristic tagsChar = new Characteristic();
                tagsChar.setName("tags");
                tagsChar.setValue(new Any(tagsList.toString()));
                tagsChar.setValueType("string");
                characteristics.add(tagsChar);
            }
        }
        
        // 9. Store Hugging Face model ID for retrieval
        Characteristic modelIdChar = new Characteristic();
        modelIdChar.setName("huggingFaceModelId");
        modelIdChar.setValue(new Any(modelId));
        modelIdChar.setValueType("string");
        characteristics.add(modelIdChar);
        
        aiModel.setServiceCharacteristic(characteristics);
        
        return aiModel;
    }

    /**
     * Downloads a specific file from a Hugging Face model repository
     * Uses streaming to handle large files efficiently without loading them entirely into memory
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The name of the file to download
     * @return File object pointing to the downloaded file
     */
    public File downloadModelFile(String modelId, String filename) throws IOException {
        log.info("Downloading file {} from model {}", filename, modelId);
        String url = HF_HUB_URL + "/" + modelId + "/resolve/main/" + filename;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Resource> response = streamingRestTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        
        // Create a temporary file
        File tempFile = Files.createTempFile("hf_model_", "_" + filename.replace("/", "_")).toFile();
        tempFile.deleteOnExit();
        
        log.debug("Created temp file: {}", tempFile.getAbsolutePath());
        
        // Write the response body to the file using streaming with a larger buffer
        try (InputStream inputStream = response.getBody().getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // Log progress for large files (every 10MB)
                if (totalBytesRead % (10 * 1024 * 1024) == 0) {
                    log.info("Downloaded {} MB of {}", (totalBytesRead / (1024 * 1024)), filename);
                }
            }
            log.info("Completed download of {} ({} MB)", filename, (totalBytesRead / (1024 * 1024)));
        } catch (IOException e) {
            log.error("Error during file download for {}: {}", filename, e.getMessage(), e);
            throw e;
        }
        
        return tempFile;
    }
    
    /**
     * Streams a file directly from Hugging Face without saving to disk
     * Useful for immediate forwarding to client
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The name of the file to stream
     * @return InputStream for the file
     */
    public InputStream streamModelFile(String modelId, String filename) throws IOException {
        log.info("Streaming file {} from model {}", filename, modelId);
        String url = HF_HUB_URL + "/" + modelId + "/resolve/main/" + filename;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Resource> response = streamingRestTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        
        log.debug("Stream established for {} from model {}", filename, modelId);
        return response.getBody().getInputStream();
    }
    
    /**
     * Gets the size of a file in the Hugging Face repository
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The name of the file
     * @return File size in bytes, or -1 if unknown
     */
    public long getFileSize(String modelId, String filename) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        if (siblings != null && siblings.isArray()) {
            for (JsonNode sibling : siblings) {
                if (sibling.has("rfilename") && sibling.get("rfilename").asText().equals(filename)) {
                    if (sibling.has("size")) {
                        return sibling.get("size").asLong();
                    }
                }
            }
        }
        
        return -1;
    }

    /**
     * Downloads all model files from a Hugging Face repository
     * Useful for downloading complete model weights, config files, etc.
     * 
     * @param modelId The Hugging Face model ID
     * @return List of downloaded files
     */
    public List<File> downloadAllModelFiles(String modelId) throws IOException {
        List<String> files = listModelFiles(modelId);
        List<File> downloadedFiles = new ArrayList<>();
        
        for (String filename : files) {
            try {
                File file = downloadModelFile(modelId, filename);
                downloadedFiles.add(file);
            } catch (Exception e) {
                // Log the error and continue with other files
                System.err.println("Failed to download file: " + filename + " - " + e.getMessage());
            }
        }
        
        return downloadedFiles;
    }

    /**
     * Searches for models on Hugging Face Hub
     * 
     * @param searchQuery Search query string
     * @param limit Maximum number of results to return
     * @return List of model IDs matching the search
     */
    public List<String> searchModels(String searchQuery, int limit) throws IOException {
        String url = HF_API_BASE_URL + "/models?search=" + searchQuery + "&limit=" + limit;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        JsonNode results = objectMapper.readTree(response.getBody());
        List<String> modelIds = new ArrayList<>();
        
        if (results.isArray()) {
            for (JsonNode model : results) {
                if (model.has("modelId")) {
                    modelIds.add(model.get("modelId").asText());
                }
            }
        }
        
        return modelIds;
    }

    /**
     * Gets dataset information associated with a model if available
     * 
     * @param modelId The Hugging Face model ID
     * @return List of dataset names used for training
     */
    public List<String> getTrainingDatasets(String modelId) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        List<String> datasets = new ArrayList<>();
        
        if (modelInfo.has("cardData") && modelInfo.get("cardData").has("datasets")) {
            JsonNode datasetsNode = modelInfo.get("cardData").get("datasets");
            if (datasetsNode.isArray()) {
                for (JsonNode dataset : datasetsNode) {
                    datasets.add(dataset.asText());
                }
            }
        }
        
        return datasets;
    }
}
