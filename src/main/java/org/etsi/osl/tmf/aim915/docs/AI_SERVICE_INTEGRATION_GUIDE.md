# Creating a TMF Service with AIModel Reference - OSOM Integration Guide

This guide describes how to create a TMF Service that references an AIModel and integrates with OSOM for lifecycle management.

## Overview

The integration allows you to:
- Create services that deploy and manage AI models
- Automatically provision AI model containers when a service is ordered
- Link services to their supporting AI models via ServiceRelationship
- Automatically terminate AI models when services are deleted

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Component Architecture                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────────┐   │
│  │   Portal     │───>│    OSOM      │───>│      TMF API             │   │
│  │  (Order UI)  │    │(Orchestrator)│    │ (Service Inventory)      │   │
│  └──────────────┘    └──────────────┘    └──────────────────────────┘   │
│                             │                        │                  │
│                             │ LCM Rules              │                  │
│                             ▼                        ▼                  │
│                      ┌──────────────┐    ┌──────────────────────────┐   │
│                      │  Camel Bus   │───>│  MLflow Integration      │   │
│                      │  (Routes)    │    │  Service                 │   │
│                      └──────────────┘    └──────────────────────────┘   │
│                                                      │                  │
│                                                      ▼                  │
│                                          ┌──────────────────────────┐   │
│                                          │  Docker (AI Container)   │   │
│                                          └──────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Approach: Using ServiceRelationship

Since `AiModel` extends `Service`, we leverage the existing `serviceRelationship` pattern to link services to AI models.

### Why ServiceRelationship?

1. **No model changes required** - Uses existing TMF638 Service model
2. **TMF-compliant** - Follows standard relationship patterns
3. **Flexible** - Supports multiple relationship types
4. **Already integrated** - OSOM and Portal understand ServiceRelationship

## Implementation Steps

### Step 1: Create ServiceSpecification

Create a ServiceSpecification that defines an AI-powered service:

```json
{
  "name": "AI Fraud Detection Service",
  "description": "A service that deploys and manages an AI fraud detection model",
  "lifecycleStatus": "Active",
  "isBundle": false,
  "serviceSpecCharacteristic": [
    {
      "name": "mlflow_model_name",
      "description": "Name of the MLflow model to deploy",
      "valueType": "string",
      "configurable": true,
      "serviceSpecCharacteristicValue": [
        {
          "isDefault": true,
          "value": {
            "value": "fraud-detection-model",
            "alias": ""
          }
        }
      ]
    },
    {
      "name": "mlflow_model_version",
      "description": "Version of the MLflow model",
      "valueType": "string",
      "configurable": true,
      "serviceSpecCharacteristicValue": [
        {
          "isDefault": true,
          "value": {
            "value": "1",
            "alias": ""
          }
        }
      ]
    },
    {
      "name": "aimodel_uuid",
      "description": "UUID of the deployed AiModel (populated after deployment)",
      "valueType": "string",
      "configurable": false
    },
    {
      "name": "inference_endpoint",
      "description": "URL endpoint for model inference (populated after deployment)",
      "valueType": "string",
      "configurable": false
    }
  ]
}
```

### Step 2: Create LCM Rules

Attach LCM (Lifecycle Management) rules to your ServiceSpecification.

#### 2.1 PRE_PROVISION Rule - Deploy AI Model

**Name:** `Deploy AI Model on Service Creation`
**Phase:** `PRE_PROVISION`
**Priority:** 1

```groovy
// PRE_PROVISION: Deploy the AI model from MLflow

import org.etsi.osl.tmf.common.model.service.Characteristic
import org.etsi.osl.tmf.common.model.Any

// Get model configuration from service characteristics
def modelName = ""
def modelVersion = ""

for (char in serviceUpdate.getServiceCharacteristic()) {
    if (char.getName() == "mlflow_model_name") {
        modelName = char.getValue().getValue()
    }
    if (char.getName() == "mlflow_model_version") {
        modelVersion = char.getValue().getValue()
    }
}

if (modelName && modelVersion) {
    // Call MLflow integration to deploy the model
    // This creates an AiModel with RESERVED state, deploys container, then ACTIVE
    def result = serviceOrderManager.deployAiModel(modelName, modelVersion)
    
    if (result != null) {
        // Store the AiModel UUID for later reference
        def aiModelChar = new Characteristic()
        aiModelChar.setName("aimodel_uuid")
        def aiModelValue = new Any()
        aiModelValue.setValue(result.aiModelUuid)
        aiModelChar.setValue(aiModelValue)
        serviceUpdate.addServiceCharacteristicItem(aiModelChar)
        
        // Store the inference endpoint
        def endpointChar = new Characteristic()
        endpointChar.setName("inference_endpoint")
        def endpointValue = new Any()
        endpointValue.setValue(result.inferenceEndpoint)
        endpointChar.setValue(endpointValue)
        serviceUpdate.addServiceCharacteristicItem(endpointChar)
        
        // Add note about deployment
        addServiceNote("AI Model deployed successfully. UUID: " + result.aiModelUuid)
    } else {
        addServiceNote("ERROR: Failed to deploy AI model")
    }
}
```

