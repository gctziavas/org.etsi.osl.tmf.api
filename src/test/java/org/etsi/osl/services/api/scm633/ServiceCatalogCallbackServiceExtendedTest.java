package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeEvent;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCatalogCallbackService;
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
public class ServiceCatalogCallbackServiceExtendedTest {

    @Mock
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendServiceCategoryCreateCallback() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setUuid("test-category-123");
        serviceCategory.setName("Test Category");

        ServiceCategoryCreateEvent event = new ServiceCategoryCreateEvent();
        event.setServiceCategory(serviceCategory);

        EventSubscription subscription1 = new EventSubscription();
        subscription1.setId("sub-1");
        subscription1.setCallback("http://localhost:8080/callback");

        EventSubscription subscription2 = new EventSubscription();
        subscription2.setId("sub-2");
        subscription2.setCallback("http://localhost:8081/callback");
        subscription2.setQuery("servicecategory");

        List<EventSubscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        ServiceCategoryCreateNotification notif = new ServiceCategoryCreateNotification();
        notif.setEvent(event);
        // Act
        serviceCatalogCallbackService.sendServiceCategoryCreateCallback(notif );

        // Assert
        verify(restTemplate, times(2)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSendServiceCategoryDeleteCallback() {
        // Arrange
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setUuid("test-category-456");
        serviceCategory.setName("Test Category to Delete");

        ServiceCategoryDeleteEvent event = new ServiceCategoryDeleteEvent();
        event.setServiceCategory(serviceCategory);

        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("servicecategory.delete");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));


        ServiceCategoryDeleteNotification notif = new ServiceCategoryDeleteNotification();
        notif.setEvent(event);
        // Act
        serviceCatalogCallbackService.sendServiceCategoryDeleteCallback(notif);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSendServiceSpecificationCreateCallback() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-123");
        serviceSpecification.setName("Test Specification");

        ServiceSpecificationCreateEvent event = new ServiceSpecificationCreateEvent();
        event.setServiceSpecification(serviceSpecification);

        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("servicespecification.create");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act

        ServiceSpecificationCreateNotification notif = new ServiceSpecificationCreateNotification();
        notif.setEvent(event);
        serviceCatalogCallbackService.sendServiceSpecificationCreateCallback(notif);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSendServiceSpecificationDeleteCallback() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-456");
        serviceSpecification.setName("Test Specification to Delete");

        ServiceSpecificationDeleteEvent event = new ServiceSpecificationDeleteEvent();
        event.setServiceSpecification(serviceSpecification);

        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act
        ServiceSpecificationDeleteNotification notif = new ServiceSpecificationDeleteNotification();
        notif.setEvent(event);
        serviceCatalogCallbackService.sendServiceSpecificationDeleteCallback(notif);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testSendServiceSpecificationChangeCallback() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-789");
        serviceSpecification.setName("Test Specification to Change");

        ServiceSpecificationChangeEvent event = new ServiceSpecificationChangeEvent();
        event.setServiceSpecification(serviceSpecification);

        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("servicespecification.change");

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act
        ServiceSpecificationChangeNotification notif = new ServiceSpecificationChangeNotification();
        notif.setEvent(event);
        serviceCatalogCallbackService.sendServiceSpecificationChangeCallback(notif);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testShouldNotifySubscriptionWithNoQuery() {
        // Test that subscriptions with no query receive all events
        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");
        // No query set

        ServiceCategoryCreateEvent event = new ServiceCategoryCreateEvent();
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("Test");
        event.setServiceCategory(serviceCategory);

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act
        ServiceCategoryCreateNotification notif = new ServiceCategoryCreateNotification();
        notif.setEvent(event);
        serviceCatalogCallbackService.sendServiceCategoryCreateCallback(notif);

        // Assert - should call callback even with no query
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testShouldNotifySubscriptionWithMatchingQuery() {
        // Test specific query matching
        EventSubscription subscription = new EventSubscription();
        subscription.setId("sub-1");
        subscription.setCallback("http://localhost:8080/callback");
        subscription.setQuery("servicespecification");

        ServiceSpecificationCreateEvent event = new ServiceSpecificationCreateEvent();
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setName("Test");
        event.setServiceSpecification(serviceSpecification);

        List<EventSubscription> subscriptions = Arrays.asList(subscription);

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Act
        ServiceSpecificationCreateNotification notif = new ServiceSpecificationCreateNotification();
        notif.setEvent(event);
        serviceCatalogCallbackService.sendServiceSpecificationCreateCallback(notif);

        // Assert
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}