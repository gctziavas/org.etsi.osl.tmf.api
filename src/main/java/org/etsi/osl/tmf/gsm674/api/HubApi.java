package org.etsi.osl.tmf.gsm674.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.gsm674.model.EventSubscription;
import org.etsi.osl.tmf.gsm674.model.EventSubscriptionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;
import java.util.Optional;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-04-24T14:24:54.867613034Z[Etc/UTC]", comments = "Generator version: 7.6.0-SNAPSHOT")
@Validated
@Tag(name = "events subscription", description = "Endpoints to register and terminate an Event Listener")
public interface HubApi {
    Logger log = LoggerFactory.getLogger(HubApi.class);
    default Optional<ObjectMapper> getObjectMapper(){
        return Optional.empty();
    }

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    /**
     * POST /hub : Register a listener
     * Sets the communication endpoint address the service instance must use to deliver information about its health state, execution state, failures and metrics.
     *
     * @param eventSubscriptionInput Data containing the callback endpoint to deliver the information (required)
     * @return Notified (status code 201)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "registerListener",
        summary = "Register a listener",
        description = "Sets the communication endpoint address the service instance must use to deliver information about its health state, execution state, failures and metrics.",
        tags = { "events subscription" },
        responses = {
            @ApiResponse(responseCode = "201", description = "Notified", content = {
                @Content(mediaType = "application/json;charset=utf-8", schema = @Schema(implementation = EventSubscription.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = EventSubscription.class))
            }),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json;charset=utf-8", schema = @Schema(implementation = Error.class)),
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/hub",
        produces = { "application/json;charset=utf-8", "application/json" },
        consumes = { "application/json" }
    )
    
    default ResponseEntity<EventSubscription> registerListener(
        @Parameter(name = "EventSubscriptionInput", description = "Data containing the callback endpoint to deliver the information", required = true) @Valid @RequestBody EventSubscriptionInput eventSubscriptionInput
    ) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"query\" : \"query\",  \"callback\" : \"callback\",  \"id\" : \"id\"}",EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default HubApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * DELETE /hub/{id} : Unregister a listener
     * Resets the communication endpoint address the service instance must use to deliver information about its health state, execution state, failures and metrics.
     *
     * @param id Identifier of the Service (required)
     * @return Deleted (status code 204)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "unregisterListener",
        summary = "Unregister a listener",
        description = "Resets the communication endpoint address the service instance must use to deliver information about its health state, execution state, failures and metrics.",
        tags = { "events subscription" },
        responses = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/hub/{id}",
        produces = { "application/json" }
    )
    
    default ResponseEntity<Void> unregisterListener(
        @Parameter(name = "id", description = "Identifier of the Service", required = true, in = ParameterIn.PATH) @PathVariable("id") String id
    ) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default HubApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
