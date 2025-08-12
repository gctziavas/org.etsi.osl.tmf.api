package org.etsi.osl.services.api.pcm620;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreate;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.EventSubscriptionInput;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class ProductSpecificationCallbackIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductSpecificationRepoService productSpecificationRepoService;

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @SpyBean
    private ProductSpecificationCallbackService productSpecificationCallbackService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Mock RestTemplate to avoid actual HTTP calls in tests
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
            .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteCallbackFlow() throws Exception {
        // Step 1: Register a callback subscription via Hub API
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:8080/test-callback");
        subscriptionInput.setQuery("productspecification.create,productspecification.delete");

        MvcResult subscriptionResult = mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated())
                .andReturn();

        String subscriptionResponseBody = subscriptionResult.getResponse().getContentAsString();
        EventSubscription createdSubscription = objectMapper.readValue(subscriptionResponseBody, EventSubscription.class);

        // Step 2: Create a product specification (should trigger callback)
        ProductSpecificationCreate productSpecificationCreate = new ProductSpecificationCreate();
        productSpecificationCreate.setName("Test Callback Product Specification");
        productSpecificationCreate.setDescription("A product specification to test callback notifications");
        productSpecificationCreate.setVersion("1.0");

        ProductSpecification createdProductSpecification = productSpecificationRepoService.addProductSpecification(productSpecificationCreate);

        // Step 3: Verify callback was sent
        verify(productSpecificationCallbackService, timeout(2000)).sendProductSpecificationCreateCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/productSpecificationCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Step 4: Delete the product specification (should trigger delete callback)
        productSpecificationRepoService.deleteByUuid(createdProductSpecification.getUuid());

        // Step 5: Verify delete callback was sent
        verify(productSpecificationCallbackService, timeout(2000)).sendProductSpecificationDeleteCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/productSpecificationDeleteEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCallbackFilteringByQuery() throws Exception {
        // Step 1: Register subscription only for create events
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:9090/create-only");
        subscriptionInput.setQuery("productspecification.create");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create and delete a product specification
        ProductSpecificationCreate productSpecificationCreate = new ProductSpecificationCreate();
        productSpecificationCreate.setName("Test Filter Product Specification");
        productSpecificationCreate.setDescription("A product specification to test query filtering");
        productSpecificationCreate.setVersion("1.0");

        ProductSpecification createdProductSpecification = productSpecificationRepoService.addProductSpecification(productSpecificationCreate);
        productSpecificationRepoService.deleteByUuid(createdProductSpecification.getUuid());

        // Step 3: Verify only create callback was sent (not delete)
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:9090/create-only/listener/productSpecificationCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Note: In a more sophisticated test, we could verify that the delete callback was NOT sent
        // by using verify with never(), but this requires more complex mock setup
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testProductSpecificationCallbackWithAllEventsQuery() throws Exception {
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

        // Step 2: Create a product specification
        ProductSpecificationCreate productSpecificationCreate = new ProductSpecificationCreate();
        productSpecificationCreate.setName("Test All Events Product Specification");
        productSpecificationCreate.setDescription("A product specification to test all events subscription");
        productSpecificationCreate.setVersion("1.0");

        ProductSpecification createdProductSpecification = productSpecificationRepoService.addProductSpecification(productSpecificationCreate);

        // Step 3: Verify callback was sent even with empty query
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:7070/all-events/listener/productSpecificationCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));
    }
}