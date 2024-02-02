package org.etsi.osl.services.api.rcm634;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.rcm634.api.ResourceSpecificationApiController;
import org.etsi.osl.tmf.rcm634.model.*;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = OpenAPISpringBoot.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@ActiveProfiles("testing")
public class ResourceSpecificationApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    ResourceSpecificationApiController resourceSpecificationApiController;

    @Mock
    ResourceSpecificationRepoService resourceSpecificationRepoService;

    @Mock
    ResourceSpecification resourceSpecification;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    // Test CREATE with role ADMIN
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    public void testCreateResourceSpecificationAdmin() throws Exception {

        // Testing a valid physical resource specification at the correct endpoint
        File resourceSpecFile = new File("src/test/resources/testPhysicalResourceSpec.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String resourceSpecString = IOUtils.toString(in, "UTF-8");
        PhysicalResourceSpecificationCreate physicalResourceSpecCreate = JsonUtils.toJsonObj(resourceSpecString, PhysicalResourceSpecificationCreate.class);

        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(physicalResourceSpecCreate)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Resource Spec")))
                .andExpect(status().isOk())
                .andReturn().getResponse();


        LogicalResourceSpecificationCreate logicalResourceSpecificationCreate = new LogicalResourceSpecificationCreate();
        logicalResourceSpecificationCreate.setName("Test Logical Resource Spec");
        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(logicalResourceSpecificationCreate)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Logical Resource Spec")))
                .andExpect(status().isOk())
                .andReturn().getResponse();


        ResourceFunctionSpecificationCreate resourceFunctionSpecificationCreate = new ResourceFunctionSpecificationCreate();
        resourceFunctionSpecificationCreate.setName("Test Resource Function Specification");
        resourceFunctionSpecificationCreate.setType("ResourceFunctionSpecification");
        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(resourceFunctionSpecificationCreate)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Resource Function Specification")))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // Testing a valid physical resource specification wrong endpoint
        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecificat")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(physicalResourceSpecCreate)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // Testing a valid physical resource specification wrong payload correct endpoint
        String wrongStringPayload = "random string as payload";
        byte[] wrongPayload = wrongStringPayload.getBytes();

        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPayload))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
    }

    // Test CREATE with incorrect role USER
    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    public void testCreateResourceSpecificationUser() throws Exception {
        // Testing a valid physical resource specification
        File physicalResourceSpecFile = new File("src/test/resources/testResourceSpec.json");
        InputStream in = new FileInputStream(physicalResourceSpecFile);
        String physicalResourceSpecString = IOUtils.toString(in, "UTF-8");
        LogicalResourceSpecificationCreate physicalResourceSpecCreate = JsonUtils.toJsonObj(physicalResourceSpecString, LogicalResourceSpecificationCreate.class);
        mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(physicalResourceSpecCreate)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();

    }

    // Test DELETE method with role ADMIN
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    public void testDeleteResourceSpecificationAdmin() throws Exception {
        ResourceFunctionSpecificationCreate resourceFunctionSpecificationCreate = new ResourceFunctionSpecificationCreate();
        resourceFunctionSpecificationCreate.setName("Test Resource Function Specification");
        resourceFunctionSpecificationCreate.setType("ResourceFunctionSpecification");
        String createdSpecificationString = mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(resourceFunctionSpecificationCreate)))
                .andReturn().getResponse().getContentAsString();
        System.out.println(createdSpecificationString);

        ResourceSpecification createdSpecification = JsonUtils.toJsonObj(createdSpecificationString, ResourceFunctionSpecification.class);
        String idToBeDeleted = createdSpecification.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/resourceCatalogManagement/v4/resourceSpecification/" + idToBeDeleted)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(null)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

    }

    // Test DELETE method with incorrect role USER
    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    public void testDeleteResourceSpecificationUser() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/resourceCatalogManagement/v4/resourceSpecification/idToBeDeleted")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(null)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();

    }

    // Test GET by id method with no role
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    public void testRetrieveResourceSpecification() throws Exception {
        ResourceFunctionSpecificationCreate resourceFunctionSpecificationCreate = new ResourceFunctionSpecificationCreate();
        resourceFunctionSpecificationCreate.setName("Test Resource Function Specification");
        resourceFunctionSpecificationCreate.setType("ResourceFunctionSpecification");
        String createdSpecificationString = mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(resourceFunctionSpecificationCreate)))
                .andReturn().getResponse().getContentAsString();

        ResourceSpecification createdSpecification = JsonUtils.toJsonObj(createdSpecificationString, ResourceFunctionSpecification.class);
        String id = createdSpecification.getId();
        String fields = "name";
        mvc.perform(MockMvcRequestBuilders.get("/resourceCatalogManagement/v4/resourceSpecification/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(null))
                        .param("fields", fields))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Resource Function Specification")))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    public void testListResourceSpecification() throws Exception {
        String listResSpecsResponseString = mvc.perform(MockMvcRequestBuilders.get("/resourceCatalogManagement/v4/resourceSpecification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(null)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<LogicalResourceSpecification> listResSpecsResponse = JsonUtils.toListOfJsonObj(listResSpecsResponseString, LogicalResourceSpecification.class);
        assertEquals(7, listResSpecsResponse.size());

        mvc.perform(MockMvcRequestBuilders.get("/resourceCatalogManagement/v4/resourceSpecification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(null))
                        .param("fields", "lastUpdate"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testGetObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResourceSpecificationApiController controller = new ResourceSpecificationApiController(objectMapper, request);

        Optional<ObjectMapper> result = controller.getObjectMapper();
        assertTrue(result.isPresent());
        assertEquals(objectMapper, result.get());
    }

    @Test
    void testGetRequest() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResourceSpecificationApiController controller = new ResourceSpecificationApiController(objectMapper, request);

        Optional<HttpServletRequest> result = controller.getRequest();
        assertTrue(result.isPresent());
        assertEquals(request, result.get());
    }
}