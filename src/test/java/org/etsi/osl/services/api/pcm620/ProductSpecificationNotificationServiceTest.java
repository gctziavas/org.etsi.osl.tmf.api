package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteNotification;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationNotificationService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationCallbackService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
public class ProductSpecificationNotificationServiceTest {

    @Mock
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Mock
    private ProductSpecificationCallbackService productSpecificationCallbackService;

    @InjectMocks
    private ProductSpecificationNotificationService productSpecificationNotificationService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishProductSpecificationCreateNotification() {
        // Arrange
        ProductSpecification productSpecification = new ProductSpecification();
        productSpecification.setUuid("test-productspec-123");
        productSpecification.setName("Test Product Specification");
        productSpecification.setDescription("A test product specification for notifications");

        // Act
        productSpecificationNotificationService.publishProductSpecificationCreateNotification(productSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductSpecificationCreateNotification.class), eq("test-productspec-123"));
        verify(productSpecificationCallbackService, times(1)).sendProductSpecificationCreateCallback(any());
    }

    @Test
    public void testPublishProductSpecificationDeleteNotification() {
        // Arrange
        ProductSpecification productSpecification = new ProductSpecification();
        productSpecification.setUuid("test-productspec-456");
        productSpecification.setName("Test Product Specification to Delete");
        productSpecification.setDescription("A test product specification for delete notifications");

        // Act
        productSpecificationNotificationService.publishProductSpecificationDeleteNotification(productSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductSpecificationDeleteNotification.class), eq("test-productspec-456"));
        verify(productSpecificationCallbackService, times(1)).sendProductSpecificationDeleteCallback(any());
    }

    @Test
    public void testCreateNotificationStructure() {
        // Arrange
        ProductSpecification productSpecification = new ProductSpecification();
        productSpecification.setUuid("test-productspec-789");
        productSpecification.setName("Test Product Specification Structure");

        // Act
        productSpecificationNotificationService.publishProductSpecificationCreateNotification(productSpecification);

        // Assert - verify the notification was published with correct structure
        verify(eventPublisher).publishEvent(any(ProductSpecificationCreateNotification.class), eq("test-productspec-789"));
        verify(productSpecificationCallbackService).sendProductSpecificationCreateCallback(any());
    }
}