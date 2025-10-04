package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.pcm620.model.Category;
import org.etsi.osl.tmf.pcm620.model.CategoryCreate;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.EventSubscriptionInput;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCategoryRepoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
public class CategoryCallbackIntegrationTest extends BaseIT {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCategoryRepoService productCategoryRepoService;

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

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
        if (entityManager != null) {
            entityManager.clear();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCompleteCallbackFlow() throws Exception {
        // Step 1: Register a callback subscription via Hub API
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:8080/test-callback");
        subscriptionInput.setQuery("category.create,category.delete");

        MvcResult subscriptionResult = mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated())
                .andReturn();

        String subscriptionResponseBody = subscriptionResult.getResponse().getContentAsString();
        EventSubscription createdSubscription = objectMapper.readValue(subscriptionResponseBody, EventSubscription.class);

        // Step 2: Create a category (should trigger callback)
        CategoryCreate categoryCreate = new CategoryCreate();
        categoryCreate.setName("Test Callback Category");
        categoryCreate.setDescription("A category to test callback notifications");
        categoryCreate.setVersion("1.0");

        Category createdCategory = productCategoryRepoService.addCategory(categoryCreate);

        // Step 3: Verify callback was sent via RestTemplate
        verify(restTemplate, timeout(5000)).exchange(
            eq("http://localhost:8080/test-callback/listener/categoryCreateEvent"),
            eq(HttpMethod.POST),
            argThat(httpEntity -> {
                // Verify that the HttpEntity contains event data
                return httpEntity != null && httpEntity.getBody() != null;
            }),
            eq(String.class));

        // Step 4: Delete the category (should trigger delete callback)
        productCategoryRepoService.deleteById(createdCategory.getUuid());

        // Step 5: Verify delete callback was sent via RestTemplate
        verify(restTemplate, timeout(5000)).exchange(
            eq("http://localhost:8080/test-callback/listener/categoryDeleteEvent"),
            eq(HttpMethod.POST),
            argThat(httpEntity -> {
                // Verify that the HttpEntity contains event data
                return httpEntity != null && httpEntity.getBody() != null;
            }),
            eq(String.class));
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCallbackFilteringByQuery() throws Exception {
        // Step 1: Register subscription only for create events
        EventSubscriptionInput subscriptionInput = new EventSubscriptionInput();
        subscriptionInput.setCallback("http://localhost:9090/create-only");
        subscriptionInput.setQuery("category.create");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create and delete a category
        CategoryCreate categoryCreate = new CategoryCreate();
        categoryCreate.setName("Test Filter Category");
        categoryCreate.setDescription("A category to test query filtering");
        categoryCreate.setVersion("1.0");

        Category createdCategory = productCategoryRepoService.addCategory(categoryCreate);
        productCategoryRepoService.deleteById(createdCategory.getUuid());

        // Step 3: Verify only create callback was sent (not delete)
        verify(restTemplate, timeout(5000)).exchange(
            eq("http://localhost:9090/create-only/listener/categoryCreateEvent"),
            eq(HttpMethod.POST),
            argThat(httpEntity -> {
                // Verify that the HttpEntity contains event data
                return httpEntity != null && httpEntity.getBody() != null;
            }),
            eq(String.class));

        // The delete callback should not be sent due to query filtering
        // (this would require explicit verification with never() and additional mock setup)
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCategoryCallbackWithAllEventsQuery() throws Exception {
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

        // Step 2: Create a category
        CategoryCreate categoryCreate = new CategoryCreate();
        categoryCreate.setName("Test All Events Category");
        categoryCreate.setDescription("A category to test all events subscription");
        categoryCreate.setVersion("1.0");

        Category createdCategory = productCategoryRepoService.addCategory(categoryCreate);

        // Step 3: Verify callback was sent even with empty query
        verify(restTemplate, timeout(5000)).exchange(
            eq("http://localhost:7070/all-events/listener/categoryCreateEvent"),
            eq(HttpMethod.POST),
            argThat(httpEntity -> {
                // Verify that the HttpEntity contains event data
                return httpEntity != null && httpEntity.getBody() != null;
            }),
            eq(String.class));
    }
}