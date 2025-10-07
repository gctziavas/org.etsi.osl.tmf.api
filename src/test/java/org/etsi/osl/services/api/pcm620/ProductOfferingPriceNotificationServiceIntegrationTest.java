package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPrice;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteNotification;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeNotification;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingPriceCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingPriceNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
public class ProductOfferingPriceNotificationServiceIntegrationTest  extends BaseIT{

    @Mock
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @Mock
    private ProductOfferingPriceCallbackService productOfferingPriceCallbackService;

    @InjectMocks
    private ProductOfferingPriceNotificationService productOfferingPriceNotificationService;

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
    public void testPublishProductOfferingPriceCreateNotification() {
        // Arrange
        ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
        productOfferingPrice.setUuid("test-productofferingprice-123");
        productOfferingPrice.setName("Test Product Offering Price");
        productOfferingPrice.setDescription("A test product offering price for notifications");

        // Act
        productOfferingPriceNotificationService.publishProductOfferingPriceCreateNotification(productOfferingPrice);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingPriceCreateNotification.class), eq("test-productofferingprice-123"));
        verify(productOfferingPriceCallbackService, times(1)).sendProductOfferingPriceCreateCallback(any());
    }

    @Test
    public void testPublishProductOfferingPriceDeleteNotification() {
        // Arrange
        ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
        productOfferingPrice.setUuid("test-productofferingprice-456");
        productOfferingPrice.setName("Test Product Offering Price to Delete");
        productOfferingPrice.setDescription("A test product offering price for delete notifications");

        // Act
        productOfferingPriceNotificationService.publishProductOfferingPriceDeleteNotification(productOfferingPrice);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingPriceDeleteNotification.class), eq("test-productofferingprice-456"));
        verify(productOfferingPriceCallbackService, times(1)).sendProductOfferingPriceDeleteCallback(any());
    }

    @Test
    public void testPublishProductOfferingPriceAttributeValueChangeNotification() {
        // Arrange
        ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
        productOfferingPrice.setUuid("test-productofferingprice-789");
        productOfferingPrice.setName("Test Product Offering Price Attribute Change");
        productOfferingPrice.setDescription("A test product offering price for attribute change notifications");

        // Act
        productOfferingPriceNotificationService.publishProductOfferingPriceAttributeValueChangeNotification(productOfferingPrice);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingPriceAttributeValueChangeNotification.class), eq("test-productofferingprice-789"));
        verify(productOfferingPriceCallbackService, times(1)).sendProductOfferingPriceAttributeValueChangeCallback(any());
    }

    @Test
    public void testPublishProductOfferingPriceStateChangeNotification() {
        // Arrange
        ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
        productOfferingPrice.setUuid("test-productofferingprice-101");
        productOfferingPrice.setName("Test Product Offering Price State Change");
        productOfferingPrice.setDescription("A test product offering price for state change notifications");

        // Act
        productOfferingPriceNotificationService.publishProductOfferingPriceStateChangeNotification(productOfferingPrice);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ProductOfferingPriceStateChangeNotification.class), eq("test-productofferingprice-101"));
        verify(productOfferingPriceCallbackService, times(1)).sendProductOfferingPriceStateChangeCallback(any());
    }

    @Test
    public void testCreateNotificationStructure() {
        // Arrange
        ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
        productOfferingPrice.setUuid("test-productofferingprice-structure");
        productOfferingPrice.setName("Test Product Offering Price Structure");

        // Act
        productOfferingPriceNotificationService.publishProductOfferingPriceCreateNotification(productOfferingPrice);

        // Assert - verify the notification was published with correct structure
        verify(eventPublisher).publishEvent(any(ProductOfferingPriceCreateNotification.class), eq("test-productofferingprice-structure"));
        verify(productOfferingPriceCallbackService).sendProductOfferingPriceCreateCallback(any());
    }
}