package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationNotificationService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class ServiceSpecificationNotificationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @SpyBean
    private ServiceSpecificationNotificationService serviceSpecificationNotificationService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationCreateNotificationTriggered() {
        // Arrange
        ServiceSpecificationCreate serviceSpecificationCreate = new ServiceSpecificationCreate();
        serviceSpecificationCreate.setName("Test Integration Specification");
        serviceSpecificationCreate.setDescription("A test service specification for integration testing");

        // Act
        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(serviceSpecificationCreate);

        // Assert
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationCreateNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationUpdateNotificationTriggered() {
        // Arrange - First create a specification
        ServiceSpecificationCreate serviceSpecificationCreate = new ServiceSpecificationCreate();
        serviceSpecificationCreate.setName("Test Specification for Update");
        serviceSpecificationCreate.setDescription("A test service specification to be updated");
        
        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(serviceSpecificationCreate);
        String specId = createdSpec.getUuid();

        // Act - Update the specification
        ServiceSpecificationUpdate serviceSpecificationUpdate = new ServiceSpecificationUpdate();
        serviceSpecificationUpdate.setDescription("Updated description");
        
        ServiceSpecification updatedSpec = serviceSpecificationRepoService.updateServiceSpecification(specId, serviceSpecificationUpdate);

        // Assert
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationCreateNotification(any(ServiceSpecification.class));
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationChangeNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationDeleteNotificationTriggered() {
        // Arrange - First create a specification
        ServiceSpecificationCreate serviceSpecificationCreate = new ServiceSpecificationCreate();
        serviceSpecificationCreate.setName("Test Specification for Deletion");
        serviceSpecificationCreate.setDescription("A test service specification to be deleted");
        
        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(serviceSpecificationCreate);
        String specId = createdSpec.getUuid();

        // Act - Delete the specification
        serviceSpecificationRepoService.deleteByUuid(specId);

        // Assert - Focus only on delete notification
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationDeleteNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationUpdateWithNullSpecification() {
        // Act - Try to update a non-existent specification
        ServiceSpecificationUpdate serviceSpecificationUpdate = new ServiceSpecificationUpdate();
        serviceSpecificationUpdate.setDescription("Updated description");
        
        ServiceSpecification result = serviceSpecificationRepoService.updateServiceSpecification("non-existent-id", serviceSpecificationUpdate);

        // Assert - No notification should be triggered for non-existent specifications
        verify(serviceSpecificationNotificationService, timeout(2000).times(0))
            .publishServiceSpecificationChangeNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testMultipleServiceSpecificationOperationsNotifications() {
        // Test multiple operations to ensure notifications are properly triggered

        // Create first specification
        ServiceSpecificationCreate spec1 = new ServiceSpecificationCreate();
        spec1.setName("Test Specification 1");
        spec1.setDescription("First test specification");
        ServiceSpecification created1 = serviceSpecificationRepoService.addServiceSpecification(spec1);

        // Create second specification
        ServiceSpecificationCreate spec2 = new ServiceSpecificationCreate();
        spec2.setName("Test Specification 2");
        spec2.setDescription("Second test specification");
        ServiceSpecification created2 = serviceSpecificationRepoService.addServiceSpecification(spec2);

        // Update first specification
        ServiceSpecificationUpdate spec1Update = new ServiceSpecificationUpdate();
        spec1Update.setDescription("Updated first specification");
        ServiceSpecification updated1 = serviceSpecificationRepoService.updateServiceSpecification(created1.getUuid(), spec1Update);

        // Delete second specification
        serviceSpecificationRepoService.deleteByUuid(created2.getUuid());

        // Assert multiple notifications
        verify(serviceSpecificationNotificationService, timeout(5000).times(2))
            .publishServiceSpecificationCreateNotification(any(ServiceSpecification.class));
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationChangeNotification(any(ServiceSpecification.class));
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationDeleteNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationLifecycleNotifications() {
        // Test complete lifecycle: Create -> Update -> Delete

        // Create
        ServiceSpecificationCreate serviceSpecificationCreate = new ServiceSpecificationCreate();
        serviceSpecificationCreate.setName("Lifecycle Test Specification");
        serviceSpecificationCreate.setDescription("A specification for lifecycle testing");
        
        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(serviceSpecificationCreate);
        String specId = createdSpec.getUuid();

        // Update
        ServiceSpecificationUpdate serviceSpecificationUpdate = new ServiceSpecificationUpdate();
        serviceSpecificationUpdate.setName("Updated Lifecycle Test Specification");
        serviceSpecificationUpdate.setDescription("Updated description for lifecycle testing");
        
        ServiceSpecification updatedSpec = serviceSpecificationRepoService.updateServiceSpecification(specId, serviceSpecificationUpdate);

        // Delete
        serviceSpecificationRepoService.deleteByUuid(specId);

        // Assert complete lifecycle notifications
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationCreateNotification(any(ServiceSpecification.class));
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationChangeNotification(any(ServiceSpecification.class));
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationDeleteNotification(any(ServiceSpecification.class));
    }
}