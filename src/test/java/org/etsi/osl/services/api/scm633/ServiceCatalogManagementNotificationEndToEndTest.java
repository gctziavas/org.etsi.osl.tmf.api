package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.ProducerTemplate;
import org.etsi.osl.services.api.BaseIT;
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
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;


public class ServiceCatalogManagementNotificationEndToEndTest  extends BaseIT{

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

    @MockBean
    @Qualifier("scm633EventSubscriptionRepoService")
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @MockBean
    private ProducerTemplate producerTemplate;

    @MockBean
    private RestTemplate restTemplate;

    private AutoCloseable mocks;

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Reset all mocks to clear state between tests
        reset(producerTemplate);
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
        when(restTemplate.exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Catalog lifecycle
        ServiceCatalogCreate catalogCreate = new ServiceCatalogCreate();
        catalogCreate.setName("End-to-End Test Catalog");
        catalogCreate.setDescription("A catalog for end-to-end testing");

        ServiceCatalog createdCatalog = catalogRepoService.addCatalog(catalogCreate);

        // Verify catalog create notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCatalogCreateNotification")),
            anyMap()
        );

        // Delete catalog
        catalogRepoService.deleteById(createdCatalog.getUuid());

        // Verify catalog delete notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCatalogDeleteNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteServiceCategoryNotificationFlow() {
        // Setup event subscription for testing callbacks
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/test-callback");
        subscription.setQuery("servicecategory");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Category lifecycle
        ServiceCategoryCreate categoryCreate = new ServiceCategoryCreate();
        categoryCreate.setName("End-to-End Test Category");
        categoryCreate.setDescription("A category for end-to-end testing");

        ServiceCategory createdCategory = categoryRepoService.addCategory(categoryCreate);

        // Verify category create notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryCreateNotification")),
            anyMap()
        );

        // Delete category
        categoryRepoService.deleteById(createdCategory.getUuid());

        // Verify category delete notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryDeleteNotification")),
            anyMap()
        );
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteServiceSpecificationNotificationFlow() {
        // Setup event subscription for testing callbacks
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://localhost:8080/test-callback");
        subscription.setQuery("servicespecification");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Test Service Specification lifecycle
        ServiceSpecificationCreate specCreate = new ServiceSpecificationCreate();
        specCreate.setName("End-to-End Test Specification");
        specCreate.setDescription("A specification for end-to-end testing");

        ServiceSpecification createdSpec = serviceSpecificationRepoService.addServiceSpecification(specCreate);

        // Verify specification create notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationCreateNotification")),
            anyMap()
        );

        // Update specification
        ServiceSpecificationUpdate specUpdate = new ServiceSpecificationUpdate();
        specUpdate.setDescription("Updated description for end-to-end testing");

        ServiceSpecification updatedSpec = serviceSpecificationRepoService.updateServiceSpecification(createdSpec.getUuid(), specUpdate);

        // Verify specification change notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationChangeNotification")),
            anyMap()
        );

        // Delete specification
        serviceSpecificationRepoService.deleteByUuid(createdSpec.getUuid());

        // Verify specification delete notification was published to Camel
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationDeleteNotification")),
            anyMap()
        );
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
        when(restTemplate.exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class)))
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

        // Verify notifications were published to Camel for all three entity types
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCatalogCreateNotification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCategoryCreateNotification")),
            anyMap()
        );

        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceSpecificationCreateNotification")),
            anyMap()
        );

        // Verify multiple HTTP calls are made for different subscriptions
        verify(restTemplate, timeout(5000).atLeast(3))
            .exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testNotificationSystemWithInvalidCallback() {
        // Setup event subscription with invalid callback URL
        EventSubscription subscription = new EventSubscription();
        subscription.setCallback("http://invalid-callback-url:9999/callback");

        when(eventSubscriptionRepoService.findAll()).thenReturn(Arrays.asList(subscription));
        when(restTemplate.exchange(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        // Create entity to trigger notification
        ServiceCatalogCreate catalogCreate = new ServiceCatalogCreate();
        catalogCreate.setName("Invalid Callback Test Catalog");
        ServiceCatalog catalog = catalogRepoService.addCatalog(catalogCreate);

        // Verify notification was still published to Camel even with callback failure
        verify(producerTemplate, timeout(5000)).sendBodyAndHeaders(
            anyString(),
            argThat(body -> body != null && body.toString().contains("ServiceCatalogCreateNotification")),
            anyMap()
        );
    }
}
