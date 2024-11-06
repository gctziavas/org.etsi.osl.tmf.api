package org.etsi.osl.services.reposervices.ram702;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.ram702.api.ApiException;
import org.etsi.osl.tmf.ram702.api.ResourceNotFoundException;
import org.etsi.osl.tmf.ram702.repo.ResourceActivationRepository;
import org.etsi.osl.tmf.ram702.reposervices.ResourceActivationRepoService;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.etsi.osl.tmf.ri639.model.ResourceUpdate;
import org.etsi.osl.tmf.ri639.model.ResourceUsageStateType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link ResourceActivationRepoService}.
 * 
 * This class uses Mockito and Spring's testing framework to mock dependencies
 * and verify the behavior of the ResourceActivationRepoService.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
@SpringBootTest(classes = OpenAPISpringBoot.class)
public class ResourceActivationRepoServiceTest {
    /**
     * The service being tested, with a spy to allow partial mocking of certain methods.
     */
    @SpyBean
    @Autowired
    private ResourceActivationRepoService resourceActivationRepoService;

    /**
     * Mock for the {@link ResourceActivationRepository} to simulate repository operations.
     */
    @MockBean
    private ResourceActivationRepository resourceActivationRepo;

    private static ResourceCreate resourceCreate;

    private static Resource resource;

    /**
     * Loads test data from JSON files before all tests.
     * 
     * @throws Exception if there is an error loading the test data.
     */
    @BeforeClass
    public static void setupBeforeClass() {
        // Load resourceCreate and resourceUpdare from the 
        // JSON files into the respective classes
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Configure the mapper to ignore unknown fields that are present in 
            // the JSON but not in the class. This is needed to be able to 
            // update a resource using a Resource or a ResourceUpdate.
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            resourceCreate = mapper.readValue(
                new File(
                    "src/test/resources/reposervices/ram702/resourceCreate.json"
                ),
                ResourceCreate.class
            );

            resource = mapper.readValue(
                new File(
                    "src/test/resources/reposervices/ram702/resource.json"
                ),
                Resource.class
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Assert that the files were properly loaded
        assertNotNull(resourceCreate);
        assertNotNull(resource);
    }
    
    /**
     * Sets up common mock behavior for the repository before each test.
     * @throws ResourceNotFoundException 
     */
    @Before
    public void setupBefore() throws ResourceNotFoundException {
        when(resourceActivationRepo.findByUuid(anyString())).thenReturn(Optional.of(resource));
        when(resourceActivationRepo.save(any(Resource.class))).thenReturn(resource);
        doReturn(resource).when(resourceActivationRepoService).getResourceEager(anyString());
    }

    /**
     * Test for {@link ResourceActivationRepoService#findByUuid(String)} when a resource is found.
     * 
     * @throws ResourceNotFoundException if the resource is not found (not expected).
     */
    @Test
    public void testFindByUuidWhenResourceIsFound() throws ResourceNotFoundException {        
        // When
        Resource result = resourceActivationRepoService.findByUuid(anyString());

        // Then
        assertNotNull(result);
        verify(resourceActivationRepo, times(1)).findByUuid(anyString());
    }

    /**
     * Test for {@link ResourceActivationRepoService#findByUuid(String)} when no resource is found.
     * This ensures that a {@link ResourceNotFoundException} is thrown when the resource does not exist.
     * 
     * @throws ResourceNotFoundException expected exception if the resource is not found.
     */
    @Test
    public void testFindByUuidWhenResourceIsNotFound() throws ResourceNotFoundException {
        // Given
        when(resourceActivationRepo.findByUuid(anyString())).thenReturn(Optional.empty());

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            resourceActivationRepoService.findByUuid("123");
        });

