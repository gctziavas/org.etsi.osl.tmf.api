
package org.etsi.osl.tmf.gsm674.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.gsm674.model.GeographicSiteAttributeValueChangeEvent;
import org.etsi.osl.tmf.gsm674.model.GeographicSiteCreateEvent;
import org.etsi.osl.tmf.gsm674.model.GeographicSiteDeleteEvent;
import org.etsi.osl.tmf.gsm674.model.GeographicSiteStateChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@Generated
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-04-24T14:24:54.867613034Z[Etc/UTC]", comments = "Generator version: 7.6.0-SNAPSHOT")
@Validated
@Tag(name = "notification listener", description = "Notifications for Resource Lifecycle and event notifications")
public interface ListenerApi {
    Logger log = LoggerFactory.getLogger(ListenerApi.class);
    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }
    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }
    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }
    /**
     * POST /listener/geographicSiteAttributeValueChangeEvent : Client listener for entity GeographicSiteCreateEvent
     * Example of a client listener for receiving the notification GeographicSiteAttributeValueChangeEvent
     *
     * @param geographicSiteAttributeValueChangeEvent The event data (required)
     * @return Notified (status code 204)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "geographicSiteAttributeValueChangeEvent",
        summary = "Client listener for entity GeographicSiteCreateEvent",
        description = "Example of a client listener for receiving the notification GeographicSiteAttributeValueChangeEvent",
        tags = { "notification listener" },
        responses = {
            @ApiResponse(responseCode = "204", description = "Notified"),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/listener/geographicSiteAttributeValueChangeEvent",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    default ResponseEntity<Void> geographicSiteAttributeValueChangeEvent(
        @Parameter(name = "GeographicSiteAttributeValueChangeEvent", description = "The event data", required = true) @Valid @RequestBody GeographicSiteAttributeValueChangeEvent geographicSiteAttributeValueChangeEvent
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"reason\" : \"reason\", \"code\" : \"code\", \"@baseType\" : \"@baseType\", \"@type\" : \"@type\", \"@schemaLocation\" : \"@schemaLocation\", \"message\" : \"message\", \"referenceError\" : \"referenceError\", \"status\" : \"status\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * POST /listener/geographicSiteCreateEvent : Client listener for entity GeographicSiteCreateEvent
     * Example of a client listener for receiving the notification GeographicSiteCreateEvent
     *
     * @param geographicSiteCreateEvent The event data (required)
     * @return Notified (status code 204)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "geographicSiteCreateEvent",
        summary = "Client listener for entity GeographicSiteCreateEvent",
        description = "Example of a client listener for receiving the notification GeographicSiteCreateEvent",
        tags = { "notification listener" },
        responses = {
            @ApiResponse(responseCode = "204", description = "Notified"),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/listener/geographicSiteCreateEvent",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    default ResponseEntity<Void> geographicSiteCreateEvent(
        @Parameter(name = "GeographicSiteCreateEvent", description = "The event data", required = true) @Valid @RequestBody GeographicSiteCreateEvent geographicSiteCreateEvent
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"reason\" : \"reason\", \"code\" : \"code\", \"@baseType\" : \"@baseType\", \"@type\" : \"@type\", \"@schemaLocation\" : \"@schemaLocation\", \"message\" : \"message\", \"referenceError\" : \"referenceError\", \"status\" : \"status\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * POST /listener/geographicSiteDeleteEvent : Client listener for entity GeographicSiteCreateEvent
     * Example of a client listener for receiving the notification GeographicSiteDeleteEvent
     *
     * @param geographicSiteDeleteEvent The event data (required)
     * @return Notified (status code 204)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "geographicSiteDeleteEvent",
        summary = "Client listener for entity GeographicSiteCreateEvent",
        description = "Example of a client listener for receiving the notification GeographicSiteDeleteEvent",
        tags = { "notification listener" },
        responses = {
            @ApiResponse(responseCode = "204", description = "Notified"),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/listener/geographicSiteDeleteEvent",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    default ResponseEntity<Void> geographicSiteDeleteEvent(
        @Parameter(name = "GeographicSiteDeleteEvent", description = "The event data", required = true) @Valid @RequestBody GeographicSiteDeleteEvent geographicSiteDeleteEvent
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"reason\" : \"reason\", \"code\" : \"code\", \"@baseType\" : \"@baseType\", \"@type\" : \"@type\", \"@schemaLocation\" : \"@schemaLocation\", \"message\" : \"message\", \"referenceError\" : \"referenceError\", \"status\" : \"status\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * POST /listener/geographicSiteStateChangeEvent : Client listener for entity GeographicSiteCreateEvent
     * Example of a client listener for receiving the notification GeographicSiteStateChangeEvent
     *
     * @param geographicSiteStateChangeEvent The event data (required)
     * @return Notified (status code 204)
     *         or Error (status code 200)
     */
    @Operation(
        operationId = "geographicSiteStateChangeEvent",
        summary = "Client listener for entity GeographicSiteCreateEvent",
        description = "Example of a client listener for receiving the notification GeographicSiteStateChangeEvent",
        tags = { "notification listener" },
        responses = {
            @ApiResponse(responseCode = "204", description = "Notified"),
            @ApiResponse(responseCode = "default", description = "Error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/listener/geographicSiteStateChangeEvent",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    default ResponseEntity<Void> geographicSiteStateChangeEvent(
        @Parameter(name = "GeographicSiteStateChangeEvent", description = "The event data", required = true) @Valid @RequestBody GeographicSiteStateChangeEvent geographicSiteStateChangeEvent
    ) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"reason\" : \"reason\", \"code\" : \"code\", \"@baseType\" : \"@baseType\", \"@type\" : \"@type\", \"@schemaLocation\" : \"@schemaLocation\", \"message\" : \"message\", \"referenceError\" : \"referenceError\", \"status\" : \"status\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
