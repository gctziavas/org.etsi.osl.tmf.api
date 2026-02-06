package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeEvent;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingCallbackService;
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


public class ProductOfferingCallbackServiceIntegrationTest extends BaseIT{

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductOfferingCallbackService productOfferingCallbackService;

    private AutoCloseable mocks;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

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
    public void testSendProductOfferingCreateCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productoffering");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingCreateEvent event = new ProductOfferingCreateEvent();
        event.setEventId("test-event-123");

        // Act
        productOfferingCallbackService.sendProductOfferingCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingDeleteCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productoffering");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingDeleteEvent event = new ProductOfferingDeleteEvent();
        event.setEventId("test-event-456");

        // Act
        productOfferingCallbackService.sendProductOfferingDeleteCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingDeleteEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingAttributeValueChangeCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productoffering.attributevaluechange");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingAttributeValueChangeEvent event = new ProductOfferingAttributeValueChangeEvent();
        event.setEventId("test-event-789");

        // Act
        productOfferingCallbackService.sendProductOfferingAttributeValueChangeCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingAttributeValueChangeEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingStateChangeCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productoffering.statechange");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingStateChangeEvent event = new ProductOfferingStateChangeEvent();
        event.setEventId("test-event-101");

        // Act
        productOfferingCallbackService.sendProductOfferingStateChangeCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingStateChangeEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testCallbackWithTrailingSlash() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback/");
        subscription.setQuery("productoffering");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingCreateEvent event = new ProductOfferingCreateEvent();
        event.setEventId("test-event-trailing-slash");

        // Act
        productOfferingCallbackService.sendProductOfferingCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testFilterSubscriptionsByQuery() {
        // Arrange
        EventSubscription productOfferingSubscription = new EventSubscription();
        productOfferingSubscription.setCallback("http://localhost:8080/productoffering-callback");
        productOfferingSubscription.setQuery("productoffering");

        EventSubscription otherSubscription = new EventSubscription();
        otherSubscription.setCallback("http://localhost:8080/other-callback");
        otherSubscription.setQuery("catalog");

        List<EventSubscription> subscriptions = Arrays.asList(productOfferingSubscription, otherSubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingCreateEvent event = new ProductOfferingCreateEvent();
        event.setEventId("test-event-filter");

        // Act
        productOfferingCallbackService.sendProductOfferingCreateCallback(event);

        // Assert - only product offering subscription should receive callback
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/productoffering-callback/listener/productOfferingCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSpecificEventTypeQueries() {
        // Arrange
        EventSubscription createOnlySubscription = new EventSubscription();
        createOnlySubscription.setCallback("http://localhost:9090/create-only");
        createOnlySubscription.setQuery("productoffering.create");

        EventSubscription stateChangeOnlySubscription = new EventSubscription();
        stateChangeOnlySubscription.setCallback("http://localhost:9091/state-change-only");
        stateChangeOnlySubscription.setQuery("productoffering.statechange");

        List<EventSubscription> subscriptions = Arrays.asList(createOnlySubscription, stateChangeOnlySubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingCreateEvent createEvent = new ProductOfferingCreateEvent();
        createEvent.setEventId("test-create-event");

        ProductOfferingStateChangeEvent stateChangeEvent = new ProductOfferingStateChangeEvent();
        stateChangeEvent.setEventId("test-state-change-event");

        // Act
        productOfferingCallbackService.sendProductOfferingCreateCallback(createEvent);
        productOfferingCallbackService.sendProductOfferingStateChangeCallback(stateChangeEvent);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:9090/create-only/listener/productOfferingCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:9091/state-change-only/listener/productOfferingStateChangeEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }
}