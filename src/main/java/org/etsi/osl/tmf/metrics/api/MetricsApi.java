package org.etsi.osl.tmf.metrics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.etsi.osl.tmf.metrics.PublishedServiceSpecifications;
import org.etsi.osl.tmf.metrics.RegisteredIndividuals;
import org.etsi.osl.tmf.metrics.RegisteredResourceSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Tag(name = "MetricsApi")
public interface MetricsApi {

    Logger log = LoggerFactory.getLogger(MetricsApi.class);

    @Operation(summary = "Get total number of registered individuals", operationId = "getRegisteredIndividuals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/registeredIndividuals", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<RegisteredIndividuals> getRegisteredIndividuals();

    @Operation(summary = "Get total number of published service specifications", operationId = "getPublishedServiceSpecifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/publishedServiceSpecifications", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<PublishedServiceSpecifications> getPublishedServiceSpecifications();

    @Operation(summary = "Get total number of registered resource specifications", operationId = "getRegisteredResourceSpecifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/registeredResourceSpecifications", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<RegisteredResourceSpecifications> getRegisteredResourceSpecifications();
}
