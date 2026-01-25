package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.apache.camel.ProducerTemplate;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


public class ServiceSpecificationNotificationIntegrationTest extends BaseIT{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @MockBean
    private ProducerTemplate producerTemplate;

    private AutoCloseable mocks;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @PersistenceContext
    private EntityManager entityManager;


    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        if (entityManager != null) {
          entityManager.clear();
      }
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

        // Assert - Verify notification was published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationCreateNotification")),
            anyMap()
        );
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

        // Assert - Verify both create and change notifications were published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationCreateNotification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationChangeNotification")),
            anyMap()
        );
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

        // Assert - Verify delete notification was published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationDeleteNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceSpecificationUpdateWithNullSpecification() {
        // Act - Try to update a non-existent specification
        ServiceSpecificationUpdate serviceSpecificationUpdate = new ServiceSpecificationUpdate();
        serviceSpecificationUpdate.setDescription("Updated description");

        ServiceSpecification result = serviceSpecificationRepoService.updateServiceSpecification("non-existent-id", serviceSpecificationUpdate);

        // Assert - No notification should be triggered for non-existent specifications
        verify(producerTemplate, timeout(2000).times(0)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationChangeNotification")),
            anyMap()
        );
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

        // Assert multiple notifications were published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("First test specification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(2)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("Second test specification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("Updated first specification")),
            anyMap()
        );
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

        // Assert complete lifecycle notifications were published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationCreateNotification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationChangeNotification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationDeleteNotification")),
            anyMap()
        );
    }
}
