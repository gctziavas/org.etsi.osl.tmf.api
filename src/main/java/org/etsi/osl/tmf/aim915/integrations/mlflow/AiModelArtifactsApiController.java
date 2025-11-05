package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.springframework.core.io.ByteArrayResource;
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
import java.nio.file.Files;

@RestController
public class AiModelArtifactsApiController implements AiModelArtifactsApi {

    private static final Logger log = LoggerFactory.getLogger(AiModelArtifactsApiController.class);
    private final MlflowService mlflowService;

    public AiModelArtifactsApiController(MlflowService mlflowService) {
        this.mlflowService = mlflowService;
    }

    @Override
    public ResponseEntity<Resource> downloadMlflowArtifact(String modelId, String artifactType) {
        log.info("Request to download MLflow artifact: type={} for modelId={}", artifactType, modelId);
        try {
            // Parse modelId to extract model name (format: modelName_vVersion)
            String modelName = extractModelName(modelId);
            log.debug("Extracted model name: {}", modelName);
            
            // Map artifact type to MLflow artifact path
            String artifactPath = mapArtifactTypeToPath(artifactType);
            
            if (artifactPath == null) {
                log.warn("Invalid artifact type: {}", artifactType);
                return ResponseEntity.badRequest().build();
            }
            
            log.debug("Mapped artifact type '{}' to path '{}'", artifactType, artifactPath);
            
            File downloadedArtifact = mlflowService.downloadArtifact(modelName, artifactPath);

            if (downloadedArtifact == null) {
                log.warn("Artifact not found: {} for model {}", artifactPath, modelName);
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(downloadedArtifact.toPath()));

            log.info("Successfully downloaded artifact {} for model {}, size: {} bytes", 
                artifactType, modelName, resource.contentLength());
                
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadedArtifact.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (IOException e) {
            log.error("Error downloading MLflow artifact {} for model {}: {}", 
                artifactType, modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error downloading artifact {} for model {}: {}", 
                artifactType, modelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Extracts the model name from the modelId (format: modelName_vVersion)
     */
    private String extractModelName(String modelId) {
        int versionIndex = modelId.lastIndexOf("_v");
        if (versionIndex > 0) {
            return modelId.substring(0, versionIndex);
        }
        return modelId;
    }
    
    /**
     * Maps artifact type to MLflow artifact path
     * @param artifactType: 'model', 'training_data', 'evaluation_data'
     * @return MLflow artifact path or null if invalid
     */
    private String mapArtifactTypeToPath(String artifactType) {
        switch (artifactType.toLowerCase()) {
            case "model":
                return "model";
            case "training_data":
                return "training_data";
            case "evaluation_data":
                return "evaluation_data";
            default:
                return null;
        }
    }
}
