/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2024 openslice.io
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

package org.etsi.osl.tmf.ram702.api;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceUpdate;
import org.etsi.osl.tmf.ri639.reposervices.ResourceRepoService;
import org.etsi.osl.tmf.util.AddUserAsOwnerToRelatedParties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.etsi.osl.model.nfv.UserRoleType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controller class that implements the Resource Activation API.
 * Handles HTTP requests for creating, retrieving, updating, and deleting resources.
 */
@Controller
@RequestMapping("/ResourceActivationAndConfiguration/v4/")
public class ResourceActivationApiController implements ResourceActivationApi {

    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Autowired
    private ResourceRepoService resourceRepoService;


    /**
     * Constructs a new ResourceActivationApiController with the specified ObjectMapper and HttpServletRequest.
     *
     * @param objectMapper the ObjectMapper for JSON processing
     * @param request      the HttpServletRequest for accessing request data
     */
    @Autowired
    public ResourceActivationApiController(
		ObjectMapper objectMapper,
		HttpServletRequest request
	) {
        this.objectMapper = objectMapper;
        this.request = request;
    }


    /**
     * Creates a new resource.
     *
     * @param principal the security principal of the user making the request
     * @param resource  the resource to create
     * @return a ResponseEntity containing the created resource and HTTP status
     */
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    @Override
    public ResponseEntity<Resource> createResource(
        Principal principal,
        @Valid ResourceCreate resource
    ) {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                resource.setRelatedParty(
                    AddUserAsOwnerToRelatedParties.addUser(
                        principal.getName(),
                        principal.getName(),
                        UserPartRoleType.REQUESTER,
                        "",
                        resource.getRelatedParty()
                    )
                );

                Resource createdResource = resourceRepoService.addResource(resource);
                return new ResponseEntity<>(createdResource, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("Error creating resource", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error for other exceptions
        }
    }


    /**
     * Deletes a resource by its identifier.
     *
     * @param id the identifier of the resource to delete
     * @return a ResponseEntity with the appropriate HTTP status
     */
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    @Override
    public ResponseEntity<Void> deleteResource(String id) {
        try {

			return new ResponseEntity<Void>( resourceRepoService.deleteByUuid(id), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Couldn't serialize response for content type application/json", e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }


    /**
     * Lists resources based on the provided parameters.
     *
     * @param principal the security principal of the user making the request
     * @param fields    the fields to include in the response
     * @param offset    the offset for pagination
     * @param limit     the maximum number of resources to return
     * @param allParams additional parameters for filtering
     * @return a ResponseEntity containing the list of resources and HTTP status
     */
    @SuppressWarnings("unchecked")
	@PreAuthorize("hasAnyAuthority('ROLE_USER')")
    @Override
    public ResponseEntity<List<Resource>> listResource(
        Principal principal,
        @Valid String fields,
        @Valid Integer offset,
        @Valid Integer limit,
        Map<String, String> allParams
    ) {
        try {
            List<Resource> resources = resourceRepoService.findAll(
                principal.getName(), UserPartRoleType.REQUESTER
            );
            return new ResponseEntity<>(resources, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error listing resources", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Updates an existing resource by partially updating its fields.
     *
     * @param principal the security principal of the user making the request
     * @param resource  the resource data to update
     * @param id        the identifier of the resource to update
     * @return a ResponseEntity containing the updated resource and HTTP status
     */
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    @Override
    public ResponseEntity<Resource> patchResource(
        Principal principal,
        @Valid ResourceUpdate resource,
        String id
    ) {
        Resource c = resourceRepoService.updateResource(id, resource, true);

		return new ResponseEntity< Resource >(c, HttpStatus.OK);
    }


    /**
     * Retrieves a resource by its identifier.
     *
     * @param principal the security principal of the user making the request
     * @param id        the identifier of the resource to retrieve
     * @param fields    the fields to include in the response
     * @return a ResponseEntity containing the resource and HTTP status
     */
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    @Override
    public ResponseEntity<Resource> retrieveResource(
        Principal principal,
        String id,
        @Valid String fields
    ) {
        try {

			return new ResponseEntity<Resource>( resourceRepoService.findByUuid(id), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Couldn't serialize response for content type application/json", e);
			return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
}

