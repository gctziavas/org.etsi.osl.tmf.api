package org.etsi.osl.tmf.aim915.integrations.mlflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI Model Artifacts", description = "Endpoints for downloading AI Model artifacts")
@RequestMapping("/tmf-api/aim/v1")
public interface MlFlowArtifactsApi {

    @Operation(
        summary = "Downloads a model artifact from MLflow",
        description = "Downloads an artifact associated with an AI Model from MLflow, such as the model file itself, training data, evaluation data, model data sheet, deployment record, or inherited model.",
        tags = { "AI Model Artifacts" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping(
        value = "/aiModel/{modelId}/artifacts/mlflow/{artifactType}",
        produces = { "application/octet-stream" }
    )
    ResponseEntity<Resource> downloadMlflowArtifact(
        @Parameter(in = ParameterIn.PATH, description = "Identifier of the AiModel (format: modelName_vVersion)", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @PathVariable("modelId") String modelId,
        @Parameter(in = ParameterIn.PATH, description = "Type of artifact to download: 'model', 'training_data', 'evaluation_data', 'model_data_sheet', 'datasheet', 'model_card', 'deployment_record', 'deployment', 'inherited_model', 'base_model'", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @PathVariable("artifactType") String artifactType
    );
}
