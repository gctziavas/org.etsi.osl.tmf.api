package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Service for deploying MLflow models to a remote Docker host.
 * 
 * This service acts as a facade over DockerDeploymentService, providing
 * a simplified API for model deployment operations.
 * 
 * Prerequisites:
 * - Docker daemon on remote host with TCP API enabled
 * - MLflow tracking server accessible from the Docker host
 * - MLflow Docker image available on the remote host
 */
@Service
public class MlflowDeploymentService {

    @Autowired
    private DockerDeploymentService dockerDeploymentService;

    // ========== DEPLOYMENT METHODS ==========

    /**
     * Deploys a model to the remote Docker host.
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @return DeploymentResult with endpoint URL and container details
     */
    public DeploymentResult deployModel(String modelName, String version) throws IOException {
        if (!dockerDeploymentService.isEnabled()) {
            throw new IllegalStateException(
                "Docker deployment is not enabled. Configure mlflow.docker.enabled=true and mlflow.docker.host");
        }
        
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.deployModel(modelName, version);
        
        return new DeploymentResult(
            info.getModelName(),
            info.getVersion(),
            info.getInferenceUrl(),
            info.getContainerId(),
            info.getContainerName(),
            info.getHostPort(),
            info.getDockerHost()
        );
    }

    /**
     * Deploys a model to the remote Docker host on a specific port.
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @param hostPort The host port to expose
     * @return DeploymentResult with endpoint URL and container details
     */
    public DeploymentResult deployModel(String modelName, String version, int hostPort) throws IOException {
        if (!dockerDeploymentService.isEnabled()) {
            throw new IllegalStateException(
                "Docker deployment is not enabled. Configure mlflow.docker.enabled=true and mlflow.docker.host");
        }
        
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.deployModel(modelName, version, hostPort);
        
        return new DeploymentResult(
            info.getModelName(),
            info.getVersion(),
            info.getInferenceUrl(),
            info.getContainerId(),
            info.getContainerName(),
            info.getHostPort(),
            info.getDockerHost()
        );
    }

    /**
     * Deploys a model to a CUSTOM Docker host.
     * Use this to deploy models to Docker hosts other than the configured one.
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @param customDockerHost The Docker host IP/hostname to deploy to
     * @param customDockerPort The Docker API port on the custom host
     * @param hostPort The host port to expose (null for auto-assign)
     * @return DeploymentResult with endpoint URL and container details
     */
    public DeploymentResult deployModelToHost(String modelName, String version, 
            String customDockerHost, int customDockerPort, Integer hostPort) throws IOException {
        
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.deployModelToHost(modelName, version, 
                    customDockerHost, customDockerPort, hostPort);
        
        return new DeploymentResult(
            info.getModelName(),
            info.getVersion(),
            info.getInferenceUrl(),
            info.getContainerId(),
            info.getContainerName(),
            info.getHostPort(),
            info.getDockerHost()
        );
    }

    // ========== STOP DEPLOYMENT ==========

    /**
     * Stops and removes a deployed model container.
     * 
     * @param modelName The model name
     * @param version The model version
     */
    public void stopDeployment(String modelName, String version) {
        dockerDeploymentService.stopDeployment(modelName, version);
    }

    /**
     * Stops and removes a deployed model container on a CUSTOM Docker host.
     * 
     * @param modelName The model name
     * @param version The model version
     * @param customDockerHost The Docker host
     * @param customDockerPort The Docker API port
     */
    public void stopDeploymentOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        dockerDeploymentService.stopDeploymentOnHost(modelName, version, 
                customDockerHost, customDockerPort);
    }

    // ========== QUERY METHODS ==========

    /**
     * Checks if a model is currently deployed.
     */
    public boolean isDeployed(String modelName, String version) {
        return dockerDeploymentService.isDeployed(modelName, version);
    }

    /**
     * Checks if a model is currently deployed on a CUSTOM Docker host.
     */
    public boolean isDeployedOnHost(String modelName, String version, 
            String customDockerHost, int customDockerPort) {
        return dockerDeploymentService.isDeployedOnHost(modelName, version, 
                customDockerHost, customDockerPort);
    }

    /**
     * Gets the inference URL for a deployed model.
     * 
     * @return The inference URL, or null if not deployed
     */
    public String getInferenceUrl(String modelName, String version) {
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.getDeployment(modelName, version);
        return info != null ? info.getInferenceUrl() : null;
    }

    /**
     * Gets the inference URL for a deployed model on a CUSTOM Docker host.
     * 
     * @return The inference URL, or null if not deployed
     */
    public String getInferenceUrlOnHost(String modelName, String version, String customDockerHost) {
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.getDeploymentOnHost(modelName, version, customDockerHost);
        return info != null ? info.getInferenceUrl() : null;
    }

    /**
     * Gets detailed info about a deployed model.
     */
    public DeploymentResult getDeployment(String modelName, String version) {
        DockerDeploymentService.ContainerDeploymentInfo info = 
            dockerDeploymentService.getDeployment(modelName, version);
        
        if (info == null) {
            return null;
        }
        
        return new DeploymentResult(
            info.getModelName(),
            info.getVersion(),
            info.getInferenceUrl(),
            info.getContainerId(),
            info.getContainerName(),
            info.getHostPort(),
            info.getDockerHost()
        );
    }

    /**
     * Gets all running deployments.
     */
    public Map<String, DockerDeploymentService.ContainerDeploymentInfo> getRunningDeployments() {
        return dockerDeploymentService.getRunningDeployments();
    }

    /**
     * Checks if Docker deployment is enabled and configured.
     */
    public boolean isEnabled() {
        return dockerDeploymentService.isEnabled();
    }

    /**
     * Finds an available port on the Docker host.
     * If a preferred port is specified but in use, finds the next available one.
     * 
     * @param preferredPort The preferred port, or null to auto-assign
     * @return An available port
     */
    public int findAvailablePort(Integer preferredPort) {
        return dockerDeploymentService.findAvailablePort(preferredPort);
    }

    /**
     * Checks if a port is in use on the Docker host.
     * 
     * @param port The port to check
     * @return true if the port is in use
     */
    public boolean isPortInUse(int port) {
        return dockerDeploymentService.isPortInUse(port);
    }

    // ========== DEPLOYMENT RESULT CLASS ==========

    /**
     * Result of a model deployment operation.
     */
    public static class DeploymentResult {
        private final String modelName;
        private final String version;
        private final String inferenceUrl;
        private final String containerId;
        private final String containerName;
        private final int hostPort;
        private final String dockerHost;
        private final long startTime;

        public DeploymentResult(String modelName, String version, String inferenceUrl,
                String containerId, String containerName, int hostPort, String dockerHost) {
            this.modelName = modelName;
            this.version = version;
            this.inferenceUrl = inferenceUrl;
            this.containerId = containerId;
            this.containerName = containerName;
            this.hostPort = hostPort;
            this.dockerHost = dockerHost;
            this.startTime = System.currentTimeMillis();
        }

        public String getModelName() { return modelName; }
        public String getVersion() { return version; }
        public String getInferenceUrl() { return inferenceUrl; }
        public String getContainerId() { return containerId; }
        public String getContainerName() { return containerName; }
        public int getHostPort() { return hostPort; }
        public String getDockerHost() { return dockerHost; }
        public long getStartTime() { return startTime; }

        public String getDeploymentKey() {
            return modelName + ":" + version;
        }
    }
}
