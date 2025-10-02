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
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteEvent;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationCallbackService;
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

public class ProductSpecificationCallbackServiceIntegrationTest  extends BaseIT{

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductSpecificationCallbackService productSpecificationCallbackService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendProductSpecificationCreateCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productspecification");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductSpecificationCreateEvent event = new ProductSpecificationCreateEvent();
        event.setEventId("test-event-123");

        // Act
        productSpecificationCallbackService.sendProductSpecificationCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productSpecificationCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductSpecificationDeleteCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productspecification");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductSpecificationDeleteEvent event = new ProductSpecificationDeleteEvent();
        event.setEventId("test-event-456");

        // Act
        productSpecificationCallbackService.sendProductSpecificationDeleteCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productSpecificationDeleteEvent"), 
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
        subscription.setQuery("productspecification");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductSpecificationCreateEvent event = new ProductSpecificationCreateEvent();
        event.setEventId("test-event-789");

        // Act
        productSpecificationCallbackService.sendProductSpecificationCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productSpecificationCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testFilterSubscriptionsByQuery() {
        // Arrange
        EventSubscription productSpecSubscription = new EventSubscription();
        productSpecSubscription.setCallback("http://localhost:8080/productspec-callback");
        productSpecSubscription.setQuery("productspecification");

        EventSubscription otherSubscription = new EventSubscription();
        otherSubscription.setCallback("http://localhost:8080/other-callback");
        otherSubscription.setQuery("catalog");

        List<EventSubscription> subscriptions = Arrays.asList(productSpecSubscription, otherSubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductSpecificationCreateEvent event = new ProductSpecificationCreateEvent();
        event.setEventId("test-event-filter");

        // Act
        productSpecificationCallbackService.sendProductSpecificationCreateCallback(event);

        // Assert - only product specification subscription should receive callback
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/productspec-callback/listener/productSpecificationCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testProductSpecificationSpecificQueries() {
        // Arrange
        EventSubscription createOnlySubscription = new EventSubscription();
        createOnlySubscription.setCallback("http://localhost:9090/create-only");
        createOnlySubscription.setQuery("productspecification.create");

        List<EventSubscription> subscriptions = Arrays.asList(createOnlySubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductSpecificationCreateEvent event = new ProductSpecificationCreateEvent();
        event.setEventId("test-event-specific-query");

        // Act
        productSpecificationCallbackService.sendProductSpecificationCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:9090/create-only/listener/productSpecificationCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }
}