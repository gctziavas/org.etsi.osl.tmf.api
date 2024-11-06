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

package org.etsi.osl.tmf.ram702.reposervices;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.ram702.api.ApiException;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRef;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.etsi.osl.tmf.ram702.api.ResourceActivationApiRouteBuilderEvents;
import org.etsi.osl.tmf.ram702.api.ResourceNotFoundException;
import org.etsi.osl.tmf.ri639.model.Characteristic;
import org.etsi.osl.tmf.ri639.model.Feature;
import org.etsi.osl.tmf.ri639.model.LogicalResource;
import org.etsi.osl.tmf.ri639.model.PhysicalResource;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceAttributeValueChangeEvent;
import org.etsi.osl.tmf.ri639.model.ResourceAttributeValueChangeNotification;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceCreateEvent;
import org.etsi.osl.tmf.ri639.model.ResourceCreateNotification;
import org.etsi.osl.tmf.ri639.model.ResourceRelationship;
import org.etsi.osl.tmf.ri639.model.ResourceStateChangeEvent;
import org.etsi.osl.tmf.ri639.model.ResourceStateChangeNotification;
import org.etsi.osl.tmf.ri639.model.ResourceUpdate;
import org.etsi.osl.tmf.ram702.repo.ResourceActivationRepository;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Valid;

/**
 * Service class for managing Resource Activation operations.
 * Provides methods to add, update, delete, and retrieve resources.
 */
@org.springframework.stereotype.Service
public class ResourceActivationRepoService {

  private static final transient Log logger =
      LogFactory.getLog(ResourceActivationRepoService.class.getName());

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  ResourceActivationRepository resourceRepo;

  @Autowired
  ResourceSpecificationRepoService resourceSpecRepoService;

  private SessionFactory sessionFactory;

  @Autowired
  ResourceActivationApiRouteBuilderEvents resourceApiRouteBuilder;


   /**
   * Constructs the ResourceActivationRepoService with the given EntityManagerFactory.
   *
   * @param factory the EntityManagerFactory to use
   */
  @Autowired
  public ResourceActivationRepoService(EntityManagerFactory factory) {
    if (factory.unwrap(SessionFactory.class) == null) {
      throw new NullPointerException("factory is not a hibernate factory");
    }
    this.sessionFactory = factory.unwrap(SessionFactory.class);
  }


  /**
   * Retrieves all resources from the repository.
   *
   * @return a list of all resources
   */
  public List<Resource> findAll() {

    return (List<Resource>) this.resourceRepo.findAll();
  }

  /**
   * Finds resources by name and requester role.
   *
   * @param name      the name of the resource
   * @param requester the requester role
   * @return a list of resources matching the name and requester role
   */
  public List<Resource> findAll(String name, UserPartRoleType requester) {

    return (List<Resource>) this.resourceRepo.findByRolename(name);
  }