#### 2.2 AFTER_ACTIVATION Rule - Link AiModel

**Name:** `Link AiModel to Service`
**Phase:** `AFTER_ACTIVATION`
**Priority:** 1

```groovy
// AFTER_ACTIVATION: Create ServiceRelationship to AiModel

import org.etsi.osl.tmf.common.model.service.ServiceRelationship
import org.etsi.osl.tmf.common.model.service.ServiceRef

// Get the deployed AiModel UUID
def aiModelUuid = ""
for (char in service.getServiceCharacteristic()) {
    if (char.getName() == "aimodel_uuid") {
        aiModelUuid = char.getValue().getValue()
        break
    }
}

if (aiModelUuid) {
    // Create relationship to the AiModel
    def relationship = new ServiceRelationship()
    relationship.setRelationshipType("uses-aimodel")
    
    def serviceRef = new ServiceRef()
    serviceRef.setId(aiModelUuid)
    serviceRef.setName("Supporting AI Model")
    serviceRef.setReferredType("AiModel")
    relationship.setService(serviceRef)
    
    serviceUpdate.addServiceRelationshipItem(relationship)
    
    addServiceNote("Linked to AI Model: " + aiModelUuid)
}
```

#### 2.3 PRE_TERMINATE Rule - Stop AI Model

**Name:** `Terminate AI Model on Service Delete`
**Phase:** `PRE_TERMINATE`
**Priority:** 1

```groovy
// PRE_TERMINATE: Stop the AI model container

// Get the AiModel UUID from service characteristics
def aiModelUuid = ""
for (char in service.getServiceCharacteristic()) {
    if (char.getName() == "aimodel_uuid") {
        aiModelUuid = char.getValue().getValue()
        break
    }
}

if (aiModelUuid) {
    // Terminate the AI model (stops container, sets state to TERMINATED)
    def success = serviceOrderManager.terminateAiModel(aiModelUuid)
    
    if (success) {
        addServiceNote("AI Model terminated: " + aiModelUuid)
    } else {
        addServiceNote("WARNING: Failed to terminate AI Model: " + aiModelUuid)
    }
}
```

### Step 3: Add Camel Routes to OSOM

Add Camel routes in OSOM to communicate with TMF API's MLflow services.

#### 3.1 application.yml Configuration

Add to `org.etsi.osl.osom/src/main/resources/application.yml`:

```yaml
# AI Model Integration Routes
AIMODEL_DEPLOY:
  activemq:topic:AIMODEL.DEPLOY

AIMODEL_TERMINATE:
  activemq:topic:AIMODEL.TERMINATE

AIMODEL_GET_STATUS:
  activemq:topic:AIMODEL.GET.STATUS
```

#### 3.2 ServiceOrderManager Methods

Add to `ServiceOrderManager.java`:

```java
@Value("${AIMODEL_DEPLOY}")
private String AIMODEL_DEPLOY;

@Value("${AIMODEL_TERMINATE}")
private String AIMODEL_TERMINATE;

/**
 * Deploy an AI model from MLflow
 * @param modelName MLflow model name
 * @param modelVersion MLflow model version
 * @return Map with aiModelUuid and inferenceEndpoint
 */
public Map<String, String> deployAiModel(String modelName, String modelVersion) {
    Map<String, Object> request = new HashMap<>();
    request.put("modelName", modelName);
    request.put("modelVersion", modelVersion);
    
    try {
        Object response = template.requestBody(AIMODEL_DEPLOY, request);
        if (response instanceof String) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue((String) response, 
                new TypeReference<Map<String, String>>() {});
        }
    } catch (Exception e) {
        logger.error("Failed to deploy AI model: " + e.getMessage());
    }
    return null;
}

/**
 * Terminate an AI model
 * @param aiModelUuid UUID of the AiModel to terminate
 * @return true if successful
 */
public boolean terminateAiModel(String aiModelUuid) {
    try {
        Object response = template.requestBody(AIMODEL_TERMINATE, aiModelUuid);
        return "true".equals(response);
    } catch (Exception e) {
        logger.error("Failed to terminate AI model: " + e.getMessage());
    }
    return false;
}
```

### Step 4: Add Camel Routes to TMF API

Add routes in TMF API to handle requests from OSOM.

Create `AiModelCamelRoutes.java`:

