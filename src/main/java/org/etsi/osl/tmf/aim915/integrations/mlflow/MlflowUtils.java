package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.mlflow.tracking.MlflowClient;
import org.mlflow.api.proto.Service.FileInfo;
import org.mlflow.api.proto.ModelRegistry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for retrieving different types of artifacts from MLflow runs.
 */
public class MlflowUtils {

    private static final Logger log = LoggerFactory.getLogger(MlflowUtils.class);

    private final MlflowClient client;

    public MlflowUtils(MlflowClient client) {
        this.client = client;
    }

    /**
     * Retrieves the model artifact from a run.
     * Looks for artifacts in common model paths: "model", "models", "MLmodel"
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the model artifact, or null if not found
     */
    public File getModelArtifact(String runId) {
        log.debug("Retrieving model artifact for run: {}", runId);
        try {
            // Try common model paths
            String[] modelPaths = {"model", "models", "MLmodel"};
            
            for (String path : modelPaths) {
                List<FileInfo> artifacts = client.listArtifacts(runId, "");
                Optional<FileInfo> modelInfo = artifacts.stream()
                        .filter(info -> info.getPath().equals(path))
                        .findFirst();
                
                if (modelInfo.isPresent()) {
                    log.info("Found model artifact at path: {}", path);
                    return client.downloadArtifacts(runId, path);
                }
            }
            
            log.warn("No model artifact found for run: {}", runId);
            return null;
            
        } catch (Exception e) {
            log.error("Error retrieving model artifact for run {}: {}", runId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves training data artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the training data artifact, or null if not found
     */
    public File getTrainingDataArtifact(String runId) {
        log.debug("Retrieving training data artifact for run: {}", runId);
        return getArtifactByPath(runId, "training_data");
    }

    /**
     * Retrieves evaluation data artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the evaluation data artifact, or null if not found
     */
    public File getEvaluationDataArtifact(String runId) {
        log.debug("Retrieving evaluation data artifact for run: {}", runId);
        return getArtifactByPath(runId, "evaluation_data");
    }

    /**
     * Retrieves validation data artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the validation data artifact, or null if not found
     */
    public File getValidationDataArtifact(String runId) {
        log.debug("Retrieving validation data artifact for run: {}", runId);
        return getArtifactByPath(runId, "validation_data");
    }

    /**
     * Retrieves plot/figure artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the plots directory, or null if not found
     */
    public File getPlotsArtifact(String runId) {
        log.debug("Retrieving plots artifact for run: {}", runId);
        
        // Try common plot paths
        String[] plotPaths = {"plots", "figures", "visualizations"};
        for (String path : plotPaths) {
            File artifact = getArtifactByPath(runId, path);
            if (artifact != null) {
                return artifact;
            }
        }
        
        log.warn("No plots artifact found for run: {}", runId);
        return null;
    }

    /**
     * Retrieves model requirements file (requirements.txt or conda.yaml).
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the requirements file, or null if not found
     */
    public File getRequirementsArtifact(String runId) {
        log.debug("Retrieving requirements artifact for run: {}", runId);
        
        try {
            // Try to get requirements.txt from model directory
            List<FileInfo> modelArtifacts = client.listArtifacts(runId, "model");
            
            Optional<FileInfo> requirementsInfo = modelArtifacts.stream()
                    .filter(info -> info.getPath().endsWith("requirements.txt") || 
                                   info.getPath().endsWith("conda.yaml"))
                    .findFirst();
            
            if (requirementsInfo.isPresent()) {
                log.info("Found requirements artifact: {}", requirementsInfo.get().getPath());
                return client.downloadArtifacts(runId, requirementsInfo.get().getPath());
            }
            
            log.warn("No requirements artifact found for run: {}", runId);
            return null;
            
        } catch (Exception e) {
            log.error("Error retrieving requirements artifact for run {}: {}", runId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves model checkpoints from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the checkpoints directory, or null if not found
     */
    public File getCheckpointsArtifact(String runId) {
        log.debug("Retrieving checkpoints artifact for run: {}", runId);
        return getArtifactByPath(runId, "checkpoints");
    }

    /**
     * Retrieves preprocessor artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the preprocessor artifact, or null if not found
     */
    public File getPreprocessorArtifact(String runId) {
        log.debug("Retrieving preprocessor artifact for run: {}", runId);
        return getArtifactByPath(runId, "preprocessor");
    }

    /**
     * Retrieves feature engineering artifacts from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the feature engineering artifact, or null if not found
     */
    public File getFeatureEngineeringArtifact(String runId) {
        log.debug("Retrieving feature engineering artifact for run: {}", runId);
        return getArtifactByPath(runId, "feature_engineering");
    }

    /**
     * Retrieves log files from a run.
     * 
     * @param runId The MLflow run ID
     * @return File pointing to the logs directory, or null if not found
     */
    public File getLogsArtifact(String runId) {
        log.debug("Retrieving logs artifact for run: {}", runId);
        return getArtifactByPath(runId, "logs");
    }

    /**
     * Lists all available artifacts at the root level of a run.
     * 
     * @param runId The MLflow run ID
     * @return List of artifact paths
     */
    public List<String> listAllArtifacts(String runId) {
        log.debug("Listing all artifacts for run: {}", runId);
        try {
            return client.listArtifacts(runId, "").stream()
                    .map(FileInfo::getPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing artifacts for run {}: {}", runId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts in a specific directory within a run.
     * 
     * @param runId The MLflow run ID
     * @param path The directory path to list
     * @return List of artifact paths under the specified directory
     */
    public List<String> listArtifactsInDirectory(String runId, String path) {
        log.debug("Listing artifacts in directory {} for run: {}", path, runId);
        try {
            return client.listArtifacts(runId, path).stream()
                    .map(FileInfo::getPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing artifacts in directory {} for run {}: {}", path, runId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a specific artifact by its exact path.
     * 
     * @param runId The MLflow run ID
     * @param artifactPath The exact path of the artifact
     * @return File pointing to the artifact, or null if not found
     */
    public File getArtifactByPath(String runId, String artifactPath) {
        log.debug("Retrieving artifact at path {} for run: {}", artifactPath, runId);
        try {
            // Check if the artifact exists
            List<FileInfo> rootArtifacts = client.listArtifacts(runId, "");
            boolean exists = rootArtifacts.stream()
                    .anyMatch(info -> info.getPath().equals(artifactPath) || 
                                     info.getPath().startsWith(artifactPath + "/"));
            
            if (!exists) {
                log.warn("Artifact not found at path {} for run: {}", artifactPath, runId);
                return null;
            }
            
            File artifact = client.downloadArtifacts(runId, artifactPath);
            log.info("Successfully retrieved artifact at path: {}", artifactPath);
            return artifact;
            
        } catch (Exception e) {
            log.error("Error retrieving artifact at path {} for run {}: {}", artifactPath, runId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds and retrieves an artifact by searching for a pattern in the artifact path.
     * Returns the first matching artifact.
     * 
     * @param runId The MLflow run ID
     * @param pathPattern Pattern to search for in artifact paths (case-insensitive)
     * @return File pointing to the first matching artifact, or null if not found
     */
    public File findArtifactByPattern(String runId, String pathPattern) {
        log.debug("Finding artifact matching pattern '{}' for run: {}", pathPattern, runId);
        try {
            List<FileInfo> artifacts = client.listArtifacts(runId, "");
            
            Optional<FileInfo> matchingArtifact = artifacts.stream()
                    .filter(info -> info.getPath().toLowerCase().contains(pathPattern.toLowerCase()))
                    .findFirst();
            
            if (matchingArtifact.isPresent()) {
                String path = matchingArtifact.get().getPath();
                log.info("Found matching artifact at path: {}", path);
                return client.downloadArtifacts(runId, path);
            }
            
            log.warn("No artifact found matching pattern '{}' for run: {}", pathPattern, runId);
            return null;
            
        } catch (Exception e) {
            log.error("Error finding artifact by pattern '{}' for run {}: {}", pathPattern, runId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves all artifacts matching a pattern.
     * 
     * @param runId The MLflow run ID
     * @param pathPattern Pattern to search for in artifact paths (case-insensitive)
     * @return List of Files pointing to matching artifacts
     */
    public List<File> findAllArtifactsByPattern(String runId, String pathPattern) {
        log.debug("Finding all artifacts matching pattern '{}' for run: {}", pathPattern, runId);
        List<File> matchingFiles = new ArrayList<>();
        
        try {
            List<FileInfo> artifacts = client.listArtifacts(runId, "");
            
            List<String> matchingPaths = artifacts.stream()
                    .filter(info -> info.getPath().toLowerCase().contains(pathPattern.toLowerCase()))
                    .map(FileInfo::getPath)
                    .collect(Collectors.toList());
            
            for (String path : matchingPaths) {
                try {
                    File artifact = client.downloadArtifacts(runId, path);
                    matchingFiles.add(artifact);
                    log.debug("Retrieved artifact: {}", path);
                } catch (Exception e) {
                    log.warn("Failed to download artifact at path {}: {}", path, e.getMessage());
                }
            }
            
            log.info("Found {} artifacts matching pattern '{}'", matchingFiles.size(), pathPattern);
            return matchingFiles;
            
        } catch (Exception e) {
            log.error("Error finding artifacts by pattern '{}' for run {}: {}", pathPattern, runId, e.getMessage(), e);
            return matchingFiles;
        }
    }

    /**
     * Checks if an artifact exists at the given path.
     * 
     * @param runId The MLflow run ID
     * @param artifactPath The path to check
     * @return true if the artifact exists, false otherwise
     */
    public boolean artifactExists(String runId, String artifactPath) {
        log.debug("Checking if artifact exists at path {} for run: {}", artifactPath, runId);
        try {
            List<FileInfo> artifacts = client.listArtifacts(runId, "");
            boolean exists = artifacts.stream()
                    .anyMatch(info -> info.getPath().equals(artifactPath) || 
                                     info.getPath().startsWith(artifactPath + "/"));
            
            log.debug("Artifact at path {} {} for run: {}", artifactPath, exists ? "exists" : "does not exist", runId);
            return exists;
            
        } catch (Exception e) {
            log.error("Error checking artifact existence at path {} for run {}: {}", artifactPath, runId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Downloads a specific artifact for a given model by name.
     * 
     * @param modelName The name of the model
     * @param artifactPath The path to the artifact to download
     * @return File pointing to the downloaded artifact, or null if not found
     */
    public File downloadArtifact(String modelName, String artifactPath) {
        log.info("Downloading artifact {} for model: {}", artifactPath, modelName);
        try {
            // Search for model versions - returns a paginated result
            var versionsPage = client.searchModelVersions("name='" + modelName + "'");
            List<ModelVersion> versions = versionsPage.getItems();
            
            if (versions == null || versions.isEmpty()) {
                log.warn("No versions found for model: {}", modelName);
                return null;
            }
            
            // Get the latest version's run ID
            String runId = versions.get(0).getRunId();
            log.debug("Using run ID {} for model {}", runId, modelName);
            
            File artifact = client.downloadArtifacts(runId, artifactPath);
            log.info("Successfully downloaded artifact {} from model {}", artifactPath, modelName);
            return artifact;
            
        } catch (Exception e) {
            log.error("Error downloading artifact {} for model {}: {}", 
                artifactPath, modelName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lists all available artifacts for a given model.
     * This is useful to discover what artifacts (model files, datasets, etc.) are available.
     * 
     * @param modelName The name of the model
     * @return List of artifact paths available for the model's run
     */
    public List<String> listAvailableArtifacts(String modelName) {
        log.debug("Listing available artifacts for model: {}", modelName);
        try {
            // Search for model versions
            var versionsPage = client.searchModelVersions("name='" + modelName + "'");
            List<ModelVersion> versions = versionsPage.getItems();
            
            if (versions == null || versions.isEmpty()) {
                log.warn("No versions found for model: {}", modelName);
                return new ArrayList<>();
            }
            
            // Get the latest version's run ID
            String runId = versions.get(0).getRunId();
                       
            // List all artifacts for this run
            log.debug("Listing artifacts for run ID: {}", runId); 
            List<String> artifacts = client.listArtifacts(runId).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(Collectors.toList());
                    
            log.info("Found {} artifacts for model {}", artifacts.size(), modelName);
            return artifacts;
            
        } catch (Exception e) {
            log.error("Error listing artifacts for model {}: {}", modelName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts for a specific run ID.
     * 
     * @param runId The MLflow run ID
     * @return List of artifact paths
     */
    public List<String> listArtifactsByRunId(String runId) {
        log.debug("Listing artifacts for run ID: {}", runId);
        try {
            List<String> artifacts = client.listArtifacts(runId).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(Collectors.toList());
            log.info("Found {} artifacts for run {}", artifacts.size(), runId);
            return artifacts;
        } catch (Exception e) {
            log.error("Error listing artifacts for run {}: {}", runId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts under a specific path in a run.
     * 
     * @param runId The MLflow run ID
     * @param path The path within the artifacts directory (e.g., "training_data")
     * @return List of artifact paths under the specified path
     */
    public List<String> listArtifactsByPath(String runId, String path) {
        log.debug("Listing artifacts by path: runId={}, path={}", runId, path);
        try {
            List<String> artifacts = client.listArtifacts(runId, path).stream()
                    .map(fileInfo -> fileInfo.getPath())
                    .collect(Collectors.toList());
            log.info("Found {} artifacts for run {} at path {}", artifacts.size(), runId, path);
            return artifacts;
        } catch (Exception e) {
            log.error("Error listing artifacts by path for run {} at {}: {}", runId, path, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }
}