  /**
   * Adds a new resource to the repository.
   *
   * @param resource the resource to add
   * @return the added resource
   */
  public Resource addResource(@Valid ResourceCreate resource){
    logger.info("Will add Resource: " + resource.getName());

    Resource s;

    if (resource.getAtType() != null
        && resource.getAtType().toLowerCase().contains("physicalresource")) {
      s = new PhysicalResource();
    } else {
      s = new LogicalResource();
    }

    if (resource.getAtType() != null) {
      s.setType(resource.getAtType());
    }
    s.setName(resource.getName());
    s.setCategory(resource.getCategory());
    s.setDescription(resource.getDescription());
    
    if ( resource.getStartOperatingDate() == null ) {
      s.setStartOperatingDate(OffsetDateTime.now(ZoneOffset.UTC));
    } else {
      s.setStartOperatingDate(resource.getStartOperatingDate());
    }
    s.setEndOperatingDate(resource.getEndOperatingDate());
    s.setUsageState(resource.getUsageState());
    s.setResourceStatus(resource.getResourceStatus());
    s.setResourceVersion(resource.getResourceVersion());
    s.setOperationalState(resource.getOperationalState());
    s.setAdministrativeState(resource.getAdministrativeState());

    ResourceSpecificationRef thespecRef = new ResourceSpecificationRef();
    thespecRef.setId(resource.getResourceSpecification().getId());
    thespecRef.setName(resource.getResourceSpecification().getName());

    s.setResourceSpecification(thespecRef);

    if (resource.getPlace() != null) {
      s.setPlace(resource.getPlace());
    }
    if (resource.getNote() != null) {
      s.getNote().addAll(resource.getNote());
    }
    if (resource.getRelatedParty() != null) {
      s.getRelatedParty().addAll(resource.getRelatedParty());
    }
    if (resource.getResourceCharacteristic() != null) {
      s.getResourceCharacteristic().addAll(resource.getResourceCharacteristic());
    }
    if (resource.getResourceRelationship() != null) {
      s.getResourceRelationship().addAll(resource.getResourceRelationship());
    }
    if (resource.getAttachment() != null) {
      s.getAttachment().addAll(resource.getAttachment());
    }
    if (resource.getActivationFeature() != null) {
      s.getActivationFeature().addAll(resource.getActivationFeature());
    }

    Note noteItem = new Note();
    noteItem.setText("Resource status: " + s.getResourceStatus());
    noteItem.setAuthor("API");
    noteItem.setDate(OffsetDateTime.now(ZoneOffset.UTC));
    s.addNoteItem(noteItem);

    s = this.resourceRepo.save(s);

    raiseResourceCreateNotification(s);
    return s;
  }


  /**
   * Finds a resource by its UUID.
   *
   * @param id the UUID of the resource
   * @return the resource if found, or null if not found
   */
  @Transactional
  public Resource findByUuid(String id) throws ResourceNotFoundException {
    Optional<Resource> optionalCat = this.resourceRepo.findByUuid(id);
    
    if (optionalCat.isEmpty()) {
        throw new ResourceNotFoundException("Resource not found with UUID: " + id);
    }

    return optionalCat.get();
  }


