package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.scm633.model.EventSubscription;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreate;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.etsi.osl.tmf.scm633.reposervices.CatalogRepoService;
import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
import org.etsi.osl.tmf.scm633.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCatalogCallbackService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCatalogNotificationService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCategoryNotificationService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationNotificationService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class ServiceCatalogManagementNotificationEndToEndTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CatalogRepoService catalogRepoService;

    @Autowired
    private CategoryRepoService categoryRepoService;

    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @SpyBean
    @Qualifier("scm633EventSubscriptionRepoService")
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @SpyBean
    private ServiceCatalogNotificationService serviceCatalogNotificationService;

    @SpyBean
    private ServiceCategoryNotificationService serviceCategoryNotificationService;

    @SpyBean
    private ServiceSpecificationNotificationService serviceSpecificationNotificationService;

    // NOTE: We don't use @SpyBean on ServiceCatalogApiRouteBuilderEvents because it extends Camel's RouteBuilder
    // which has void methods that cause Mockito conflicts during Spring context initialization.
    // Instead, we verify the notification flow at the service level, which provides sufficient coverage.

    @SpyBean
    private ServiceCatalogCallbackService callbackService;

    @MockBean
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Reset all mocks to clear state between tests
        reset(eventSubscriptionRepoService);
        reset(serviceCatalogNotificationService);
        reset(serviceCategoryNotificationService);
        reset(serviceSpecificationNotificationService);
        reset(callbackService);
        reset(restTemplate);
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteServiceCatalogManagementNotificationFlow() {
        // Setup event subscription for testing callbacks
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/test-callback");
        subscription.setQuery("servicecatalog");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(any(String.class), any(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Catalog lifecycle
        ServiceCatalogCreate catalogCreate = new ServiceCatalogCreate();
        catalogCreate.setName("End-to-End Test Catalog");
        catalogCreate.setDescription("A catalog for end-to-end testing");

        ServiceCatalog createdCatalog = catalogRepoService.addCatalog(catalogCreate);

        // Verify catalog create notification flow
        verify(serviceCatalogNotificationService, timeout(5000).times(1))
            .publishServiceCatalogCreateNotification(any(ServiceCatalog.class));
        verify(callbackService, timeout(5000).times(1))
            .sendServiceCatalogCreateCallback(any());

        // Delete catalog
        catalogRepoService.deleteById(createdCatalog.getUuid());

        // Verify catalog delete notification flow
        verify(serviceCatalogNotificationService, timeout(5000).times(1))
            .publishServiceCatalogDeleteNotification(any(ServiceCatalog.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteServiceCategoryNotificationFlow() {
        // Setup event subscription for testing callbacks
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/test-callback");
        subscription.setQuery("servicecategory");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(any(String.class), any(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Category lifecycle
        ServiceCategoryCreate categoryCreate = new ServiceCategoryCreate();
        categoryCreate.setName("End-to-End Test Category");
        categoryCreate.setDescription("A category for end-to-end testing");

        ServiceCategory createdCategory = categoryRepoService.addCategory(categoryCreate);

        // Verify category create notification flow
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryCreateNotification(any(ServiceCategory.class));
        verify(callbackService, timeout(5000).times(1))
            .sendServiceCategoryCreateCallback(any());

        // Delete category
        categoryRepoService.deleteById(createdCategory.getUuid());

        // Verify category delete notification flow
        verify(serviceCategoryNotificationService, timeout(5000).times(1))
            .publishServiceCategoryDeleteNotification(any(ServiceCategory.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteServiceSpecificationNotificationFlow() {
        // Setup event subscription for testing callbacks
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/test-callback");
        subscription.setQuery("servicespecification");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(any(String.class), any(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Specification lifecycle
        ServiceSpecificationCreate specCreate = new ServiceSpecificationCreate();
        specCreate.setName("End-to-End Test Specification");
        specCreate.setDescription("A specification for end-to-end testing");

        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(specCreate);

        // Verify specification create notification flow
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationCreateNotification(any(ServiceSpecification.class));
        verify(callbackService, timeout(5000).times(1))
            .sendServiceSpecificationCreateCallback(any());

        // Update specification
        ServiceSpecificationUpdate specUpdate = new ServiceSpecificationUpdate();
        specUpdate.setDescription("Updated description for end-to-end testing");
        
        ServiceSpecification updatedSpec = serviceSpecificationRepoService.updateServiceSpecification(createdSpec.getUuid(), specUpdate);

        // Verify specification change notification flow
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationChangeNotification(any(ServiceSpecification.class));

        // Delete specification
        serviceSpecificationRepoService.deleteByUuid(createdSpec.getUuid());

        // Verify specification delete notification flow
        verify(serviceSpecificationNotificationService, timeout(5000).times(1))
            .publishServiceSpecificationDeleteNotification(any(ServiceSpecification.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testMultipleSubscriptionsWithDifferentQueries() {
        // Setup multiple event subscriptions with different query filters
        EventSubscription allEventsSubscription = new EventSubscription();
        allEventsSubscription.setCallback("http://localhost:8080/all-events");
        // No query - should receive all events

        EventSubscription catalogOnlySubscription = new EventSubscription();
        catalogOnlySubscription.setCallback("http://localhost:8080/catalog-only");
        catalogOnlySubscription.setQuery("servicecatalog");

        EventSubscription categoryOnlySubscription = new EventSubscription();
        categoryOnlySubscription.setCallback("http://localhost:8080/category-only");
        categoryOnlySubscription.setQuery("servicecategory");

        EventSubscription specOnlySubscription = new EventSubscription();
        specOnlySubscription.setCallback("http://localhost:8080/spec-only");
        specOnlySubscription.setQuery("servicespecification");

        List<EventSubscription> subscriptions = Arrays.asList(
            allEventsSubscription, catalogOnlySubscription, categoryOnlySubscription, specOnlySubscription
        );

        when(eventSubscriptionRepoService.findAll()).thenReturn(subscriptions);
        when(restTemplate.exchange(any(String.class), any(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Create entities to trigger notifications
        ServiceCatalogCreate catalogCreate = new ServiceCatalogCreate();
        catalogCreate.setName("Multi-Subscription Test Catalog");
        ServiceCatalog catalog = catalogRepoService.addCatalog(catalogCreate);

        ServiceCategoryCreate categoryCreate = new ServiceCategoryCreate();
        categoryCreate.setName("Multi-Subscription Test Category");
        ServiceCategory category = categoryRepoService.addCategory(categoryCreate);

        ServiceSpecificationCreate specCreate = new ServiceSpecificationCreate();
        specCreate.setName("Multi-Subscription Test Specification");
        ServiceSpecification spec = serviceSpecificationRepoService.addServiceSpecification(specCreate);

        // Verify that callbacks are sent according to subscription filters
        // All events subscription should receive all 3 callbacks
        // Each specific subscription should receive only their relevant callback

        verify(callbackService, timeout(5000).times(1))
            .sendServiceCatalogCreateCallback(any());
        verify(callbackService, timeout(5000).times(1))
            .sendServiceCategoryCreateCallback(any());
        verify(callbackService, timeout(5000).times(1))
            .sendServiceSpecificationCreateCallback(any());

        // Verify multiple HTTP calls are made for different subscriptions
        verify(restTemplate, timeout(5000).atLeast(6))
            .exchange(any(String.class), any(), any(), eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testNotificationSystemWithInvalidCallback() {
        // Setup event subscription with invalid callback URL
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://invalid-callback-url:9999/callback");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(any(String.class), any(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        // Create entity to trigger notification
        ServiceCatalogCreate catalogCreate = new ServiceCatalogCreate();
        catalogCreate.setName("Invalid Callback Test Catalog");
        ServiceCatalog catalog = catalogRepoService.addCatalog(catalogCreate);

        // Verify notification service still completes successfully even with callback failure
        verify(serviceCatalogNotificationService, timeout(5000).times(1))
            .publishServiceCatalogCreateNotification(any(ServiceCatalog.class));
        
        // Callback should be attempted but may fail gracefully
        verify(callbackService, timeout(5000).times(1))
            .sendServiceCatalogCreateCallback(any());
    }
}