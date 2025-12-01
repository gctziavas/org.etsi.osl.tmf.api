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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.EventSubscriptionInput;
import org.etsi.osl.tmf.scm633.reposervices.EventSubscriptionRepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
@jakarta.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-04-29T19:18:54.771Z")

@Controller("HubApiController633")
@RequestMapping("/serviceCatalogManagement/v4/")
public class HubApiController implements HubApi {

    private static final Logger log = LoggerFactory.getLogger(HubApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired
    @Qualifier("scm633EventSubscriptionRepoService")
    EventSubscriptionRepoService eventSubscriptionRepoService;

    @org.springframework.beans.factory.annotation.Autowired
    public HubApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<EventSubscription> registerListener(@Parameter(description = "Data containing the callback endpoint to deliver the information" ,required=true )  @Valid @RequestBody EventSubscriptionInput data) {
        try {
            EventSubscription eventSubscription = eventSubscriptionRepoService.addEventSubscription(data);
            return new ResponseEntity<>(eventSubscription, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for listener registration: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error registering listener", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @RequestMapping(value = "/hub/{id}", method = RequestMethod.DELETE, produces = { "application/json;charset=utf-8" })
    public ResponseEntity<Void> unregisterListener(@Parameter(description = "The id of the registered listener",required=true) @PathVariable("id") String id) {
        try {
            EventSubscription existing = eventSubscriptionRepoService.findById(id);
            if (existing == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            eventSubscriptionRepoService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error unregistering listener with id: " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<EventSubscription>> getListeners() {
        try {
            List<EventSubscription> eventSubscriptions = eventSubscriptionRepoService.findAll();
            return new ResponseEntity<>(eventSubscriptions, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving listeners", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