  /**
   * Updates an existing resource with the given updates.
   *
   * @param id                        the UUID of the resource to update
   * @param resourceUpd               the resource update data
   * @param triggerServiceActionQueue whether to trigger service action queue
   * @return the updated resource
   */
  @Transactional
  public Resource updateResource(String id, @Valid ResourceUpdate resourceUpd,
      boolean triggerServiceActionQueue) throws ResourceNotFoundException {
    Resource resource = this.getResourceEager(id);

    if (resource == null) {
      throw new ResourceNotFoundException("Resource not found with UUID: " + id);
    }

    logger.info("Will update Resource: " + resource.getName());

    if (resourceUpd.getResourceRelationship() != null) {
      resource.setResourceRelationship(
        new LinkedHashSet<ResourceRelationship>(resourceUpd.getResourceRelationship()
      ));
    }

    if (resourceUpd.getAtType() != null) {
      resource.setType(resourceUpd.getAtType());
    }
    if (resourceUpd.getName() != null) {
      resource.setName(resourceUpd.getName());
    }
    if (resourceUpd.getCategory() != null) {
      resource.setCategory(resourceUpd.getCategory());
    }
    if (resourceUpd.getDescription() != null) {
      resource.setDescription(resourceUpd.getDescription());
    }
    if (resourceUpd.getStartOperatingDate() != null) {
      resource.setStartOperatingDate(resourceUpd.getStartOperatingDate());
    }
    if (resourceUpd.getEndOperatingDate() != null) {
      resource.setEndOperatingDate(resourceUpd.getEndOperatingDate());
    }
    if (resourceUpd.getUsageState() != null) {
      resource.setUsageState(resourceUpd.getUsageState());
    }

    boolean resourceStateChanged = false;
    if (resourceUpd.getResourceStatus() != null) {
      if (!resourceUpd.getResourceStatus().equals(resource.getResourceStatus())) {
        resourceStateChanged = true;
      }

      resource.setResourceStatus(resourceUpd.getResourceStatus());
    }
    if (resourceUpd.getResourceVersion() != null) {
      resource.setResourceVersion(resourceUpd.getResourceVersion());
    }
    if (resourceUpd.getOperationalState() != null) {
      resource.setOperationalState(resourceUpd.getOperationalState());
    }
    if (resourceUpd.getAdministrativeState() != null) {
      resource.setAdministrativeState(resourceUpd.getAdministrativeState());
    }
    if (resourceUpd.getResourceSpecification() != null) {
      resource.setResourceSpecification(resourceUpd.getResourceSpecification());
    }

    if (resourceUpd.getPlace() != null) {
      resource.setPlace(resourceUpd.getPlace());
    }

    if (resourceUpd.getNote() != null) {
      for (Note n : resourceUpd.getNote()) {
        if (n.getUuid() == null) {
          resource.addNoteItem(n);
        }
      }
    }

    if (resourceUpd.getRelatedParty() != null) {
      for (RelatedParty n : resourceUpd.getRelatedParty()) {
        if (n.getUuid() == null) {
          resource.addRelatedPartyItem(n);
        }
      }
    }

    boolean resourceCharacteristicChanged = false;
    String charsChanged="";
    if (resourceUpd.getResourceCharacteristic() != null) {
      for (Characteristic n : resourceUpd.getResourceCharacteristic()) {

        if (resource.getResourceCharacteristicByName(n.getName()) != null) {

          Characteristic origChar = resource.getResourceCharacteristicByName(n.getName());
          if ((origChar != null) && (origChar.getValue() != null) && (origChar.getValue().getValue() != null)) {
            if (!origChar.getValue().getValue().equals(n.getValue().getValue())) {
              resourceCharacteristicChanged = true;
              charsChanged = charsChanged + n.getName() + ",";
            }
          }

          resource.getResourceCharacteristicByName(n.getName())
              .setValue(new Any(n.getValue().getValue(), n.getValue().getAlias()));
        } else {
          resource.addResourceCharacteristicItem(n);
          resourceCharacteristicChanged = true;
          charsChanged = charsChanged + n.getName() + ",";
        }
      }
    }
    
    if (resourceCharacteristicChanged) {
      Note n = new Note();
      n.setText("Resource characteristics changed : " + charsChanged);
      n.setAuthor( "RAM702-API" );
      n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
      resource.addNoteItem( n );                  
      
    }
    
    if (resourceStateChanged) {
      Note n = new Note();
      n.setText("Resource resourceStateChanged changed to " + resource.getResourceStatus().toString() );
      n.setAuthor( "RAM702-API" );
      n.setDate( OffsetDateTime.now(ZoneOffset.UTC).toString() );
      resource.addNoteItem( n );                  
      
    }

    if (resourceUpd.getActivationFeature() != null) {
      for (Feature n : resourceUpd.getActivationFeature()) {
        if (n.getId() != null) {
          // we need to update this ?
        } else {
          resource.getActivationFeature().add(n);
        }

      }
    }
    
    resource = this.resourceRepo.save(resource);

    if (resourceCharacteristicChanged) {
      raiseResourceAttributeValueChangeEventNotification(resource);
    } else if (resourceStateChanged) {
      raiseResourceStateChangeEventNotification(resource);      
    } 
    return resource;
  }

  /**
   * Retrieves a resource by UUID and returns it as a JSON string.
   *
   * @param id the UUID of the resource
   * @return the resource as a JSON string
   * @throws JsonProcessingException if JSON processing fails
   * @throws ResourceNotFoundException if Resource not found
   */
  public String getResourceEagerAsString(String id) throws JsonProcessingException, ResourceNotFoundException {
    Resource s = this.getResourceEager(id);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Hibernate5JakartaModule());
    String res = mapper.writeValueAsString(s);

