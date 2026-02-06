package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for deploying MLflow models to a remote Docker host.
 * 
 * Uses Docker Remote API to create and manage containers running
 * `mlflow models serve` on a remote Docker daemon.
 * 
 * Prerequisites:
 * - Docker daemon on remote host with TCP API enabled
 * - MLflow tracking server accessible from the Docker host
 * - MLflow Docker image available on the remote host
 */
@Service
public class DockerDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DockerDeploymentService.class);
    private static final String DOCKER_API_VERSION = "v1.43";

    @Value("${mlflow.docker.enabled:false}")
    private boolean dockerEnabled;

    @Value("${mlflow.docker.host:}")
    private String dockerHost;

    @Value("${mlflow.docker.port:2375}")
    private int dockerPort;

    @Value("${mlflow.docker.tls-enabled:false}")
    private boolean tlsEnabled;

    @Value("${mlflow.docker.image:ghcr.io/mlflow/mlflow:v2.10.0}")
    private String mlflowImage;

    @Value("${mlflow.tracking-uri:}")
    private String trackingUri;

    @Value("${mlflow.host:127.0.0.1}")
    private String mlflowHost;

    @Value("${mlflow.port:5000}")
    private int mlflowPort;

    @Value("${mlflow.docker.container-port:5001}")
    private int defaultContainerPort;

    @Value("${mlflow.docker.startup-timeout-seconds:120}")
    private int startupTimeoutSeconds;

    // Track running containers: key = "modelName:version", value = ContainerDeploymentInfo
    private final ConcurrentHashMap<String, ContainerDeploymentInfo> runningContainers = new ConcurrentHashMap<>();

    // ========== PUBLIC DEPLOYMENT METHODS ==========

    /**
     * Deploys a model to the remote Docker host.
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @return ContainerDeploymentInfo with endpoint URL and container ID
     */
    public ContainerDeploymentInfo deployModel(String modelName, String version) throws IOException {
        return deployModel(modelName, version, null);
    }

    /**
     * Deploys a model to the remote Docker host on a specific port.
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @param hostPort The host port to expose (null for auto-assign starting from defaultContainerPort)
     * @return ContainerDeploymentInfo with endpoint URL and container ID
     */
    public ContainerDeploymentInfo deployModel(String modelName, String version, Integer hostPort) throws IOException {
        if (!dockerEnabled) {
            throw new IllegalStateException("Docker deployment is not enabled. Set mlflow.docker.enabled=true");
        }
        if (dockerHost == null || dockerHost.isEmpty()) {
            throw new IllegalStateException("Docker host not configured. Set mlflow.docker.host");
        }

        String deploymentKey = modelName + ":" + version;

        // Check if already deployed
        ContainerDeploymentInfo existing = runningContainers.get(deploymentKey);
        if (existing != null && isContainerRunning(existing.getContainerId())) {
            log.info("Model {} v{} already deployed at {}", modelName, version, existing.getInferenceUrl());
            return existing;
        }

        // Find available port (uses preferred if available, otherwise finds next free)
        int port = findAvailablePort(hostPort);
        String containerName = buildContainerName(modelName, version);

        log.info("Deploying MLflow model {} v{} to Docker host {}:{} on port {}",
                modelName, version, dockerHost, dockerPort, port);

        // Remove existing container with same name if exists
        removeContainerIfExists(containerName);

        // Create container
        String containerId = createContainer(modelName, version, containerName, port);
        log.debug("Created container {} with ID {}", containerName, containerId);

        // Start container
        startContainer(containerId);
        log.debug("Started container {}", containerId);

        String inferenceUrl = String.format("http://%s:%d/invocations", dockerHost, port);

        ContainerDeploymentInfo deploymentInfo = new ContainerDeploymentInfo(
                modelName, version, containerId, containerName, port, inferenceUrl, dockerHost
        );

        // Wait for container to be healthy
        boolean ready = waitForContainerReady(inferenceUrl);
        if (!ready) {
            log.error("Container failed to become ready, stopping and removing");
            stopAndRemoveContainer(containerId);
            throw new IOException("MLflow container failed to start within " + startupTimeoutSeconds + " seconds");
        }

        runningContainers.put(deploymentKey, deploymentInfo);
        log.info("Model {} v{} deployed to Docker at {}", modelName, version, inferenceUrl);

        return deploymentInfo;
    }

    /**
     * Deploys a model to a CUSTOM Docker host (not the configured one).
     * 
     * @param modelName The registered model name in MLflow
     * @param version The model version
     * @param customDockerHost The Docker host IP/hostname to deploy to
     * @param customDockerPort The Docker API port on the custom host
     * @param hostPort The host port to expose (null for auto-assign starting from defaultContainerPort)
     * @return ContainerDeploymentInfo with endpoint URL and container ID
     */
    public ContainerDeploymentInfo deployModelToHost(String modelName, String version, 
            String customDockerHost, int customDockerPort, Integer hostPort) throws IOException {
        
        if (customDockerHost == null || customDockerHost.isEmpty()) {
            throw new IllegalArgumentException("Custom Docker host is required");
        }

        String deploymentKey = modelName + ":" + version + "@" + customDockerHost;

        // Check if already deployed to this host
        ContainerDeploymentInfo existing = runningContainers.get(deploymentKey);
        if (existing != null && isContainerRunningOnHost(existing.getContainerId(), customDockerHost, customDockerPort)) {
            log.info("Model {} v{} already deployed at {}", modelName, version, existing.getInferenceUrl());
            return existing;
        }

        // Find available port on custom host
        int port = (hostPort != null) ? hostPort : defaultContainerPort;
        String containerName = buildContainerName(modelName, version);

        log.info("Deploying MLflow model {} v{} to custom Docker host {}:{} on port {}",
                modelName, version, customDockerHost, customDockerPort, port);

        // Remove existing container with same name if exists on custom host
        removeContainerIfExistsOnHost(containerName, customDockerHost, customDockerPort);

        // Create container on custom host
        String containerId = createContainerOnHost(modelName, version, containerName, port, 
                customDockerHost, customDockerPort);
        log.debug("Created container {} with ID {} on host {}", containerName, containerId, customDockerHost);

        // Start container on custom host
        startContainerOnHost(containerId, customDockerHost, customDockerPort);
        log.debug("Started container {} on host {}", containerId, customDockerHost);

        String inferenceUrl = String.format("http://%s:%d/invocations", customDockerHost, port);

        ContainerDeploymentInfo deploymentInfo = new ContainerDeploymentInfo(
                modelName, version, containerId, containerName, port, inferenceUrl, customDockerHost
        );

        // Wait for container to be healthy
        boolean ready = waitForContainerReady(inferenceUrl);
        if (!ready) {
            log.error("Container failed to become ready on custom host, stopping and removing");
            stopAndRemoveContainerOnHost(containerId, customDockerHost, customDockerPort);
            throw new IOException("MLflow container failed to start within " + startupTimeoutSeconds + " seconds");
        }

        runningContainers.put(deploymentKey, deploymentInfo);
        log.info("Model {} v{} deployed to custom Docker host {} at {}", 
                modelName, version, customDockerHost, inferenceUrl);

        return deploymentInfo;
    }

    // ========== CONTAINER LIFECYCLE ==========

    /**
     * Stops and removes a deployed model container (on the configured Docker host).
     */
    public void stopDeployment(String modelName, String version) {
        String deploymentKey = modelName + ":" + version;
        ContainerDeploymentInfo deployment = runningContainers.remove(deploymentKey);

        if (deployment == null) {
            log.warn("No Docker deployment found for {} v{}", modelName, version);
            return;
        }

        log.info("Stopping Docker deployment: {} v{}", modelName, version);
        try {
            stopAndRemoveContainer(deployment.getContainerId());
        } catch (IOException e) {
            log.error("Failed to stop container {}: {}", deployment.getContainerId(), e.getMessage());
        }
    }

    /**
     * Stops and removes a deployed model container on a CUSTOM Docker host.
     */
    public void stopDeploymentOnHost(String modelName, String version, String customDockerHost, int customDockerPort) {
        String deploymentKey = modelName + ":" + version + "@" + customDockerHost;
        ContainerDeploymentInfo deployment = runningContainers.remove(deploymentKey);

        if (deployment == null) {
            log.warn("No Docker deployment found for {} v{} on host {}", modelName, version, customDockerHost);
            return;
        }

        log.info("Stopping Docker deployment: {} v{} on host {}", modelName, version, customDockerHost);
        try {
            stopAndRemoveContainerOnHost(deployment.getContainerId(), customDockerHost, customDockerPort);
        } catch (IOException e) {
            log.error("Failed to stop container {} on host {}: {}", deployment.getContainerId(), customDockerHost, e.getMessage());
        }
    }

    @PreDestroy
    public void stopAllDeployments() {
        log.info("Stopping all Docker deployments...");
        for (ContainerDeploymentInfo deployment : runningContainers.values()) {
            try {
                stopAndRemoveContainer(deployment.getContainerId());
            } catch (IOException e) {
                log.error("Failed to stop container {}: {}", deployment.getContainerId(), e.getMessage());
            }
        }
        runningContainers.clear();
    }

    // ========== QUERY METHODS ==========

    /**
     * Gets info about a deployed container.
     */
    public ContainerDeploymentInfo getDeployment(String modelName, String version) {
        return runningContainers.get(modelName + ":" + version);
    }

    /**
     * Gets info about a deployed container on a CUSTOM Docker host.
     */
    public ContainerDeploymentInfo getDeploymentOnHost(String modelName, String version, String customDockerHost) {
        return runningContainers.get(modelName + ":" + version + "@" + customDockerHost);
    }

    /**
     * Checks if a model is currently deployed.
     */
    public boolean isDeployed(String modelName, String version) {
        ContainerDeploymentInfo info = runningContainers.get(modelName + ":" + version);
        if (info == null) {
            return false;
        }
        try {
            return isContainerRunning(info.getContainerId());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if a model is currently deployed on a CUSTOM Docker host.
     */
    public boolean isDeployedOnHost(String modelName, String version, String customDockerHost, int customDockerPort) {
        ContainerDeploymentInfo info = runningContainers.get(modelName + ":" + version + "@" + customDockerHost);
        if (info == null) {
            return false;
        }
        return isContainerRunningOnHost(info.getContainerId(), customDockerHost, customDockerPort);
    }

    /**
     * Gets all running deployments.
     */
    public Map<String, ContainerDeploymentInfo> getRunningDeployments() {
        return Map.copyOf(runningContainers);
    }

    /**
     * Checks if Docker deployment is enabled and configured.
     */
    public boolean isEnabled() {
        return dockerEnabled && dockerHost != null && !dockerHost.isEmpty();
    }

    // ========== DOCKER API CALLS ==========

    private String createContainer(String modelName, String version, String containerName, int hostPort) throws IOException {
        String url = buildDockerUrl("/containers/create?name=" + containerName);

        String modelUri = String.format("models:/%s/%s", modelName, version);
        String effectiveTrackingUri = getEffectiveTrackingUri();

        // Build container configuration JSON
        String requestBody = String.format("""
            {
              "Image": "%s",
              "Cmd": [
                "mlflow", "models", "serve",
                "-m", "%s",
                "-h", "0.0.0.0",
                "-p", "5001",
                "--env-manager", "local"
              ],
              "Env": [
                "MLFLOW_TRACKING_URI=%s"
              ],
              "ExposedPorts": {
                "5001/tcp": {}
              },
              "HostConfig": {
                "PortBindings": {
                  "5001/tcp": [{"HostPort": "%d"}]
                }
              }
            }
            """, mlflowImage, modelUri, effectiveTrackingUri, hostPort);

        String response = httpPost(url, requestBody);

        // Parse container ID from response: {"Id":"abc123...","Warnings":[]}
        String containerId = parseJsonField(response, "Id");
        if (containerId == null || containerId.isEmpty()) {
            throw new IOException("Failed to create container: " + response);
        }

        return containerId;
    }

    private void startContainer(String containerId) throws IOException {
        String url = buildDockerUrl("/containers/" + containerId + "/start");
        httpPost(url, null);
    }

    private void stopAndRemoveContainer(String containerId) throws IOException {
        // Stop container (with 10 second timeout)
        try {
            String stopUrl = buildDockerUrl("/containers/" + containerId + "/stop?t=10");
            httpPost(stopUrl, null);
        } catch (IOException e) {
            log.debug("Stop failed (may already be stopped): {}", e.getMessage());
        }

        // Remove container
        String removeUrl = buildDockerUrl("/containers/" + containerId + "?force=true");
        httpDelete(removeUrl);
    }

    private void removeContainerIfExists(String containerName) {
        try {
            // Try to get container by name
            String url = buildDockerUrl("/containers/" + containerName + "/json");
            String response = httpGet(url);
            if (response != null) {
                String containerId = parseJsonField(response, "Id");
                if (containerId != null) {
                    log.debug("Removing existing container {}", containerName);
                    stopAndRemoveContainer(containerId);
                }
            }
        } catch (IOException e) {
            // Container doesn't exist, which is fine
            log.debug("No existing container to remove: {}", containerName);
        }
    }

    private boolean isContainerRunning(String containerId) throws IOException {
        String url = buildDockerUrl("/containers/" + containerId + "/json");
        try {
            String response = httpGet(url);
            // Check if State.Running is true
            return response.contains("\"Running\":true");
        } catch (IOException e) {
            return false;
        }
    }

    // ========== CUSTOM HOST CONTAINER OPERATIONS ==========

    private String buildDockerUrlForHost(String path, String host, int port) {
        String protocol = tlsEnabled ? "https" : "http";
        return String.format("%s://%s:%d/%s%s", protocol, host, port, DOCKER_API_VERSION, path);
    }

    private boolean isContainerRunningOnHost(String containerId, String host, int port) {
        String url = buildDockerUrlForHost("/containers/" + containerId + "/json", host, port);
        try {
            String response = httpGet(url);
            return response.contains("\"Running\":true");
        } catch (IOException e) {
            return false;
        }
    }

    private void removeContainerIfExistsOnHost(String containerName, String host, int port) {
        try {
            String url = buildDockerUrlForHost("/containers/" + containerName + "/json", host, port);
            String response = httpGet(url);
            if (response != null) {
                String containerId = parseJsonField(response, "Id");
                if (containerId != null) {
                    log.debug("Removing existing container {} on host {}", containerName, host);
                    stopAndRemoveContainerOnHost(containerId, host, port);
                }
            }
        } catch (IOException e) {
            log.debug("No existing container to remove on host {}: {}", host, containerName);
        }
    }

    private String createContainerOnHost(String modelName, String version, String containerName, 
            int hostPort, String host, int dockerPort) throws IOException {
        String url = buildDockerUrlForHost("/containers/create?name=" + containerName, host, dockerPort);

        String modelUri = String.format("models:/%s/%s", modelName, version);
        String effectiveTrackingUri = getEffectiveTrackingUri();

        String requestBody = String.format("""
            {
              "Image": "%s",
              "Cmd": [
                "mlflow", "models", "serve",
                "-m", "%s",
                "-h", "0.0.0.0",
                "-p", "5001",
                "--env-manager", "local"
              ],
              "Env": [
                "MLFLOW_TRACKING_URI=%s"
              ],
              "ExposedPorts": {
                "5001/tcp": {}
              },
              "HostConfig": {
                "PortBindings": {
                  "5001/tcp": [{"HostPort": "%d"}]
                }
              }
            }
            """, mlflowImage, modelUri, effectiveTrackingUri, hostPort);

        String response = httpPost(url, requestBody);

        String containerId = parseJsonField(response, "Id");
        if (containerId == null || containerId.isEmpty()) {
            throw new IOException("Failed to create container on host " + host + ": " + response);
        }

        return containerId;
    }

    private void startContainerOnHost(String containerId, String host, int port) throws IOException {
        String url = buildDockerUrlForHost("/containers/" + containerId + "/start", host, port);
        httpPost(url, null);
    }

    private void stopAndRemoveContainerOnHost(String containerId, String host, int port) throws IOException {
        try {
            String stopUrl = buildDockerUrlForHost("/containers/" + containerId + "/stop?t=10", host, port);
            httpPost(stopUrl, null);
        } catch (IOException e) {
            log.debug("Stop failed on host {} (may already be stopped): {}", host, e.getMessage());
        }

        String removeUrl = buildDockerUrlForHost("/containers/" + containerId + "?force=true", host, port);
        httpDelete(removeUrl);
    }

    // ========== HTTP UTILITIES ==========

    private String httpGet(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                throw new IOException("Not found: " + urlString);
            }
            if (responseCode >= 400) {
                throw new IOException("HTTP " + responseCode + ": " + readErrorStream(conn));
            }
            return readInputStream(conn);
        } finally {
            conn.disconnect();
        }
    }

    private String httpPost(String urlString, String body) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        try {
            int responseCode = conn.getResponseCode();
            // 204 No Content is success for start/stop
            if (responseCode == 204) {
                return "";
            }
            // 304 Not Modified is also ok (container already started/stopped)
            if (responseCode == 304) {
                return "";
            }
            if (responseCode >= 400) {
                throw new IOException("HTTP " + responseCode + ": " + readErrorStream(conn));
            }
            return readInputStream(conn);
        } finally {
            conn.disconnect();
        }
    }

    private void httpDelete(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                // Already deleted
                return;
            }
            if (responseCode >= 400) {
                throw new IOException("HTTP " + responseCode + ": " + readErrorStream(conn));
            }
        } finally {
            conn.disconnect();
        }
    }

    private String readInputStream(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String readErrorStream(HttpURLConnection conn) {
        try {
            if (conn.getErrorStream() != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
            return conn.getResponseMessage();
        } catch (IOException e) {
            return "Unknown error";
        }
    }

    // ========== UTILITY METHODS ==========

    private String buildDockerUrl(String path) {
        String protocol = tlsEnabled ? "https" : "http";
        return String.format("%s://%s:%d/%s%s", protocol, dockerHost, dockerPort, DOCKER_API_VERSION, path);
    }

    private String buildContainerName(String modelName, String version) {
        // Sanitize for Docker container name (alphanumeric, underscores, hyphens)
        String sanitized = (modelName + "-v" + version)
                .toLowerCase()
                .replaceAll("[^a-z0-9_-]", "-")
                .replaceAll("-+", "-");
        return "mlflow-" + sanitized;
    }

    private String getEffectiveTrackingUri() {
        if (trackingUri != null && !trackingUri.isEmpty()) {
            return trackingUri;
        }
        return String.format("http://%s:%d", mlflowHost, mlflowPort);
    }

    /**
     * Finds an available port, optionally starting from a preferred port.
     * If the preferred port is in use, finds the next available one.
     * 
     * @param preferredPort The preferred port to use, or null to auto-assign
     * @return An available port
     */
    public int findAvailablePort(Integer preferredPort) {
        int startPort = preferredPort != null ? preferredPort : defaultContainerPort;
        int maxAttempts = 100;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int candidatePort = startPort + attempt;
            
            // Check if used by our tracked deployments
            boolean usedByUs = runningContainers.values().stream()
                    .anyMatch(info -> info.getHostPort() == candidatePort);
            
            if (!usedByUs && !isPortInUse(candidatePort)) {
                if (preferredPort != null && candidatePort != preferredPort) {
                    log.info("Preferred port {} is in use, using port {} instead", preferredPort, candidatePort);
                }
                return candidatePort;
            }
        }
        
        throw new IllegalStateException("Could not find available port after " + maxAttempts + " attempts starting from " + startPort);
    }

    /**
     * Checks if a port is already in use on the Docker host.
     * This attempts to connect to the port to see if something is listening.
     * 
     * @param port The port to check
     * @return true if the port is in use
     */
    public boolean isPortInUse(int port) {
        try {
            // Try to connect to the port on the Docker host
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(dockerHost, port), 500);
            socket.close();
            return true; // Connection succeeded, port is in use
        } catch (java.net.SocketTimeoutException e) {
            return false; // Timeout = nothing listening
        } catch (java.io.IOException e) {
            return false; // Connection refused = nothing listening
        }
    }

    private boolean waitForContainerReady(String inferenceUrl) {
        String healthUrl = inferenceUrl.replace("/invocations", "/ping");
        long startTime = System.currentTimeMillis();
        long timeoutMs = startupTimeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                if (checkHealth(healthUrl)) {
                    return true;
                }
            } catch (Exception e) {
                // Server not ready yet
            }
            try {
                Thread.sleep(2000); // Check every 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean checkHealth(String healthUrl) {
        try {
            URL url = URI.create(healthUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            try {
                return conn.getResponseCode() == 200;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Simple JSON field parser (avoids Jackson dependency for this service).
     * Extracts a string value for a given key from a JSON object.
     */
    private String parseJsonField(String json, String field) {
        String searchPattern = "\"" + field + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchPattern.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return json.substring(startIndex, endIndex);
    }

    // ========== DEPLOYMENT INFO CLASS ==========

    /**
     * Information about a Docker container deployment.
     */
    public static class ContainerDeploymentInfo {
        private final String modelName;
        private final String version;
        private final String containerId;
        private final String containerName;
        private final int hostPort;
        private final String inferenceUrl;
        private final String dockerHost;
        private final long startTime;

        public ContainerDeploymentInfo(String modelName, String version, String containerId,
                String containerName, int hostPort, String inferenceUrl, String dockerHost) {
            this.modelName = modelName;
            this.version = version;
            this.containerId = containerId;
            this.containerName = containerName;
            this.hostPort = hostPort;
            this.inferenceUrl = inferenceUrl;
            this.dockerHost = dockerHost;
            this.startTime = System.currentTimeMillis();
        }

        public String getModelName() { return modelName; }
        public String getVersion() { return version; }
        public String getContainerId() { return containerId; }
        public String getContainerName() { return containerName; }
        public int getHostPort() { return hostPort; }
        public String getInferenceUrl() { return inferenceUrl; }
        public String getDockerHost() { return dockerHost; }
        public long getStartTime() { return startTime; }

        public String getDeploymentKey() {
            return modelName + ":" + version;
        }

        public long getUptimeMs() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
