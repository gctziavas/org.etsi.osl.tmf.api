package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCatalogCallbackService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCategoryNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


public class ServiceCategoryNotificationServiceTest  extends BaseIT{

    @Mock
    private ServiceCatalogApiRouteBuilderEvents eventPublisher;
    
    @Mock
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    @InjectMocks
    private ServiceCategoryNotificationService serviceCategoryNotificationService;

    private AutoCloseable mocks;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
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
    public void testPublishServiceCategoryCreateNotification() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setUuid("test-category-123");
        serviceCategory.setName("Test Service Category");
        serviceCategory.setDescription("A test service category for notifications");

        // Act
        serviceCategoryNotificationService.publishServiceCategoryCreateNotification(serviceCategory);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceCategoryCreateNotification.class), eq("test-category-123"));
        verify(serviceCatalogCallbackService, times(1)).sendServiceCategoryCreateCallback(any());
    }

    @Test
    public void testPublishServiceCategoryDeleteNotification() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setUuid("test-category-456");
        serviceCategory.setName("Test Service Category to Delete");
        serviceCategory.setDescription("A test service category for deletion notifications");

        // Act
        serviceCategoryNotificationService.publishServiceCategoryDeleteNotification(serviceCategory);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceCategoryDeleteNotification.class), eq("test-category-456"));
        verify(serviceCatalogCallbackService, times(1)).sendServiceCategoryDeleteCallback(any());
    }

    @Test
    public void testPublishServiceCategoryCreateNotificationWithNullUuid() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("Test Service Category with Null UUID");
        serviceCategory.setDescription("A test service category with null UUID");

        // Act
        serviceCategoryNotificationService.publishServiceCategoryCreateNotification(serviceCategory);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceCategoryCreateNotification.class), eq(null));
        verify(serviceCatalogCallbackService, times(1)).sendServiceCategoryCreateCallback(any());
    }

    @Test
    public void testPublishServiceCategoryDeleteNotificationWithNullUuid() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("Test Service Category with Null UUID");
        serviceCategory.setDescription("A test service category with null UUID");

        // Act
        serviceCategoryNotificationService.publishServiceCategoryDeleteNotification(serviceCategory);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceCategoryDeleteNotification.class), eq(null));
        verify(serviceCatalogCallbackService, times(1)).sendServiceCategoryDeleteCallback(any());
    }
}