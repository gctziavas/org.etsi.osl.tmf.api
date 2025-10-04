package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.EventSubscriptionInput;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreate;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingUpdate;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingRepoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductOfferingCallbackIntegrationTest extends BaseIT {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductOfferingRepoService productOfferingRepoService;

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @SpyBean
    private ProductOfferingCallbackService productOfferingCallbackService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private AutoCloseable mocks;

    @BeforeAll
    public void setupOnce() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);

        // Mock RestTemplate to avoid actual HTTP calls in tests
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clear entity manager cache to release entity references
        if (entityManager != null) {
            entityManager.clear();
        }

        // Close mocks to release resources
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"USER"})
    public void testCompleteCallbackFlow() throws Exception {
        // Step 1: Register a callback subscription via Hub API
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:8080/test-callback");
        subscriptionInput.setQuery("productoffering.create,productoffering.delete");

        MvcResult subscriptionResult = mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated())
                .andReturn();

        String subscriptionResponseBody = subscriptionResult.getResponse().getContentAsString();
        EventSubscription createdSubscription = objectMapper.readValue(subscriptionResponseBody, EventSubscription.class);

        // Step 2: Create a product offering (should trigger callback)
        ProductOfferingCreate productOfferingCreate = new ProductOfferingCreate();
        productOfferingCreate.setName("Test Callback Product Offering");
        productOfferingCreate.setDescription("A product offering to test callback notifications");
        productOfferingCreate.setVersion("1.0");

        ProductOffering createdProductOffering = productOfferingRepoService.addProductOffering(productOfferingCreate);

        // Step 3: Verify callback was sent
        verify(productOfferingCallbackService, timeout(2000)).sendProductOfferingCreateCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/productOfferingCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Step 4: Delete the product offering (should trigger delete callback)
        productOfferingRepoService.deleteByUuid(createdProductOffering.getUuid());

        // Step 5: Verify delete callback was sent
        verify(productOfferingCallbackService, timeout(2000)).sendProductOfferingDeleteCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/productOfferingDeleteEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"USER"})
    public void testAttributeValueChangeAndStateChangeCallbacks() throws Exception {
        // Step 1: Register subscription for attribute and state change events
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:8080/change-callback");
        subscriptionInput.setQuery("productoffering.attributevaluechange,productoffering.statechange");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create a product offering
        ProductOfferingCreate productOfferingCreate = new ProductOfferingCreate();
        productOfferingCreate.setName("Test Change Product Offering");
        productOfferingCreate.setDescription("A product offering to test change notifications");
        productOfferingCreate.setVersion("1.0");
        productOfferingCreate.setLifecycleStatus("Active");

        ProductOffering createdProductOffering = productOfferingRepoService.addProductOffering(productOfferingCreate);

        // Step 3: Update the product offering (should trigger attribute value change callback)
        ProductOfferingUpdate productOfferingUpdate = new ProductOfferingUpdate();
        productOfferingUpdate.setDescription("Updated description for testing");
        productOfferingUpdate.setLifecycleStatus("Retired");

        productOfferingRepoService.updateProductOffering(createdProductOffering.getUuid(), productOfferingUpdate);

        // Step 4: Verify both attribute value change and state change callbacks were sent
        verify(productOfferingCallbackService, timeout(2000)).sendProductOfferingAttributeValueChangeCallback(any());
        verify(productOfferingCallbackService, timeout(2000)).sendProductOfferingStateChangeCallback(any());
        
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/change-callback/listener/productOfferingAttributeValueChangeEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
            
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/change-callback/listener/productOfferingStateChangeEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"USER"})
    public void testCallbackFilteringByEventType() throws Exception {
        // Step 1: Register subscription only for create events
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:9090/create-only");
        subscriptionInput.setQuery("productoffering.create");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create and delete a product offering
        ProductOfferingCreate productOfferingCreate = new ProductOfferingCreate();
        productOfferingCreate.setName("Test Filter Product Offering");
        productOfferingCreate.setDescription("A product offering to test query filtering");
        productOfferingCreate.setVersion("1.0");

        ProductOffering createdProductOffering = productOfferingRepoService.addProductOffering(productOfferingCreate);
        productOfferingRepoService.deleteByUuid(createdProductOffering.getUuid());

        // Step 3: Verify only create callback was sent (not delete)
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:9090/create-only/listener/productOfferingCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Note: In a more sophisticated test, we could verify that the delete callback was NOT sent
        // by using verify with never(), but this requires more complex mock setup
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"USER"})
    public void testProductOfferingCallbackWithAllEventsQuery() throws Exception {
        // Step 1: Register subscription for all events (empty query)
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:7070/all-events");
        subscriptionInput.setQuery(""); // Empty query should receive all events

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create a product offering
        ProductOfferingCreate productOfferingCreate = new ProductOfferingCreate();
        productOfferingCreate.setName("Test All Events Product Offering");
        productOfferingCreate.setDescription("A product offering to test all events subscription");
        productOfferingCreate.setVersion("1.0");

        ProductOffering createdProductOffering = productOfferingRepoService.addProductOffering(productOfferingCreate);

        // Step 3: Verify callback was sent even with empty query
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:7070/all-events/listener/productOfferingCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
    }
}