    return res;
  }


  /**
   * Retrieves a resource by UUID with all associations eagerly loaded.
   *
   * @param id the UUID of the resource
   * @return the resource with all associations initialized
   * @throws ResourceNotFoundException if Resource not found
   */
  public Resource getResourceEager(String id) throws ResourceNotFoundException {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    Resource s = null;
    try {
      s = (Resource) session.get(Resource.class, id);
      if (s == null) {
        return this.findByUuid(id);
      }

      Hibernate.initialize(s.getRelatedParty());
      Hibernate.initialize(s.getNote());
      Hibernate.initialize(s.getResourceCharacteristic());
      Hibernate.initialize(s.getResourceSpecification());
      Hibernate.initialize(s.getResourceRelationship());
      Hibernate.initialize(s.getAttachment());
      Hibernate.initialize(s.getActivationFeature());

      tx.commit();
    } finally {
      session.close();
    }

    return s;
  }


  /**
   * Raises a resource create notification event.
   *
   * @param so the resource that was created
   */
  @Transactional
  private void raiseResourceCreateNotification(Resource so) {
    ResourceCreateNotification n = new ResourceCreateNotification();
    ResourceCreateEvent event = new ResourceCreateEvent();
    event.getEvent().setResource(so);
    n.setEvent(event);
    resourceApiRouteBuilder.publishEvent(n, so.getId());

  }


  /**
   * Raises a resource attribute value change notification event.
   *
   * @param so the resource that has changed
   */
  @Transactional
  private void raiseResourceAttributeValueChangeEventNotification(Resource so) {
    ResourceAttributeValueChangeNotification n = new ResourceAttributeValueChangeNotification();
    ResourceAttributeValueChangeEvent event = new ResourceAttributeValueChangeEvent();
    event.getEvent().setResource(so);
    n.setEvent(event);
    resourceApiRouteBuilder.publishEvent(n, so.getId());

  }
  
  
  /**
   * Raises a resource state change notification event.
   *
   * @param so the resource whose state has changed
   */
  @Transactional
  private void raiseResourceStateChangeEventNotification(Resource so) {
    ResourceStateChangeNotification n = new ResourceStateChangeNotification();
    ResourceStateChangeEvent event = new ResourceStateChangeEvent();
    event.getEvent().setResource(so);
    n.setEvent(event);
    resourceApiRouteBuilder.publishEvent(n, so.getId());

  }


  /**
   * Finds all active resources that need to be terminated.
   *
   * @return a list of resource UUIDs to terminate
   */
  @Transactional
  public List<String> findAllActiveResourcesToTerminate() {

    List<String> result = new ArrayList<>();
    List<Resource> resourcs = this.resourceRepo.findActiveToTerminate();
    for (Resource r : resourcs) {
      result.add(r.getId());
    }

    return result;
  }


  /**
   * Deletes a resource by its UUID.
   *
   * @param id the UUID of the resource to delete
   * @return null
   * @throws ApiException
   */
  public Void deleteByUuid(String id) throws ApiException {
    Optional<Resource> optionalCat = this.resourceRepo.findByUuid(id);
    
    if (optionalCat.isPresent()) {
        Resource s = optionalCat.get();
        this.resourceRepo.delete(s);
        
        logger.info("Deleted resource with UUID = " + id);
    } else {
        throw new ApiException(404, "Resource not found with UUID: " + id);
    }

    return null;
  }


  /**
   * Adds a new resource or updates an existing one based on name, category, and version.
   *
   * @param aName          the name of the resource
   * @param aCategory      the category of the resource
   * @param aVersion       the version of the resource
   * @param resourceCreate the resource data to create or update
   * @return the added or updated resource
   */
  @Transactional
  public Resource addOrUpdateResourceByNameCategoryVersion(String aName, String aCategory,
     String aVersion, ResourceCreate aResourceCreate) throws ApiException {

    List<Resource> resources =
        this.resourceRepo.findByNameAndCategoryAndResourceVersion(aName, aCategory, aVersion);
    Resource result = null;

    if (resources.size() > 0) {
      // perform update to the first one
      String resID = resources.get(0).getUuid();
      result = this.updateResource(resID, aResourceCreate, false);

    } else {
      result = this.addResource(aResourceCreate);
    }

    ObjectMapper mapper = new ObjectMapper();
    try {
      String originaServiceAsJson = mapper.writeValueAsString(result);
      logger.debug(originaServiceAsJson);
    } catch (JsonProcessingException e) {
      logger.error("cannot umarshall service: " + result.getName());
      e.printStackTrace();
    }

    return result;
  }
}
