package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Example usage of MLflow Platform integration
 * 
 * This class demonstrates how to:
 * 1. Create the MLflow platform specification
 * 2. Import models from MLflow registry
 * 3. Validate model existence
 * 4. List registered models
 */
@Component
public class MLflowIntegrationExample {

    private static final Logger log = LoggerFactory.getLogger(MLflowIntegrationExample.class);
    
    private final MLflowPlatformService mlflowService;
    
    public MLflowIntegrationExample(MLflowPlatformService mlflowService) {
        this.mlflowService = mlflowService;
    }
    
    /**
     * Example: Create MLflow platform specification
     */
    public void createPlatformSpecificationExample() {
        log.info("=== MLflow Platform Specification Example ===");
        
        AiModelSpecification platformSpec = mlflowService.getOrCreatePlatformSpecification();
        
        log.info("Platform Specification Created:");
        log.info("  Name: {}", platformSpec.getName());
        log.info("  Version: {}", platformSpec.getVersion());
        log.info("  Description: {}", platformSpec.getDescription());
        log.info("  Characteristics: {}", platformSpec.getSpecCharacteristic().size());
    }
    
    /**
     * Example: Import a specific model from MLflow registry
     */
    public void importModelExample(String modelName, String version) {
        log.info("=== Import MLflow Model Example ===");
        log.info("Model: {}, Version: {}", modelName, version);
        
        try {
            String baseUrl = "http://localhost:13082";
            AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(modelName, version, baseUrl);
            
            log.info("Model imported successfully:");
            log.info("  Name: {}", aiModel.getName());
            log.info("  Description: {}", aiModel.getDescription());
            log.info("  Characteristics: {}", aiModel.getServiceCharacteristic().size());
            
            // Log some key characteristics
            aiModel.getServiceCharacteristic().forEach(characteristic -> {
                log.info("    {} = {}", characteristic.getName(), characteristic.getValue());
            });
            
        } catch (IOException e) {
            log.error("Failed to import model: {}", e.getMessage());
        }
    }
    
    /**
     * Example: List all registered models in MLflow
     */
    public void listModelsExample() {
        log.info("=== List MLflow Models Example ===");
        
        try {
            List<String> models = mlflowService.listRegisteredModels();
            
            log.info("Found {} registered models:", models.size());
            models.forEach(modelName -> log.info("  - {}", modelName));
            
        } catch (IOException e) {
            log.error("Failed to list models: {}", e.getMessage());
        }
    }
    
    /**
     * Example: Validate model existence
     */
    public void validateModelExample(String modelName) {
        log.info("=== Validate MLflow Model Example ===");
        log.info("Validating model: {}", modelName);
        
        boolean exists = mlflowService.validateModelExists(modelName);
        
        if (exists) {
            log.info("✓ Model '{}' exists in MLflow registry", modelName);
        } else {
            log.warn("✗ Model '{}' not found in MLflow registry", modelName);
        }
    }
    
    /**
     * Example: Import latest version of a model
     */
    public void importLatestVersionExample(String modelName) {
        log.info("=== Import Latest Model Version Example ===");
        log.info("Model: {} (latest version)", modelName);
        
        try {
            String baseUrl = "http://localhost:13082";
            // Pass null for version to get latest
            AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(modelName, null, baseUrl);
            
            log.info("Latest version imported successfully:");
            log.info("  Name: {}", aiModel.getName());
            log.info("  Description: {}", aiModel.getDescription());
            
        } catch (IOException e) {
            log.error("Failed to import latest version: {}", e.getMessage());
        }
    }
    
    /**
     * Example: Complete workflow - list, validate, and import
     */
    public void completeWorkflowExample() {
        log.info("=== Complete MLflow Integration Workflow ===");
        
        // Step 1: Create platform specification
        log.info("Step 1: Creating platform specification...");
        createPlatformSpecificationExample();
        
        // Step 2: List all models
        log.info("\nStep 2: Listing all models...");
        try {
            List<String> models = mlflowService.listRegisteredModels();
            
            if (models.isEmpty()) {
                log.warn("No models found in MLflow registry");
                return;
            }
            
            // Step 3: Import first model
            String firstModel = models.get(0);
            log.info("\nStep 3: Importing first model: {}", firstModel);
            importLatestVersionExample(firstModel);
            
            // Step 4: Validate the model
            log.info("\nStep 4: Validating model...");
            validateModelExample(firstModel);
            
        } catch (IOException e) {
            log.error("Workflow failed: {}", e.getMessage());
        }
        
        log.info("\n=== Workflow Complete ===");
    }
    
    /**
     * Example showing TMF 915 field mappings
     */
    public void showFieldMappingsExample(String modelName) {
        log.info("=== TMF 915 Field Mappings Example ===");
        
        try {
            AiModelCreate aiModel = mlflowService.mlflowModelToAiModelCreate(modelName, null, "http://localhost:13082");
            
            log.info("TMF 915 Standard Fields:");
            log.info("  name: {}", aiModel.getName());
            log.info("  description: {}", aiModel.getDescription());
            log.info("  aiModelSpecification: {} v{}", 
                aiModel.getAiModelSpecification().getName(),
                aiModel.getAiModelSpecification().getVersion());
            
            log.info("\nMLflow-Specific Characteristics:");
            aiModel.getServiceCharacteristic().forEach(characteristic -> {
                String name = characteristic.getName();
                Object value = characteristic.getValue();
                
                switch (name) {
                    case "modelName":
                        log.info("  modelName: {} (MLflow registered model name)", value);
                        break;
                    case "modelVersion":
                        log.info("  modelVersion: {} (MLflow model version)", value);
                        break;
                    case "currentStage":
                        log.info("  currentStage: {} (None/Staging/Production/Archived)", value);
                        break;
                    case "runId":
                        log.info("  runId: {} (MLflow experiment run ID)", value);
                        break;
                    case "modelUri":
                        log.info("  modelUri: {} (Artifact storage location)", value);
                        break;
                    case "modelDataSheet":
                        log.info("  modelDataSheet: {} (TMF 915 field - MLflow model page)", value);
                        break;
                    case "deploymentRecord":
                        log.info("  deploymentRecord: {} (TMF 915 field - Serving endpoint)", value);
                        break;
                }
            });
            
        } catch (IOException e) {
            log.error("Failed to show field mappings: {}", e.getMessage());
        }
    }
}
