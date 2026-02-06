package org.etsi.osl.tmf.aim915.integrations.mlflow;

import jakarta.annotation.PreDestroy;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.tracking.ModelVersionsPage;
import org.mlflow.tracking.ExperimentsPage;
import org.mlflow.api.proto.ModelRegistry.*;
import org.mlflow.api.proto.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Low-level service for communicating with MLflow API.
 * 
 * This service handles all direct interactions with MLflow:
 * - Model Registry operations (list, get, search models)
 * - Artifact operations (list, download, check existence)
 * - Run information retrieval
 * 
 * Higher-level services should use this service for MLflow access.
 */
@Service
public class MlflowClientService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MlflowClientService.class);

    private final MlflowClient client;
    private final String trackingUri;

    public MlflowClientService(MlflowClient mlflowClient, 
                                String mlflowTrackingUri) {
        this.client = mlflowClient;
        this.trackingUri = mlflowTrackingUri;
        log.info("MlflowClientService initialized with tracking URI: {}", trackingUri);
    }

    /**
     * Cleanup resources when the service is destroyed.
     */
    @PreDestroy
    @Override
    public void close() {
        log.info("Closing MlflowClient");
        try {
            client.close();
        } catch (Exception e) {
            log.warn("Error closing MlflowClient: {}", e.getMessage());
        }
    }

    // ========================================
    // Model Registry Operations
    // ========================================

    /**
     * Lists all registered models in MLflow.
     * Uses the native client to search model versions and extract unique model names.
     * 
     * @return List of registered model names
     * @throws IOException if the request fails
     */
    public List<String> listRegisteredModels() throws IOException {
        log.debug("Listing all registered models");
        
        try {
            // Use searchModelVersions to get all models
            ModelVersionsPage page = client.searchModelVersions("");
            List<String> modelNames = page.getItems().stream()
                    .map(ModelVersion::getName)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.debug("Found {} registered models", modelNames.size());
            return modelNames;
            
        } catch (Exception e) {
            log.error("Error listing registered models: {}", e.getMessage());
            throw new IOException("Error listing registered models: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a registered model by name.
     * 
     * @param modelName The registered model name
     * @return RegisteredModel containing model metadata
     * @throws IOException if model not found or request fails
     */
    public RegisteredModel getRegisteredModel(String modelName) throws IOException {
        log.debug("Fetching registered model: {}", modelName);
        
        try {
            RegisteredModel model = client.getRegisteredModel(modelName);
            log.debug("Found registered model: {}", modelName);
            return model;
            
        } catch (Exception e) {
            log.error("Error fetching model {}: {}", modelName, e.getMessage());
            throw new IOException("Error fetching model '" + modelName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific model version.
     * 
     * @param modelName The registered model name
     * @param version The version number
     * @return ModelVersion containing version metadata
     * @throws IOException if version not found or request fails
     */
    public ModelVersion getModelVersion(String modelName, String version) throws IOException {
        log.debug("Fetching model version: {} v{}", modelName, version);
        
        try {
            ModelVersion modelVersion = client.getModelVersion(modelName, version);
            log.debug("Found model version: {} v{}", modelName, version);
            return modelVersion;
            
        } catch (Exception e) {
            log.error("Error fetching model version {} v{}: {}", modelName, version, e.getMessage());
            throw new IOException("Error fetching model version: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all versions of a registered model.
     * 
     * @param modelName The registered model name
     * @return List of ModelVersion objects
     */
    public List<ModelVersion> getModelVersions(String modelName) {
        log.debug("Fetching all versions for model: {}", modelName);
        try {
            var versionsPage = client.searchModelVersions("name='" + modelName + "'");
            List<ModelVersion> versions = versionsPage.getItems();
            log.debug("Found {} versions for model {}", versions.size(), modelName);
            return versions;
        } catch (Exception e) {
            log.error("Error fetching versions for model {}: {}", modelName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the latest version of a model.
     * 
     * @param modelName The registered model name
     * @return The latest ModelVersion, or null if none found
     */
    public ModelVersion getLatestModelVersion(String modelName) {
        List<ModelVersion> versions = getModelVersions(modelName);
        if (versions.isEmpty()) {
            log.warn("No versions found for model: {}", modelName);
            return null;
        }
        // Versions are returned in descending order, so first is latest
        return versions.get(0);
    }

    /**
     * Gets the latest model versions for specific stages.
     * Available stages: None, Staging, Production, Archived
     * 
     * @param modelName The registered model name
     * @param stages List of stages to get versions for (e.g., "Production", "Staging")
     * @return List of latest ModelVersions for each requested stage
     */
    public List<ModelVersion> getLatestVersionsByStage(String modelName, String... stages) {
        log.debug("Fetching latest versions for model {} in stages: {}", modelName, Arrays.toString(stages));
        try {
            List<ModelVersion> versions = client.getLatestVersions(modelName, Arrays.asList(stages));
            log.debug("Found {} versions for stages", versions.size());
            return versions;
        } catch (Exception e) {
            log.error("Error fetching latest versions by stage for {}: {}", modelName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the production version of a model.
     * 
     * @param modelName The registered model name
     * @return Optional containing the production ModelVersion, or empty if none
     */
    public Optional<ModelVersion> getProductionVersion(String modelName) {
        List<ModelVersion> versions = getLatestVersionsByStage(modelName, "Production");
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }

    /**
     * Gets the staging version of a model.
     * 
     * @param modelName The registered model name
     * @return Optional containing the staging ModelVersion, or empty if none
     */
    public Optional<ModelVersion> getStagingVersion(String modelName) {
        List<ModelVersion> versions = getLatestVersionsByStage(modelName, "Staging");
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }

    /**
     * Checks if a model exists in the registry.
     * 
     * @param modelName The registered model name
     * @return true if model exists
     */
    public boolean modelExists(String modelName) {
        try {
            getRegisteredModel(modelName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ========================================
    // Run Operations
    // ========================================

    /**
     * Gets a run by its ID.
     * 
     * @param runId The run ID
     * @return Run object containing run information
     */
    public Run getRun(String runId) {
        log.debug("Fetching run: {}", runId);
        return client.getRun(runId);
    }

    // ========================================
    // Metric Operations
    // ========================================

    /**
     * Gets the metric history for a specific metric key.
     * 
     * @param runId The run ID
     * @param metricKey The metric key (e.g., "accuracy", "loss")
     * @return List of Metric values over time
     */
    public List<Metric> getMetricHistory(String runId, String metricKey) {
        log.debug("Fetching metric history for run {} key: {}", runId, metricKey);
        try {
            List<Metric> metrics = client.getMetricHistory(runId, metricKey);
            log.debug("Found {} metric values for key {}", metrics.size(), metricKey);
            return metrics;
        } catch (Exception e) {
            log.error("Error fetching metric history for run {} key {}: {}", runId, metricKey, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all metrics from a run.
     * 
     * @param runId The run ID
     * @return List of latest Metric values
     */
    public List<Metric> getRunMetrics(String runId) {
        log.debug("Fetching all metrics for run: {}", runId);
        try {
            Run run = client.getRun(runId);
            return run.getData().getMetricsList();
        } catch (Exception e) {
            log.error("Error fetching metrics for run {}: {}", runId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all parameters from a run.
     * 
     * @param runId The run ID
     * @return List of Param values
     */
    public List<Param> getRunParams(String runId) {
        log.debug("Fetching all params for run: {}", runId);
        try {
            Run run = client.getRun(runId);
            return run.getData().getParamsList();
        } catch (Exception e) {
            log.error("Error fetching params for run {}: {}", runId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all tags from a run.
     * 
     * @param runId The run ID
     * @return List of RunTag values
     */
    public List<RunTag> getRunTags(String runId) {
        log.debug("Fetching all tags for run: {}", runId);
        try {
            Run run = client.getRun(runId);
            return run.getData().getTagsList();
        } catch (Exception e) {
            log.error("Error fetching tags for run {}: {}", runId, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========================================
    // Experiment Operations
    // ========================================

    /**
     * Gets an experiment by ID.
     * 
     * @param experimentId The experiment ID
     * @return Experiment object
     */
    public Experiment getExperiment(String experimentId) {
        log.debug("Fetching experiment: {}", experimentId);
        return client.getExperiment(experimentId);
    }

    /**
     * Gets an experiment by name.
     * 
     * @param experimentName The experiment name
     * @return Optional containing the Experiment, or empty if not found
     */
    public Optional<Experiment> getExperimentByName(String experimentName) {
        log.debug("Fetching experiment by name: {}", experimentName);
        return client.getExperimentByName(experimentName);
    }

    /**
     * Searches for experiments matching a filter.
     * 
     * @param searchFilter SQL-like filter (e.g., "attribute.name = 'MyExperiment'")
     * @return List of matching Experiments
     */
    public List<Experiment> searchExperiments(String searchFilter) {
        log.debug("Searching experiments with filter: {}", searchFilter);
        try {
            ExperimentsPage page = client.searchExperiments(searchFilter);
            List<Experiment> experiments = page.getItems();
            log.debug("Found {} experiments matching filter", experiments.size());
            return experiments;
        } catch (Exception e) {
            log.error("Error searching experiments: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lists all active experiments.
     * 
     * @return List of all active Experiments
     */
    public List<Experiment> listExperiments() {
        log.debug("Listing all experiments");
        try {
            ExperimentsPage page = client.searchExperiments();
            List<Experiment> experiments = page.getItems();
            log.debug("Found {} experiments", experiments.size());
            return experiments;
        } catch (Exception e) {
            log.error("Error listing experiments: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========================================
    // Artifact Operations
    // ========================================

    /**
     * Lists all artifacts for a run.
     * 
     * @param runId The run ID
     * @return List of artifact paths
     */
    public List<String> listArtifacts(String runId) {
        log.debug("Listing artifacts for run: {}", runId);
        try {
            return client.listArtifacts(runId).stream()
                    .map(FileInfo::getPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing artifacts for run {}: {}", runId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lists artifacts under a specific path.
     * 
     * @param runId The run ID
     * @param path The path within artifacts
     * @return List of artifact paths
     */
    public List<String> listArtifacts(String runId, String path) {
        log.debug("Listing artifacts for run {} at path: {}", runId, path);
        try {
            return client.listArtifacts(runId, path).stream()
                    .map(FileInfo::getPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing artifacts for run {} at {}: {}", runId, path, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Checks if an artifact exists at the given path.
     * 
     * @param runId The run ID
     * @param artifactPath The artifact path
     * @return true if artifact exists
     */
    public boolean artifactExists(String runId, String artifactPath) {
        log.debug("Checking artifact existence: {} in run {}", artifactPath, runId);
        try {
            List<FileInfo> artifacts = client.listArtifacts(runId, "");
            return artifacts.stream()
                    .anyMatch(info -> info.getPath().equals(artifactPath) || 
                                     info.getPath().startsWith(artifactPath + "/"));
        } catch (Exception e) {
            log.error("Error checking artifact existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Downloads an artifact by path.
     * 
     * @param runId The run ID
     * @param artifactPath The artifact path
     * @return File pointing to downloaded artifact, or null if not found
     */
    public File downloadArtifact(String runId, String artifactPath) {
        log.debug("Downloading artifact: {} from run {}", artifactPath, runId);
        try {
            if (!artifactExists(runId, artifactPath)) {
                log.warn("Artifact not found: {} in run {}", artifactPath, runId);
                return null;
            }
            File artifact = client.downloadArtifacts(runId, artifactPath);
            log.info("Successfully downloaded artifact: {}", artifactPath);
            return artifact;
        } catch (Exception e) {
            log.error("Error downloading artifact {} from run {}: {}", artifactPath, runId, e.getMessage());
            return null;
        }
    }

    /**
     * Downloads an artifact for a model (using latest version's run).
     * 
     * @param modelName The model name
     * @param artifactPath The artifact path
     * @return File pointing to downloaded artifact, or null if not found
     */
    public File downloadArtifactForModel(String modelName, String artifactPath) {
        log.debug("Downloading artifact {} for model: {}", artifactPath, modelName);
        ModelVersion version = getLatestModelVersion(modelName);
        if (version == null) {
            log.warn("No versions found for model: {}", modelName);
            return null;
        }
        return downloadArtifact(version.getRunId(), artifactPath);
    }

    /**
     * Finds artifact by trying multiple common paths.
     * 
     * @param runId The run ID
     * @param possiblePaths Array of paths to try
     * @return The first matching path, or null if none found
     */
    public String findArtifactPath(String runId, String... possiblePaths) {
        for (String path : possiblePaths) {
            if (artifactExists(runId, path)) {
                log.debug("Found artifact at path: {}", path);
                return path;
            }
        }
        return null;
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Gets the tracking URI.
     * 
     * @return The MLflow tracking URI
     */
    public String getTrackingUri() {
        return trackingUri;
    }

    /**
     * Builds the MLflow UI URL for a model.
     * 
     * @param modelName The model name
     * @param version Optional version number
     * @return URL to view model in MLflow UI
     */
    public String buildModelUrl(String modelName, String version) {
        String url = trackingUri + "/#/models/" + modelName;
        if (version != null) {
            url += "/versions/" + version;
        }
        return url;
    }

    /**
     * Builds the MLflow UI URL for a run.
     * 
     * @param experimentId The experiment ID
     * @param runId The run ID
     * @return URL to view run in MLflow UI
     */
    public String buildRunUrl(String experimentId, String runId) {
        return trackingUri + "/#/experiments/" + experimentId + "/runs/" + runId;
    }
}
