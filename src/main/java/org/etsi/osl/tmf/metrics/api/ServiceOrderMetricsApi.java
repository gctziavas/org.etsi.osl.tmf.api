package org.etsi.osl.tmf.metrics.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.metrics.ActiveServiceOrders;
import org.etsi.osl.tmf.metrics.ServiceOrdersGroupByDay;
import org.etsi.osl.tmf.metrics.ServiceOrdersGroupByState;
import org.etsi.osl.tmf.metrics.TotalServiceOrders;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
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
public interface ServiceOrderMetricsApi {

    Logger log = LoggerFactory.getLogger(ServiceOrderMetricsApi.class);

    @Operation(summary = "Get total number of service orders", operationId = "getTotalServiceOrders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/totalServiceOrders", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<TotalServiceOrders> getTotalServiceOrders(
            @Valid @RequestParam(value = "state", required = false) ServiceOrderStateType state
    );


    @Operation(summary = "Get total number of active service orders", operationId = "getTotalActiveServiceOrders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/activeServiceOrders", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<ActiveServiceOrders> getTotalActiveServiceOrders();


    @Operation(summary = "Get service orders grouped by day", operationId = "getServiceOrdersGroupedByDay")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/serviceOrdersGroupByDay", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<ServiceOrdersGroupByDay> getServiceOrdersGroupedByDay(
            @Valid @RequestParam(value = "starttime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime starttime,
            @Valid @RequestParam(value = "endtime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endtime
    );


    @Operation(summary = "Get service orders grouped by state", operationId = "getServiceOrdersGroupedByState")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/metrics/serviceOrdersGroupByState", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    ResponseEntity<ServiceOrdersGroupByState> getServiceOrdersGroupedByState(
            @Valid @RequestParam(value = "starttime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime starttime,
            @Valid @RequestParam(value = "endtime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endtime
    );
}
