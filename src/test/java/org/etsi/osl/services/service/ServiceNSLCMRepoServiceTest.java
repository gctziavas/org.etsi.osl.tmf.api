/**
 * @Author: Eduardo Santos
 * @Date:   2024-05-29 09:52:16
 * @Last Modified by:   Eduardo Santos
 * @Last Modified time: 2024-05-31 15:01:52
 */
package org.etsi.osl.services.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.common.model.service.Place;
import org.etsi.osl.tmf.common.model.service.ResourceRef;
import org.etsi.osl.tmf.common.model.service.ServiceRef;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.sim638.model.Service;
import org.etsi.osl.tmf.sim638.model.ServiceOrderRef;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.etsi.osl.tmf.sim638.repo.ServiceRepository;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


public class ServiceNSLCMRepoServiceTest  extends BaseIT {

    @Autowired
    ServiceRepoService serviceRepoService;

    @Autowired
    private ServiceRepository serviceRepository;

    private static Service initialService;

    private static ServiceUpdate servUpd;

    private static ObjectMapper objectMapper;

    private Service createdTestService;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeAll
    public static void setupBeforeClass() {
    }

    @BeforeEach
    public void setupBefore() {
        try {
            objectMapper = new ObjectMapper();

            initialService = objectMapper.readValue(
                    new File(
                            "src/test/resources/ServiceRepoServiceTestResources/104426_forTesting/initial_service.json"),
                            Service.class
                            );

            servUpd = objectMapper.readValue(
                    new File(
                            "src/test/resources/ServiceRepoServiceTestResources/104426_forTesting/servUpd.json"),
                            ServiceUpdate.class
                            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        assertNotNull(initialService);

        // Create a real service in the repository for testing
        // Save and flush to ensure it's committed to the database
        createdTestService = serviceRepository.saveAndFlush(initialService);
        assertNotNull(createdTestService);
        assertNotNull(createdTestService.getId());

        // Clear the persistence context to avoid stale data
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @AfterEach
    public void tearDown() {
        // Clear the persistence context first
        if (entityManager != null) {
            entityManager.clear();
        }

        // Clean up the test service created in @BeforeEach
        if (createdTestService != null && createdTestService.getId() != null) {
            try {
              serviceRepository.findByUuid(createdTestService.getUuid()).ifPresent(service -> {
                                      serviceRepository.delete(service);
                                  });
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }


    /**
     * Tests the updateService method when the service is not found.
     *
     * This test verifies that the method returns null when the service is not found
     * in the repository.
     */
    @Test
    public void testUpdateServiceWhenServiceNotFound() {
        // Execute the method to be tested with a non-existing ID
        Service result = serviceRepoService.updateService("non-existing-id-12345", servUpd, false, null, null);

        // Assert the expected outcome
        assertNull(result);
    }


    /**
     * Tests the updateService method when the service is found.
     *
     * This test verifies that the method returns a non-null Service object when the
     * service is found in the repository.
     */
    @Test
    public void testUpdateServiceWhenServiceFound() {
        // Execute the method to be tested with the created test service
        // Use String.valueOf to convert Long ID to String
        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Assert the expected outcome
        assertNotNull(result);
    }


    /**
     * Tests that the service can be retrieved after being created.
     *
     * This test verifies that getServiceEager returns the service that was created.
     */
    @Test
    public void testVerifyGetServiceEagerReturnsService() {
        // Execute the method to be tested
        // Use String.valueOf to convert Long ID to String
        Service retrievedService = serviceRepoService.getServiceEager(String.valueOf(createdTestService.getId()));

        // Verify the expected outcome
        assertNotNull(retrievedService);
        assertEquals(createdTestService.getId(), retrievedService.getId());
    }

    
     /**
     * Tests the updateNSLCMCharacteristic method when the NSLCM value to update is null.
     *
     * This test verifies that if a service characteristic's name contains "NSLCM" and its value is updated to null,
     * the characteristic value in the service is correctly updated.
     */
    @Test
    public void testUpdateNSLCMCharacteristicMethodWhenNSLCMValueToUpdateIsNull() {
        Service service = serviceRepoService.getServiceEager(String.valueOf(createdTestService.getId()));

        // Mimic initial behaviour of the updateService method
        updateServiceDetails(service, servUpd);
        
        if ( servUpd.getServiceCharacteristic()!=null ) {

            for (Characteristic n : servUpd.getServiceCharacteristic()) {
                    if ( service.getServiceCharacteristicByName( n.getName() )!= null ) {
                        Characteristic origChar = service.getServiceCharacteristicByName( n.getName() );
                        if ( ( origChar !=null ) && ( origChar.getValue() !=null ) && ( origChar.getValue().getValue() !=null )) {
                            if ( !origChar.getValue().getValue().equals(n.getValue().getValue()) ) {
                                                                    
                                // Check if the name contains "NSLCM" in any case
                                if (n.getName().toUpperCase().contains("NSLCM")) {
                                    
                                    // Set the value of NSLCM to null
                                    n.getValue().setValue(null);

                                    serviceRepoService.updateNSLCMCharacteristic(service, n);

                                    try {
                                        ArrayNode expected = (ArrayNode) objectMapper.readTree(
                                            "[]"
                                        );
                                        ArrayNode actual = (ArrayNode) objectMapper.readTree(
                                            service.getServiceCharacteristicByName(n.getName()).getValue().getValue()
                                        );

                                        assertEquals(
                                            expected,
                                            actual
                                        );
                                        break;
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }   
                    }
            }                       
        }
    }

    
    /**
     * Tests the updateNSLCMCharacteristic method when the NSLCM value to update is not null and NSLCM does not already exist.
     *
     * This test verifies that if a service characteristic's name contains "NSLCM" and its value is updated to a non-null value,
     * the characteristic value in the service is correctly updated when NSLCM does not already exist.
     */
    @Test
    public void testUpdateNSLCMCharacteristicMethodWhenNSLCMValueToUpdateIsNotNullAndNSLCMDoesntAlreadyExist() {
        Service service = serviceRepoService.getServiceEager(String.valueOf(createdTestService.getId()));

        // Mimic initial behaviour of the updateService method
        updateServiceDetails(service, servUpd);
        
        if ( servUpd.getServiceCharacteristic()!=null ) {
            for (Characteristic n : servUpd.getServiceCharacteristic()) {
                    if ( service.getServiceCharacteristicByName( n.getName() )!= null ) {
                        Characteristic origChar = service.getServiceCharacteristicByName( n.getName() );
                        if ( ( origChar !=null ) && ( origChar.getValue() !=null ) && ( origChar.getValue().getValue() !=null )) {
                            if ( !origChar.getValue().getValue().equals(n.getValue().getValue()) ) {
                                                                    
                                // Check if the name contains "NSLCM" in any case
                                if (n.getName().toUpperCase().contains("NSLCM")) {

                                    service.getServiceCharacteristicByName(n.getName()).getValue().setValue("");

                                    serviceRepoService.updateNSLCMCharacteristic(service, n);

                                    try {
                                        ArrayNode expected = (ArrayNode) objectMapper.readTree(
                                            "[{\"queuePosition\":0,\"lcmOperationType\":\"instantiate\",\"detailed-status\":\"Done\",\"operationState\":\"COMPLETED\",\"errorMessage\":null,\"nsInstanceId\":\"420fa806-f2f8-405e-8348-11e4fcd13f25\",\"_admin\":{\"projects_write\":[\"92636b50-d607-4801-98b5-f0da541363be\"],\"created\":1.7169792184842422E9,\"modified\":1.7169794444025614E9,\"worker\":\"d6f95b754d12\",\"projects_read\":[\"92636b50-d607-4801-98b5-f0da541363be\"]},\"detailedStatus\":null,\"stage\":\"\",\"operationParams\":{\"nsInstanceId\":\"420fa806-f2f8-405e-8348-11e4fcd13f25\",\"ssh_keys\":[\"\"],\"lcmOperationType\":\"instantiate\",\"nsdId\":\"338d3a8c-af70-446a-af37-ed8bb97a6641\",\"nsName\":\"Service_Order_65bcf307-1a47-4a48-b211-be94c3390b81\",\"vimAccountId\":\"479356bf-72ff-4dfd-8483-5c23f48dd0bc\"},\"startTime\":1.7169792184841862E9,\"links\":{\"nsInstance\":\"/osm/nslcm/v1/ns_instances/420fa806-f2f8-405e-8348-11e4fcd13f25\",\"self\":\"/osm/nslcm/v1/ns_lcm_op_occs/e0836187-7d4a-49ac-a317-fc4108ed2f93\"},\"_id\":\"e0836187-7d4a-49ac-a317-fc4108ed2f93\",\"id\":\"e0836187-7d4a-49ac-a317-fc4108ed2f93\",\"isAutomaticInvocation\":false,\"isCancelPending\":false,\"statusEnteredTime\":1.7169794444025595E9}]"
                                        );
                                        ArrayNode actual = (ArrayNode) objectMapper.readTree(
                                            service.getServiceCharacteristicByName(n.getName()).getValue().getValue()
                                        );

                                        assertEquals(
                                            expected,
                                            actual
                                        );
                                        break;
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }   
                    }
            }                       
        }
    }


    /**
     * Tests the updateNSLCMCharacteristic method when the NSLCM value to update is not null and NSLCM already exists.
     *
     * This test verifies that if a service characteristic's name contains "NSLCM" and its value is updated to a non-null value,
     * the characteristic value in the service is correctly updated when NSLCM already exists.
     */
    @Test
    public void testUpdateNSLCMCharacteristicMethodWhenNSLCMValueToUpdateIsNotNullAndNSLCMAlreadyExists() {
        Service service = serviceRepoService.getServiceEager(String.valueOf(createdTestService.getId()));

        // Mimic initial behaviour of the updateService method
        updateServiceDetails(service, servUpd);
        
        if ( servUpd.getServiceCharacteristic()!=null ) {
            for (Characteristic n : servUpd.getServiceCharacteristic()) {
                    if ( service.getServiceCharacteristicByName( n.getName() )!= null ) {
                        Characteristic origChar = service.getServiceCharacteristicByName( n.getName() );
                        if ( ( origChar !=null ) && ( origChar.getValue() !=null ) && ( origChar.getValue().getValue() !=null )) {
                            if ( !origChar.getValue().getValue().equals(n.getValue().getValue()) ) {
                                                                    
                                // Check if the name contains "NSLCM" in any case
                                if (n.getName().toUpperCase().contains("NSLCM")) {
                                    
                                    // Set the value of NSLCM
                                    service.getServiceCharacteristicByName(n.getName()).getValue().setValue("[{\"test\": 2}]");

                                    serviceRepoService.updateNSLCMCharacteristic(service, n);

                                    try {
                                        ArrayNode expected = (ArrayNode) objectMapper.readTree(
                                            "[{\"test\": 2}, {\"queuePosition\":0,\"lcmOperationType\":\"instantiate\",\"detailed-status\":\"Done\",\"operationState\":\"COMPLETED\",\"errorMessage\":null,\"nsInstanceId\":\"420fa806-f2f8-405e-8348-11e4fcd13f25\",\"_admin\":{\"projects_write\":[\"92636b50-d607-4801-98b5-f0da541363be\"],\"created\":1.7169792184842422E9,\"modified\":1.7169794444025614E9,\"worker\":\"d6f95b754d12\",\"projects_read\":[\"92636b50-d607-4801-98b5-f0da541363be\"]},\"detailedStatus\":null,\"stage\":\"\",\"operationParams\":{\"nsInstanceId\":\"420fa806-f2f8-405e-8348-11e4fcd13f25\",\"ssh_keys\":[\"\"],\"lcmOperationType\":\"instantiate\",\"nsdId\":\"338d3a8c-af70-446a-af37-ed8bb97a6641\",\"nsName\":\"Service_Order_65bcf307-1a47-4a48-b211-be94c3390b81\",\"vimAccountId\":\"479356bf-72ff-4dfd-8483-5c23f48dd0bc\"},\"startTime\":1.7169792184841862E9,\"links\":{\"nsInstance\":\"/osm/nslcm/v1/ns_instances/420fa806-f2f8-405e-8348-11e4fcd13f25\",\"self\":\"/osm/nslcm/v1/ns_lcm_op_occs/e0836187-7d4a-49ac-a317-fc4108ed2f93\"},\"_id\":\"e0836187-7d4a-49ac-a317-fc4108ed2f93\",\"id\":\"e0836187-7d4a-49ac-a317-fc4108ed2f93\",\"isAutomaticInvocation\":false,\"isCancelPending\":false,\"statusEnteredTime\":1.7169794444025595E9}]"
                                        );
                                        ArrayNode actual = (ArrayNode) objectMapper.readTree(
                                            service.getServiceCharacteristicByName(n.getName()).getValue().getValue()
                                        );

                                        assertEquals(
                                            expected,
                                            actual
                                        );
                                        break;
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }   
                    }
            }                       
        }
    }


    /**
     * Tests updating the service type.
     *
     * This test verifies that the service type is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_Type() {
        servUpd.setType("NewType");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewType", result.getType());
    }


    /**
     * Tests updating the service name.
     *
     * This test verifies that the service name is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_Name() {
        servUpd.setName("NewName");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewName", result.getName());
    }


    /**
     * Tests updating the service category.
     *
     * This test verifies that the service category is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_Category() {
        servUpd.setCategory("NewCategory");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewCategory", result.getCategory());
    }


    /**
     * Tests updating the service description.
     *
     * This test verifies that the service description is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_Description() {
        servUpd.setDescription("NewDescription");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewDescription", result.getDescription());
    }


     /**
     * Tests updating the service start date.
     *
     * This test verifies that the service start date is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_StartDate() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        servUpd.setStartDate(offsetDateTime);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals(offsetDateTime, result.getStartDate());
    }


     /**
     * Tests updating the service end date.
     *
     * This test verifies that the service end date is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_EndDate() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusDays(1);
        servUpd.setEndDate(offsetDateTime);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals(offsetDateTime, result.getEndDate());
    }


     /**
     * Tests updating the hasStarted attribute of the service.
     *
     * This test verifies that the hasStarted attribute is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_HasStarted() {
        servUpd.setHasStarted(true);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertTrue(result.isHasStarted());
    }


     /**
     * Tests updating the isServiceEnabled attribute of the service.
     *
     * This test verifies that the isServiceEnabled attribute is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_IsServiceEnabled() {
        servUpd.setIsServiceEnabled(true);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertTrue(result.isIsServiceEnabled());
    }


    /**
     * Tests updating the isStateful attribute of the service.
     *
     * This test verifies that the isStateful attribute is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_IsStateful() {
        servUpd.setIsStateful(true);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertTrue(result.isIsStateful());
    }


    /**
     * Tests updating the service date.
     *
     * This test verifies that the service date is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_ServiceDate() {
        OffsetDateTime newServiceDate = OffsetDateTime.now();
        servUpd.setServiceDate(newServiceDate.toString());

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals(newServiceDate.toString(), result.getServiceDate());
    }


     /**
     * Tests updating the service type.
     *
     * This test verifies that the service type is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_ServiceType() {
        servUpd.setServiceType("NewServiceType");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewServiceType", result.getServiceType());
    }


    /**
     * Tests updating the start mode of the service.
     *
     * This test verifies that the start mode is correctly updated in the service object.
     */
    @Test
    public void testUpdateService_StartMode() {
        servUpd.setStartMode("NewStartMode");

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        assertEquals("NewStartMode", result.getStartMode());
    }


    /**
     * Tests adding notes to the service.
     *
     * This test verifies that notes with null UUIDs are added to the service,
     * while notes with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddNote() {
        Note note1 = new Note();
        note1.setUuid(null);
        note1.setText("test1");
        Note note2 = new Note();
        note2.setUuid("existing-uuid");
        note2.setText("test2");

        List<Note> notes = new ArrayList<>();
        notes.add(note1);
        notes.add(note2);
        servUpd.setNote(notes);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that the note was added by verifying the text content
        assertNotNull(result.getNote());
        assertTrue(result.getNote().stream().anyMatch(n -> "test1".equals(n.getText())));
        assertFalse(result.getNote().stream().anyMatch(n -> "test2".equals(n.getText())));
    }


    /**
     * Tests adding places to the service.
     *
     * This test verifies that places with null UUIDs are added to the service,
     * while places with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddPlace() {
        Place place1 = new Place();
        place1.setUuid(null);
        place1.setName("Place1");
        Place place2 = new Place();
        place2.setUuid("existing-uuid");
        place2.setName("Place2");

        List<Place> places = new ArrayList<>();
        places.add(place1);
        places.add(place2);
        servUpd.setPlace(places);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that places were added
        assertNotNull(result.getPlace());
        assertTrue(result.getPlace().size() >= 1);
        // Verify places were added by checking names
        assertTrue(result.getPlace().stream().anyMatch(p -> "Place1".equals(p.getName())));
        assertFalse(result.getPlace().stream().anyMatch(p -> "Place2".equals(p.getName())));
    }


     /**
     * Tests adding related parties to the service.
     *
     * This test verifies that related parties with null UUIDs are added to the service,
     * while related parties with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddRelatedParty() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setUuid(null);
        relatedParty1.setName("Party1");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setUuid("existing-uuid");
        relatedParty2.setName("Party2");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        servUpd.setRelatedParty(relatedParties);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that related parties were added
        assertNotNull(result.getRelatedParty());
        assertTrue(result.getRelatedParty().size() >= 1);
        // Verify parties were added by checking names
        assertTrue(result.getRelatedParty().stream().anyMatch(p -> "Party1".equals(p.getName())));
        assertFalse(result.getRelatedParty().stream().anyMatch(p -> "Party2".equals(p.getName())));
    }


     /**
     * Tests adding service orders to the service.
     *
     * This test verifies that service orders with null UUIDs are added to the service,
     * while service orders with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddServiceOrder() {
        ServiceOrderRef order1 = new ServiceOrderRef();
        order1.setUuid(null);
        order1.setId("order1-id");
        ServiceOrderRef order2 = new ServiceOrderRef();
        order2.setUuid("existing-uuid");
        order2.setId("order2-id");

        List<ServiceOrderRef> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        servUpd.setServiceOrder(orders);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that service orders were added
        assertNotNull(result.getServiceOrder());
        assertTrue(result.getServiceOrder().size() >= 1);
        // Verify orders were added by checking IDs
        assertTrue(result.getServiceOrder().stream().anyMatch(o -> "order1-id".equals(o.getId())));
        assertFalse(result.getServiceOrder().stream().anyMatch(o -> "order2-id".equals(o.getId())));
    }


    /**
     * Tests adding service relationships to the service.
     * 
     * This test verifies that service relationships with null UUIDs are added to the service,
     * while service relationships with existing UUIDs are not.
     */
    //@Test
    //public void testUpdateService_AddServiceRelationship() {
    //    ServiceRelationship relationship1 = new ServiceRelationship();
    //    relationship1.setUuid(null);
    //    ServiceRelationship relationship2 = new ServiceRelationship();
    //    relationship2.setUuid("existing-uuid");
//
    //    List<ServiceRelationship> relationships = new ArrayList<>();
    //    relationships.add(relationship1);
    //    relationships.add(relationship2);
    //    servUpd.setServiceRelationship(relationships);
//
    //    serviceRepoService.updateService("test-id", servUpd, false, null, null);
//
    //    assertTrue(initialService.getServiceRelationship().contains(relationship1));
    //    assertFalse(initialService.getServiceRelationship().contains(relationship2));
    //}


     /**
     * Tests adding supporting resources to the service.
     *
     * This test verifies that supporting resources with null UUIDs are added to the service,
     * while supporting resources with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddSupportingResource() {
        ResourceRef resource1 = new ResourceRef();
        resource1.setUuid(null);
        resource1.setId("resource1-id");
        resource1.setName("Resource1");
        ResourceRef resource2 = new ResourceRef();
        resource2.setUuid("existing-uuid");
        resource2.setId("resource2-id");
        resource2.setName("Resource2");

        List<ResourceRef> resources = new ArrayList<>();
        resources.add(resource1);
        resources.add(resource2);
        servUpd.setSupportingResource(resources);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that supporting resources were added
        assertNotNull(result.getSupportingResource());
        assertTrue(result.getSupportingResource().size() >= 1);
        // Verify resources were added by checking names
        assertTrue(result.getSupportingResource().stream().anyMatch(r -> "Resource1".equals(r.getName())));
        assertFalse(result.getSupportingResource().stream().anyMatch(r -> "Resource2".equals(r.getName())));
    }


    /**
     * Tests adding supporting services to the service.
     *
     * This test verifies that supporting services with null UUIDs are added to the service,
     * while supporting services with existing UUIDs are not.
     */
    @Test
    public void testUpdateService_AddSupportingService() {
        ServiceRef serviceRef1 = new ServiceRef();
        serviceRef1.setUuid(null);
        serviceRef1.setId("service1-id");
        serviceRef1.setName("Service1");
        ServiceRef serviceRef2 = new ServiceRef();
        serviceRef2.setUuid("existing-uuid");
        serviceRef2.setId("service2-id");
        serviceRef2.setName("Service2");

        List<ServiceRef> serviceRefs = new ArrayList<>();
        serviceRefs.add(serviceRef1);
        serviceRefs.add(serviceRef2);
        servUpd.setSupportingService(serviceRefs);

        Service result = serviceRepoService.updateService(String.valueOf(createdTestService.getId()), servUpd, false, null, null);

        // Check that supporting services were added
        assertNotNull(result.getSupportingService());
        assertTrue(result.getSupportingService().size() >= 1);
        // Verify services were added by checking names
        assertTrue(result.getSupportingService().stream().anyMatch(s -> "Service1".equals(s.getName())));
        assertFalse(result.getSupportingService().stream().anyMatch(s -> "Service2".equals(s.getName())));
    }

    /**
     * Updates the details of the given service based on the non-null values of the provided service update object.
     *
     * @param service The service object to be updated.
     * @param servUpd The service update object containing new values.
     */
    public void updateServiceDetails(Service service, ServiceUpdate servUpd) {
        if (servUpd.getType() != null) service.setType(servUpd.getType());
        if (servUpd.getName() != null) service.setName(servUpd.getName());
        if (servUpd.getCategory() != null) service.setCategory(servUpd.getCategory());
        if (servUpd.getDescription() != null) service.setDescription(servUpd.getDescription());
        if (servUpd.getStartDate() != null) service.setStartDate(servUpd.getStartDate());
        if (servUpd.getEndDate() != null) service.setEndDate(servUpd.getEndDate());
        if (servUpd.isHasStarted() != null) service.setHasStarted(servUpd.isHasStarted());
        if (servUpd.isIsServiceEnabled() != null) service.setIsServiceEnabled(servUpd.isIsServiceEnabled());
        if (servUpd.isIsStateful() != null) service.setIsStateful(servUpd.isIsStateful());
        if (servUpd.getServiceDate() != null) service.setServiceDate(servUpd.getServiceDate());
        if (servUpd.getServiceType() != null) service.setServiceType(servUpd.getServiceType());
        if (servUpd.getStartMode() != null) service.setStartMode(servUpd.getStartMode());
        if (servUpd.getState() != null) service.setState(servUpd.getState());
        if (servUpd.getServiceSpecificationRef() != null) service.setServiceSpecificationRef(servUpd.getServiceSpecificationRef());
    }

}
