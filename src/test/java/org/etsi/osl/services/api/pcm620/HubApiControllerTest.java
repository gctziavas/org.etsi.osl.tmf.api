package org.etsi.osl.services.api.pcm620;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.etsi.osl.tmf.pcm620.model.EventSubscriptionInput;
import org.etsi.osl.tmf.pcm620.reposervices.EventSubscriptionRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class HubApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testRegisterListener() throws Exception {
        File resourceSpecFile = new File("src/test/resources/testPCM620EventSubscriptionInput.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String eventSubscriptionInputString = IOUtils.toString(in, "UTF-8");
        EventSubscriptionInput eventSubscriptionInput = JsonUtils.toJsonObj(eventSubscriptionInputString, EventSubscriptionInput.class);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(eventSubscriptionInput)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json;charset=utf-8"))
                .andExpect(jsonPath("$.callback").value("http://localhost:8080/callback"))
                .andExpect(jsonPath("$.query").value("productOffering.create,productOffering.delete"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        EventSubscription createdSubscription = objectMapper.readValue(responseBody, EventSubscription.class);
        
        // Verify the subscription was actually saved to the database
        EventSubscription retrievedSubscription = eventSubscriptionRepoService.findById(createdSubscription.getId());
        assert retrievedSubscription != null;
        assert retrievedSubscription.getCallback().equals("http://localhost:8080/callback");
        assert retrievedSubscription.getQuery().equals("productOffering.create,productOffering.delete");
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testRegisterListenerWithoutAcceptHeader() throws Exception {
        File resourceSpecFile = new File("src/test/resources/testPCM620EventSubscriptionInput.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String eventSubscriptionInputString = IOUtils.toString(in, "UTF-8");
        EventSubscriptionInput eventSubscriptionInput = JsonUtils.toJsonObj(eventSubscriptionInputString, EventSubscriptionInput.class);

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(eventSubscriptionInput)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.callback").value("http://localhost:8080/callback"))
                .andExpect(jsonPath("$.query").value("productOffering.create,productOffering.delete"))
                .andExpect(jsonPath("$.id").exists());
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testUnregisterListener() throws Exception {
        // First, create a subscription
        EventSubscriptionInput input = new EventSubscriptionInput();
        input.setCallback("http://localhost:8080/callback");
        input.setQuery("test.event");
        
        EventSubscription created = eventSubscriptionRepoService.addEventSubscription(input);
        String subscriptionId = created.getId();

        // Verify the subscription exists
        assert eventSubscriptionRepoService.findById(subscriptionId) != null;

        // Delete the subscription
        mvc.perform(MockMvcRequestBuilders.delete("/productCatalogManagement/v4/hub/" + subscriptionId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify the subscription was deleted
        assert eventSubscriptionRepoService.findById(subscriptionId) == null;
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testUnregisterNonExistentListener() throws Exception {
        String nonExistentId = "non-existent-id";

        mvc.perform(MockMvcRequestBuilders.delete("/productCatalogManagement/v4/hub/" + nonExistentId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testRegisterListenerWithInvalidData() throws Exception {
        // Test with missing required callback field
        EventSubscriptionInput invalidInput = new EventSubscriptionInput();
        invalidInput.setQuery("test.event");
        // callback is missing, which is required

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    @Test
    public void testRegisterListenerWithEmptyCallback() throws Exception {
        EventSubscriptionInput invalidInput = new EventSubscriptionInput();
        invalidInput.setCallback("");
        invalidInput.setQuery("test.event");

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    public void testRegisterListenerUnauthorized() throws Exception {
        File resourceSpecFile = new File("src/test/resources/testPCM620EventSubscriptionInput.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String eventSubscriptionInputString = IOUtils.toString(in, "UTF-8");
        EventSubscriptionInput eventSubscriptionInput = JsonUtils.toJsonObj(eventSubscriptionInputString, EventSubscriptionInput.class);

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(eventSubscriptionInput)))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    public void testUnregisterListenerUnauthorized() throws Exception {
        // First create a subscription as admin
        EventSubscriptionInput input = new EventSubscriptionInput();
        input.setCallback("http://localhost:8080/callback");
        input.setQuery("test.event");
        EventSubscription created = eventSubscriptionRepoService.addEventSubscription(input);
        String subscriptionId = created.getId();

        // Try to delete as regular user (should be forbidden)
        mvc.perform(MockMvcRequestBuilders.delete("/productCatalogManagement/v4/hub/" + subscriptionId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify the subscription still exists
        assert eventSubscriptionRepoService.findById(subscriptionId) != null;
    }

    @Test
    public void testRegisterListenerUnauthenticated() throws Exception {
        File resourceSpecFile = new File("src/test/resources/testPCM620EventSubscriptionInput.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String eventSubscriptionInputString = IOUtils.toString(in, "UTF-8");
        EventSubscriptionInput eventSubscriptionInput = JsonUtils.toJsonObj(eventSubscriptionInputString, EventSubscriptionInput.class);

        mvc.perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/hub")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(eventSubscriptionInput)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnregisterListenerUnauthenticated() throws Exception {
        String testId = "test-subscription-id";

        mvc.perform(MockMvcRequestBuilders.delete("/productCatalogManagement/v4/hub/" + testId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}