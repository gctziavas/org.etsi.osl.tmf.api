package org.etsi.osl.tmf.metrics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.Map;

@Tag(name = "MetricsApi")
public interface MetricsApi {

    Logger log = LoggerFactory.getLogger(MetricsApi.class);

    @Operation(summary = "Get total number of resources", operationId = "getTotalResources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/totalResources", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<Map<String, Integer>> getTotalResources(
            @Valid @RequestParam(value = "state", required = false) ResourceStatusType state
    );

    @Operation(summary = "Get resources grouped by state", operationId = "getResourcesGroupedByState")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/resourcesGroupByState", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<Map<String, Object>> getResourcesGroupedByState(
            @Valid @RequestParam(value = "starttime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime starttime,
            @Valid @RequestParam(value = "endtime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endtime
    );
}
