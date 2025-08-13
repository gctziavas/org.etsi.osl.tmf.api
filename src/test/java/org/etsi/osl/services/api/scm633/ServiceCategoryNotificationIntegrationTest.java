package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreate;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCategoryNotificationService;
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
public class ServiceCategoryNotificationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryRepoService categoryRepoService;

    @SpyBean
    private ServiceCategoryNotificationService serviceCategoryNotificationService;

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
    public void testServiceCategoryCreateNotificationTriggered() {
        // Arrange
        ServiceCategoryCreate serviceCategoryCreate = new ServiceCategoryCreate();
        serviceCategoryCreate.setName("Test Integration Category");
        serviceCategoryCreate.setDescription("A test service category for integration testing");

        // Act
        ServiceCategory createdCategory = categoryRepoService.addCategory(serviceCategoryCreate);

        // Assert
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryCreateNotification(any(ServiceCategory.class));
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

        // Assert - Focus only on delete notification
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryDeleteNotification(any(ServiceCategory.class));
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

        // Assert
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryCreateNotification(any(ServiceCategory.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testServiceCategoryDeleteWithNonExistentId() {
        // Act
        boolean deleted = categoryRepoService.deleteById("non-existent-id");

        // Assert - No notification should be triggered for non-existent categories
        verify(serviceCategoryNotificationService, timeout(2000).times(0))
            .publishServiceCategoryDeleteNotification(any(ServiceCategory.class));
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

        // Assert multiple notifications
        verify(serviceCategoryNotificationService, timeout(5000).times(2))
            .publishServiceCategoryCreateNotification(any(ServiceCategory.class));
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryDeleteNotification(any(ServiceCategory.class));
    }
}