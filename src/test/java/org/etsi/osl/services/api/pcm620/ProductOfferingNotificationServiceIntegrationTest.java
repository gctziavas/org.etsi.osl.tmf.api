package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeNotification;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
public class ProductOfferingNotificationServiceIntegrationTest  extends BaseIT{

    @Mock
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Mock
    private ProductOfferingCallbackService productOfferingCallbackService;

    @InjectMocks
    private ProductOfferingNotificationService productOfferingNotificationService;

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
        // Clear entity manager cache to release entity references
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @Test
    public void testPublishProductOfferingCreateNotification() {
        // Arrange
        ProductOffering productOffering = new ProductOffering();
        productOffering.setUuid("test-productoffering-123");
        productOffering.setName("Test Product Offering");
        productOffering.setDescription("A test product offering for notifications");

        // Act
        productOfferingNotificationService.publishProductOfferingCreateNotification(productOffering);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingCreateNotification.class), eq("test-productoffering-123"));
        verify(productOfferingCallbackService, times(1)).sendProductOfferingCreateCallback(any());
    }

    @Test
    public void testPublishProductOfferingDeleteNotification() {
        // Arrange
        ProductOffering productOffering = new ProductOffering();
        productOffering.setUuid("test-productoffering-456");
        productOffering.setName("Test Product Offering to Delete");
        productOffering.setDescription("A test product offering for delete notifications");

        // Act
        productOfferingNotificationService.publishProductOfferingDeleteNotification(productOffering);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingDeleteNotification.class), eq("test-productoffering-456"));
        verify(productOfferingCallbackService, times(1)).sendProductOfferingDeleteCallback(any());
    }

    @Test
    public void testPublishProductOfferingAttributeValueChangeNotification() {
        // Arrange
        ProductOffering productOffering = new ProductOffering();
        productOffering.setUuid("test-productoffering-789");
        productOffering.setName("Test Product Offering Attribute Change");
        productOffering.setDescription("A test product offering for attribute change notifications");

        // Act
        productOfferingNotificationService.publishProductOfferingAttributeValueChangeNotification(productOffering);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingAttributeValueChangeNotification.class), eq("test-productoffering-789"));
        verify(productOfferingCallbackService, times(1)).sendProductOfferingAttributeValueChangeCallback(any());
    }

    @Test
    public void testPublishProductOfferingStateChangeNotification() {
        // Arrange
        ProductOffering productOffering = new ProductOffering();
        productOffering.setUuid("test-productoffering-101");
        productOffering.setName("Test Product Offering State Change");
        productOffering.setDescription("A test product offering for state change notifications");

        // Act
        productOfferingNotificationService.publishProductOfferingStateChangeNotification(productOffering);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingStateChangeNotification.class), eq("test-productoffering-101"));
        verify(productOfferingCallbackService, times(1)).sendProductOfferingStateChangeCallback(any());
    }

    @Test
    public void testCreateNotificationStructure() {
        // Arrange
        ProductOffering productOffering = new ProductOffering();
        productOffering.setUuid("test-productoffering-structure");
        productOffering.setName("Test Product Offering Structure");

        // Act
        productOfferingNotificationService.publishProductOfferingCreateNotification(productOffering);

        // Assert - verify the notification was published with correct structure
        verify(eventPublisher).publishEvent(any(ProductOfferingCreateNotification.class), eq("test-productoffering-structure"));
        verify(productOfferingCallbackService).sendProductOfferingCreateCallback(any());
    }
}