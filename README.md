# org.etsi.osl.tmf.api

TMF OpenAPIs implementation

## OpenAPI Generator Guide and TMF APIs Development Guide

### Useful links

* [OpenApi Generator](https://openapi-generator.tech/)
* [Documentation for the spring Generator](https://openapi-generator.tech/docs/generators/spring/)
* [Online swagger editor](https://editor.swagger.io/)
* [TM Forum](https://www.tmforum.org/)
* [TM Forum Resources](https://www.tmforum.org/resources/)
* [TM Forum Open API table](https://www.tmforum.org/oda/open-apis/table)

### Introduction

TM Forum is an alliance of 800+ global companies working together to break down technology and cultural barriers. One of their prominent contributions is the TMF Open APIs. Their goal is to create a unified set of endpoints to implement TM Forum's ODA.

### How to generate a TMF API

1) Visit the [TM Forum Resources](https://www.tmforum.org/resources/) and search for the API you intend to generate. There you will find the REST Specification guidelines.

2) Visit the [TM Forum Open API table](https://www.tmforum.org/oda/open-apis/table) to find the swagger file that is related to your API and download it.

3) Use the downloaded JSON to generate the code.

> **IMPORTANT NOTE:** The source of truth is always the OpenAPI tables and not the TMF github.

### Example with using docker

```
docker run --rm \
-v ${PWD}:/local openapitools/openapi-generator-cli generate \
-i /local/TMF628_Performance_Management_API_v5.0.0.oas.yaml \
-g spring \
-o /local/ \

--additional-properties=dateLibrary=java8,developerOrganizationUrl=https://osl.etsi.org,parentArtifactId=org.etsi.osl.main,parentGroupId=org.etsi.osl,parentVersion=1.0.0-SNAPSHOT,artifactId=org.etsi.osl.TMF628,developerName=<YOUR_NAME>,developerEmail=<YOUR_EMAIL>

```

**Explanation:**

* {PWD}:/local --> The directory you are currently in

* -i /local/TMF628-Performance-v4.0.0.swagger.json --> The OpenAPI Specification used for the code generation
* -g spring --> The generator that was used
* -o /local/out --> Where the code will be generated
* --additional-properties --> More info at [Documentation for the spring Generator](https://openapi-generator.tech/docs/generators/spring/)

## After code generation

After code generation a scafold of the project will be made, including the **model** and the **APIs**.

> **IMPORTANT:** Do not delete parts of the model or parts of the API so that the generated project will be maintainable and extendable in the future.

### Split the model and the API
  
### Add custom names to the entities

This is mandatory to be done in Entities with big names (multiple characters). Big table names (consider tables also joining) lead to JPA errors, and reserved words lead to conflicts

@Entity(name= )

@Column(name= )

### Best practices & Tips

* Name the entities after the concatenation of the TMF API’s abbreviated name and the entity’s class name (e.g. for class Resource of PM628 TMF API use the name “PM628_Resource”) to avoid conflicts.

* Check the “common” model and other existing model implementations for common classes that can be imported from there to avoid code duplication.

* For entities extend BaseEntity, BaseNamedEntity, or BaseRootNamedEntity when possible.

* If a class that needs to be an entity has an “id” attribute, check the TMF API’s documentation about whether this id is used for the entity instance identifier or for another purpose. In the first case, you can change it to “uuid” to use the inheritance as described in the above tip.

* Follow the conventions for href, schemaLocation etc with “@“ as in existing TMF API implementations in OSL code

* Use the MapStruct mapper for automatic mapping of the values to objects in services. For nested and complex models, custom code will be needed. Check PM628 for reference

* v5 of TMP API has some differences from previous versions

* Create and Update are now FVO and MVO

* Models have nested models as attribute types, making it more difficult to use inheritance between FVO and MVO

* Write unit tests to ensure the code is working properly and to increase the test coverage percentage.
