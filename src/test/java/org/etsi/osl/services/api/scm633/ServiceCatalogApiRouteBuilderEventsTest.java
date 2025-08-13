package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.camel.ProducerTemplate;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
public class ServiceCatalogApiRouteBuilderEventsTest {

    @Mock
    private ProducerTemplate template;

    @Mock
    private CentralLogger centralLogger;

    @InjectMocks
    private ServiceCatalogApiRouteBuilderEvents routeBuilderEvents;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Set event topic properties using reflection
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_CATALOG_CREATE", "direct:EVENT_SERVICE_CATALOG_CREATE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_CATALOG_DELETE", "direct:EVENT_SERVICE_CATALOG_DELETE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_CATEGORY_CREATE", "direct:EVENT_SERVICE_CATEGORY_CREATE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_CATEGORY_DELETE", "direct:EVENT_SERVICE_CATEGORY_DELETE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_SPECIFICATION_CREATE", "direct:EVENT_SERVICE_SPECIFICATION_CREATE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_SPECIFICATION_DELETE", "direct:EVENT_SERVICE_SPECIFICATION_DELETE");
        ReflectionTestUtils.setField(routeBuilderEvents, "EVENT_SERVICE_SPECIFICATION_CHANGE", "direct:EVENT_SERVICE_SPECIFICATION_CHANGE");
    }

    @Test
    public void testPublishServiceCatalogCreateEvent() {
        // Arrange
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setUuid("test-catalog-123");

        ServiceCatalogCreateNotification notification = new ServiceCatalogCreateNotification();
        notification.setEventId("event-123");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-catalog-123");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_CATALOG_CREATE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceCatalogDeleteEvent() {
        // Arrange
        ServiceCatalogDeleteNotification notification = new ServiceCatalogDeleteNotification();
        notification.setEventId("event-456");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-catalog-456");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_CATALOG_DELETE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceCategoryCreateEvent() {
        // Arrange
        ServiceCategoryCreateNotification notification = new ServiceCategoryCreateNotification();
        notification.setEventId("event-789");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-category-789");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_CATEGORY_CREATE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceCategoryDeleteEvent() {
        // Arrange
        ServiceCategoryDeleteNotification notification = new ServiceCategoryDeleteNotification();
        notification.setEventId("event-101112");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-category-101112");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_CATEGORY_DELETE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceSpecificationCreateEvent() {
        // Arrange
        ServiceSpecificationCreateNotification notification = new ServiceSpecificationCreateNotification();
        notification.setEventId("event-131415");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-spec-131415");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_SPECIFICATION_CREATE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceSpecificationDeleteEvent() {
        // Arrange
        ServiceSpecificationDeleteNotification notification = new ServiceSpecificationDeleteNotification();
        notification.setEventId("event-161718");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-spec-161718");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_SPECIFICATION_DELETE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishServiceSpecificationChangeEvent() {
        // Arrange
        ServiceSpecificationChangeNotification notification = new ServiceSpecificationChangeNotification();
        notification.setEventId("event-192021");
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-spec-192021");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_SPECIFICATION_CHANGE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishEventWithNullEventId() {
        // Arrange
        ServiceCatalogCreateNotification notification = new ServiceCatalogCreateNotification();
        notification.setEventId(null);
        notification.setEventTime(OffsetDateTime.now(ZoneOffset.UTC));

        // Act
        routeBuilderEvents.publishEvent(notification, "test-catalog-null");

        // Assert
        verify(template, times(1)).sendBodyAndHeaders(
            eq("direct:EVENT_SERVICE_CATALOG_CREATE"), 
            any(String.class), 
            any(Map.class)
        );
    }

    @Test
    public void testPublishEventWithDifferentNotificationTypes() {
        // Test that all notification types are handled correctly

        // Service Catalog
        ServiceCatalogCreateNotification catalogCreate = new ServiceCatalogCreateNotification();
        catalogCreate.setEventId("catalog-create");
        routeBuilderEvents.publishEvent(catalogCreate, "catalog-id");

        ServiceCatalogDeleteNotification catalogDelete = new ServiceCatalogDeleteNotification();
        catalogDelete.setEventId("catalog-delete");
        routeBuilderEvents.publishEvent(catalogDelete, "catalog-id");

        // Service Category
        ServiceCategoryCreateNotification categoryCreate = new ServiceCategoryCreateNotification();
        categoryCreate.setEventId("category-create");
        routeBuilderEvents.publishEvent(categoryCreate, "category-id");

        ServiceCategoryDeleteNotification categoryDelete = new ServiceCategoryDeleteNotification();
        categoryDelete.setEventId("category-delete");
        routeBuilderEvents.publishEvent(categoryDelete, "category-id");

        // Service Specification
        ServiceSpecificationCreateNotification specCreate = new ServiceSpecificationCreateNotification();
        specCreate.setEventId("spec-create");
        routeBuilderEvents.publishEvent(specCreate, "spec-id");

        ServiceSpecificationDeleteNotification specDelete = new ServiceSpecificationDeleteNotification();
        specDelete.setEventId("spec-delete");
        routeBuilderEvents.publishEvent(specDelete, "spec-id");

        ServiceSpecificationChangeNotification specChange = new ServiceSpecificationChangeNotification();
        specChange.setEventId("spec-change");
        routeBuilderEvents.publishEvent(specChange, "spec-id");

        // Assert all events were published
        verify(template, times(7)).sendBodyAndHeaders(any(String.class), any(String.class), any(Map.class));
    }
}