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
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreate;
import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
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


public class ServiceCategoryNotificationIntegrationTest  extends BaseIT{

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryRepoService categoryRepoService;

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
    public void testServiceCategoryCreateNotificationTriggered() {
        // Arrange
        ServiceCategoryCreate serviceCategoryCreate = new ServiceCategoryCreate();
        serviceCategoryCreate.setName("Test Integration Category");
        serviceCategoryCreate.setDescription("A test service category for integration testing");

        // Act
        ServiceCategory createdCategory = categoryRepoService.addCategory(serviceCategoryCreate);

        // Assert - Verify notification was published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryCreateNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceCategoryDeleteNotificationTriggered() {
        // Arrange - First create a category
        ServiceCategoryCreate serviceCategoryCreate = new ServiceCategoryCreate();
        serviceCategoryCreate.setName("Test Category for Deletion");
        serviceCategoryCreate.setDescription("A test service category to be deleted");

        ServiceCategory createdCategory = categoryRepoService.addCategory(serviceCategoryCreate);
        String categoryId = createdCategory.getUuid();

        // Act - Delete the category
        boolean deleted = categoryRepoService.deleteById(categoryId);

        // Assert - Verify delete notification was published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryDeleteNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceCategoryAddWithDirectObjectNotificationTriggered() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("Test Direct Category");
        serviceCategory.setDescription("A test service category added directly");

        // Act
        ServiceCategory createdCategory = categoryRepoService.addCategory(serviceCategory);

        // Assert - Verify notification was published to Camel
        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryCreateNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceCategoryDeleteWithNonExistentId() {
        // Act
        boolean deleted = categoryRepoService.deleteById("non-existent-id");

        // Assert - No notification should be triggered for non-existent categories
        verify(producerTemplate, timeout(2000).times(0)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryDeleteNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testMultipleServiceCategoryOperationsNotifications() {
        // Test multiple operations to ensure notifications are properly triggered

        // Create first category
        ServiceCategoryCreate category1 = new ServiceCategoryCreate();
        category1.setName("Test Category 1");
        category1.setDescription("First test category");
        ServiceCategory created1 = categoryRepoService.addCategory(category1);

        // Create second category
        ServiceCategoryCreate category2 = new ServiceCategoryCreate();
        category2.setName("Test Category 2");
        category2.setDescription("Second test category");
        ServiceCategory created2 = categoryRepoService.addCategory(category2);

        // Delete first category
        boolean deleted1 = categoryRepoService.deleteById(created1.getUuid());

        // Assert multiple notifications were published to Camel
        verify(producerTemplate, timeout(5000).times(2)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("First test category")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000).times(1)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("Second test category")),
            anyMap()
        );
    }
}
