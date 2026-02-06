package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.concurrent.TimeUnit;

/**
 * Service for deploying MLflow models to local or remote MLflow servers.
 * 
 * Supports two deployment modes:
 * - LOCAL: Uses `mlflow models serve` for a local inference server
 * - REMOTE: Deploys to a remote MLflow server with model serving capabilities
 * 
 * For remote MLflow servers, uses the MLflow Deployments REST API.
 */
@Service
public class MlflowDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(MlflowDeploymentService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mlflow.host:127.0.0.1}")
    private String mlflowHost;

    @Value("${mlflow.port:5000}")
    private int mlflowPort;

    @Value("${mlflow.tracking-uri:}")
    private String trackingUri;

    @Value("${mlflow.deployment.host:0.0.0.0}")
    private String localDeploymentHost;

    @Value("${mlflow.deployment.startup-timeout-seconds:60}")
    private int startupTimeoutSeconds;

    @Value("${mlflow.deployment.health-check-interval-ms:1000}")
    private int healthCheckIntervalMs;

    // Track running deployments: key = "modelName:version", value = DeploymentInfo
    private final Map<String, DeploymentInfo> runningDeployments = new ConcurrentHashMap<>();

    /**
     * Deployment target types.
     */
    public enum DeploymentTarget {
        LOCAL,   // Local mlflow models serve
        REMOTE   // Remote MLflow server with serving capability
    }

    // ========== PUBLIC DEPLOYMENT METHODS ==========

    /**
     * Deploys a model locally on an auto-assigned port.
     * 
     * @param modelName The registered model name
     * @param version The model version
     * @return DeploymentInfo with endpoint URL
     */
    public DeploymentInfo deployModel(String modelName, String version) throws IOException {
        return deployModelLocally(modelName, version, null);
    }

    /**
     * Deploys a model locally on a specific port.
     * 
     * @param modelName The registered model name
     * @param version The model version
     * @param port The port to serve on
     * @return DeploymentInfo with endpoint URL
     */
    public DeploymentInfo deployModel(String modelName, String version, int port) throws IOException {
        return deployModelLocally(modelName, version, port);
    }

    /**
     * Deploys a model to a remote MLflow server.
     * 
     * @param modelName The registered model name
     * @param version The model version
     * @param remoteUrl The remote MLflow server URL (e.g., https://mlflow.example.com)
     * @param authToken Optional authentication token
     * @return DeploymentInfo with endpoint URL
     */
    public DeploymentInfo deployToRemote(String modelName, String version, 
            String remoteUrl, String authToken) throws IOException {
        return deployToRemoteMlflow(modelName, version, remoteUrl, authToken, null);
    }

    /**
     * Deploys a model to a remote MLflow server with custom endpoint name.
     * 
     * @param modelName The registered model name
     * @param version The model version
     * @param remoteUrl The remote MLflow server URL
     * @param authToken Optional authentication token
     * @param endpointName Custom endpoint name (defaults to modelName-v{version})
     * @return DeploymentInfo with endpoint URL
     */
    public DeploymentInfo deployToRemote(String modelName, String version, 
            String remoteUrl, String authToken, String endpointName) throws IOException {
        return deployToRemoteMlflow(modelName, version, remoteUrl, authToken, endpointName);
    }

    // ========== LOCAL DEPLOYMENT ==========

    /**
     * Deploys locally using mlflow models serve.
     */
    private DeploymentInfo deployModelLocally(String modelName, String version, Integer port) throws IOException {
        String deploymentKey = modelName + ":" + version;
        
        // Check if already deployed
        DeploymentInfo existing = runningDeployments.get(deploymentKey);
        if (existing != null && existing.isRunning()) {
            log.info("Model {} v{} already deployed locally at {}", modelName, version, existing.getInferenceUrl());
            return existing;
        }

        int actualPort = port != null ? port : findAvailablePort();
        log.info("Deploying MLflow model {} v{} locally on port {}", modelName, version, actualPort);

        String modelUri = String.format("models:/%s/%s", modelName, version);
        ProcessBuilder processBuilder = buildLocalServeCommand(modelUri, actualPort);
        Process process = processBuilder.start();

        String inferenceUrl = String.format("http://%s:%d/invocations", 
            "0.0.0.0".equals(localDeploymentHost) ? "localhost" : localDeploymentHost, actualPort);
        
        DeploymentInfo deploymentInfo = new DeploymentInfo(
            modelName, version, actualPort, inferenceUrl, process, deploymentKey,
            DeploymentTarget.LOCAL, null, null
        );

        startLogReaders(process, modelName, version);

        boolean ready = waitForServerReady(inferenceUrl, process);
        if (!ready) {
            process.destroyForcibly();
            throw new IOException("MLflow server failed to start within " + startupTimeoutSeconds + " seconds");
        }

        runningDeployments.put(deploymentKey, deploymentInfo);
        log.info("Model {} v{} deployed locally at {}", modelName, version, inferenceUrl);
        
        return deploymentInfo;
    }

    private ProcessBuilder buildLocalServeCommand(String modelUri, int port) {
        ProcessBuilder pb = new ProcessBuilder(
            "mlflow", "models", "serve",
            "-m", modelUri,
            "-h", localDeploymentHost,
            "-p", String.valueOf(port),
            "--no-conda"
        );

        pb.environment().put("MLFLOW_TRACKING_URI", getTrackingUri());
        pb.redirectErrorStream(false);
        
        return pb;
    }

    // ========== REMOTE MLFLOW DEPLOYMENT ==========

    /**
     * Deploys to a remote MLflow server with serving capabilities.
     * 
     * Uses MLflow's REST API to create a deployment endpoint.
     * Falls back to CLI if REST API is not available.
     */
    private DeploymentInfo deployToRemoteMlflow(String modelName, String version, 
            String remoteUrl, String authToken, String endpointName) throws IOException {
        
        if (remoteUrl == null || remoteUrl.isEmpty()) {
            throw new IllegalArgumentException("remoteUrl is required for remote deployment");
        }

        String deploymentKey = modelName + ":" + version;
        String actualEndpointName = endpointName != null ? endpointName : modelName + "-v" + version;
        
        // Check if already deployed
        DeploymentInfo existing = runningDeployments.get(deploymentKey);
        if (existing != null && existing.getTarget() == DeploymentTarget.REMOTE) {
            log.info("Model {} v{} already deployed remotely at {}", modelName, version, existing.getInferenceUrl());
            return existing;
        }

        log.info("Deploying {} v{} to remote MLflow: {}", modelName, version, remoteUrl);

        String inferenceUrl;
        
        // Try REST API first
        try {
            inferenceUrl = deployViaRestApi(modelName, version, remoteUrl, authToken, actualEndpointName);
        } catch (Exception e) {
            log.warn("REST API deployment failed, trying CLI: {}", e.getMessage());
            // Fallback to CLI-based deployment
            inferenceUrl = deployViaCli(modelName, version, remoteUrl, actualEndpointName);
        }

        DeploymentInfo info = new DeploymentInfo(
            modelName, version, -1, inferenceUrl, null, deploymentKey,
            DeploymentTarget.REMOTE, remoteUrl, actualEndpointName
        );

        runningDeployments.put(deploymentKey, info);
        log.info("Model {} v{} deployed to remote MLflow at: {}", modelName, version, inferenceUrl);
        
        return info;
    }

    /**
     * Deploys via MLflow REST API.
     */
    private String deployViaRestApi(String modelName, String version, String remoteUrl, 
            String authToken, String endpointName) throws IOException {
        
        // Try the deployments API endpoint
        String deployEndpoint = remoteUrl.replaceAll("/$", "") + "/api/2.0/mlflow/deployments/create";
        
        String requestBody = String.format("""
            {
                "name": "%s",
                "model_uri": "models:/%s/%s"
            }
            """, 
            endpointName, modelName, version
        );

        String response = httpPost(deployEndpoint, requestBody, authToken);
        JsonNode json = objectMapper.readTree(response);

        // Extract endpoint URL from response
        if (json.has("endpoint_url")) {
            return json.get("endpoint_url").asText();
        }
        
        // Construct default inference URL
        return remoteUrl.replaceAll("/$", "") + "/invocations/" + endpointName;
    }

    /**
     * Deploys via MLflow CLI (mlflow deployments create).
     */
    private String deployViaCli(String modelName, String version, String remoteUrl, 
            String endpointName) throws IOException {
        
        String modelUri = String.format("models:/%s/%s", modelName, version);

        ProcessBuilder pb = new ProcessBuilder(
            "mlflow", "deployments", "create",
            "-t", remoteUrl,
            "-m", modelUri,
            "--name", endpointName
        );

        pb.environment().put("MLFLOW_TRACKING_URI", getTrackingUri());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("[mlflow deployments] {}", line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Deployment interrupted", e);
        }

        if (exitCode != 0) {
            throw new IOException("MLflow CLI deployment failed: " + output);
        }

        // Try to get endpoint info
        return getDeploymentEndpoint(remoteUrl, endpointName);
    }

    private String getDeploymentEndpoint(String remoteUrl, String endpointName) {
        // Try to get endpoint info from the deployment
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "mlflow", "deployments", "get-endpoint",
                "-t", remoteUrl,
                "--name", endpointName
            );
            pb.environment().put("MLFLOW_TRACKING_URI", getTrackingUri());
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && line.contains("http")) {
                    return line.trim();
                }
            }
        } catch (Exception e) {
            log.debug("Could not get endpoint info: {}", e.getMessage());
        }
        
        // Fallback: construct URL
        return remoteUrl.replaceAll("/$", "") + "/invocations/" + endpointName;
    }

    // ========== STOP DEPLOYMENT ==========

    /**
     * Stops a running deployment (local or remote).
     * 
     * @param modelName The model name
     * @param version The model version
     */
    public void stopDeployment(String modelName, String version) throws IOException {
        String deploymentKey = modelName + ":" + version;
        DeploymentInfo deployment = runningDeployments.remove(deploymentKey);
        
        if (deployment == null) {
            log.warn("No deployment found for {} v{}", modelName, version);
            return;
        }

        log.info("Stopping deployment: {} v{} (target: {})", modelName, version, deployment.getTarget());

        if (deployment.getTarget() == DeploymentTarget.LOCAL) {
            deployment.stop();
        } else if (deployment.getTarget() == DeploymentTarget.REMOTE) {
            stopRemoteDeployment(deployment);
        }
    }

    /**
     * Stops a remote deployment.
     */
    private void stopRemoteDeployment(DeploymentInfo deployment) throws IOException {
        if (deployment.getEndpointName() == null || deployment.getRemoteUrl() == null) {
            return;
        }

        // Try REST API first
        try {
            String deleteEndpoint = deployment.getRemoteUrl().replaceAll("/$", "") + 
                "/api/2.0/mlflow/deployments/" + deployment.getEndpointName();
            httpDelete(deleteEndpoint, null);
            log.info("Deleted remote deployment: {}", deployment.getEndpointName());
            return;
        } catch (Exception e) {
            log.debug("REST delete failed, trying CLI: {}", e.getMessage());
        }

        // Fallback to CLI
        ProcessBuilder pb = new ProcessBuilder(
            "mlflow", "deployments", "delete",
            "-t", deployment.getRemoteUrl(),
            "--name", deployment.getEndpointName()
        );

        pb.environment().put("MLFLOW_TRACKING_URI", getTrackingUri());
        Process process = pb.start();
        
        try {
            process.waitFor(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========== QUERY METHODS ==========

    /**
     * Gets info about a running deployment.
     */
    public DeploymentInfo getDeployment(String modelName, String version) {
        return runningDeployments.get(modelName + ":" + version);
    }

    /**
     * Checks if a model is currently deployed.
     */
    public boolean isDeployed(String modelName, String version) {
        DeploymentInfo info = runningDeployments.get(modelName + ":" + version);
        if (info == null) {
            return false;
        }
        return info.getTarget() == DeploymentTarget.REMOTE || info.isRunning();
    }

    /**
     * Gets all running deployments.
     */
    public Map<String, DeploymentInfo> getRunningDeployments() {
        return Map.copyOf(runningDeployments);
    }

    // ========== LIFECYCLE ==========

    @PreDestroy
    public void stopAllDeployments() {
        log.info("Stopping all MLflow deployments...");
        for (DeploymentInfo deployment : runningDeployments.values()) {
            if (deployment.getTarget() == DeploymentTarget.LOCAL) {
                deployment.stop();
            }
            // Note: Remote deployments are not stopped on shutdown
        }
        runningDeployments.clear();
    }

    // ========== UTILITY METHODS ==========

    private String getTrackingUri() {
        return trackingUri.isEmpty() 
            ? String.format("http://%s:%d", mlflowHost, mlflowPort)
            : trackingUri;
    }

    private boolean waitForServerReady(String inferenceUrl, Process process) {
        String healthUrl = inferenceUrl.replace("/invocations", "/ping");
        long startTime = System.currentTimeMillis();
        long timeoutMs = startupTimeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (!process.isAlive()) {
                return false;
            }
            try {
                if (checkHealth(healthUrl)) {
                    return true;
                }
            } catch (Exception e) {
                // Server not ready
            }
            try {
                Thread.sleep(healthCheckIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean checkHealth(String healthUrl) throws IOException {
        URL url = URI.create(healthUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(1000);
        try {
            return conn.getResponseCode() == 200;
        } finally {
            conn.disconnect();
        }
    }

    private String httpPost(String urlString, String body, String authToken) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new IOException("HTTP " + responseCode + ": " + conn.getResponseMessage());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

    private void httpDelete(String urlString, String authToken) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new IOException("HTTP " + responseCode + ": " + conn.getResponseMessage());
        }
        conn.disconnect();
    }

    private void startLogReaders(Process process, String modelName, String version) {
        String prefix = String.format("[mlflow-serve %s:%s]", modelName, version);
        
        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("{} {}", prefix, line);
                }
            } catch (IOException e) { /* Process ended */ }
        }, "mlflow-stdout-" + modelName);
        stdoutReader.setDaemon(true);
        stdoutReader.start();

        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("{} {}", prefix, line);
                }
            } catch (IOException e) { /* Process ended */ }
        }, "mlflow-stderr-" + modelName);
        stderrReader.setDaemon(true);
        stderrReader.start();
    }

    private int findAvailablePort() throws IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    // ========== DEPLOYMENT INFO CLASS ==========

    /**
     * Information about a deployment.
     */
    public static class DeploymentInfo {
        private final String modelName;
        private final String version;
        private final int port;
        private final String inferenceUrl;
        private final Process process;
        private final String deploymentKey;
        private final DeploymentTarget target;
        private final String remoteUrl;
        private final String endpointName;
        private final long startTime;

        public DeploymentInfo(String modelName, String version, int port, String inferenceUrl,
                Process process, String deploymentKey, DeploymentTarget target, 
                String remoteUrl, String endpointName) {
            this.modelName = modelName;
            this.version = version;
            this.port = port;
            this.inferenceUrl = inferenceUrl;
            this.process = process;
            this.deploymentKey = deploymentKey;
            this.target = target;
            this.remoteUrl = remoteUrl;
            this.endpointName = endpointName;
            this.startTime = System.currentTimeMillis();
        }

        public String getModelName() { return modelName; }
        public String getVersion() { return version; }
        public int getPort() { return port; }
        public String getInferenceUrl() { return inferenceUrl; }
        public String getDeploymentKey() { return deploymentKey; }
        public DeploymentTarget getTarget() { return target; }
        public String getRemoteUrl() { return remoteUrl; }
        public String getEndpointName() { return endpointName; }
        public long getStartTime() { return startTime; }
        
        public boolean isRunning() {
            return process != null && process.isAlive();
        }

        public void stop() {
            if (process != null && process.isAlive()) {
                process.destroy();
                try {
                    if (!process.waitFor(10, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                    Thread.currentThread().interrupt();
                }
            }
        }

        public long getUptimeMs() {
            return System.currentTimeMillis() - startTime;
        }

        public boolean isLocal() {
            return target == DeploymentTarget.LOCAL;
        }

        public boolean isRemote() {
            return target == DeploymentTarget.REMOTE;
        }
    }
}