```java
package org.etsi.osl.tmf.aim915.api;

import org.apache.camel.builder.RouteBuilder;
import org.etsi.osl.tmf.aim915.integrations.mlflow.MlflowIntegrationService;
import org.etsi.osl.tmf.aim915.integrations.mlflow.AiModelLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AiModelCamelRoutes extends RouteBuilder {

    @Autowired
    private MlflowIntegrationService mlflowIntegrationService;
    
    @Autowired
    private AiModelLifecycleService aiModelLifecycleService;

    @Override
    public void configure() throws Exception {
        
        // Deploy AI Model
        from("activemq:topic:AIMODEL.DEPLOY")
            .log("Received AI Model deploy request")
            .process(exchange -> {
                Map<String, Object> request = exchange.getIn().getBody(Map.class);
                String modelName = (String) request.get("modelName");
                String modelVersion = (String) request.get("modelVersion");
                
                var aiModel = mlflowIntegrationService.deployModelFromMlflow(
                    modelName, modelVersion);
                
                Map<String, String> response = new HashMap<>();
                response.put("aiModelUuid", aiModel.getId());
                response.put("inferenceEndpoint", 
                    getCharacteristicValue(aiModel, "inference_endpoint"));
                
                exchange.getMessage().setBody(response);
            });
        
        // Terminate AI Model
        from("activemq:topic:AIMODEL.TERMINATE")
            .log("Received AI Model terminate request")
            .process(exchange -> {
                String aiModelUuid = exchange.getIn().getBody(String.class);
                aiModelLifecycleService.terminateModel(aiModelUuid);
                exchange.getMessage().setBody("true");
            });
    }
    
    private String getCharacteristicValue(Object aiModel, String charName) {
        // Extract characteristic value from AiModel
        // Implementation depends on your model structure
        return "";
    }
}
```

### Step 5: Service Order Flow

The complete flow when ordering an AI-powered service:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Service Order Flow                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. User creates ServiceOrder via Portal                                │
│     ├─ ServiceOrderItem: action=add                                     │
│     └─ ServiceSpecification: "AI Fraud Detection Service"               │
│                                                                         │
│  2. OSOM receives ServiceOrder                                          │
│     └─> ProcessOrderItem starts workflow                                │
│                                                                         │
│  3. Service created (state: FEASIBILITY_CHECKED)                        │
│     └─> Copies characteristics from ServiceSpec                         │
│                                                                         │
│  4. PRE_PROVISION Phase                                                 │
│     └─> LCM Rule: "Deploy AI Model"                                     │
│         ├─> Calls ServiceOrderManager.deployAiModel()                   │
│         ├─> MlflowIntegrationService creates AiModel (RESERVED)         │
│         ├─> Docker container starts                                     │
│         ├─> AiModel updated to ACTIVE                                   │
│         └─> aimodel_uuid & inference_endpoint saved                     │
│                                                                         │
│  5. Service transitions to RESERVED → ACTIVE                            │
│                                                                         │
│  6. AFTER_ACTIVATION Phase                                              │
│     └─> LCM Rule: "Link AiModel to Service"                             │
│         └─> ServiceRelationship created (uses-aimodel)                  │
│                                                                         │
│  7. ServiceOrder completes (state: COMPLETED)                           │
│     └─> Service is ACTIVE with linked AiModel                           │
│                                                                         │
│  ════════════════════════════════════════════════════════════════════   │
│                                                                         │
│  8. User requests service termination                                   │
│     └─> ServiceOrder: action=delete                                     │
│                                                                         │
│  9. PRE_TERMINATE Phase                                                 │
│     └─> LCM Rule: "Terminate AI Model"                                  │
│         ├─> Calls ServiceOrderManager.terminateAiModel()                │
│         ├─> AiModelLifecycleService.terminateModel()                    │
│         ├─> Docker container stopped                                    │
│         └─> AiModel state → TERMINATED                                  │
│                                                                         │
│  10. Service terminated                                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## AiModel Lifecycle States

The AiModel follows this lifecycle:

```
    ┌────────────┐
    │  RESERVED  │  ← Created before container deployment
    └─────┬──────┘
          │ Container starts successfully
          ▼
    ┌────────────┐
    │   ACTIVE   │  ← Container running, endpoint available
    └─────┬──────┘
          │ Container stops or termination requested
          ▼
    ┌────────────┐
    │ TERMINATED │  ← Container stopped, model inactive
    └────────────┘
```

### Bidirectional Sync

The `AiModelLifecycleService` provides bidirectional synchronization:

1. **State → Container**: When AiModel state is set to TERMINATED, the container is stopped
2. **Container → State**: When container stops (external event), AiModel state is updated to TERMINATED

