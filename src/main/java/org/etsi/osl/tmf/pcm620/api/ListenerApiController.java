/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 openslice.io
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
package org.etsi.osl.tmf.pcm620.api;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.pcm620.model.CatalogBatchEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
@jakarta.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-10-19T00:15:57.249+03:00")

@Controller("ListenerApiController620")
@RequestMapping("/productCatalogManagement/v4/")
public class ListenerApiController implements ListenerApi {

    private static final Logger log = LoggerFactory.getLogger(ListenerApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public ListenerApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.ofNullable(objectMapper);
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<EventSubscription> listenToCatalogBatchEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody CatalogBatchEvent data) {
        try {
            log.info("Received CatalogBatchEvent: {}", data.getEventId());
            log.debug("CatalogBatchEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing CatalogBatchEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToCatalogCreateEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody CatalogCreateEvent data) {
        try {
            log.info("Received CatalogCreateEvent: {}", data.getEventId());
            log.debug("CatalogCreateEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing CatalogCreateEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToCatalogDeleteEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody CatalogDeleteEvent data) {
        try {
            log.info("Received CatalogDeleteEvent: {}", data.getEventId());
            log.debug("CatalogDeleteEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing CatalogDeleteEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToCategoryCreateEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody CategoryCreateEvent data) {
        try {
            log.info("Received CategoryCreateEvent: {}", data.getEventId());
            log.debug("CategoryCreateEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing CategoryCreateEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToCategoryDeleteEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody CategoryDeleteEvent data) {
        try {
            log.info("Received CategoryDeleteEvent: {}", data.getEventId());
            log.debug("CategoryDeleteEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing CategoryDeleteEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingAttributeValueChangeEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingAttributeValueChangeEvent data) {
        try {
            log.info("Received ProductOfferingAttributeValueChangeEvent: {}", data.getEventId());
            log.debug("ProductOfferingAttributeValueChangeEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingAttributeValueChangeEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingCreateEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingCreateEvent data) {
        try {
            log.info("Received ProductOfferingCreateEvent: {}", data.getEventId());
            log.debug("ProductOfferingCreateEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingCreateEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingDeleteEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingDeleteEvent data) {
        try {
            log.info("Received ProductOfferingDeleteEvent: {}", data.getEventId());
            log.debug("ProductOfferingDeleteEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingDeleteEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingPriceAttributeValueChangeEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingPriceAttributeValueChangeEvent data) {
        try {
            log.info("Received ProductOfferingPriceAttributeValueChangeEvent: {}", data.getEventId());
            log.debug("ProductOfferingPriceAttributeValueChangeEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingPriceAttributeValueChangeEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingPriceCreateEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingPriceCreateEvent data) {
        try {
            log.info("Received ProductOfferingPriceCreateEvent: {}", data.getEventId());
            log.debug("ProductOfferingPriceCreateEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingPriceCreateEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingPriceDeleteEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingPriceDeleteEvent data) {
        try {
            log.info("Received ProductOfferingPriceDeleteEvent: {}", data.getEventId());
            log.debug("ProductOfferingPriceDeleteEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingPriceDeleteEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingPriceStateChangeEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingPriceStateChangeEvent data) {
        try {
            log.info("Received ProductOfferingPriceStateChangeEvent: {}", data.getEventId());
            log.debug("ProductOfferingPriceStateChangeEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingPriceStateChangeEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductOfferingStateChangeEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductOfferingStateChangeEvent data) {
        try {
            log.info("Received ProductOfferingStateChangeEvent: {}", data.getEventId());
            log.debug("ProductOfferingStateChangeEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductOfferingStateChangeEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductSpecificationCreateEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductSpecificationCreateEvent data) {
        try {
            log.info("Received ProductSpecificationCreateEvent: {}", data.getEventId());
            log.debug("ProductSpecificationCreateEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductSpecificationCreateEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToProductSpecificationDeleteEvent(@Parameter(description = "The event data", required = true) @Valid @RequestBody ProductSpecificationDeleteEvent data) {
        try {
            log.info("Received ProductSpecificationDeleteEvent: {}", data.getEventId());
            log.debug("ProductSpecificationDeleteEvent details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ProductSpecificationDeleteEvent", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
