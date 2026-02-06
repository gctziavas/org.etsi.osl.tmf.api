package org.etsi.osl.tmf.aim915.integrations.huggingface;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * Low-level service for communicating with Hugging Face Hub API.
 * 
 * This service handles all direct interactions with Hugging Face:
 * - Model information retrieval
 * - Model search operations
 * - Artifact download and streaming
 * - File listing
 * 
 * Higher-level services should use this service for Hugging Face access.
 */
@Service
public class HuggingFaceClientService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceClientService.class);
    private static final int BUFFER_SIZE = 65536; // 64KB buffer for streaming

    private final RestTemplate restTemplate;
    private final RestTemplate streamingRestTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final String apiBaseUrl;
    private final String hubUrl;

    public HuggingFaceClientService(
            @Qualifier("huggingFaceRestTemplate") RestTemplate restTemplate,
            @Qualifier("huggingFaceStreamingRestTemplate") RestTemplate streamingRestTemplate,
            @Qualifier("huggingFaceApiToken") String apiToken,
            @Qualifier("huggingFaceApiBaseUrl") String apiBaseUrl,
            @Qualifier("huggingFaceHubUrl") String hubUrl) {
        this.restTemplate = restTemplate;
        this.streamingRestTemplate = streamingRestTemplate;
        this.objectMapper = new ObjectMapper();
        this.apiToken = apiToken;
        this.apiBaseUrl = apiBaseUrl;
        this.hubUrl = hubUrl;
        log.info("HuggingFaceClientService initialized");
    }

    // ========================================
    // Model Information Operations
    // ========================================

    /**
     * Retrieves model information from Hugging Face Hub.
     * 
     * @param modelId The Hugging Face model ID (e.g., "bert-base-uncased", "openai/whisper-large")
     * @return JsonNode containing model metadata
     * @throws ModelNotFoundException if model is not found
     * @throws IOException if API access fails
     */
    public JsonNode getModelInfo(String modelId) throws IOException {
        log.debug("Fetching model info for: {}", modelId);
        String url = apiBaseUrl + "/models/" + modelId;
        
        HttpEntity<String> entity = createAuthenticatedEntity();
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Successfully retrieved model info for: {}", modelId);
            return objectMapper.readTree(response.getBody());
            
        } catch (HttpClientErrorException e) {
            handleHttpError(modelId, e);
            throw new IOException("Unexpected error accessing model: " + modelId, e);
        } catch (Exception e) {
            log.error("Unexpected error accessing model {}: {}", modelId, e.getMessage(), e);
            throw new IOException("Unexpected error accessing model '" + modelId + "': " + e.getMessage(), e);
        }
    }

    /**
     * Validates if a model exists on Hugging Face Hub.
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
     * Searches for models on Hugging Face Hub.
     * 
     * @param searchQuery Search query string
     * @param limit Maximum number of results to return
     * @return List of model IDs matching the search
     * @throws IOException if search fails
     */
    public List<String> searchModels(String searchQuery, int limit) throws IOException {
        log.debug("Searching for models with query: {}, limit: {}", searchQuery, limit);
        String url = apiBaseUrl + "/models?search=" + searchQuery + "&limit=" + limit;
        
        HttpEntity<String> entity = createAuthenticatedEntity();
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
        
        log.debug("Found {} models matching query: {}", modelIds.size(), searchQuery);
        return modelIds;
    }

    /**
     * Finds similar model IDs based on partial match.
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

    // ========================================
    // File Operations
    // ========================================

    /**
     * Lists all files in a Hugging Face model repository.
     * 
     * @param modelId The Hugging Face model ID
     * @return List of file paths in the repository
     * @throws IOException if model cannot be accessed
     */
    public List<String> listModelFiles(String modelId) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        List<String> files = new ArrayList<>();
        if (siblings != null && siblings.isArray()) {
            for (JsonNode sibling : siblings) {
                if (sibling.has("rfilename")) {
                    files.add(sibling.get("rfilename").asText());
                }
            }
        }
        
        log.debug("Found {} files in model {}", files.size(), modelId);
        return files;
    }

    /**
     * Gets the size of a file in the Hugging Face repository.
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
     * Calculates total size of all files in a model.
     * 
     * @param modelInfo The model info JSON node
     * @return Total size in bytes
     */
    public long calculateTotalSize(JsonNode modelInfo) {
        JsonNode siblings = modelInfo.get("siblings");
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

    // ========================================
    // Download Operations
    // ========================================

    /**
     * Downloads a specific file from a Hugging Face model repository.
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The name of the file to download
     * @return File object pointing to the downloaded file
     * @throws IOException if download fails
     */
    public File downloadModelFile(String modelId, String filename) throws IOException {
        log.info("Downloading file {} from model {}", filename, modelId);
        String url = hubUrl + "/" + modelId + "/resolve/main/" + filename;
        
        HttpEntity<String> entity = createAuthenticatedEntity();
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
        }
        
        return tempFile;
    }

    /**
     * Streams a file directly from Hugging Face without saving to disk.
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The name of the file to stream
     * @return InputStream for the file
     * @throws IOException if streaming fails
     */
    public InputStream streamModelFile(String modelId, String filename) throws IOException {
        log.info("Streaming file {} from model {}", filename, modelId);
        String url = hubUrl + "/" + modelId + "/resolve/main/" + filename;
        
        HttpEntity<String> entity = createAuthenticatedEntity();
        ResponseEntity<Resource> response = streamingRestTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        
        log.debug("Stream established for {} from model {}", filename, modelId);
        return response.getBody().getInputStream();
    }

    /**
     * Downloads a file from Hugging Face as byte array.
     * 
     * @param modelId The Hugging Face model ID
     * @param filename The file to download
     * @return byte array containing the file content
     * @throws IOException if download fails
     */
    public byte[] downloadFileAsBytes(String modelId, String filename) throws IOException {
        String fileUrl = hubUrl + "/" + modelId + "/resolve/main/" + filename;
        
        HttpEntity<String> entity = createAuthenticatedEntity();
        
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                fileUrl, 
                HttpMethod.GET, 
                entity, 
                byte[].class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to download file {} from model {}: {}", filename, modelId, e.getMessage());
            throw new IOException("Failed to download " + filename + ": " + e.getMessage(), e);
        }
    }

    /**
     * Downloads deployment artifacts as a tar.gz archive.
     * 
     * @param modelId The Hugging Face model ID
     * @return byte array containing the tar.gz archive
     * @throws IOException if download fails
     */
    public byte[] downloadDeploymentArtifactsAsTargz(String modelId) throws IOException {
        log.info("Downloading deployment artifacts for model: {}", modelId);
        
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        if (siblings == null || !siblings.isArray()) {
            throw new IOException("No artifacts found for model: " + modelId);
        }
        
        List<String> essentialFiles = getEssentialDeploymentFiles();
        List<String> filesToDownload = new ArrayList<>();
        
        for (JsonNode sibling : siblings) {
            if (sibling.has("rfilename")) {
                String filename = sibling.get("rfilename").asText();
                if (essentialFiles.contains(filename)) {
                    filesToDownload.add(filename);
                    log.debug("Found essential file: {}", filename);
                }
            }
        }
        
        if (filesToDownload.isEmpty()) {
            throw new IOException("No essential deployment files found for model: " + modelId);
        }
        
        log.info("Downloading {} essential files for deployment", filesToDownload.size());
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(gzipOutputStream);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
            
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            
            for (String filename : filesToDownload) {
                try {
                    log.debug("Downloading file: {}", filename);
                    byte[] fileContent = downloadFileAsBytes(modelId, filename);
                    
                    TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
                    tarEntry.setSize(fileContent.length);
                    
                    tarOutputStream.putArchiveEntry(tarEntry);
                    tarOutputStream.write(fileContent);
                    tarOutputStream.closeArchiveEntry();
                    
                    log.debug("Added {} to archive ({} bytes)", filename, fileContent.length);
                    
                } catch (Exception e) {
                    log.warn("Failed to download {}: {}. Skipping...", filename, e.getMessage());
                }
            }
            
            tarOutputStream.finish();
        }
        
        byte[] tarGzData = byteArrayOutputStream.toByteArray();
        log.info("Created tar.gz archive with {} files ({} bytes)", filesToDownload.size(), tarGzData.length);
        
        return tarGzData;
    }

    /**
     * Streams deployment artifacts as tar.gz directly to an output stream.
     * 
     * @param modelId The Hugging Face model ID
     * @param outputStream The output stream to write to
     * @throws IOException if streaming fails
     */
    public void streamDeploymentArtifactsAsTargz(String modelId, OutputStream outputStream) throws IOException {
        log.info("Streaming deployment artifacts for model: {}", modelId);
        
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        if (siblings == null || !siblings.isArray()) {
            throw new IOException("No artifacts found for model: " + modelId);
        }
        
        List<String> essentialFiles = getEssentialDeploymentFiles();
        List<String> filesToDownload = new ArrayList<>();
        
        for (JsonNode sibling : siblings) {
            if (sibling.has("rfilename")) {
                String filename = sibling.get("rfilename").asText();
                if (essentialFiles.contains(filename)) {
                    filesToDownload.add(filename);
                }
            }
        }
        
        if (filesToDownload.isEmpty()) {
            throw new IOException("No essential deployment files found for model: " + modelId);
        }
        
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(gzipOutputStream);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
            
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            
            for (String filename : filesToDownload) {
                try {
                    byte[] fileContent = downloadFileAsBytes(modelId, filename);
                    
                    TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
                    tarEntry.setSize(fileContent.length);
                    
                    tarOutputStream.putArchiveEntry(tarEntry);
                    tarOutputStream.write(fileContent);
                    tarOutputStream.closeArchiveEntry();
                    
                } catch (Exception e) {
                    log.warn("Failed to stream {}: {}. Skipping...", filename, e.getMessage());
                }
            }
            
            tarOutputStream.finish();
        }
        
        log.info("Successfully streamed tar.gz archive for model: {}", modelId);
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Gets the Hub URL for a model.
     * 
     * @param modelId The model ID
     * @return The Hub URL
     */
    public String getModelHubUrl(String modelId) {
        return hubUrl + "/" + modelId;
    }

    /**
     * Gets the API URL for a model.
     * 
     * @param modelId The model ID
     * @return The API URL
     */
    public String getModelApiUrl(String modelId) {
        return apiBaseUrl + "/models/" + modelId;
    }

    /**
     * Gets the Hub base URL.
     * 
     * @return The Hub URL
     */
    public String getHubUrl() {
        return hubUrl;
    }

    /**
     * Gets the API base URL.
     * 
     * @return The API base URL
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * Checks if an API token is configured.
     * 
     * @return true if token is configured
     */
    public boolean hasApiToken() {
        return apiToken != null && !apiToken.isEmpty();
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private HttpEntity<String> createAuthenticatedEntity() {
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        return new HttpEntity<>(headers);
    }

    private void handleHttpError(String modelId, HttpClientErrorException e) throws IOException {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("Model not found: {}. Searching for similar models...", modelId);
            List<String> suggestions = findSimilarModels(modelId, 5);
            throw new ModelNotFoundException(modelId, suggestions, false);
            
        } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                   e.getStatusCode() == HttpStatus.FORBIDDEN) {
            log.warn("Access denied to model: {}. Model may be private.", modelId);
            throw new ModelNotFoundException(modelId, new ArrayList<>(), true);
            
        } else {
            log.error("HTTP error accessing model {}: {} - {}", modelId, e.getStatusCode(), e.getMessage());
            throw new IOException("Error accessing model '" + modelId + "': " + 
                                 e.getStatusCode() + " - " + e.getMessage(), e);
        }
    }

    private List<String> getEssentialDeploymentFiles() {
        return List.of(
            "config.json",
            "tokenizer.json",
            "tokenizer_config.json",
            "vocab.json",
            "merges.txt",
            "special_tokens_map.json",
            "pytorch_model.bin",
            "model.safetensors",
            "tf_model.h5",
            "flax_model.msgpack"
        );
    }

    // ========================================
    // Custom Exceptions
    // ========================================

    /**
     * Exception for model not found scenarios with helpful suggestions.
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
                msg.append("\n- You have set 'huggingface.api.token' in application.yml");
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
}
