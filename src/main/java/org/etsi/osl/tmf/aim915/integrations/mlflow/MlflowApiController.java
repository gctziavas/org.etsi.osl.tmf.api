package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller implementing the MLflow integration API.
 */
@RestController
public class MlflowApiController implements MlflowApi {

    private static final Logger log = LoggerFactory.getLogger(MlflowApiController.class);

    private final MlflowIntegrationService integrationService;

    public MlflowApiController(MlflowIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    // ========================================
    // Specification Import Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> importModelAsSpecification(String modelName, String version) {
        log.info("API: Import model as specification - {} v{}", modelName, version);
        
        try {
            AiModelSpecification spec = integrationService.importModelAsSpecification(modelName, version);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Specification imported successfully");
            response.put("specificationId", spec.getId());
            response.put("name", spec.getName());
            response.put("version", spec.getVersion());
            response.put("description", spec.getDescription());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to import model {}: {}", modelName, e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "Model not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error importing model {}: {}", modelName, e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> syncAllModels() {
        log.info("API: Sync all MLflow models");
        
        try {
            List<AiModelSpecification> specs = integrationService.syncAllModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Sync completed");
            response.put("count", specs.size());
            response.put("specifications", specs.stream()
                .map(s -> Map.of(
                    "id", s.getId(),
                    "name", s.getName(),
                    "version", s.getVersion()
                ))
                .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error syncing models: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    // ========================================
    // Model Instance Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> deployModel(String modelName, String version, String deploymentEndpoint) {
        log.info("API: Deploy model - {} v{} to {}", modelName, version, deploymentEndpoint);
        
        try {
            AiModel model = integrationService.deployModelFromMlflow(modelName, version, deploymentEndpoint);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Model deployed successfully");
            response.put("modelId", model.getId());
            response.put("name", model.getName());
            response.put("state", model.getState());
            response.put("specificationId", model.getAiModelSpecification() != null ? 
                model.getAiModelSpecification().getId() : null);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to deploy model {}: {}", modelName, e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, "Model not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deploying model {}: {}", modelName, e.getMessage(), e);
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
    // Query Endpoints
    // ========================================

    @Override
    public ResponseEntity<Map<String, Object>> listModels() {
        log.info("API: List MLflow models");
        
        try {
            List<String> models = integrationService.listMlflowModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", models.size());
            response.put("models", models);
            response.put("trackingUri", integrationService.getMlflowTrackingUri());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error listing models: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "MLflow connection error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkModelExists(String modelName) {
        log.info("API: Check model exists - {}", modelName);
        
        boolean exists = integrationService.modelExistsInMlflow(modelName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("modelName", modelName);
        response.put("exists", exists);
        if (exists) {
            response.put("mlflowUrl", integrationService.getMlflowModelUrl(modelName, null));
        }
        
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        log.info("API: Get MLflow connection info");
        
        Map<String, Object> response = new HashMap<>();
        response.put("trackingUri", integrationService.getMlflowTrackingUri());
        
        // Test connection
        try {
            List<String> models = integrationService.listMlflowModels();
            response.put("status", "connected");
            response.put("modelCount", models.size());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // ========================================
    // Artifact Endpoints
    // ========================================

    @Override
    public ResponseEntity<Resource> downloadArtifact(String modelId, String artifactType) {
        log.info("API: Download artifact - {} for model {}", artifactType, modelId);
        
        try {
            // Parse modelId (format: modelName_vVersion)
            String modelName = extractModelName(modelId);
            String artifactPath = mapArtifactTypesToPath(artifactType);
            
            if (artifactPath == null) {
                log.warn("Invalid artifact type: {}", artifactType);
                return ResponseEntity.badRequest().build();
            }
            
            File artifact = integrationService.downloadArtifact(modelName, artifactPath);
            
            if (artifact == null) {
                log.warn("Artifact not found: {} for model {}", artifactPath, modelName);
                return ResponseEntity.notFound().build();
            }
            
            byte[] content = Files.readAllBytes(artifact.toPath());
            ByteArrayResource resource = new ByteArrayResource(content);
            
            log.info("Downloaded artifact {} for model {}, size: {} bytes", 
                artifactType, modelName, content.length);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + artifact.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(resource);
            
        } catch (IOException e) {
            log.error("Error downloading artifact: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> listArtifacts(String modelName) {
        log.info("API: List artifacts for model - {}", modelName);
        
        try {
            List<String> artifacts = integrationService.listArtifacts(modelName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("modelName", modelName);
            response.put("count", artifacts.size());
            response.put("artifacts", artifacts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error listing artifacts: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Extracts model name from modelId (format: modelName_vVersion).
     */
    private String extractModelName(String modelId) {
        int versionIndex = modelId.lastIndexOf("_v");
        if (versionIndex > 0) {
            return modelId.substring(0, versionIndex);
        }
        return modelId;
    }

    /**
     * Maps artifact type to MLflow artifact path.
     */
    private String mapArtifactTypesToPath(String artifactType) {
        return switch (artifactType.toLowerCase()) {
            case "model" -> "model";
            case "training_data", "train_data" -> "training_data";
            case "evaluation_data", "eval_data", "test_data" -> "evaluation_data";
            case "model_data_sheet", "datasheet", "model_card", "readme" -> "model_data_sheet";
            case "deployment_record", "deployment", "deployment_config" -> "deployment_record";
            case "inherited_model", "base_model", "parent_model" -> "inherited_model";
            case "requirements", "conda" -> "requirements.txt";
            case "checkpoints" -> "checkpoints";
            case "logs" -> "logs";
            case "plots", "figures" -> "plots";
            default -> artifactType; // Allow direct artifact paths
        };
    }

    /**
     * Creates an error response.
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
