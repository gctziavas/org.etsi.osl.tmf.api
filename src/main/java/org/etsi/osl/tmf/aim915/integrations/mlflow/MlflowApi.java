package org.etsi.osl.tmf.aim915.integrations.mlflow;

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
 * REST API for MLflow integration with TMF 915 AI Model Management.
 * 
 * Provides endpoints for:
 * - Importing MLflow models as specifications
 * - Creating model instances (deployments)
 * - Downloading model artifacts
 * - Querying MLflow registry
 */
@Tag(name = "MLflow Integration", description = "Endpoints for MLflow AI Model integration")
@RequestMapping("/aim/v4")
public interface MlflowApi {

    // ========================================
    // Specification Import Endpoints
    // ========================================

    @Operation(
        summary = "Import MLflow model as specification",
        description = "Creates an AiModelSpecification from a registered MLflow model. " +
                      "If the specification already exists, returns the existing one.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Specification created or already exists"),
        @ApiResponse(responseCode = "404", description = "Model not found in MLflow"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/mlflow/import/{modelName}")
    ResponseEntity<Map<String, Object>> importModelAsSpecification(
        @Parameter(description = "Name of the registered model in MLflow", required = true)
        @PathVariable("modelName") String modelName,
        @Parameter(description = "Model version (optional, uses latest if not specified)")
        @RequestParam(value = "version", required = false) String version
    );

    @Operation(
        summary = "Sync all MLflow models",
        description = "Synchronizes all registered models from MLflow, creating specifications for each. " +
                      "Skips models that already have specifications.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sync completed"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/mlflow/sync")
    ResponseEntity<Map<String, Object>> syncAllModels();

    // ========================================
    // Model Instance Endpoints
    // ========================================

    @Operation(
        summary = "Deploy model from MLflow",
        description = "Creates an AiModel instance from an MLflow model. " +
                      "This imports the specification if needed and creates a deployed model instance.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model deployed successfully"),
        @ApiResponse(responseCode = "404", description = "Model not found in MLflow"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/mlflow/deploy/{modelName}")
    ResponseEntity<Map<String, Object>> deployModel(
        @Parameter(description = "Name of the registered model in MLflow", required = true)
        @PathVariable("modelName") String modelName,
        @Parameter(description = "Model version (optional, uses latest if not specified)")
        @RequestParam(value = "version", required = false) String version,
        @Parameter(description = "Deployment endpoint URL (optional)")
        @RequestParam(value = "endpoint", required = false) String deploymentEndpoint
    );

    @Operation(
        summary = "Create model instance from specification",
        description = "Creates an AiModel instance from an existing AiModelSpecification.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model instance created"),
        @ApiResponse(responseCode = "404", description = "Specification not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/mlflow/instantiate/{specificationId}")
    ResponseEntity<Map<String, Object>> instantiateModel(
        @Parameter(description = "ID of the AiModelSpecification", required = true)
        @PathVariable("specificationId") String specificationId,
        @Parameter(description = "Name for the model instance (optional)")
        @RequestParam(value = "name", required = false) String instanceName,
        @Parameter(description = "Deployment endpoint URL (optional)")
        @RequestParam(value = "endpoint", required = false) String deploymentEndpoint
    );

    // ========================================
    // Query Endpoints
    // ========================================

    @Operation(
        summary = "List MLflow registered models",
        description = "Returns a list of all registered model names in MLflow.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of models"),
        @ApiResponse(responseCode = "500", description = "MLflow connection error")
    })
    @GetMapping("/mlflow/models")
    ResponseEntity<Map<String, Object>> listModels();

    @Operation(
        summary = "Check model exists in MLflow",
        description = "Checks if a model exists in the MLflow registry.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Model existence status")
    })
    @GetMapping("/mlflow/models/{modelName}/exists")
    ResponseEntity<Map<String, Object>> checkModelExists(
        @Parameter(description = "Name of the model to check", required = true)
        @PathVariable("modelName") String modelName
    );

    @Operation(
        summary = "Get MLflow connection info",
        description = "Returns information about the MLflow connection.",
        tags = {"MLflow Integration"}
    )
    @GetMapping("/mlflow/info")
    ResponseEntity<Map<String, Object>> getConnectionInfo();

    // ========================================
    // Artifact Endpoints
    // ========================================

    @Operation(
        summary = "Download model artifact",
        description = "Downloads an artifact associated with an AI Model from MLflow, " +
                      "such as the model file, training data, evaluation data, or documentation.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artifact downloaded successfully",
            content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "400", description = "Invalid artifact type"),
        @ApiResponse(responseCode = "404", description = "Artifact not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/aiModel/{modelId}/artifacts/mlflow/{artifactType}", 
                produces = "application/octet-stream")
    ResponseEntity<Resource> downloadArtifact(
        @Parameter(description = "Model identifier (format: modelName_vVersion)", required = true)
        @PathVariable("modelId") String modelId,
        @Parameter(description = "Type of artifact: model, training_data, evaluation_data, " +
                                 "model_data_sheet, deployment_record, inherited_model", required = true)
        @PathVariable("artifactType") String artifactType
    );

    @Operation(
        summary = "List available artifacts",
        description = "Lists all available artifacts for a model.",
        tags = {"MLflow Integration"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of artifacts"),
        @ApiResponse(responseCode = "404", description = "Model not found")
    })
    @GetMapping("/mlflow/models/{modelName}/artifacts")
    ResponseEntity<Map<String, Object>> listArtifacts(
        @Parameter(description = "Name of the model", required = true)
        @PathVariable("modelName") String modelName
    );
}
