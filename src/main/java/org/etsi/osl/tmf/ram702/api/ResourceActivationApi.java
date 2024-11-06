/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2024 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package org.etsi.osl.tmf.ram702.api;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.etsi.osl.tmf.ram702.utils.JSONResponseUtils;

@Tag(name = "resource", description = "The resource Activation API")
public interface ResourceActivationApi {

    Logger log = LoggerFactory.getLogger(ResourceActivationApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @Operation(
        summary = "Creates a Resource",
        operationId = "createResource",
        description = "This operation creates a Resource entity.",
        tags = {"resource"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "405", description = "Method Not allowed"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
        value = "/resource",
        produces = {"application/json;charset=utf-8"},
        consumes = {"application/json;charset=utf-8"},
        method = RequestMethod.POST
    )
    default ResponseEntity<Resource> createResource(
        Principal principal,
        @Parameter(description = "The Resource to be created", required = true)
        @Valid @RequestBody ResourceCreate body
    ) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(
                        getObjectMapper().get().readValue(
                            JSONResponseUtils.RESOURCE_JSON_RESPONSE, Resource.class
                        ),
                        HttpStatus.NOT_IMPLEMENTED
                    );
                } catch (IOException e) {
                    log.error(
                        "Couldn't serialize response for content type application/json", e
                    );
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn(
                "ObjectMapper or HttpServletRequest not configured in default ResourceActivationApi interface so no example is generated"
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
        summary = "Deletes a Resource",
        operationId = "deleteResource",
        description = "This operation deletes a Resource entity.",
        tags = {"resource"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "405", description = "Method Not allowed"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
        value = "/resource/{id}",
        produces = {"application/json;charset=utf-8"},
        method = RequestMethod.DELETE
    )
    default ResponseEntity<Void> deleteResource(
        @Parameter(description = "Identifier of the Resource", required = true)
        @PathVariable("id") String id
    ) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            // Implementation goes here
        } else {
            log.warn(
                "ObjectMapper or HttpServletRequest not configured in default ResourceActivationApi interface so no example is generated"
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
        summary = "List or find Resource objects",
        operationId = "listResource",
        description = "This operation lists or finds Resource entities.",
        tags = {"resource"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "405", description = "Method Not allowed"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
        value = "/resource",
        produces = {"application/json;charset=utf-8"},
        method = RequestMethod.GET
    )
    default ResponseEntity<List<Resource>> listResource(
        Principal principal,
        @Parameter(description = "Comma-separated properties to be provided in response")
        @Valid @RequestParam(value = "fields", required = false) String fields,
        @Parameter(description = "Requested index for start of resources to be provided in response")
        @Valid @RequestParam(value = "offset", required = false) Integer offset,
        @Parameter(description = "Requested number of resources to be provided in response")
        @Valid @RequestParam(value = "limit", required = false) Integer limit,
        @Parameter(hidden = true) @Valid @RequestParam Map<String, String> allParams
    ) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(
                        getObjectMapper().get().readValue(
                            JSONResponseUtils.RESOURCE_LIST_JSON_RESPONSE,
                            new TypeReference<List<Resource>>() {}
                        ),
                        HttpStatus.NOT_IMPLEMENTED
                    );
                } catch (IOException e) {
                    log.error(
                        "Couldn't serialize response for content type application/json", e
                    );
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn(
                "ObjectMapper or HttpServletRequest not configured in default ResourceActivationApi interface so no example is generated"
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
        summary = "Updates partially a Resource",
        operationId = "patchResource",
        description = "This operation updates partially a Resource entity.",
        tags = {"resource"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "405", description = "Method Not allowed"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
        value = "/resource/{id}",
        produces = {"application/json;charset=utf-8"},
        consumes = {"application/json;charset=utf-8"},
        method = RequestMethod.PATCH
    )
    default ResponseEntity<Resource> patchResource(
        Principal principal,
        @Parameter(description = "The Resource to be updated", required = true)
        @Valid @RequestBody ResourceUpdate body,
        @Parameter(description = "Identifier of the Resource", required = true)
        @PathVariable("id") String id
    ) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(
                        getObjectMapper().get().readValue(
                            JSONResponseUtils.RESOURCE_JSON_RESPONSE, Resource.class
                        ),
                        HttpStatus.NOT_IMPLEMENTED
                    );
                } catch (IOException e) {
                    log.error(
                        "Couldn't serialize response for content type application/json", e
                    );
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn(
                "ObjectMapper or HttpServletRequest not configured in default ResourceActivationApi interface so no example is generated"
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(
        summary = "Retrieves a Resource by ID",
        operationId = "retrieveResource",
        description = "This operation retrieves a Resource entity. Attribute selection is enabled for all first-level attributes.",
        tags = {"resource"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "405", description = "Method Not allowed"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
        value = "/resource/{id}",
        produces = {"application/json;charset=utf-8"},
        method = RequestMethod.GET
    )
    default ResponseEntity<Resource> retrieveResource(
        Principal principal,
        @Parameter(description = "Identifier of the Resource", required = true)
        @PathVariable("id") String id,
        @Parameter(description = "Comma-separated properties to provide in response")
        @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        if (getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(
                        getObjectMapper().get().readValue(
                            JSONResponseUtils.RESOURCE_JSON_RESPONSE, Resource.class
                        ),
                        HttpStatus.NOT_IMPLEMENTED
                    );
                } catch (IOException e) {
                    log.error(
                        "Couldn't serialize response for content type application/json", e
                    );
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn(
                "ObjectMapper or HttpServletRequest not configured in default ResourceActivationApi interface so no example is generated"
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
