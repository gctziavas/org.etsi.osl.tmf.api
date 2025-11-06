package org.etsi.osl.tmf.aim915.integrations.mlflow;

import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.etsi.osl.tmf.aim915.model.CharacteristicSpecification;
import org.etsi.osl.tmf.aim915.model.CharacteristicValueSpecification;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService; 
import org.mlflow.tracking.MlflowClient;
import org.mlflow.api.proto.ModelRegistry.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.mlflow.api.proto.Service.Run;
import org.mlflow.api.proto.Service.RunData;
import org.mlflow.api.proto.Service.Param;
import org.mlflow.api.proto.Service.RunTag;

import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.common.model.Any;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Service
public class MlflowService {

    private static final Logger log = LoggerFactory.getLogger(MlflowService.class);
    private final MlflowClient client;
    private final MlflowUtils mlflowUtils;
    private final AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService;

    public MlflowService(@Value("${mlflow.host:127.0.0.1}") String host,
                         @Value("${mlflow.port:5000}") int port,
                         AiModelSpecificationRepositoryService aiModelSpecificationRepositoryService) {
        String url = String.format("http://%s:%d", host, port);
        log.info("Initializing MlflowService with URL: {}", url);
        this.client = new MlflowClient(url);
        this.mlflowUtils = new MlflowUtils(client);
        this.aiModelSpecificationRepositoryService = aiModelSpecificationRepositoryService;
    }

    public AiModelSpecificationCreate mlFlowToAiModelSpecificationCreate(ModelVersion modelVersion, Run run, String baseUrl) {
        log.debug("Converting MLflow ModelVersion to AiModelSpecificationCreate: name={}, version={}", 
            modelVersion.getName(), modelVersion.getVersion());

        AiModelSpecificationCreate specCreate = new AiModelSpecificationCreate();

        specCreate.setName(modelVersion.getName());
        specCreate.setVersion(modelVersion.getVersion());
        specCreate.setDescription(modelVersion.getDescription());

        // Create a spec characteristic with the model version
        if (modelVersion.getVersion() != null) {
            CharacteristicSpecification modelVersionSpec = new CharacteristicSpecification();
            modelVersionSpec.setName("modelVersion");
            CharacteristicValueSpecification characteristicValueSpec = new CharacteristicValueSpecification();
            characteristicValueSpec.setValueType("string");
            characteristicValueSpec.setValue(modelVersion.getVersion());
            List<CharacteristicValueSpecification> characteristicValueSpecs = List.of(characteristicValueSpec);
            modelVersionSpec.setCharacteristicValueSpecification(characteristicValueSpecs);
            specCreate.addSpecCharacteristicItem(modelVersionSpec);
        } else {
            log.warn("ModelVersion version is null for model: {}", modelVersion.getName());
        }

        // Create a spec characteristic with the model lifecycle status
        if (modelVersion.getStatus().name()!= null) {
            CharacteristicSpecification lifecycleStatusSpec = new CharacteristicSpecification();
            lifecycleStatusSpec.setName("lifecycleStatus");
            CharacteristicValueSpecification lifecycleValueSpec = new CharacteristicValueSpecification();
            lifecycleValueSpec.setValueType("string");
            lifecycleValueSpec.setValue(modelVersion.getStatus().name());
            List<CharacteristicValueSpecification> lifecycleValueSpecs = List.of(lifecycleValueSpec);
            lifecycleStatusSpec.setCharacteristicValueSpecification(lifecycleValueSpecs);
            specCreate.addSpecCharacteristicItem(lifecycleStatusSpec);
        } else {
            log.warn("ModelVersion status is null for model: {}", modelVersion.getName());
        }
       

        // Model data sheet: Set URL to the datasheet artifact if it exists
        if (run != null) {
            // Check which datasheet path actually exists
            String[] datasheetPaths = {"model_data_sheet", "datasheet", "model_card", "model_datasheet"};
            String foundPath = null;
            
            for (String path : datasheetPaths) {
                if (mlflowUtils.artifactExists(run.getInfo().getRunId(), path)) {
                    foundPath = path;
                    log.debug("Found datasheet artifact at path: {}", path);
                    break;
                }
            }
            
            if (foundPath != null) {
                String modelDataSheetUrl = baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/" + foundPath;
                specCreate.setModelDataSheet(modelDataSheetUrl);
                log.debug("Set model data sheet URL: {}", modelDataSheetUrl);
            } else {
                log.debug("No datasheet artifact found for model: {}", modelVersion.getName());
            }
        }
        
        // Deployment record from model version tags or artifact
        String deploymentRecordUrl = null;
        
        // First check if it exists as a tag
        for (ModelVersionTag tag : modelVersion.getTagsList()) {
            if ("deployment.record.url".equals(tag.getKey())) {
                deploymentRecordUrl = tag.getValue();
                log.debug("Found deployment record URL from tag: {}", deploymentRecordUrl);
                break;
            }
        }
        
        // If not found in tags, check if it exists as an artifact
        if (deploymentRecordUrl == null && run != null) {
            String[] deploymentPaths = {"deployment_record", "deployment", "deployment.yaml", "deployment.json", "deployment_config"};
            String foundPath = null;
            
            for (String path : deploymentPaths) {
                if (mlflowUtils.artifactExists(run.getInfo().getRunId(), path)) {
                    foundPath = path;
                    log.debug("Found deployment record artifact at path: {}", path);
                    break;
                }
            }
            
            if (foundPath != null) {
                deploymentRecordUrl = baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/" + foundPath;
                log.debug("Set deployment record URL from artifact: {}", deploymentRecordUrl);
            }
        }
        
        if (deploymentRecordUrl != null) {
            specCreate.setDeploymentRecord(deploymentRecordUrl);
        } else {
            log.debug("No deployment record found for model: {}", modelVersion.getName());
        }

        if (run.getData() != null) {
            RunData runData = run.getData();

            // Inherited model from run parameters, tags, or artifacts
            String inheritedModelUrl = null;
            
            // First check parameters
            for (Param param : runData.getParamsList()) {
                if ("base_model_uri".equals(param.getKey())) {
                    inheritedModelUrl = param.getValue();
                    log.debug("Found inherited model in params: {}", inheritedModelUrl);
                    break;
                }
            }
            
            // If not found in params, check tags
            if (inheritedModelUrl == null) {
                for (RunTag tag : runData.getTagsList()) {
                    if ("base_model_uri".equals(tag.getKey())) {
                        inheritedModelUrl = tag.getValue();
                        log.debug("Found inherited model in tags: {}", inheritedModelUrl);
                        break;
                    }
                }
            }
            
            // If not found in params or tags, check artifacts
            if (inheritedModelUrl == null) {
                String[] inheritedModelPaths = {"base_model", "inherited_model", "parent_model", "base_model.pkl", "foundation_model"};
                String foundPath = null;
                
                for (String path : inheritedModelPaths) {
                    if (mlflowUtils.artifactExists(run.getInfo().getRunId(), path)) {
                        foundPath = path;
                        log.debug("Found inherited model artifact at path: {}", path);
                        break;
                    }
                }
                
                if (foundPath != null) {
                    inheritedModelUrl = baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/" + foundPath;
                    log.debug("Set inherited model URL from artifact: {}", inheritedModelUrl);
                }
            }
            
            if (inheritedModelUrl != null) {
                specCreate.setInheritedModel(inheritedModelUrl);
            } else {
                log.debug("No inherited model found for model: {}", modelVersion.getName());
            }
        }

        log.info("Successfully converted ModelVersion {} v{} to AiModelSpecification", 
            specCreate.getName(), specCreate.getVersion());
        return specCreate;
    }



