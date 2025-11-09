package org.etsi.osl.tmf.aim915.integrations.huggingface;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Example demonstrating the differences between the two Hugging Face integration approaches
 * 
 * This class shows:
 * 1. Original approach (HuggingFaceService) - One specification per model
 * 2. New approach (HuggingFacePlatformService) - One specification for the platform
 * 
 * To run this example, activate the 'huggingface-demo' profile:
 * --spring.profiles.active=huggingface-demo
 */
@Component
@Profile("huggingface-demo")
public class HuggingFaceIntegrationExample implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(HuggingFaceIntegrationExample.class);
    private static final String BASE_URL = "http://localhost:8080";
    
    @Autowired(required = false)
    private HuggingFaceService originalService;
    
    @Autowired(required = false)
    private HuggingFacePlatformService platformService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=".repeat(80));
        log.info("Hugging Face Integration Approaches - Comparison Demo");
        log.info("=".repeat(80));
        
        String[] testModels = {
            "gpt2",
            "distilbert-base-uncased",
            "facebook/bart-large-cnn"
        };
        
        // Demonstrate both approaches
        if (originalService != null) {
            demonstrateOriginalApproach(testModels);
        } else {
            log.warn("HuggingFaceService not available - skipping original approach demo");
        }
        
        log.info("");
        log.info("-".repeat(80));
        log.info("");
        
        if (platformService != null) {
            demonstrateNewApproach(testModels);
        } else {
            log.warn("HuggingFacePlatformService not available - skipping new approach demo");
        }
        
        log.info("");
        log.info("=".repeat(80));
        log.info("Demo completed - Check the logs above to see the differences");
        log.info("=".repeat(80));
    }
    
    /**
     * Demonstrates the original approach: One specification per model
     */
    private void demonstrateOriginalApproach(String[] models) {
        log.info("ORIGINAL APPROACH: One Specification Per Model");
        log.info("-".repeat(80));
        
        for (String modelId : models) {
            try {
                log.info("\nProcessing model: {}", modelId);
                
                // Step 1: Create a specification for this specific model
                log.info("  → Creating specification for model...");
                AiModelSpecificationCreate specCreate = originalService
                    .huggingFaceToAiModelSpecificationCreate(modelId);
                log.info("     Specification Name: {}", specCreate.getName());
                log.info("     Description: {}", specCreate.getDescription());
                
                // Step 2: Create the AI model
                log.info("  → Creating AiModel linked to this specification...");
                AiModelCreate modelCreate = originalService
                    .huggingFaceToAiModelCreate(modelId, BASE_URL);
                log.info("     Model Name: {}", modelCreate.getName());
                log.info("     Specification: {}", 
                    modelCreate.getAiModelSpecification() != null 
                        ? modelCreate.getAiModelSpecification().getName() 
                        : "null");
                
                log.info("  ✓ Created 1 specification for 1 model");
                
            } catch (Exception e) {
                log.error("  ✗ Error processing model {}: {}", modelId, e.getMessage());
            }
        }
        
        log.info("\n" + "-".repeat(80));
        log.info("SUMMARY (Original Approach):");
        log.info("  - Specifications created: {} (one per model)", models.length);
        log.info("  - Each model has its own specification");
        log.info("  - Specification names match model IDs");
        log.info("-".repeat(80));
    }
    
    /**
     * Demonstrates the new approach: One platform specification for all models
     */
    private void demonstrateNewApproach(String[] models) {
        log.info("NEW APPROACH: One Platform Specification For All Models");
        log.info("-".repeat(80));
        
        try {
            // Step 1: Get or create the single platform specification
            log.info("\nGetting/Creating the platform specification...");
            AiModelSpecification platformSpec = platformService.getOrCreatePlatformSpecification();
            log.info("  Platform Specification:");
            log.info("    Name: {}", platformSpec.getName());
            log.info("    Version: {}", platformSpec.getVersion());
            log.info("    Description: {}", platformSpec.getDescription());
            log.info("  ✓ Platform specification ready (created once, reused for all models)");
            
            // Step 2: Convert multiple models using the same specification
            log.info("\nConverting models (all will use the same platform specification):");
            for (String modelId : models) {
                try {
                    log.info("\n  Processing model: {}", modelId);
                    
                    AiModelCreate modelCreate = platformService
                        .huggingFaceModelToAiModelCreate(modelId, BASE_URL);
                    
                    log.info("    Model Name: {}", modelCreate.getName());
                    log.info("    Specification: {}", 
                        modelCreate.getAiModelSpecification() != null 
                            ? modelCreate.getAiModelSpecification().getName() 
                            : "null");
                    log.info("    Specification ID: {}", 
                        modelCreate.getAiModelSpecification() != null 
                            ? modelCreate.getAiModelSpecification().getId() 
                            : "null");
                    
                    // Show some model-specific characteristics
                    if (modelCreate.getServiceCharacteristic() != null) {
                        modelCreate.getServiceCharacteristic().stream()
                            .filter(c -> c.getName().equals("modelType") || c.getName().equals("library"))
                            .forEach(c -> log.info("    {}: {}", c.getName(), c.getValue()));
                    }
                    
                    log.info("  ✓ Model converted successfully");
                    
                } catch (Exception e) {
                    log.error("  ✗ Error processing model {}: {}", modelId, e.getMessage());
                }
            }
            
            log.info("\n" + "-".repeat(80));
            log.info("SUMMARY (New Approach):");
            log.info("  - Specifications created: 1 (platform specification)");
            log.info("  - All {} models reference the same specification", models.length);
            log.info("  - Model-specific details are in AiModel characteristics");
            log.info("  - Specification represents the platform, not individual models");
            log.info("-".repeat(80));
            
            // Demonstrate search functionality
            demonstrateSearch();
            
        } catch (Exception e) {
            log.error("Error in platform approach demo: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Demonstrates the search functionality
     */
    private void demonstrateSearch() {
        log.info("\nBonus: Searching for models...");
        try {
            List<String> bertModels = platformService.searchModels("bert", 5);
            log.info("  Found {} BERT models:", bertModels.size());
            bertModels.forEach(model -> log.info("    - {}", model));
            
            // Validate a model
            String testModel = "gpt2";
            boolean exists = platformService.validateModelExists(testModel);
            log.info("\n  Validation check for '{}': {}", testModel, exists ? "✓ EXISTS" : "✗ NOT FOUND");
            
        } catch (Exception e) {
            log.error("  Error during search demo: {}", e.getMessage());
        }
    }
}
