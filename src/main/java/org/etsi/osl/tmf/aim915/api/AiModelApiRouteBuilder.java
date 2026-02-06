package org.etsi.osl.tmf.aim915.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.aim915.model.AiModelCreate;
import org.etsi.osl.tmf.aim915.model.AiModelUpdate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationCreate;
import org.etsi.osl.tmf.aim915.model.AiModelSpecificationUpdate;
import org.etsi.osl.tmf.aim915.reposervices.AiModelRepositoryService;
import org.etsi.osl.tmf.aim915.reposervices.AiModelSpecificationRepositoryService;
import org.etsi.osl.tmf.aim915.integrations.mlflow.MlflowIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Camel route builder for TMF 915 AI Model Management API.
 * 
 * Provides JMS message routes for:
 * - AiModel CRUD operations
 * - AiModelSpecification CRUD operations
 * - Model deployment operations (deploy to Docker)
 */
@Configuration
@Component
public class AiModelApiRouteBuilder extends RouteBuilder {

    private static final transient Log logger = LogFactory.getLog(AiModelApiRouteBuilder.class.getName());

    // ========== AiModel Routes ==========
    
    @Value("${AIM_AIMODELS_GET}")
    private String AIM_AIMODELS_GET;

    @Value("${AIM_AIMODEL_GET_BY_ID}")
    private String AIM_AIMODEL_GET_BY_ID;

    @Value("${AIM_AIMODEL_ADD}")
    private String AIM_AIMODEL_ADD;

    @Value("${AIM_AIMODEL_UPDATE}")
    private String AIM_AIMODEL_UPDATE;

    @Value("${AIM_AIMODEL_DELETE}")
    private String AIM_AIMODEL_DELETE;

    // ========== AiModelSpecification Routes ==========
    
    @Value("${AIM_AIMODEL_SPECIFICATIONS_GET}")
    private String AIM_AIMODEL_SPECIFICATIONS_GET;

    @Value("${AIM_AIMODEL_SPECIFICATION_GET_BY_ID}")
    private String AIM_AIMODEL_SPECIFICATION_GET_BY_ID;

    @Value("${AIM_AIMODEL_SPECIFICATION_ADD}")
    private String AIM_AIMODEL_SPECIFICATION_ADD;

    @Value("${AIM_AIMODEL_SPECIFICATION_UPDATE}")
    private String AIM_AIMODEL_SPECIFICATION_UPDATE;

    @Value("${AIM_AIMODEL_SPECIFICATION_DELETE}")
    private String AIM_AIMODEL_SPECIFICATION_DELETE;

    // ========== Deployment Routes ==========
    
    @Value("${AIM_AIMODEL_DEPLOY}")
    private String AIM_AIMODEL_DEPLOY;

    @Value("${AIM_AIMODEL_DEPLOY_TO_HOST}")
    private String AIM_AIMODEL_DEPLOY_TO_HOST;

    @Value("${AIM_AIMODEL_UNDEPLOY}")
    private String AIM_AIMODEL_UNDEPLOY;

    @Value("${AIM_AIMODEL_UNDEPLOY_FROM_HOST}")
    private String AIM_AIMODEL_UNDEPLOY_FROM_HOST;

    @Value("${AIM_AIMODEL_REGISTER_INSTANCE}")
    private String AIM_AIMODEL_REGISTER_INSTANCE;

    @Autowired
    private AiModelRepositoryService aiModelService;

    @Autowired
    private AiModelSpecificationRepositoryService aiModelSpecificationService;

    @Autowired
    private MlflowIntegrationService mlflowIntegrationService;

    @Autowired
    private CentralLogger centralLogger;

