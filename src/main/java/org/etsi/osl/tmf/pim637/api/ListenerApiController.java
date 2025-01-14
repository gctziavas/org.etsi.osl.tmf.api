package org.etsi.osl.tmf.pim637.api;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.pim637.model.EventSubscription;
import org.etsi.osl.tmf.pim637.model.ProductAttributeValueChangeEvent;
import org.etsi.osl.tmf.pim637.model.ProductBatchEvent;
import org.etsi.osl.tmf.pim637.model.ProductCreateEvent;
import org.etsi.osl.tmf.pim637.model.ProductDeleteEvent;
import org.etsi.osl.tmf.pim637.model.ProductStateChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2024-08-04T00:27:07.324017400+03:00[Europe/Athens]")
@Controller("ListenerApiController637")
@RequestMapping("/productInventoryManagement/v4/")
public class ListenerApiController implements ListenerApi {

    private static final Logger log = LoggerFactory.getLogger(ListenerApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public ListenerApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<EventSubscription> listenToProductAttributeValueChangeEvent(@Parameter(in = ParameterIn.DEFAULT, description = "The event data", required=true, schema=@Schema()) @Valid @RequestBody ProductAttributeValueChangeEvent body
) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<EventSubscription>(objectMapper.readValue("{\r\n  \"query\" : \"query\",\r\n  \"callback\" : \"callback\",\r\n  \"id\" : \"id\"\r\n}", EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<EventSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<EventSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<EventSubscription> listenToProductBatchEvent(@Parameter(in = ParameterIn.DEFAULT, description = "The event data", required=true, schema=@Schema()) @Valid @RequestBody ProductBatchEvent body
) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<EventSubscription>(objectMapper.readValue("{\r\n  \"query\" : \"query\",\r\n  \"callback\" : \"callback\",\r\n  \"id\" : \"id\"\r\n}", EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<EventSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<EventSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<EventSubscription> listenToProductCreateEvent(@Parameter(in = ParameterIn.DEFAULT, description = "The event data", required=true, schema=@Schema()) @Valid @RequestBody ProductCreateEvent body
) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<EventSubscription>(objectMapper.readValue("{\r\n  \"query\" : \"query\",\r\n  \"callback\" : \"callback\",\r\n  \"id\" : \"id\"\r\n}", EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<EventSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<EventSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<EventSubscription> listenToProductDeleteEvent(@Parameter(in = ParameterIn.DEFAULT, description = "The event data", required=true, schema=@Schema()) @Valid @RequestBody ProductDeleteEvent body
) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<EventSubscription>(objectMapper.readValue("{\r\n  \"query\" : \"query\",\r\n  \"callback\" : \"callback\",\r\n  \"id\" : \"id\"\r\n}", EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<EventSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<EventSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<EventSubscription> listenToProductStateChangeEvent(@Parameter(in = ParameterIn.DEFAULT, description = "The event data", required=true, schema=@Schema()) @Valid @RequestBody ProductStateChangeEvent body
) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<EventSubscription>(objectMapper.readValue("{\r\n  \"query\" : \"query\",\r\n  \"callback\" : \"callback\",\r\n  \"id\" : \"id\"\r\n}", EventSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<EventSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<EventSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

}
