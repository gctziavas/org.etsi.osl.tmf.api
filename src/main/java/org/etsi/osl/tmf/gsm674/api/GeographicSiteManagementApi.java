package org.etsi.osl.tmf.gsm674.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Generated
public interface GeographicSiteManagementApi {
    Logger log = LoggerFactory.getLogger(GeographicSiteManagementApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @Operation(summary = "Creates a 'GeographicSite'", operationId = "createGeographicSite", description = "", tags={ "geographicSite", })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Created" ),
            @ApiResponse(responseCode = "400", description = "Bad Request" ),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ),
            @ApiResponse(responseCode = "403", description = "Forbidden" ),
            @ApiResponse(responseCode = "404", description = "Not Found" ),
            @ApiResponse(responseCode = "405", description = "Method Not allowed" ),
            @ApiResponse(responseCode = "409", description = "Conflict" ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error" ) })
    @RequestMapping(value = "/geographicSite",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.POST)
    default ResponseEntity<GeographicSite> createGeographicSite(@Parameter(description = "The geographic site to be created" ,required=true )  @Valid @RequestBody GeographicSite geographicSite) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]}", GeographicSite.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default GeographicSiteManagementApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Operation(summary = "Deletes a 'GeographicSite' by Id", operationId = "deleteGeographicSite", description = "", tags={ "GeographicSite", })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted" ),
            @ApiResponse(responseCode = "400", description = "Bad Request" ),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ),
            @ApiResponse(responseCode = "403", description = "Forbidden" ),
            @ApiResponse(responseCode = "404", description = "Not Found" ),
            @ApiResponse(responseCode = "405", description = "Method Not allowed" ),
            @ApiResponse(responseCode = "409", description = "Conflict" ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error" ) })
    @RequestMapping(value = "/geographicSite/{id}",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.DELETE)
    default ResponseEntity<Void> deleteGeographicSite(@Parameter(description = "Identifier of the Geographic site",required=true) @PathVariable("id") String id) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default GeographicSiteManagementApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


//    @Operation(summary = "List or find 'GeographicSite' objects", operationId = "listGeographicSite", description = "" , tags={ "GeographicSite", })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode ="200", description = "Ok" ),
//            @ApiResponse(responseCode = "400", description = "Bad Request" ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized" ),
//            @ApiResponse(responseCode = "403", description = "Forbidden" ),
//            @ApiResponse(responseCode = "404", description = "Not Found" ),
//            @ApiResponse(responseCode = "405", description = "Method Not allowed" ),
//            @ApiResponse(responseCode = "409", description = "Conflict" ),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error" ) })
//    @RequestMapping(value = "/geographicSite",
//            produces = { "application/json" },
//            consumes = { "application/json" },
//            method = RequestMethod.GET)
//    default ResponseEntity<List<GeographicSite>> listGeographicSite(@Parameter(description = "Comma separated properties to display in response") @Valid @RequestParam(value = "fields", required = false) String fields, @Parameter(description = "Requested index for start of resources to be provided in response") @Valid @RequestParam(value = "offset", required = false) Integer offset, @Parameter(description = "Requested number of resources to be provided in response") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
//        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
//            if (getAcceptHeader().get().contains("application/json")) {
//                try {
//                    return new ResponseEntity<>(getObjectMapper().get().readValue("[ {  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]}, {  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
//                } catch (IOException e) {
//                    log.error("Couldn't serialize response for content type application/json", e);
//                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//                }
//            }
//        } else {
//            log.warn("ObjectMapper or HttpServletRequest not configured in default GeographicSiteManagementApi interface so no example is generated");
//        }
//        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
//    }


    @Operation(summary = "Updates partially a 'GeographicSite' by Id", operationId = "patchGeographicSite", description = "", tags={ "GeographicSite", })
    @ApiResponses(value = {
            @ApiResponse(responseCode ="200", description = "Updated" ),
            @ApiResponse(responseCode = "400", description = "Bad Request" ),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ),
            @ApiResponse(responseCode = "403", description = "Forbidden" ),
            @ApiResponse(responseCode = "404", description = "Not Found" ),
            @ApiResponse(responseCode = "405", description = "Method Not allowed" ),
            @ApiResponse(responseCode = "409", description = "Conflict" ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error" ) })
    @RequestMapping(value = "/geographicSite/{id}",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.PATCH)
    default ResponseEntity<GeographicSite> patchGeographicalSite(@Parameter(description = "Identifier of the Geographic site",required=true) @PathVariable("id") String id,@Parameter(description = "The Service Level Specification to be updated" ,required=true )  @Valid @RequestBody GeographicSite geographicSite) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]}", GeographicSite.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default GeographicSiteManagementApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @Operation(summary = "Retrieves a 'GeographicSite' by Id", operationId = "retrieveGeographicSite", description = "" , tags={ "GeographicSite", })
    @ApiResponses(value = {
            @ApiResponse(responseCode ="200", description = "Ok" ),
            @ApiResponse(responseCode = "400", description = "Bad Request" ),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ),
            @ApiResponse(responseCode = "403", description = "Forbidden" ),
            @ApiResponse(responseCode = "404", description = "Not Found" ),
            @ApiResponse(responseCode = "405", description = "Method Not allowed" ),
            @ApiResponse(responseCode = "409", description = "Conflict" ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error" ) })
    @RequestMapping(value = "/geographicSite/{id}",
            produces = { "application/json" },
            consumes = { "application/json" },
            method = RequestMethod.GET)
    default ResponseEntity<List<GeographicSite>> retrieveGeographicSite() {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("[ {  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]}, {  \"@baseType\" : \"@baseType\",  \"validFor\" : {    \"startDateTime\" : \"2000-01-23T04:56:07.000+00:00\",    \"endDateTime\" : \"2000-01-23T04:56:07.000+00:00\"  },  \"@type\" : \"@type\",  \"name\" : \"name\",  \"description\" : \"description\",  \"href\" : \"href\",  \"id\" : \"id\",  \"@schemaLocation\" : \"@schemaLocation\",  \"objective\" : [ {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  }, {    \"@referredType\" : \"@referredType\",    \"href\" : \"href\",    \"id\" : \"id\"  } ]} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default GeographicSiteManagementApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
