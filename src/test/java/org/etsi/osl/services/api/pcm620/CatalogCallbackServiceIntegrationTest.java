package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.model.Catalog;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateEventPayload;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEventPayload;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.reposervices.CatalogCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class CatalogCallbackServiceIntegrationTest extends BaseIT {

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CatalogCallbackService catalogCallbackService;

    private AutoCloseable mocks;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    public void tearDown() throws Exception {
      if (entityManager != null) {
        entityManager.clear();
    }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testSendCatalogCreateCallback() {
        // Arrange
        EventSubscription subscription1 = createSubscription("1", "http://localhost:8080/callback", "catalog.create");
        EventSubscription subscription2 = createSubscription("2", "http://localhost:9090/webhook", "catalog");
        List<EventSubscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        CatalogCreateEvent event = createCatalogCreateEvent();

        // Act
        catalogCallbackService.sendCatalogCreateCallback(event);

        // Assert
        verify(restTemplate, times(2)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSendCatalogDeleteCallback() {
        // Arrange
        EventSubscription subscription = createSubscription("1", "http://localhost:8080/callback", "catalog.delete");
        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        CatalogDeleteEvent event = createCatalogDeleteEvent();

        // Act
        catalogCallbackService.sendCatalogDeleteCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testCallbackUrlBuilding() {
        // Arrange
        EventSubscription subscription1 = createSubscription("1", "http://localhost:8080/callback", "catalog");
        EventSubscription subscription2 = createSubscription("2", "http://localhost:8080/callback/", "catalog");
        List<EventSubscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        CatalogCreateEvent event = createCatalogCreateEvent();

        // Act
        catalogCallbackService.sendCatalogCreateCallback(event);

        // Assert - Both should result in the same URL format
        verify(restTemplate, times(2)).exchange(eq("http://localhost:8080/callback/listener/catalogCreateEvent"), 
            eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testNoSubscriptions() {
        // Arrange
        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList());
        CatalogCreateEvent event = createCatalogCreateEvent();

        // Act
        catalogCallbackService.sendCatalogCreateCallback(event);

        // Assert - No calls should be made
        verify(restTemplate, times(0)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    private EventSubscription createSubscription(String id, String callback, String query) {
        EventSubscription subscription = new EventSubscription();
        subscription.setId(id);
        subscription.setCallback(callback);
        subscription.setQuery(query);
        return subscription;
    }

    private CatalogCreateEvent createCatalogCreateEvent() {
        CatalogCreateEvent event = new CatalogCreateEvent();
        event.setEventId("test-event-123");
        event.setEventType("CatalogCreateEvent");
        
        CatalogCreateEventPayload payload = new CatalogCreateEventPayload();
        Catalog catalog = new Catalog();
        catalog.setUuid("test-catalog-123");
        catalog.setName("Test Catalog");
        payload.setCatalog(catalog);
        
        event.setEvent(payload);
        return event;
    }

    private CatalogDeleteEvent createCatalogDeleteEvent() {
        CatalogDeleteEvent event = new CatalogDeleteEvent();
        event.setEventId("test-delete-event-123");
        event.setEventType("CatalogDeleteEvent");
        
        CatalogDeleteEventPayload payload = new CatalogDeleteEventPayload();
        Catalog catalog = new Catalog();
        catalog.setUuid("test-catalog-123");
        catalog.setName("Test Catalog");
        payload.setCatalog(catalog);
        
        event.setEvent(payload);
        return event;
    }
}