    public AiModelCreate mlFlowToAiModelCreate(ModelVersion modelVersion, Run run, String baseUrl) {
        log.debug("Converting MLflow ModelVersion to AiModelCreate: name={}, version={}, baseUrl={}", 
            modelVersion.getName(), modelVersion.getVersion(), baseUrl);

        AiModelCreate aiModelCreate = new AiModelCreate();

        String modelName = modelVersion.getName();
        String modelVersionStr = modelVersion.getVersion();

        // Find or create AiModelSpecification
        AiModelSpecification aiModelSpec = aiModelSpecificationRepositoryService.findAiModelSpecificationByNameAndVersion(modelName, modelVersionStr);
        if (aiModelSpec == null) {
            log.warn("No AiModelSpecification found for model: {} v{}. Creating a new one.", modelName, modelVersionStr);
            AiModelSpecificationCreate aiModelSpecCreate = mlFlowToAiModelSpecificationCreate(modelVersion, run, baseUrl);
            aiModelSpec = aiModelSpecificationRepositoryService.createAiModelSpecification(aiModelSpecCreate);
        } else {
            log.debug("Found existing AiModelSpecification for model: {} v{}", modelName, modelVersionStr);
        }

        // Set basic properties
        aiModelCreate.setAiModelSpecification(aiModelSpec);
        aiModelCreate.setName(aiModelSpec.getName());
        aiModelCreate.setDescription(aiModelSpec.getDescription());
        
        
        // 1. Model artifact URL
        Characteristic modelArtifactChar = new Characteristic();
        modelArtifactChar.setName("modelArtifactUrl");
        modelArtifactChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/model"));
        modelArtifactChar.setValueType("string");
        aiModelCreate.addServiceCharacteristicItem(modelArtifactChar);
        log.debug("Added modelArtifactUrl characteristic");
        
        if (run != null) {
            // 2. Training data URL
            Characteristic trainingDataChar = new Characteristic();
            trainingDataChar.setName("trainingDataUrl");
            trainingDataChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/training_data"));
            trainingDataChar.setValueType("string");
            aiModelCreate.addServiceCharacteristicItem(trainingDataChar);
            
            // 3. Evaluation data URL
            Characteristic evaluationDataChar = new Characteristic();
            evaluationDataChar.setName("evaluationDataUrl");
            evaluationDataChar.setValue(new Any(baseUrl + "/aiModel/" + modelVersion.getName() + "_v" + modelVersion.getVersion() + "/artifacts/mlflow/evaluation_data"));
            evaluationDataChar.setValueType("string");
            aiModelCreate.addServiceCharacteristicItem(evaluationDataChar);
            
            // 4. Model data sheet URL - reuse from spec if available
            if (aiModelSpec.getModelDataSheet() != null) {
                String modelDataSheetUrl = (String) aiModelSpec.getModelDataSheet();
                if (!modelDataSheetUrl.isEmpty()) {
                    Characteristic modelDataSheetUrlChar = new Characteristic();
                    modelDataSheetUrlChar.setName("modelDataSheetUrl");
                    modelDataSheetUrlChar.setValue(new Any(modelDataSheetUrl));
                    modelDataSheetUrlChar.setValueType("string");
                    aiModelCreate.addServiceCharacteristicItem(modelDataSheetUrlChar);
                    log.debug("Added modelDataSheetUrl characteristic from spec");
                }
            }
            
            // 5. Deployment record URL - reuse from spec if available
            if (aiModelSpec.getDeploymentRecord() != null) {
                String deploymentRecordUrl = (String) aiModelSpec.getDeploymentRecord();
                if (!deploymentRecordUrl.isEmpty()) {
                    Characteristic deploymentRecordChar = new Characteristic();
                    deploymentRecordChar.setName("deploymentRecordUrl");
                    deploymentRecordChar.setValue(new Any(deploymentRecordUrl));
                    deploymentRecordChar.setValueType("string");
                    aiModelCreate.addServiceCharacteristicItem(deploymentRecordChar);
                    log.debug("Added deploymentRecordUrl characteristic from spec: {}", deploymentRecordUrl);
                }
            }
            
            // 6. Inherited model - reuse from spec if available
            if (aiModelSpec.getInheritedModel() != null) {
                String inheritedModelUrl = (String) aiModelSpec.getInheritedModel();
                if (!inheritedModelUrl.isEmpty()) {
                    Characteristic inheritedModelChar = new Characteristic();
                    inheritedModelChar.setName("inheritedModelUri");
                    inheritedModelChar.setValue(new Any(inheritedModelUrl));
                    inheritedModelChar.setValueType("string");
                    aiModelCreate.addServiceCharacteristicItem(inheritedModelChar);
                    log.debug("Added inheritedModelUri characteristic from spec: {}", inheritedModelUrl);
                }
            }
            
            // 7. Model data sheet from model version description
            if (modelVersion.getDescription() != null && !modelVersion.getDescription().isEmpty()) {
                Characteristic modelDataSheetChar = new Characteristic();
                modelDataSheetChar.setName("modelDataSheet");
                modelDataSheetChar.setValue(new Any(modelVersion.getDescription()));
                modelDataSheetChar.setValueType("string");
                aiModelCreate.addServiceCharacteristicItem(modelDataSheetChar);
                log.debug("Added modelDataSheet characteristic from description");
            }
            
            // 8. Store MLflow Run ID for artifact retrieval
            Characteristic runIdChar = new Characteristic();
            runIdChar.setName("mlflowRunId");
            runIdChar.setValue(new Any(run.getInfo().getRunId()));
            runIdChar.setValueType("string");
            aiModelCreate.addServiceCharacteristicItem(runIdChar);
            log.debug("Added mlflowRunId characteristic: {}", run.getInfo().getRunId());
            
            log.debug("Added training, evaluation data, model datasheet URL, deployment record, inherited model, and run ID characteristics");
        } else {
            log.debug("No Run provided, skipping run-based characteristics");
        }
        
        log.info("Successfully converted ModelVersion {} v{} to AiModelCreate", 
            modelVersion.getName(), modelVersion.getVersion());
        return aiModelCreate;
    }

}