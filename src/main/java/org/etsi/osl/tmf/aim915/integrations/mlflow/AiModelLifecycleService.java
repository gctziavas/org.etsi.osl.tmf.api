package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service for managing AiModel lifecycle with Docker container synchronization.
 * 
 * This service ensures bidirectional synchronization between:
 * - AiModel state and Docker container status
 * 
 * Lifecycle States:
 * - RESERVED: Model created, deployment pending
 * - ACTIVE: Container running and serving predictions
 * - TERMINATED: Container stopped
 * 
 * Synchronization:
 * 1. When state is set to TERMINATED → stop the container
 * 2. When container stops externally → update state to TERMINATED
 */
@Service
public class AiModelLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(AiModelLifecycleService.class);

    private final AiModelRepositoryService aiModelRepository;
    private final MlflowDeploymentService deploymentService;

    public AiModelLifecycleService(
            AiModelRepositoryService aiModelRepository,
            MlflowDeploymentService deploymentService) {
        this.aiModelRepository = aiModelRepository;
        this.deploymentService = deploymentService;
        log.info("AiModelLifecycleService initialized");
    }

    /**
     * Updates the state of an AiModel with container lifecycle synchronization.
     * 
     * If the new state is TERMINATED and the model has deployment info,
     * this method will also stop the associated Docker container.
     * 
     * @param uuid The UUID of the AiModel
     * @param newState The new state to set
     * @return The updated AiModel
     * @throws IOException if stopping the container fails
     */
    public AiModel updateStateWithContainerSync(String uuid, ServiceStateType newState) throws IOException {
        log.info("Updating AiModel {} state to {} with container sync", uuid, newState);
        
        AiModel model = aiModelRepository.findAiModelByUuid(uuid);
        if (model == null) {
            throw new IllegalArgumentException("No AI Model with UUID: " + uuid);
        }

        ServiceStateType currentState = model.getState();
        
        // If transitioning to TERMINATED, stop the container first
        if (newState == ServiceStateType.TERMINATED && currentState == ServiceStateType.ACTIVE) {
            stopContainerForModel(model);
        }
        
        // Update the state in the database
        return aiModelRepository.updateAiModelState(uuid, newState);
    }

    /**
     * Terminates an AiModel and stops its associated container.
     * 
     * Convenience method that sets state to TERMINATED and stops the container.
     * 
     * @param uuid The UUID of the AiModel
     * @return The terminated AiModel
     * @throws IOException if stopping the container fails
     */
    public AiModel terminateModel(String uuid) throws IOException {
        return updateStateWithContainerSync(uuid, ServiceStateType.TERMINATED);
    }

    /**
     * Scheduled task to synchronize container status with AiModel state.
     * 
     * Checks all ACTIVE AiModels and updates their state to TERMINATED
     * if their associated container is no longer running.
     * 
     * Runs every 30 seconds.
     */
    @Scheduled(fixedRate = 30000)
    public void syncContainerStatus() {
        log.debug("Running container status synchronization check");
        
        List<AiModel> activeModels = aiModelRepository.findByState(ServiceStateType.ACTIVE);
        
        for (AiModel model : activeModels) {
            try {
                checkAndUpdateModelStatus(model);
            } catch (Exception e) {
                log.warn("Failed to check status for model {}: {}", model.getUuid(), e.getMessage());
            }
        }
    }

    /**
     * Checks if the container for an ACTIVE model is still running.
     * If not, updates the model state to TERMINATED.
     */
    private void checkAndUpdateModelStatus(AiModel model) {
        String modelName = getCharacteristicValue(model, "mlflowModelName");
        String version = getCharacteristicValue(model, "mlflowModelVersion");
        
        if (modelName == null || version == null) {
            // Not an MLflow-deployed model, skip
            return;
        }
        
        // Check if container is still running
        boolean isRunning = deploymentService.isDeployed(modelName, version);
        
        if (!isRunning) {
            log.info("Container for model {} v{} is no longer running, updating state to TERMINATED", 
                    modelName, version);
            try {
                aiModelRepository.updateAiModelState(model.getUuid(), ServiceStateType.TERMINATED);
            } catch (Exception e) {
                log.error("Failed to update model {} state to TERMINATED: {}", model.getUuid(), e.getMessage());
            }
        }
    }

    /**
     * Stops the Docker container associated with an AiModel.
     */
    private void stopContainerForModel(AiModel model) throws IOException {
        String modelName = getCharacteristicValue(model, "mlflowModelName");
        String version = getCharacteristicValue(model, "mlflowModelVersion");
        String dockerHost = getCharacteristicValue(model, "dockerHost");
        
        if (modelName == null || version == null) {
            log.warn("Cannot stop container for model {} - missing mlflowModelName or mlflowModelVersion", 
                    model.getUuid());
            return;
        }
        
        log.info("Stopping container for model {} v{}", modelName, version);
        
        // Check if this is a custom Docker host deployment
        if (dockerHost != null && !dockerHost.isEmpty()) {
            // Try to get the Docker port from characteristics
            String portStr = getCharacteristicValue(model, "dockerPort");
            int dockerPort = 2375; // Default Docker API port
            if (portStr != null) {
                try {
                    dockerPort = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid dockerPort value: {}, using default 2375", portStr);
                }
            }
            deploymentService.stopDeploymentOnHost(modelName, version, dockerHost, dockerPort);
        } else {
            deploymentService.stopDeployment(modelName, version);
        }
        
        log.info("Container stopped for model {} v{}", modelName, version);
    }

    /**
     * Gets a characteristic value from an AiModel by name.
     * 
     * @param model The AiModel
     * @param name The characteristic name
     * @return The value as a string, or null if not found
     */
    private String getCharacteristicValue(AiModel model, String name) {
        Characteristic characteristic = model.getServiceCharacteristicByName(name);
        if (characteristic != null && characteristic.getValue() != null) {
            return characteristic.getValue().getValue();
        }
        return null;
    }

    /**
     * Manually triggers a status check for a specific AiModel.
     * 
     * @param uuid The UUID of the AiModel to check
     * @return true if the model is still running, false if terminated
     */
    public boolean checkModelStatus(String uuid) {
        AiModel model = aiModelRepository.findAiModelByUuid(uuid);
        if (model == null) {
            throw new IllegalArgumentException("No AI Model with UUID: " + uuid);
        }
        
        if (model.getState() != ServiceStateType.ACTIVE) {
            return false;
        }
        
        String modelName = getCharacteristicValue(model, "mlflowModelName");
        String version = getCharacteristicValue(model, "mlflowModelVersion");
        
        if (modelName == null || version == null) {
            return true; // Non-MLflow model, assume running
        }
        
        boolean isRunning = deploymentService.isDeployed(modelName, version);
        
        if (!isRunning) {
            log.info("Model {} is no longer running, updating state to TERMINATED", uuid);
            aiModelRepository.updateAiModelState(uuid, ServiceStateType.TERMINATED);
        }
        
        return isRunning;
    }
}
