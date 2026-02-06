package org.etsi.osl.tmf.aim915.integrations.huggingface;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for Hugging Face integration with TMF 915 AI Model Management.
 * 
 * Provides endpoints for:
 * - Importing Hugging Face models as specifications
 * - Creating model instances (deployments)
 * - Downloading model artifacts
 * - Searching the Hugging Face Hub
 */
@Tag(name = "Hugging Face Integration", description = "Endpoints for Hugging Face AI Model integration")
@RequestMapping("/AiM/v4")
public interface HuggingFaceApi {

    // ========================================
    // Specification Import Endpoints
    // ========================================

    @Operation(
        summary = "Import Hugging Face model as specification",
        description = "Creates an AiModelSpecification from a Hugging Face model. " +
                      "If the specification already exists, returns the existing one. " +
                      "Use underscore instead of slash in model IDs (e.g., 'openai_whisper-large').",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Specification created or already exists"),
        @ApiResponse(responseCode = "404", description = "Model not found on Hugging Face"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/huggingface/import/{modelId}")
    ResponseEntity<Map<String, Object>> importModelAsSpecification(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId
    );

    @Operation(
        summary = "Search and import Hugging Face models",
        description = "Searches Hugging Face Hub and imports matching models as specifications.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search and import completed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/huggingface/search-import")
    ResponseEntity<Map<String, Object>> searchAndImportModels(
        @Parameter(description = "Search query", required = true)
        @RequestParam("query") String query,
        @Parameter(description = "Maximum number of models to import")
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    );

    // ========================================
    // Model Instance Endpoints
    // ========================================

    @Operation(
        summary = "Deploy model from Hugging Face",
        description = "Creates an AiModel instance from a Hugging Face model. " +
                      "This imports the specification if needed and creates a deployed model instance.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model deployed successfully"),
        @ApiResponse(responseCode = "404", description = "Model not found on Hugging Face"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/huggingface/deploy/{modelId}")
    ResponseEntity<Map<String, Object>> deployModel(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId,
        @Parameter(description = "Deployment endpoint URL (optional)")
        @RequestParam(value = "endpoint", required = false) String deploymentEndpoint
    );

    @Operation(
        summary = "Create model instance from specification",
        description = "Creates an AiModel instance from an existing AiModelSpecification.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model instance created"),
        @ApiResponse(responseCode = "404", description = "Specification not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/huggingface/instantiate/{specificationId}")
    ResponseEntity<Map<String, Object>> instantiateModel(
        @Parameter(description = "ID of the AiModelSpecification", required = true)
        @PathVariable("specificationId") String specificationId,
        @Parameter(description = "Name for the model instance (optional)")
        @RequestParam(value = "name", required = false) String instanceName,
        @Parameter(description = "Deployment endpoint URL (optional)")
        @RequestParam(value = "endpoint", required = false) String deploymentEndpoint
    );

    // ========================================
    // Search and Query Endpoints
    // ========================================

    @Operation(
        summary = "Search Hugging Face models",
        description = "Searches the Hugging Face Hub for models matching the query.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/huggingface/search")
    ResponseEntity<Map<String, Object>> searchModels(
        @Parameter(description = "Search query", required = true)
        @RequestParam("query") String query,
        @Parameter(description = "Maximum number of results")
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    );

    @Operation(
        summary = "Validate Hugging Face model exists",
        description = "Checks if a model exists on Hugging Face Hub. " +
                      "Returns suggestions if not found.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model existence status with suggestions if not found")
    })
    @GetMapping("/huggingface/models/{modelId}/validate")
    ResponseEntity<Map<String, Object>> validateModel(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId
    );

    @Operation(
        summary = "Get Hugging Face connection info",
        description = "Returns information about the Hugging Face integration configuration.",
        tags = {"Hugging Face Integration"}
    )
    @GetMapping("/huggingface/info")
    ResponseEntity<Map<String, Object>> getConnectionInfo();

    // ========================================
    // File Operations Endpoints
    // ========================================

    @Operation(
        summary = "List files in Hugging Face model",
        description = "Returns a list of all files in the Hugging Face model repository.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of files"),
        @ApiResponse(responseCode = "404", description = "Model not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/huggingface/models/{modelId}/files")
    ResponseEntity<Map<String, Object>> listModelFiles(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId
    );

    // ========================================
    // Artifact Download Endpoints
    // ========================================

    @Operation(
        summary = "Download Hugging Face model artifact",
        description = "Downloads a specific file from a Hugging Face model repository.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File downloaded successfully", 
                     content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "Model or file not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/huggingface/models/{modelId}/artifacts", produces = "application/octet-stream")
    ResponseEntity<Resource> downloadArtifact(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId,
        @Parameter(description = "Specific file to download (e.g., 'config.json', 'pytorch_model.bin')")
        @RequestParam(value = "file", required = false, defaultValue = "config.json") String filename
    );

    @Operation(
        summary = "Download deployment artifacts as tar.gz",
        description = "Downloads all essential deployment files (config, weights, tokenizer) as a tar.gz archive.",
        tags = {"Hugging Face Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archive downloaded successfully",
                     content = @Content(mediaType = "application/gzip")),
        @ApiResponse(responseCode = "404", description = "Model not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/huggingface/models/{modelId}/artifacts/deployment.tar.gz", 
                produces = "application/gzip")
    ResponseEntity<Resource> downloadDeploymentArtifacts(
        @Parameter(description = "Hugging Face model ID (use underscore instead of slash)", required = true)
        @PathVariable("modelId") String modelId
    );
}
