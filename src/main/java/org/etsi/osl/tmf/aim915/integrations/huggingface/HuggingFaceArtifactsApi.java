package org.etsi.osl.tmf.aim915.integrations.huggingface;

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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Hugging Face Artifacts", description = "Endpoints for downloading Hugging Face model artifacts")
@RequestMapping("/AiM/v4")
public interface HuggingFaceArtifactsApi {

    @Operation(
        summary = "Downloads a Hugging Face model file",
        description = "Downloads a specific file from a Hugging Face model repository.",
        tags = { "Hugging Face Artifacts" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping(
        value = "/aiModel/{modelId}/artifacts/huggingface",
        produces = { "application/octet-stream" }
    )
    ResponseEntity<Resource> downloadHuggingFaceArtifact(
        @Parameter(in = ParameterIn.PATH, description = "Identifier of the Hugging Face model (e.g., 'bert-base-uncased', 'openai_whisper-large'). Use underscore instead of slash.", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @PathVariable("modelId") String modelId,
        @Parameter(in = ParameterIn.QUERY, description = "Specific file to download (e.g., 'pytorch_model.bin', 'config.json'). If not specified, downloads the main model file.", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @RequestParam(value = "file", required = false, defaultValue = "pytorch_model.bin") String filename
    );

    @Operation(
        summary = "Lists available files in a Hugging Face model",
        description = "Returns a list of all files available in the Hugging Face model repository.",
        tags = { "Hugging Face Artifacts" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping(
        value = "/aiModel/{modelId}/artifacts/huggingface/list",
        produces = { "application/json" }
    )
    ResponseEntity<java.util.List<String>> listHuggingFaceArtifacts(
        @Parameter(in = ParameterIn.PATH, description = "Identifier of the Hugging Face model (e.g., 'bert-base-uncased', 'openai_whisper-large'). Use underscore instead of slash.", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @PathVariable("modelId") String modelId
    );

    @Operation(
        summary = "Validates if a Hugging Face model exists",
        description = "Checks if a model exists and is accessible. Returns model info if found, or suggestions if not.",
        tags = { "Hugging Face Artifacts" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Model found"),
            @ApiResponse(responseCode = "404", description = "Model not found (check X-Error-Message header for suggestions)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping(
        value = "/huggingface/models/{modelId}/validate",
        produces = { "application/json" }
    )
    ResponseEntity<String> validateHuggingFaceModel(
        @Parameter(in = ParameterIn.PATH, description = "Identifier of the Hugging Face model to validate (e.g., 'bert-base-uncased', 'openai_whisper-large'). Use underscore instead of slash.", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @PathVariable("modelId") String modelId
    );

    @Operation(
        summary = "Searches for Hugging Face models",
        description = "Searches the Hugging Face Hub for models matching the query.",
        tags = { "Hugging Face Artifacts" },
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping(
        value = "/huggingface/models/search",
        produces = { "application/json" }
    )
    ResponseEntity<java.util.List<String>> searchHuggingFaceModels(
        @Parameter(in = ParameterIn.QUERY, description = "Search query", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        @RequestParam("query") String query,
        @Parameter(in = ParameterIn.QUERY, description = "Maximum number of results", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "10"))
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    );
}
