package org.etsi.osl.tmf.aim915.integrations.huggingface;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@RestController
public class HuggingFaceArtifactsApiController implements HuggingFaceArtifactsApi {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceArtifactsApiController.class);
    private final HuggingFaceService huggingFaceService;
    private static final long LARGE_FILE_THRESHOLD = 50 * 1024 * 1024; // 50MB

    public HuggingFaceArtifactsApiController(HuggingFaceService huggingFaceService) {
        this.huggingFaceService = huggingFaceService;
    }

    @Override
    public ResponseEntity<Resource> downloadHuggingFaceArtifact(String modelId, String filename) {
        log.info("Request to download artifact: {} from model: {}", filename, modelId);
        try {
            // Convert underscore back to slash for Hugging Face model ID
            String actualModelId = modelId.replace("_", "/");
            log.debug("Converted model ID from {} to {}", modelId, actualModelId);
            
            // Check file size to determine download strategy
            long fileSize = huggingFaceService.getFileSize(actualModelId, filename);
            
            // For large files, use streaming to avoid loading everything into memory
            if (fileSize > LARGE_FILE_THRESHOLD || fileSize == -1) {
                log.info("Using streaming strategy for large file: {} (size: {} MB)", 
                    filename, (fileSize > 0 ? fileSize / (1024 * 1024) : "unknown"));
                
                InputStream inputStream = huggingFaceService.streamModelFile(actualModelId, filename);
                InputStreamResource resource = new InputStreamResource(inputStream);
                
                ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
                
                if (fileSize > 0) {
                    builder.contentLength(fileSize);
                }
                
                return builder.body(resource);
                
            } else {
                // For smaller files, use the existing download method
                log.info("Using standard download for small file: {} (size: {} MB)", 
                    filename, (fileSize / (1024 * 1024)));
                
                File downloadedFile = huggingFaceService.downloadModelFile(actualModelId, filename);

                if (downloadedFile == null || !downloadedFile.exists()) {
                    log.warn("Downloaded file not found: {}", filename);
                    return ResponseEntity.notFound().build();
                }

                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(downloadedFile.toPath()));

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(resource.contentLength())
                        .body(resource);
            }

        } catch (HuggingFaceService.ModelNotFoundException e) {
            // Provide helpful error message with suggestions
            log.warn("Model not found: {}. Suggestions: {}", e.getModelId(), e.getSuggestions());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Error-Message", e.getMessage().replace("\n", " | "))
                    .build();
                    
        } catch (IOException e) {
            // Log the exception
            log.error("Error downloading Hugging Face artifact {} from model {}: {}", 
                filename, modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Failed to download artifact: " + e.getMessage())
                    .build();
                    
        } catch (Exception e) {
            log.error("Unexpected error downloading artifact {} from model {}: {}", 
                filename, modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseEntity<List<String>> listHuggingFaceArtifacts(String modelId) {
        log.info("Request to list artifacts for model: {}", modelId);
        try {
            // Convert underscore back to slash for Hugging Face model ID
            String actualModelId = modelId.replace("_", "/");
            log.debug("Converted model ID from {} to {}", modelId, actualModelId);
            
            List<String> files = huggingFaceService.listModelFiles(actualModelId);

            if (files == null || files.isEmpty()) {
                log.warn("No artifacts found for model: {}", actualModelId);
                return ResponseEntity.notFound().build();
            }

            log.info("Found {} artifacts for model: {}", files.size(), actualModelId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(files);

        } catch (HuggingFaceService.ModelNotFoundException e) {
            // Provide helpful error message with suggestions
            log.warn("Model not found when listing artifacts: {}. Suggestions: {}", 
                e.getModelId(), e.getSuggestions());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Error-Message", e.getMessage().replace("\n", " | "))
                    .body(null);
                    
        } catch (IOException e) {
            // Log the exception
            log.error("Error listing Hugging Face artifacts for model {}: {}", 
                modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Failed to list artifacts: " + e.getMessage())
                    .body(null);
                    
        } catch (Exception e) {
            log.error("Unexpected error listing artifacts for model {}: {}", 
                modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Unexpected error: " + e.getMessage())
                    .body(null);
        }
    }

    @Override
    public ResponseEntity<String> validateHuggingFaceModel(String modelId) {
        log.info("Request to validate model: {}", modelId);
        try {
            // Convert underscore back to slash for Hugging Face model ID
            String actualModelId = modelId.replace("_", "/");
            log.debug("Converted model ID from {} to {}", modelId, actualModelId);
            
            // Try to get model info
            JsonNode modelInfo = huggingFaceService.getModelInfo(actualModelId);
            
            log.info("Model validation successful for: {}", actualModelId);
            // If successful, return model info as JSON string
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Model-Status", "Found")
                    .body(modelInfo.toString());
                    
        } catch (HuggingFaceService.ModelNotFoundException e) {
            log.warn("Model validation failed for: {}. Private: {}, Suggestions count: {}", 
                e.getModelId(), e.isPrivate(), e.getSuggestions().size());
            
            // Model not found - return helpful suggestions
            StringBuilder response = new StringBuilder();
            response.append("{\n");
            response.append("  \"found\": false,\n");
            response.append("  \"modelId\": \"").append(e.getModelId()).append("\",\n");
            response.append("  \"isPrivate\": ").append(e.isPrivate()).append(",\n");
            
            if (e.getSuggestions() != null && !e.getSuggestions().isEmpty()) {
                response.append("  \"suggestions\": [\n");
                for (int i = 0; i < e.getSuggestions().size(); i++) {
                    response.append("    \"").append(e.getSuggestions().get(i)).append("\"");
                    if (i < e.getSuggestions().size() - 1) {
                        response.append(",");
                    }
                    response.append("\n");
                }
                response.append("  ],\n");
            }
            
            response.append("  \"message\": \"").append(e.getMessage().replace("\n", "\\n").replace("\"", "\\\"")).append("\"\n");
            response.append("}");
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Model-Status", "Not Found")
                    .header("X-Error-Message", e.getMessage().replace("\n", " | "))
                    .body(response.toString());
                    
        } catch (IOException e) {
            log.error("Error validating model {}: {}", modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Error-Message", "Validation failed: " + e.getMessage())
                    .body("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                    
        } catch (Exception e) {
            log.error("Unexpected error during validation of model {}: {}", 
                modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Unexpected error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    @Override
    public ResponseEntity<List<String>> searchHuggingFaceModels(String query, int limit) {
        log.info("Search request for models with query: '{}', limit: {}", query, limit);
        try {
            List<String> models = huggingFaceService.searchModels(query, limit);
            
            if (models == null || models.isEmpty()) {
                log.info("No models found for query: '{}'", query);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Result-Count", "0")
                        .body(List.of());
            }
            
            log.info("Found {} models for query: '{}'", models.size(), query);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Result-Count", String.valueOf(models.size()))
                    .body(models);
                    
        } catch (IOException e) {
            log.error("Error searching models with query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Search failed: " + e.getMessage())
                    .body(null);
                    
        } catch (Exception e) {
            log.error("Unexpected error during search with query '{}': {}", 
                query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Unexpected error: " + e.getMessage())
                    .body(null);
        }
    }
}