A scheduled task (every 30 seconds) monitors ACTIVE AiModels and syncs their state with container status.

## API Endpoints

### AiModel API (TMF915)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/tmf-api/aim915/aiModel` | List all AiModels |
| GET | `/tmf-api/aim915/aiModel/{id}` | Get AiModel by ID |
| POST | `/tmf-api/aim915/aiModel` | Create AiModel |
| PATCH | `/tmf-api/aim915/aiModel/{id}` | Update AiModel |
| DELETE | `/tmf-api/aim915/aiModel/{id}` | Delete AiModel |
| GET | `/tmf-api/aim915/aiModel?state=ACTIVE` | Find by state |

### Service API (TMF638)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/tmf-api/serviceInventoryManagement/v4/service` | List services |
| GET | `/tmf-api/serviceInventoryManagement/v4/service/{id}` | Get service with relationships |

## Querying Services with AI Models

To find all services that use AI models:

```bash
# Find services with 'uses-aimodel' relationship
GET /tmf-api/serviceInventoryManagement/v4/service?serviceRelationship.relationshipType=uses-aimodel
```

To get the linked AiModel from a service:

```bash
# Get the service
GET /tmf-api/serviceInventoryManagement/v4/service/{serviceId}

# Response includes:
{
  "serviceRelationship": [
    {
      "relationshipType": "uses-aimodel",
      "service": {
        "id": "aimodel-uuid-here",
        "@referredType": "AiModel"
      }
    }
  ],
  "serviceCharacteristic": [
    {
      "name": "inference_endpoint",
      "value": { "value": "http://docker-host:5001/invocations" }
    }
  ]
}

# Then get the AiModel details
GET /tmf-api/aim915/aiModel/{aimodel-uuid-here}
```

## Testing

### 1. Create ServiceSpecification

```bash
curl -X POST http://localhost:13082/tmf-api/serviceCatalogManagement/v4/serviceSpecification \
  -H "Content-Type: application/json" \
  -d @ai-fraud-detection-spec.json
```

### 2. Create LCM Rules

Attach LCM rules via the Portal UI or API.

### 3. Create Service Order

```bash
curl -X POST http://localhost:13082/tmf-api/serviceOrdering/v4/serviceOrder \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Order AI Fraud Detection Service",
    "orderItem": [
      {
        "action": "add",
        "service": {
          "serviceSpecification": {
            "id": "your-spec-id"
          },
          "serviceCharacteristic": [
            {
              "name": "mlflow_model_name",
              "value": { "value": "fraud-detection-model" }
            },
            {
              "name": "mlflow_model_version", 
              "value": { "value": "1" }
            }
          ]
        }
      }
    ]
  }'
```

### 4. Verify Deployment

```bash
# Check Service Order status
GET /tmf-api/serviceOrdering/v4/serviceOrder/{orderId}

# Check Service with AI Model relationship
GET /tmf-api/serviceInventoryManagement/v4/service/{serviceId}

# Check AiModel status
GET /tmf-api/aim915/aiModel/{aiModelId}
```

## Troubleshooting

### Common Issues

1. **AI Model stuck in RESERVED state**
   - Check Docker connectivity
   - Verify MLflow model exists
   - Check `AiModelLifecycleService` logs

2. **Service Order fails during PRE_PROVISION**
   - Verify Camel routes are configured
   - Check message broker connectivity
   - Review LCM rule syntax

3. **Container stops but AiModel state not updated**
   - Check `@EnableScheduling` is configured
   - Verify scheduled task is running (30-second interval)
   - Check Docker API connectivity

### Logs to Monitor

```bash
# OSOM logs
tail -f org.etsi.osl.osom/logs/osom.log | grep -E "(AI|LCM|ServiceOrder)"

# TMF API logs  
tail -f org.etsi.osl.tmf.api/logs/tmfapi.log | grep -E "(AiModel|Mlflow|Lifecycle)"
```

## Future Enhancements

1. **AiModelRef class** - Create dedicated reference class for cleaner model
2. **ServiceSpecRelationship** - Define AI model requirements at spec level
3. **Auto-scaling** - Scale AI model containers based on load
4. **Model versioning** - Support hot-swapping model versions
5. **Health checks** - Enhanced container health monitoring

## Related Documentation

- [TMF638 Service Inventory Management](https://www.tmforum.org/resources/specification/tmf638-service-inventory-api-rest-specification-r19-0-0/)
- [TMF915 AI Model Management](https://www.tmforum.org/resources/specification/tmf915-ai-model-management-api/)
- [OpenSlice Documentation](https://openslice.readthedocs.io/)
- [MLflow Model Serving](https://mlflow.org/docs/latest/models.html#built-in-deployment-tools)
