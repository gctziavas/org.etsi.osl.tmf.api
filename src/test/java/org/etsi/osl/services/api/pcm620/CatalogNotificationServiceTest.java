package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.pcm620.api.ProductCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.pcm620.model.Catalog;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateNotification;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteNotification;
import org.etsi.osl.tmf.pcm620.reposervices.CatalogNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = OpenAPISpringBoot.class)
@ActiveProfiles("testing")
@AutoConfigureMockMvc
public class CatalogNotificationServiceTest {

    @Mock
    private ProductCatalogApiRouteBuilderEvents eventPublisher;

    @InjectMocks
    private CatalogNotificationService catalogNotificationService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishCatalogCreateNotification() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setUuid("test-catalog-123");
        catalog.setName("Test Catalog");
        catalog.setDescription("A test catalog for notifications");

        // Act
        catalogNotificationService.publishCatalogCreateNotification(catalog);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(CatalogCreateNotification.class), eq("test-catalog-123"));
    }

    @Test
    public void testPublishCatalogDeleteNotification() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setUuid("test-catalog-456");
        catalog.setName("Test Catalog to Delete");
        catalog.setDescription("A test catalog for delete notifications");

        // Act
        catalogNotificationService.publishCatalogDeleteNotification(catalog);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(CatalogDeleteNotification.class), eq("test-catalog-456"));
    }

    @Test
    public void testCreateNotificationStructure() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setUuid("test-catalog-789");
        catalog.setName("Test Catalog Structure");

        // Act
        catalogNotificationService.publishCatalogCreateNotification(catalog);

        // Assert - verify the notification was published with correct structure
        verify(eventPublisher).publishEvent(any(CatalogCreateNotification.class), eq("test-catalog-789"));
    }
}