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
package org.etsi.osl.tmf.scm633.api;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogBatchNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
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
@jakarta.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-04-29T19:18:54.771Z")

@Controller("ListenerApiController633")
@RequestMapping("/serviceCatalogManagement/v4/")
public class ListenerApiController implements ListenerApi {

    private static final Logger log = LoggerFactory.getLogger(ListenerApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public ListenerApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.ofNullable(objectMapper);
    }

    public Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCandidateChangeNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCandidateChangeNotification data) {
        try {
            log.info("Received ServiceCandidateChangeNotification: {}", data.getEventId());
            log.debug("ServiceCandidateChangeNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCandidateChangeNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCandidateCreateNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCandidateCreateNotification data) {
        try {
            log.info("Received ServiceCandidateCreateNotification: {}", data.getEventId());
            log.debug("ServiceCandidateCreateNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCandidateCreateNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCandidateDeleteNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCandidateDeleteNotification data) {
        try {
            log.info("Received ServiceCandidateDeleteNotification: {}", data.getEventId());
            log.debug("ServiceCandidateDeleteNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCandidateDeleteNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCatalogBatchNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCatalogBatchNotification data) {
        try {
            log.info("Received ServiceCatalogBatchNotification: {}", data.getEventId());
            log.debug("ServiceCatalogBatchNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCatalogBatchNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCatalogChangeNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCatalogChangeNotification data) {
        try {
            log.info("Received ServiceCatalogChangeNotification: {}", data.getEventId());
            log.debug("ServiceCatalogChangeNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCatalogChangeNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCatalogCreateNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCatalogCreateNotification data) {
        try {
            log.info("Received ServiceCatalogCreateNotification: {}", data.getEventId());
            log.debug("ServiceCatalogCreateNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCatalogCreateNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCatalogDeleteNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCatalogDeleteNotification data) {
        try {
            log.info("Received ServiceCatalogDeleteNotification: {}", data.getEventId());
            log.debug("ServiceCatalogDeleteNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCatalogDeleteNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCategoryChangeNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCategoryChangeNotification data) {
        try {
            log.info("Received ServiceCategoryChangeNotification: {}", data.getEventId());
            log.debug("ServiceCategoryChangeNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCategoryChangeNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCategoryCreateNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCategoryCreateNotification data) {
        try {
            log.info("Received ServiceCategoryCreateNotification: {}", data.getEventId());
            log.debug("ServiceCategoryCreateNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCategoryCreateNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceCategoryDeleteNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceCategoryDeleteNotification data) {
        try {
            log.info("Received ServiceCategoryDeleteNotification: {}", data.getEventId());
            log.debug("ServiceCategoryDeleteNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceCategoryDeleteNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceSpecificationChangeNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceSpecificationChangeNotification data) {
        try {
            log.info("Received ServiceSpecificationChangeNotification: {}", data.getEventId());
            log.debug("ServiceSpecificationChangeNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceSpecificationChangeNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceSpecificationCreateNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceSpecificationCreateNotification data) {
        try {
            log.info("Received ServiceSpecificationCreateNotification: {}", data.getEventId());
            log.debug("ServiceSpecificationCreateNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceSpecificationCreateNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EventSubscription> listenToServiceSpecificationDeleteNotification(@Parameter(description = "The event data" ,required=true )  @Valid @RequestBody ServiceSpecificationDeleteNotification data) {
        try {
            log.info("Received ServiceSpecificationDeleteNotification: {}", data.getEventId());
            log.debug("ServiceSpecificationDeleteNotification details: {}", data);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing ServiceSpecificationDeleteNotification", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