        // Then
        assertEquals("Resource not found with UUID: 123", exception.getMessage());
    }

    /**
     * Test for {@link ResourceActivationRepoService#findAll()} to verify it retrieves all resources.
     */
    @Test
    public void testFindAllResources() {
        // Given
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = new Resource();
        resource1.setName("resource1");
        Resource resource2 = new Resource();
        resource2.setName("resource2");
        resources.add(resource1);
        resources.add(resource2);

        // Mock repository to return the list of resources
        when(resourceActivationRepo.findAll()).thenReturn(resources);

        // When
        List<Resource> result = resourceActivationRepoService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("resource1", result.get(0).getName());
        assertEquals("resource2", result.get(1).getName());
        verify(resourceActivationRepo, times(1)).findAll();
    }

    /**
     * Test for {@link ResourceActivationRepoService#addResource(ResourceCreate)} to verify resource creation.
     */
    @Test
    public void testAddResource() {
        // When
        Resource result = resourceActivationRepoService.addResource(resourceCreate);

        // Then
        assertNotNull(result);
        assertEquals("test_resource", result.getName());
        verify(resourceActivationRepo, times(1)).save(any(Resource.class));
    }

    /**
     * Test for {@link ResourceActivationRepoService#updateResource(String, ResourceUpdate, boolean)} 
     * to verify resource update when the resource is found.
     * 
     * @throws ResourceNotFoundException if the resource is not found (not expected).
     */
    @Test
    public void testUpdateResourceWhenResourceIsFound() throws ResourceNotFoundException{
        ResourceUpdate update = new ResourceUpdate();
        update.setName("updated_name");
        update.setCategory("updated_category");
        update.setDescription("Updated description");
        update.setStartOperatingDate(OffsetDateTime.now());
        update.setEndOperatingDate(OffsetDateTime.now().plusDays(1));
        update.setUsageState(ResourceUsageStateType.ACTIVE);
        update.setResourceStatus(ResourceStatusType.AVAILABLE);
        update.setResourceVersion("2.0");

        // When
        Resource updatedResource = resourceActivationRepoService.updateResource("123", update, false);

        // Then
        assertNotNull(updatedResource);
        assertEquals("updated_name", updatedResource.getName());
        assertEquals("updated_name", updatedResource.getName());
        assertEquals("updated_category", updatedResource.getCategory());
        assertEquals("Updated description", updatedResource.getDescription());
        assertNotNull(updatedResource.getStartOperatingDate());
        assertNotNull(updatedResource.getEndOperatingDate());
        assertEquals(ResourceUsageStateType.ACTIVE, updatedResource.getUsageState());
        assertEquals(ResourceStatusType.AVAILABLE, updatedResource.getResourceStatus());
        assertEquals("2.0", updatedResource.getResourceVersion());
        
        verify(resourceActivationRepo, times(1)).save(any(Resource.class));
    }

    /**
     * Test for {@link ResourceActivationRepoService#updateResource(String, ResourceUpdate, boolean)} 
     * to verify that a {@link ResourceNotFoundException} is thrown when the resource does not exist.
     * @throws ResourceNotFoundException 
     */
    @Test
    public void testUpdateResourceWhenResourceIsNotFound() throws ResourceNotFoundException {
        // Given
        doReturn(null).when(resourceActivationRepoService).getResourceEager(anyString());
        ResourceUpdate update = new ResourceUpdate();

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            resourceActivationRepoService.updateResource("123", update, false);
        });

        // Then
        assertEquals("Resource not found with UUID: 123", exception.getMessage());
        verify(resourceActivationRepo, never()).save(any(Resource.class));
    }

    /**
     * Test for {@link ResourceActivationRepoService#deleteByUuid(String)} to verify successful resource deletion.
     * 
     * @throws ApiException if there is an error during the deletion process (not expected).
     */
    @Test
    public void testDeleteByUuidWhenResourceIsFound() throws ApiException {
        // When
        resourceActivationRepoService.deleteByUuid("123");

        // Then
        verify(resourceActivationRepo, times(1)).delete(resource);
    }

    /**
     * Test for {@link ResourceActivationRepoService#deleteByUuid(String)} to verify that an {@link ApiException} 
     * is thrown when the resource to delete is not found.
     */
    @Test
    public void testDeleteByUuidWhenResourceIsNotFound() {
        // Given
        when(resourceActivationRepo.findByUuid("123")).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            resourceActivationRepoService.deleteByUuid("123");
        });

        // Then
        assertEquals("Resource not found with UUID: 123", exception.getMessage());
        verify(resourceActivationRepo, never()).delete(any(Resource.class));
    }

    /**
     * Test for {@link ResourceActivationRepoService#addOrUpdateResourceByNameCategoryVersion(String, String, String, ResourceCreate)}
     * when an existing resource is found and updated.
     * 
     * @throws ApiException if there is an error during the update process.
     */
    @Test
    public void testAddOrUpdateResourceByNameCategoryVersionWhenResourceExists() throws ApiException {
        // Given
        ResourceUpdate update = new ResourceUpdate();
        update.setName("updated_name");

        String name = "test_resource";
        String category = "Category 1";
        String version = "1.0";

        List<Resource> existingResources = Collections.singletonList(resource);

        // Mock the repository to return the existing resource
        when(resourceActivationRepo.findByNameAndCategoryAndResourceVersion(anyString(), anyString(), anyString()))
            .thenReturn(existingResources);

        // Mock the updateResource method to return the updated resource
        when(resourceActivationRepoService.updateResource("123", update, false))
            .thenReturn(resource);

        // When
        Resource result = resourceActivationRepoService.addOrUpdateResourceByNameCategoryVersion(name, category, version, resourceCreate);

        // Then
        assertNotNull(result);
        assertEquals("test_resource", result.getName());
        verify(resourceActivationRepoService, times(1)).updateResource("123", update, false);
    }

    /**
     * Test for {@link ResourceActivationRepoService#addOrUpdateResourceByNameCategoryVersion(String, String, String, ResourceCreate)}
     * when no existing resource is found, and a new one is created.
     * 
     * @throws ApiException if there is an error during the creation process.
     */
    @Test
    public void testAddOrUpdateResourceByNameCategoryVersionWhenResourceDoesNotExist() throws ApiException {
        // Given
        String name = "test_resource";
        String category = "Category 1";
        String version = "1.0";

        // Mock an empty list of existing resources
        List<Resource> noResources = new ArrayList<>();

        // Mock the repository to return no existing resources
        when(resourceActivationRepo.findByNameAndCategoryAndResourceVersion(anyString(), anyString(), anyString()))
            .thenReturn(noResources);

        // Mock the addResource method to return the newly created resource
        when(resourceActivationRepoService.addResource(resourceCreate)).thenReturn(resource);

        // When
        Resource result = resourceActivationRepoService.addOrUpdateResourceByNameCategoryVersion(name, category, version, resourceCreate);

        // Then
        assertNotNull(result);
        assertEquals("test_resource", result.getName());
        verify(resourceActivationRepoService, times(1)).addResource(any(ResourceCreate.class));
        verify(resourceActivationRepoService, never()).updateResource(result.getId(), resourceCreate, false);
    }

    /**
     * Test for {@link ResourceActivationRepoService#addOrUpdateResourceByNameCategoryVersion(String, String, String, ResourceCreate)}
     * to handle an {@link ApiException} being thrown during the process.
     * 
     * @throws ApiException expected exception during the process.
     */
    @Test
    public void testAddOrUpdateResourceByNameCategoryVersionThrowsApiException() throws ApiException {
        // Given
        String name = "Faulty Resource";
        String category = "Faulty Category";
        String version = "1.0";

        // Mock an existing resource
        List<Resource> existingResources = Collections.singletonList(resource);

        // Mock the repository to return the existing resource
        when(resourceActivationRepo.findByNameAndCategoryAndResourceVersion(name, category, version))
            .thenReturn(existingResources);

        // Mock the updateResource method to throw an ApiException
        doThrow(new ResourceNotFoundException("Error updating resource"))
            .when(resourceActivationRepoService).updateResource(resource.getId(), resourceCreate, false);

        // When and Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            resourceActivationRepoService.addOrUpdateResourceByNameCategoryVersion(name, category, version, resourceCreate);
        });

        assertEquals("Error updating resource", exception.getMessage());
        verify(resourceActivationRepoService, times(1)).updateResource(resource.getId(), resourceCreate, false);
        verify(resourceActivationRepoService, never()).addResource(resourceCreate);  // Add should not be called
    }

    /**
     * Test for {@link ResourceActivationRepoService#raiseResourceAttributeValueChangeEventNotification(Resource)}
     * to ensure a resource attribute value change notification is published.
     */
    @Test
    public void testFindAllActiveResourcesToTerminate() {
        // Given
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = mock(Resource.class);
        when(resource1.getId()).thenReturn("uuid1");
        resources.add(resource1);

        when(resourceActivationRepo.findActiveToTerminate()).thenReturn(resources);

        // When
        List<String> result = resourceActivationRepoService.findAllActiveResourcesToTerminate();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uuid1", result.get(0));
        verify(resourceActivationRepo, times(1)).findActiveToTerminate();
    }
}
