package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.etsi.osl.tmf.pcm620.model.ProductOfferingPrice;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeEvent;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingPriceCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
public class ProductOfferingPriceCallbackServiceTest {

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductOfferingPriceCallbackService productOfferingPriceCallbackService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendProductOfferingPriceCreateCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productofferingprice");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceCreateEvent event = new ProductOfferingPriceCreateEvent();
        event.setEventId("test-event-123");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingPriceCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingPriceDeleteCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productofferingprice");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceDeleteEvent event = new ProductOfferingPriceDeleteEvent();
        event.setEventId("test-event-456");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceDeleteCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingPriceDeleteEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingPriceAttributeValueChangeCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productofferingprice.attributevaluechange");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceAttributeValueChangeEvent event = new ProductOfferingPriceAttributeValueChangeEvent();
        event.setEventId("test-event-789");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceAttributeValueChangeCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingPriceAttributeValueChangeEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendProductOfferingPriceStateChangeCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("productofferingprice.statechange");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceStateChangeEvent event = new ProductOfferingPriceStateChangeEvent();
        event.setEventId("test-event-101");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceStateChangeCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingPriceStateChangeEvent"), 
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
        subscription.setQuery("productofferingprice");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceCreateEvent event = new ProductOfferingPriceCreateEvent();
        event.setEventId("test-event-trailing-slash");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/productOfferingPriceCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testFilterSubscriptionsByQuery() {
        // Arrange
        EventSubscription productOfferingPriceSubscription = new EventSubscription();
        productOfferingPriceSubscription.setCallback("http://localhost:8080/productofferingprice-callback");
        productOfferingPriceSubscription.setQuery("productofferingprice");

        EventSubscription otherSubscription = new EventSubscription();
        otherSubscription.setCallback("http://localhost:8080/other-callback");
        otherSubscription.setQuery("catalog");

        List<EventSubscription> subscriptions = Arrays.asList(productOfferingPriceSubscription, otherSubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceCreateEvent event = new ProductOfferingPriceCreateEvent();
        event.setEventId("test-event-filter");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceCreateCallback(event);

        // Assert - only product offering price subscription should receive callback
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/productofferingprice-callback/listener/productOfferingPriceCreateEvent"), 
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
        createOnlySubscription.setQuery("productofferingprice.create");

        EventSubscription stateChangeOnlySubscription = new EventSubscription();
        stateChangeOnlySubscription.setCallback("http://localhost:9091/state-change-only");
        stateChangeOnlySubscription.setQuery("productofferingprice.statechange");

        List<EventSubscription> subscriptions = Arrays.asList(createOnlySubscription, stateChangeOnlySubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ProductOfferingPriceCreateEvent createEvent = new ProductOfferingPriceCreateEvent();
        createEvent.setEventId("test-create-event");

        ProductOfferingPriceStateChangeEvent stateChangeEvent = new ProductOfferingPriceStateChangeEvent();
        stateChangeEvent.setEventId("test-state-change-event");

        // Act
        productOfferingPriceCallbackService.sendProductOfferingPriceCreateCallback(createEvent);
        productOfferingPriceCallbackService.sendProductOfferingPriceStateChangeCallback(stateChangeEvent);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:9090/create-only/listener/productOfferingPriceCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:9091/state-change-only/listener/productOfferingPriceStateChangeEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }
}