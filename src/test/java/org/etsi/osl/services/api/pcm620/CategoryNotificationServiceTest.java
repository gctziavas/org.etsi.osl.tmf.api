package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.Category;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateNotification;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteNotification;
import org.etsi.osl.tmf.pcm620.reposervices.CategoryCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.CategoryNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
public class CategoryNotificationServiceTest  extends BaseIT{

    @Mock
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Mock
    private CategoryCallbackService categoryCallbackService;

    @InjectMocks
    private CategoryNotificationService categoryNotificationService;

    private AutoCloseable mocks;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testPublishCategoryCreateNotification() {
        // Arrange
        Category category = new Category();
        category.setUuid("test-category-123");
        category.setName("Test Category");
        category.setDescription("A test category for notifications");

        // Act
        categoryNotificationService.publishCategoryCreateNotification(category);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(CategoryCreateNotification.class), eq("test-category-123"));
        verify(categoryCallbackService, times(1)).sendCategoryCreateCallback(any());
    }

    @Test
    public void testPublishCategoryDeleteNotification() {
        // Arrange
        Category category = new Category();
        category.setUuid("test-category-456");
        category.setName("Test Category to Delete");
        category.setDescription("A test category for delete notifications");

        // Act
        categoryNotificationService.publishCategoryDeleteNotification(category);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(CategoryDeleteNotification.class), eq("test-category-456"));
        verify(categoryCallbackService, times(1)).sendCategoryDeleteCallback(any());
    }

    @Test
    public void testCreateNotificationStructure() {
        // Arrange
        Category category = new Category();
        category.setUuid("test-category-789");
        category.setName("Test Category Structure");

        // Act
        categoryNotificationService.publishCategoryCreateNotification(category);

        // Assert - verify the notification was published with correct structure
        verify(eventPublisher).publishEvent(any(CategoryCreateNotification.class), eq("test-category-789"));
        verify(categoryCallbackService).sendCategoryCreateCallback(any());
    }
}