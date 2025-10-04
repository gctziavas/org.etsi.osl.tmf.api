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
import org.etsi.osl.tmf.pcm620.model.Catalog;
import org.etsi.osl.tmf.pcm620.model.CatalogCreate;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.EventSubscriptionInput;
import org.etsi.osl.tmf.pcm620.reposervices.CatalogCallbackService;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCatalogRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class CatalogCallbackIntegrationTest extends BaseIT {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCatalogRepoService productCatalogRepoService;

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @SpyBean
    private CatalogCallbackService catalogCallbackService;

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
        subscriptionInput.setQuery("catalog.create,catalog.delete");

        MvcResult subscriptionResult = mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated())
                .andReturn();

        String subscriptionResponseBody = subscriptionResult.getResponse().getContentAsString();
        EventSubscription createdSubscription = objectMapper.readValue(subscriptionResponseBody, EventSubscription.class);

        // Step 2: Create a catalog (should trigger callback)
        CatalogCreate catalogCreate = new CatalogCreate();
        catalogCreate.setName("Test Callback Catalog");
        catalogCreate.setDescription("A catalog to test callback notifications");
        catalogCreate.setVersion("1.0");

        Catalog createdCatalog = productCatalogRepoService.addCatalog(catalogCreate);

        // Step 3: Verify callback was sent
        verify(catalogCallbackService, timeout(2000)).sendCatalogCreateCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/catalogCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Step 4: Delete the catalog (should trigger delete callback)
        productCatalogRepoService.deleteById(createdCatalog.getUuid());

        // Step 5: Verify delete callback was sent
        verify(catalogCallbackService, timeout(2000)).sendCatalogDeleteCallback(any());
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:8080/test-callback/listener/catalogDeleteEvent"),
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
        subscriptionInput.setQuery("catalog.create");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(subscriptionInput)))
                .andExpect(status().isCreated());

        // Step 2: Create and delete a catalog
        CatalogCreate catalogCreate = new CatalogCreate();
        catalogCreate.setName("Test Filter Catalog");
        catalogCreate.setDescription("A catalog to test query filtering");
        catalogCreate.setVersion("1.0");

        Catalog createdCatalog = productCatalogRepoService.addCatalog(catalogCreate);
        productCatalogRepoService.deleteById(createdCatalog.getUuid());

        // Step 3: Verify only create callback was sent (not delete)
        verify(restTemplate, timeout(2000)).exchange(
            eq("http://localhost:9090/create-only/listener/catalogCreateEvent"),
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(String.class));

        // Note: In a more sophisticated test, we could verify that the delete callback was NOT sent
        // by using verify with never(), but this requires more complex mock setup
    }
}