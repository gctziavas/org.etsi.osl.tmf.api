package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.model.CategoryCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.reposervices.CategoryCallbackService;
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


public class CategoryCallbackServiceIntegrationTest  extends BaseIT{

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CategoryCallbackService categoryCallbackService;

    private AutoCloseable mocks;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        if (entityManager != null) {
          entityManager.clear();
      }
    }
    

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testSendCategoryCreateCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("category");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        CategoryCreateEvent event = new CategoryCreateEvent();
        event.setEventId("test-event-123");

        // Act
        categoryCallbackService.sendCategoryCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/categoryCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testSendCategoryDeleteCallback() {
        // Arrange
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("category");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        CategoryDeleteEvent event = new CategoryDeleteEvent();
        event.setEventId("test-event-456");

        // Act
        categoryCallbackService.sendCategoryDeleteCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/categoryDeleteEvent"), 
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
        subscription.setQuery("category");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        CategoryCreateEvent event = new CategoryCreateEvent();
        event.setEventId("test-event-789");

        // Act
        categoryCallbackService.sendCategoryCreateCallback(event);

        // Assert
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/callback/listener/categoryCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }

    @Test
    public void testFilterSubscriptionsByQuery() {
        // Arrange
        EventSubscription categorySubscription = new EventSubscription();
        categorySubscription.setCallback("http://localhost:8080/category-callback");
        categorySubscription.setQuery("category");

        EventSubscription otherSubscription = new EventSubscription();
        otherSubscription.setCallback("http://localhost:8080/other-callback");
        otherSubscription.setQuery("catalog");

        List<EventSubscription> subscriptions = Arrays.asList(categorySubscription, otherSubscription);
        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        CategoryCreateEvent event = new CategoryCreateEvent();
        event.setEventId("test-event-filter");

        // Act
        categoryCallbackService.sendCategoryCreateCallback(event);

        // Assert - only category subscription should receive callback
        verify(restTemplate, times(1)).exchange(
            eq("http://localhost:8080/category-callback/listener/categoryCreateEvent"), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class)
        );
    }
}