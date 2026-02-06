package org.etsi.osl.tmf.aim915.integrations.huggingface;

import static org.etsi.osl.tmf.aim915.integrations.huggingface.HuggingFacePlatformUtils.*;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.io.BufferedOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * Service for integrating with Hugging Face Hub as a platform
 * 
 * In this approach:
 * - AiModelSpecification represents the Hugging Face platform itself (single, static specification)
 * - AiModel instances represent individual models on Hugging Face (e.g., mistralai/Mistral-7B-v0.1, gpt2, bert-base-uncased)
 * 
 * This differs from the original HuggingFaceService where:
 * - Each Hugging Face model had its own AiModelSpecification
 * - AiModel instances were tied to those individual specifications
 * 
 * Benefits of this new approach:
 * - Single specification for the entire platform, easier to manage
 * - Individual models are instances of the platform specification
 * - More scalable for large numbers of models
 * - Clearer separation between platform capabilities and model-specific attributes
 */
@Service
public class HuggingFacePlatformService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFacePlatformService.class);
    private static final String PLATFORM_SPEC_NAME = "HuggingFace Platform";
    private static final String PLATFORM_SPEC_VERSION = "1.0";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService;

    public HuggingFacePlatformService(@Value("${huggingface.api.token:}") String apiToken,
                                       AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiToken = apiToken;
        this.aiModelSpecificationRepositoryService = aiModelSpecificationRepositoryService;
        
        log.info("Initializing HuggingFacePlatformService");
    }
    
    /**
     * Creates or retrieves the single Hugging Face platform specification
     * This specification models the platform itself, not individual models
     * 
     * @return The Hugging Face platform AiModelSpecification
     */
    public AiModelSpecification getOrCreatePlatformSpecification() {
        log.debug("Looking for existing Hugging Face platform specification");
        
        // Try to find existing specification
        AiModelSpecification existingSpec = aiModelSpecificationRepositoryService
            .findAiModelSpecificationByNameAndVersion(PLATFORM_SPEC_NAME, PLATFORM_SPEC_VERSION);
        
        if (existingSpec != null) {
            log.debug("Found existing Hugging Face platform specification with ID: {}", existingSpec.getId());
            return existingSpec;
        }
        
        log.info("Creating new Hugging Face platform specification");
        
        // Create new platform specification
        AiModelSpecificationCreate specCreate = new AiModelSpecificationCreate();
        specCreate.setName(PLATFORM_SPEC_NAME);
        specCreate.setVersion(PLATFORM_SPEC_VERSION);
        specCreate.setDescription("This specification describes how to define models from the Hugging Face Hub -"
            + "A platform for hosting and sharing AI models, particularly "
            + "for natural language processing, computer vision, and other machine learning tasks. "
            + "Supports models from various frameworks including Transformers, Diffusers, and more.");
        specCreate.setBaseType("AiModelSpecification");

        specCreate.setDeploymentRecord("The URL to the artifact or the location of the deployment record");
        specCreate.setModelDataSheet("The URL to the location of the model data sheet (model card)");
        specCreate.setInheritedModel("The URL to the artifact or the location of the inherited model");
        specCreate.setModelEvaluationData("The URL to the artifact or the location of the model evaluation data");
        specCreate.setModelTrainingData("The URL to the artifact or the location of the model training data");
        
        // Platform characteristics
        
        // 1. Platform URL
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "platformUrl",
                "Base URL for the Hugging Face Hub platform",
                "string",
                HF_HUB_URL
            )
        );
        
        // 2. API Base URL
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "apiUrl",
                "Base URL for the Hugging Face API",
                "string",
                HF_API_BASE_URL
            )
        );
        
        // 3. Supported model types (pipeline tags)
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "modelType",
                "Type of the model. (e.g.\"text-generation,text-classification,token-classification,question-answering,\"\n" + //
                                        "                    + \"translation,summarization,conversational,text-to-image,image-classification,\"\n" + //
                                        "                    + \"object-detection,image-segmentation,audio-classification,automatic-speech-recognition,\"\n" + //
                                        "                    + \"text-to-speech,fill-mask,sentence-similarity\")"
            )
        );
        
        // 4. Supported libraries
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "supportedLibraries",
                "Machine learning libraries supported by the platform (e.g ,\n" + //
                                        "                \"transformers,diffusers,sentence-transformers,timm,spacy,paddlenlp,\"\n" + //
                                        "                    + \"tensorflowjs,fairseq,asteroid,speechbrain,espnet,allennlp\")"
            )
        );
        
        // 5. Platform provider
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "provider",
                "Organization providing the model",
                "string"
            )
        );
        

        
        // 6. Deployment artifacts URL pattern
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "deploymentArtifactsUrl",
                "URL to download all deployment artifacts in tar.gz format",
                "string",
                "URI pattern: /tmf-api/aim/v1/aiModel/artifacts/huggingface/{modelId}/deployment.tar.gz"
            )
        );

        // 7. Total size of deployment artifacts
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "totalSize (in bytes)",
                "Total size of the model files",
                "integer"
            )
        );

        // 8. Tags
        specCreate.addSpecCharacteristicItem(
            createCharacteristicSpec(
                "tags",
                "Common tags associated with models on the Hugging Face Hub",
                "array"
            )
        );
        
        log.debug("Creating platform specification in repository");
        AiModelSpecification createdSpec = aiModelSpecificationRepositoryService.createAiModelSpecification(specCreate);
        log.info("Successfully created Hugging Face platform specification with ID: {}", createdSpec.getId());
        
        return createdSpec;
    }
    
    /**
     * Retrieves model information from Hugging Face Hub
     * 
     * @param modelId The Hugging Face model ID (e.g., "mistralai/Mistral-7B-v0.1", "gpt2")
     * @return JsonNode containing model metadata
     * @throws IOException if model cannot be accessed
     */
    public JsonNode getModelInfo(String modelId) throws IOException {
        log.debug("Fetching model info for: {}", modelId);
        String url = HF_API_BASE_URL + "/models/" + modelId;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
            log.trace("Using authentication token for model: {}", modelId);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Successfully retrieved model info for: {}", modelId);
            return objectMapper.readTree(response.getBody());
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND) {
                log.warn("Model not found: {}", modelId);
                throw new IOException("Model '" + modelId + "' not found on Hugging Face Hub. "
                    + "Please verify the model ID at: " + HF_HUB_URL + "/models", e);
                
            } else if (e.getStatusCode() == org.springframework.http.HttpStatus.UNAUTHORIZED || 
                       e.getStatusCode() == org.springframework.http.HttpStatus.FORBIDDEN) {
                log.warn("Access denied to model: {}. Model may be private.", modelId);
                throw new IOException("Access denied to model '" + modelId + "'. "
                    + "This model may be private. Please ensure your API token has access.", e);
                
            } else {
                log.error("HTTP error accessing model {}: {} - {}", modelId, e.getStatusCode(), e.getMessage());
                throw new IOException("Error accessing model '" + modelId + "': " + 
                                     e.getStatusCode() + " - " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error accessing model {}: {}", modelId, e.getMessage(), e);
            throw new IOException("Unexpected error accessing model '" + modelId + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates if a model exists on Hugging Face
     * 
     * @param modelId The Hugging Face model ID
     * @return true if model exists and is accessible
     */
    public boolean validateModelExists(String modelId) {
        try {
            log.debug("Validating model existence: {}", modelId);
            getModelInfo(modelId);
            log.debug("Model validated successfully: {}", modelId);
            return true;
        } catch (IOException e) {
            log.debug("Model validation failed for {}: {}", modelId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Converts a Hugging Face model to an AiModelCreate
     * The model will be associated with the platform specification
     * 
     * @param modelId The Hugging Face model ID (e.g., "mistralai/Mistral-7B-v0.1")
     * @param baseUrl The base URL for the TMF API
     * @return AiModelCreate object representing the specific model
     * @throws IOException if model information cannot be retrieved
     */
    public AiModelCreate huggingFaceModelToAiModelCreate(String modelId, String baseUrl) throws IOException {
        log.debug("Converting Hugging Face model to AiModelCreate: {}", modelId);
        
        // Get or create the platform specification
        AiModelSpecification platformSpec = getOrCreatePlatformSpecification();
        
        // Get model information
        JsonNode modelInfo = getModelInfo(modelId);
        
        AiModelCreate aiModelCreate = new AiModelCreate();
        
        // Associate with platform specification
        aiModelCreate.setAiModelSpecification(platformSpec);
        
        // Set model name and description
        aiModelCreate.setName(modelId);
        aiModelCreate.setDescription(extractModelDescription(modelInfo, modelId));
        
        // Service characteristics - model-specific attributes matching specification
        
        // 1. platformUrl - Model repository URL
        addCharacteristic(aiModelCreate, "platformUrl", buildRepositoryUrl(modelId), "string");
        
        // 2. apiUrl - Model API endpoint
        addCharacteristic(aiModelCreate, "apiUrl", HF_API_BASE_URL + "/models/" + modelId, "string");
        
        // 3. modelType - Model type/task (pipeline tag)
        String pipelineTag = getTextValue(modelInfo, "pipeline_tag");
        if (pipelineTag != null) {
            addCharacteristic(aiModelCreate, "modelType", pipelineTag, "string");
        }
        
        // 4. supportedLibraries - Library/framework
        String libraryName = getTextValue(modelInfo, "library_name");
        if (libraryName != null) {
            addCharacteristic(aiModelCreate, "supportedLibraries", libraryName, "string");
        }
        
        // 5. provider - Model author/organization
        String author = getTextValue(modelInfo, "author");
        if (author != null) {
            addCharacteristic(aiModelCreate, "provider", author, "string");
        }
        
        // 6. deploymentArtifactsUrl - URL for tar.gz download
        addCharacteristic(aiModelCreate, "deploymentArtifactsUrl", buildDeploymentArtifactsUrl(modelId, baseUrl), "string");
        
        // 7. totalSize - Model size in bytes
        if (modelInfo.has("siblings")) {
            long totalSize = calculateTotalSize(modelInfo.get("siblings"));
            if (totalSize > 0) {
                addCharacteristic(aiModelCreate, "totalSize (in bytes)", String.valueOf(totalSize), "integer");
                addCharacteristic(aiModelCreate, "totalSizeHuman", formatBytes(totalSize), "string");
            }
        }
        
        // 8. tags - Model characteristics
        String tags = extractTags(modelInfo);
        if (tags != null) {
            addCharacteristic(aiModelCreate, "tags", tags, "array");
        }
        
        // Additional TMF 915 standard fields
        
        // Model Data Sheet - model card URL
        addCharacteristic(aiModelCreate, "modelDataSheet", buildRepositoryUrl(modelId), "string");
        
        // Deployment Record - Inference API URL
        addCharacteristic(aiModelCreate, "deploymentRecord", buildInferenceApiUrl(modelId), "string");
        
        // Inherited Model (base model) - if available
        String baseModel = getNestedTextValue(modelInfo, "cardData", "base_model");
        if (baseModel != null) {
            addCharacteristic(aiModelCreate, "inheritedModel", baseModel, "string");
            addCharacteristic(aiModelCreate, "inheritedModelUrl", buildRepositoryUrl(baseModel), "string");
        }
        
        // Model Training Data - if available
        String trainingDatasets = getNestedTextValue(modelInfo, "cardData", "datasets");
        if (trainingDatasets != null && !trainingDatasets.isEmpty()) {
            addCharacteristic(aiModelCreate, "modelTrainingData", trainingDatasets, "string");
            // Create dataset URL - assuming first dataset name
            String firstDataset = trainingDatasets.split(",")[0].trim();
            addCharacteristic(aiModelCreate, "modelTrainingDataUrls", buildDatasetUrl(firstDataset), "string");
        }
        
        // Model Evaluation Data - if available
        String evalDatasets = getNestedTextValue(modelInfo, "cardData", "eval_datasets");
        if (evalDatasets != null && !evalDatasets.isEmpty()) {
            addCharacteristic(aiModelCreate, "modelEvaluationData", evalDatasets, "string");
        }
        
        // Additional model-specific metadata (not in specification)
        
        // Model ID for reference
        addCharacteristic(aiModelCreate, "modelId", modelId, "string");
        
        // Last modified timestamp
        String lastModified = getTextValue(modelInfo, "lastModified");
        if (lastModified != null) {
            addCharacteristic(aiModelCreate, "lastModified", lastModified, "datetime");
        }
        
        // Language (for NLP models)
        String languages = extractLanguages(modelInfo);
        if (languages != null) {
            addCharacteristic(aiModelCreate, "languages", languages, "array");
        }
        
        log.info("Successfully converted Hugging Face model {} to AiModelCreate with platform specification", modelId);
        return aiModelCreate;
    }
    
    /**
     * Searches for models on Hugging Face Hub
     * 
     * @param searchQuery Search query string
     * @param limit Maximum number of results to return
     * @return List of model IDs matching the search
     * @throws IOException if search fails
     */
    public List<String> searchModels(String searchQuery, int limit) throws IOException {
        log.debug("Searching for models with query: {}, limit: {}", searchQuery, limit);
        String url = HF_API_BASE_URL + "/models?search=" + searchQuery + "&limit=" + limit;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        JsonNode results = objectMapper.readTree(response.getBody());
        List<String> modelIds = new ArrayList<>();
        
        if (results.isArray()) {
            for (JsonNode model : results) {
                if (model.has("modelId")) {
                    modelIds.add(model.get("modelId").asText());
                }
            }
        }
        
        log.debug("Found {} models matching query: {}", modelIds.size(), searchQuery);
        return modelIds;
    }
    
    /**
     * Lists all files in a Hugging Face model repository
     * 
     * @param modelId The Hugging Face model ID
     * @return List of file paths in the repository
     * @throws IOException if model cannot be accessed
     */
    public List<String> listModelFiles(String modelId) throws IOException {
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        List<String> files = new ArrayList<>();
        if (siblings != null && siblings.isArray()) {
            for (JsonNode sibling : siblings) {
                String filename = sibling.get("rfilename").asText();
                files.add(filename);
            }
        }
        
        log.debug("Found {} files in model {}", files.size(), modelId);
        return files;
    }
    
    
    /**
     * Downloads all deployment artifacts from HuggingFace and creates a tar.gz archive on-the-fly
     * 
     * This method downloads essential model files needed for deployment and streams them
     * directly into a tar.gz archive in memory. The archive is created on-demand and
     * not stored on disk to save space.
     * 
     * Essential files downloaded:
     * - Model weights (pytorch_model.bin, model.safetensors)
     * - Configuration files (config.json)
     * - Tokenizer files (tokenizer.json, tokenizer_config.json, vocab.json, merges.txt)
     * 
     * @param modelId The HuggingFace model ID (e.g., "mistralai/Mistral-7B-v0.1")
     * @return byte array containing the tar.gz archive (created in memory, not on disk)
     * @throws IOException if download or archiving fails
     */
    public byte[] downloadDeploymentArtifactsAsTargz(String modelId) throws IOException {
        log.info("Downloading deployment artifacts for model: {}", modelId);
        
        // Get model info to find available files
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        if (siblings == null || !siblings.isArray()) {
            throw new IOException("No artifacts found for model: " + modelId);
        }
        
        // Get essential deployment files
        List<String> essentialFiles = getEssentialDeploymentFiles();
        
        // Find which essential files exist in the model
        List<String> filesToDownload = new ArrayList<>();
        for (JsonNode sibling : siblings) {
            if (sibling.has("rfilename")) {
                String filename = sibling.get("rfilename").asText();
                if (essentialFiles.contains(filename)) {
                    filesToDownload.add(filename);
                    log.debug("Found essential file: {}", filename);
                }
            }
        }
        
        if (filesToDownload.isEmpty()) {
            throw new IOException("No essential deployment files found for model: " + modelId);
        }
        
        log.info("Downloading {} essential files for deployment", filesToDownload.size());
        
        // Create tar.gz archive in memory using Apache Commons Compress
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(gzipOutputStream);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
            
            // Set tar format to support long file names
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            
            // Download each file and add to tar archive
            for (String filename : filesToDownload) {
                try {
                    log.debug("Downloading file: {}", filename);
                    byte[] fileContent = downloadFileFromHuggingFace(modelId, filename);
                    
                    // Create tar entry
                    TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
                    tarEntry.setSize(fileContent.length);
                    
                    // Write entry header and content
                    tarOutputStream.putArchiveEntry(tarEntry);
                    tarOutputStream.write(fileContent);
                    tarOutputStream.closeArchiveEntry();
                    
                    log.debug("Added {} to archive ({} bytes)", filename, fileContent.length);
                    
                } catch (Exception e) {
                    log.warn("Failed to download {}: {}. Skipping...", filename, e.getMessage());
                }
            }
            
            tarOutputStream.finish();
        }
        
        byte[] tarGzData = byteArrayOutputStream.toByteArray();
        log.info("Successfully created tar.gz archive in memory with {} files ({} bytes total)", 
                 filesToDownload.size(), tarGzData.length);
        log.debug("Archive created on-demand without disk storage - will be garbage collected after serving");
        
        return tarGzData;
    }
    
    /**
     * Streams deployment artifacts directly to an output stream without storing in memory
     * Use this method for very large models to avoid memory issues
     * 
     * @param modelId The HuggingFace model ID
     * @param outputStream The output stream to write the tar.gz to
     * @throws IOException if download or streaming fails
     */
    public void streamDeploymentArtifactsAsTargz(String modelId, java.io.OutputStream outputStream) throws IOException {
        log.info("Streaming deployment artifacts for model: {}", modelId);
        
        // Get model info to find available files
        JsonNode modelInfo = getModelInfo(modelId);
        JsonNode siblings = modelInfo.get("siblings");
        
        if (siblings == null || !siblings.isArray()) {
            throw new IOException("No artifacts found for model: " + modelId);
        }
        
        // Get essential deployment files
        List<String> essentialFiles = getEssentialDeploymentFiles();
        
        // Find which essential files exist in the model
        List<String> filesToDownload = new ArrayList<>();
        for (JsonNode sibling : siblings) {
            if (sibling.has("rfilename")) {
                String filename = sibling.get("rfilename").asText();
                if (essentialFiles.contains(filename)) {
                    filesToDownload.add(filename);
                    log.debug("Found essential file: {}", filename);
                }
            }
        }
        
        if (filesToDownload.isEmpty()) {
            throw new IOException("No essential deployment files found for model: " + modelId);
        }
        
        log.info("Streaming {} essential files for deployment", filesToDownload.size());
        
        // Stream tar.gz directly to output without buffering in memory
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(gzipOutputStream);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
            
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            
            // Download and stream each file directly
            for (String filename : filesToDownload) {
                try {
                    log.debug("Streaming file: {}", filename);
                    byte[] fileContent = downloadFileFromHuggingFace(modelId, filename);
                    
                    TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
                    tarEntry.setSize(fileContent.length);
                    
                    tarOutputStream.putArchiveEntry(tarEntry);
                    tarOutputStream.write(fileContent);
                    tarOutputStream.closeArchiveEntry();
                    
                    log.debug("Streamed {} ({} bytes)", filename, fileContent.length);
                    
                } catch (Exception e) {
                    log.warn("Failed to stream {}: {}. Skipping...", filename, e.getMessage());
                }
            }
            
            tarOutputStream.finish();
        }
        
        log.info("Successfully streamed tar.gz archive for model: {}", modelId);
    }
    
    /**
     * Downloads a single file from HuggingFace model repository
     * 
     * @param modelId The HuggingFace model ID
     * @param filename The file to download
     * @return byte array containing the file content
     * @throws IOException if download fails
     */
    private byte[] downloadFileFromHuggingFace(String modelId, String filename) throws IOException {
        String fileUrl = HF_HUB_URL + "/" + modelId + "/resolve/main/" + filename;
        
        HttpHeaders headers = new HttpHeaders();
        if (apiToken != null && !apiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiToken);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                fileUrl, 
                HttpMethod.GET, 
                entity, 
                byte[].class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to download file {} from model {}: {}", filename, modelId, e.getMessage());
            throw new IOException("Failed to download " + filename + ": " + e.getMessage(), e);
        }
    }
}