    @Override
    public void configure() throws Exception {
        
        // ========================================
        // AiModel Routes
        // ========================================

        from(AIM_AIMODELS_GET)
                .log(LoggingLevel.INFO, log, AIM_AIMODELS_GET + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelService, "findAllAiModels")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_GET_BY_ID)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_GET_BY_ID + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelService, "findAiModelByUuid(${header.aimodelid})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_ADD)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_ADD + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .unmarshal()
                .json(JsonLibrary.Jackson, AiModelCreate.class, true)
                .bean(aiModelService, "createAiModel(${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_UPDATE)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_UPDATE + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .unmarshal()
                .json(JsonLibrary.Jackson, AiModelUpdate.class, true)
                .bean(aiModelService, "updateAiModel(${header.aimodelid}, ${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_DELETE)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_DELETE + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelService, "deleteAiModel(${header.aimodelid})");

        // ========================================
        // AiModelSpecification Routes
        // ========================================

        from(AIM_AIMODEL_SPECIFICATIONS_GET)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_SPECIFICATIONS_GET + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelSpecificationService, "findAllAiModelSpecifications")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_SPECIFICATION_GET_BY_ID)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_SPECIFICATION_GET_BY_ID + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelSpecificationService, "findAiModelSpecificationByUuid(${header.aimodelspecid})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_SPECIFICATION_ADD)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_SPECIFICATION_ADD + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .unmarshal()
                .json(JsonLibrary.Jackson, AiModelSpecificationCreate.class, true)
                .bean(aiModelSpecificationService, "createAiModelSpecification(${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_SPECIFICATION_UPDATE)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_SPECIFICATION_UPDATE + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .unmarshal()
                .json(JsonLibrary.Jackson, AiModelSpecificationUpdate.class, true)
                .bean(aiModelSpecificationService, "updateAiModelSpecification(${header.aimodelspecid}, ${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(AIM_AIMODEL_SPECIFICATION_DELETE)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_SPECIFICATION_DELETE + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(aiModelSpecificationService, "deleteAiModelSpecification(${header.aimodelspecid})");

        // ========================================
        // Model Deployment Routes
        // ========================================

        /**
         * Deploy a model from specification.
         * 
         * Headers:
         * - modelName: The MLflow model name (required)
         * - version: The model version (optional, uses latest if not specified)
         * - port: The host port to expose (optional, auto-assigned if not specified)
         * 
         * Returns: The created AiModel as JSON
         */
        from(AIM_AIMODEL_DEPLOY)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_DEPLOY + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .process(exchange -> {
                    String modelName = exchange.getIn().getHeader("modelName", String.class);
                    String version = exchange.getIn().getHeader("version", String.class);
                    Integer port = exchange.getIn().getHeader("port", Integer.class);
                    
                    if (modelName == null || modelName.isEmpty()) {
                        throw new IllegalArgumentException("Header 'modelName' is required");
                    }
                    
                    var model = (port != null) 
                        ? mlflowIntegrationService.deployModelFromMlflow(modelName, version, port)
                        : mlflowIntegrationService.deployModelFromMlflow(modelName, version);
                    
                    exchange.getIn().setBody(model);
                })
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        /**
         * Undeploy (stop) a running model.
         * 
         * Headers:
         * - modelName: The model name (required)
         * - version: The model version (required)
         */
        from(AIM_AIMODEL_UNDEPLOY)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_UNDEPLOY + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .process(exchange -> {
                    String modelName = exchange.getIn().getHeader("modelName", String.class);
                    String version = exchange.getIn().getHeader("version", String.class);
                    
                    if (modelName == null || modelName.isEmpty()) {
                        throw new IllegalArgumentException("Header 'modelName' is required");
                    }
                    if (version == null || version.isEmpty()) {
                        throw new IllegalArgumentException("Header 'version' is required");
                    }
                    
                    mlflowIntegrationService.stopDeployment(modelName, version);
                    exchange.getIn().setBody("{\"status\": \"stopped\"}");
                })
                .convertBodyTo(String.class);

        /**
         * Deploy a model to a CUSTOM Docker host.
         * 
         * Headers:
         * - modelName: The MLflow model name (required)
         * - version: The model version (optional, uses latest if not specified)
         * - dockerHost: The Docker host IP/hostname (required)
         * - dockerPort: The Docker API port (required)
         * - port: The host port to expose (optional, auto-assigned if not specified)
         * 
         * Returns: The created AiModel as JSON
         */
        from(AIM_AIMODEL_DEPLOY_TO_HOST)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_DEPLOY_TO_HOST + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .process(exchange -> {
                    String modelName = exchange.getIn().getHeader("modelName", String.class);
                    String version = exchange.getIn().getHeader("version", String.class);
                    String dockerHost = exchange.getIn().getHeader("dockerHost", String.class);
                    Integer dockerPort = exchange.getIn().getHeader("dockerPort", Integer.class);
                    Integer port = exchange.getIn().getHeader("port", Integer.class);
                    
                    if (modelName == null || modelName.isEmpty()) {
                        throw new IllegalArgumentException("Header 'modelName' is required");
                    }
                    if (dockerHost == null || dockerHost.isEmpty()) {
                        throw new IllegalArgumentException("Header 'dockerHost' is required");
                    }
                    if (dockerPort == null) {
                        throw new IllegalArgumentException("Header 'dockerPort' is required");
                    }
                    
                    var model = mlflowIntegrationService.deployModelToHost(
                        modelName, version, dockerHost, dockerPort, port);
                    
                    exchange.getIn().setBody(model);
                })
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        /**
         * Undeploy (stop) a model running on a CUSTOM Docker host.
         * 
         * Headers:
         * - modelName: The model name (required)
         * - version: The model version (required)
         * - dockerHost: The Docker host IP/hostname (required)
         * - dockerPort: The Docker API port (required)
         */
        from(AIM_AIMODEL_UNDEPLOY_FROM_HOST)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_UNDEPLOY_FROM_HOST + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .process(exchange -> {
                    String modelName = exchange.getIn().getHeader("modelName", String.class);
                    String version = exchange.getIn().getHeader("version", String.class);
                    String dockerHost = exchange.getIn().getHeader("dockerHost", String.class);
                    Integer dockerPort = exchange.getIn().getHeader("dockerPort", Integer.class);
                    
                    if (modelName == null || modelName.isEmpty()) {
                        throw new IllegalArgumentException("Header 'modelName' is required");
                    }
                    if (version == null || version.isEmpty()) {
                        throw new IllegalArgumentException("Header 'version' is required");
                    }
                    if (dockerHost == null || dockerHost.isEmpty()) {
                        throw new IllegalArgumentException("Header 'dockerHost' is required");
                    }
                    if (dockerPort == null) {
                        throw new IllegalArgumentException("Header 'dockerPort' is required");
                    }
                    
                    mlflowIntegrationService.stopDeploymentOnHost(modelName, version, dockerHost, dockerPort);
                    exchange.getIn().setBody("{\"status\": \"stopped\", \"dockerHost\": \"" + dockerHost + "\"}");
                })
                .convertBodyTo(String.class);

        /**
         * Register an externally deployed model instance.
         * 
         * Use this to register models running at any inference endpoint
         * (not managed by this system's Docker deployment).
         * 
         * Headers:
         * - specificationId: The AiModelSpecification UUID (required)
         * - instanceName: Optional custom name for the instance
         * - inferenceUrl: The inference endpoint URL (required)
         * 
         * Returns: The created AiModel as JSON
         */
        from(AIM_AIMODEL_REGISTER_INSTANCE)
                .log(LoggingLevel.INFO, log, AIM_AIMODEL_REGISTER_INSTANCE + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .process(exchange -> {
                    String specificationId = exchange.getIn().getHeader("specificationId", String.class);
                    String instanceName = exchange.getIn().getHeader("instanceName", String.class);
                    String inferenceUrl = exchange.getIn().getHeader("inferenceUrl", String.class);
                    
                    if (specificationId == null || specificationId.isEmpty()) {
                        throw new IllegalArgumentException("Header 'specificationId' is required");
                    }
                    if (inferenceUrl == null || inferenceUrl.isEmpty()) {
                        throw new IllegalArgumentException("Header 'inferenceUrl' is required");
                    }
                    
                    var model = mlflowIntegrationService.createModelInstance(
                        specificationId, instanceName, inferenceUrl);
                    
                    exchange.getIn().setBody(model);
                })
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);
    }

    static String toJsonString(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }

    static <T> T toJsonObj(String content, Class<T> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(content, valueType);
    }
}
