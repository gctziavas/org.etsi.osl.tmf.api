package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller implementing the Hugging Face integration API.
 */
@RestController
public class HuggingFaceApiController implements HuggingFaceApi {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceApiController.class);
    private static final long LARGE_FILE_THRESHOLD = 50 * 1024 * 1024; // 50MB

    private final HuggingFaceIntegrationService integrationService;

    public HuggingFaceApiController(HuggingFaceIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    // ========================================
    // Specification Import Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> importModelAsSpecification(String modelId) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: Import model as specification - {}", actualModelId);
        
        try {
            AiModelSpecification spec = integrationService.importModelAsSpecification(actualModelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Specification imported successfully");
            response.put("specificationId", spec.getId());
            response.put("name", spec.getName());
            response.put("version", spec.getVersion());
            response.put("description", spec.getDescription());
            response.put("hubUrl", integrationService.getModelHubUrl(actualModelId));
            
            return ResponseEntity.ok(response);
            
        } catch (HuggingFaceClientService.ModelNotFoundException e) {
            log.warn("Model not found: {}. Suggestions: {}", e.getModelId(), e.getSuggestions());
            return createNotFoundResponse(e);
        } catch (IOException e) {
            log.error("Failed to import model {}: {}", actualModelId, e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error importing model {}: {}", actualModelId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> searchAndImportModels(String query, int limit) {
        log.info("API: Search and import models - query='{}', limit={}", query, limit);
        
        try {
            List<AiModelSpecification> specs = integrationService.searchAndImportModels(query, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Search and import completed");
            response.put("count", specs.size());
            response.put("specifications", specs.stream()
                .map(s -> Map.of(
                    "id", s.getId(),
                    "name", s.getName(),
                    "version", s.getVersion() != null ? s.getVersion() : "",
                    "hubUrl", integrationService.getModelHubUrl(s.getName())
                ))
                .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching and importing models: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    // ========================================
    // Model Instance Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> deployModel(String modelId, String deploymentEndpoint) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: Deploy model - {} to {}", actualModelId, deploymentEndpoint);
        
        try {
            AiModel model = integrationService.deployModelFromHuggingFace(actualModelId, deploymentEndpoint);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Model deployed successfully");
            response.put("modelId", model.getId());
            response.put("name", model.getName());
            response.put("state", model.getState());
            response.put("specificationId", model.getAiModelSpecification() != null ? 
                model.getAiModelSpecification().getId() : null);
            response.put("hubUrl", integrationService.getModelHubUrl(actualModelId));
            
            return ResponseEntity.ok(response);
            
        } catch (HuggingFaceClientService.ModelNotFoundException e) {
            log.warn("Model not found: {}", e.getModelId());
            return createNotFoundResponse(e);
        } catch (IOException e) {
            log.error("Failed to deploy model {}: {}", actualModelId, e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deploying model {}: {}", actualModelId, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> instantiateModel(String specificationId, 
            String instanceName, String deploymentEndpoint) {
        log.info("API: Instantiate model from specification - {}", specificationId);
        
        try {
            AiModel model = integrationService.createModelInstance(specificationId, instanceName, deploymentEndpoint);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Model instance created successfully");
            response.put("modelId", model.getId());
            response.put("name", model.getName());
            response.put("state", model.getState());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Specification not found: {}", specificationId);
            return createErrorResponse(HttpStatus.NOT_FOUND, "Specification not found: " + specificationId);
        } catch (Exception e) {
            log.error("Error instantiating model: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    // ========================================
    // Search and Query Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> searchModels(String query, int limit) {
        log.info("API: Search models - query='{}', limit={}", query, limit);
        
        try {
            List<String> models = integrationService.searchModels(query, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", models.size());
            response.put("models", models);
            response.put("hubSearchUrl", "https://huggingface.co/models?search=" + query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching models: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> validateModel(String modelId) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: Validate model - {}", actualModelId);
        
        boolean exists = integrationService.modelExists(actualModelId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("modelId", actualModelId);
        response.put("exists", exists);
        
        if (exists) {
            response.put("hubUrl", integrationService.getModelHubUrl(actualModelId));
        } else {
            List<String> suggestions = integrationService.findSimilarModels(actualModelId, 5);
            response.put("suggestions", suggestions);
            response.put("message", "Model not found. See suggestions for similar models.");
        }
        
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        log.info("API: Get Hugging Face connection info");
        
        Map<String, Object> response = new HashMap<>();
        response.put("hubUrl", integrationService.getHubUrl());
        response.put("hasApiToken", integrationService.hasApiToken());
        response.put("status", "connected");
        
        // Test connection
        try {
            List<String> models = integrationService.searchModels("bert", 1);
            response.put("connectionTest", "success");
            response.put("testResult", "Found " + models.size() + " model(s)");
        } catch (Exception e) {
            response.put("connectionTest", "failed");
            response.put("testError", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // ========================================
    // File Operations Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> listModelFiles(String modelId) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: List files for model - {}", actualModelId);
        
        try {
            List<String> files = integrationService.listModelFiles(actualModelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("modelId", actualModelId);
            response.put("count", files.size());
            response.put("files", files);
            
            return ResponseEntity.ok(response);
            
        } catch (HuggingFaceClientService.ModelNotFoundException e) {
            log.warn("Model not found: {}", e.getModelId());
            return createNotFoundResponse(e);
        } catch (IOException e) {
            log.error("Error listing files for model {}: {}", actualModelId, e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    // ========================================
    // Artifact Download Endpoints
    // ========================================

    @Override
    public ResponseEntity<Resource> downloadArtifact(String modelId, String filename) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: Download artifact {} from model {}", filename, actualModelId);
        
        try {
            // Check file size to determine download strategy
            long fileSize = integrationService.getFileSize(actualModelId, filename);
            
            // For large files, use streaming
            if (fileSize > LARGE_FILE_THRESHOLD || fileSize == -1) {
                log.info("Using streaming for large file: {} (size: {} MB)", 
                    filename, (fileSize > 0 ? fileSize / (1024 * 1024) : "unknown"));
                
                InputStream inputStream = integrationService.streamArtifact(actualModelId, filename);
                InputStreamResource resource = new InputStreamResource(inputStream);
                
                ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
                
                if (fileSize > 0) {
                    builder.contentLength(fileSize);
                }
                
                return builder.body(resource);
                
            } else {
                // For smaller files, download to temp file first
                log.info("Downloading small file: {} (size: {} MB)", filename, (fileSize / (1024 * 1024)));
                
                File downloadedFile = integrationService.downloadArtifact(actualModelId, filename);
                
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
            
        } catch (HuggingFaceClientService.ModelNotFoundException e) {
            log.warn("Model not found: {}", e.getModelId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("X-Error-Message", e.getMessage().replace("\n", " | "))
                .build();
        } catch (IOException e) {
            log.error("Error downloading artifact {} from model {}: {}", filename, actualModelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error-Message", "Failed to download: " + e.getMessage())
                .build();
        }
    }

    @Override
    public ResponseEntity<Resource> downloadDeploymentArtifacts(String modelId) {
        String actualModelId = fromUrlSafeModelId(modelId);
        log.info("API: Download deployment artifacts for model {}", actualModelId);
        
        try {
            byte[] tarGzData = integrationService.downloadDeploymentArtifacts(actualModelId);
            
            ByteArrayResource resource = new ByteArrayResource(tarGzData);
            String filename = actualModelId.replace("/", "_") + "_deployment.tar.gz";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/gzip"))
                .contentLength(resource.contentLength())
                .body(resource);
                
        } catch (HuggingFaceClientService.ModelNotFoundException e) {
            log.warn("Model not found: {}", e.getModelId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("X-Error-Message", e.getMessage().replace("\n", " | "))
                .build();
        } catch (IOException e) {
            log.error("Error downloading deployment artifacts for model {}: {}", actualModelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error-Message", "Failed to create archive: " + e.getMessage())
                .build();
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String fromUrlSafeModelId(String urlSafeModelId) {
        return urlSafeModelId.replace("_", "/");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> createNotFoundResponse(
            HuggingFaceClientService.ModelNotFoundException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Model not found: " + e.getModelId());
        response.put("modelId", e.getModelId());
        response.put("isPrivate", e.isPrivate());
        
        if (!e.getSuggestions().isEmpty()) {
            response.put("suggestions", e.getSuggestions());
        }
        
        if (e.isPrivate()) {
            response.put("hint", "This model may be private. Ensure your API token has access.");
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
