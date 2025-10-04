package org.etsi.osl.services.api.ri639;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRef;
import org.etsi.osl.tmf.ri639.api.ResourceApiController;
import org.etsi.osl.tmf.ri639.model.LogicalResource;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.reposervices.ResourceRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ResourceApiControllerTest  extends BaseIT {

    private static final int FIXED_BOOTSTRAPS_RESOURCES = 0;

    private MockMvc mvc;

	@PersistenceContext
	private EntityManager entityManager;

    @Autowired
    ResourceRepoService resourceRepoService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private ResourceApiController mockResourceApiController;

    private ResourceRepoService mockResourceRepoService;

    @BeforeAll
    public void setup(WebApplicationContext context) {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

	@AfterEach
	public void tearDown() {
		if (entityManager != null) {
			entityManager.clear();
		}

        // Mocks
        mockResourceRepoService = mock(ResourceRepoService.class);
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        mockResourceApiController = new ResourceApiController(mockObjectMapper, null);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListResource() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/resourceInventoryManagement/v4/resource")

                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<Resource> resourceList = objectMapper.readValue(response, new TypeReference<List<Resource>>() {});

        assertThat(resourceList.size()).isEqualTo(resourceRepoService.findAll().size());
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteResource() throws Exception {

        String response = createResource();

        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
        String id = responsesResource.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/resourceInventoryManagement/v4/resource/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        assertThat( resourceRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_RESOURCES );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateResourceHandleException(){

        ResourceCreate resourceCreate = createResourceObject();

        when(mockResourceRepoService.addResource(any()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<Resource> response = mockResourceApiController.createResource(mock(Principal.class), resourceCreate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteResourceHandleException() throws Exception{

        String response = createResource();

        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
        String id = responsesResource.getId();

        when(mockResourceRepoService.deleteByUuid(any()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<Void> responseDelete = mockResourceApiController.deleteResource(id);

        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListResourceHandleException(){

        when(mockResourceRepoService.findAll())
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<List<Resource>> response = mockResourceApiController.listResource(mock(Principal.class), null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveResourceHandleException(){

        when(mockResourceRepoService.findByUuid(any()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<Resource> response = mockResourceApiController.retrieveResource(mock(Principal.class), "test id", "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private String createResource() throws Exception{

        ResourceCreate resourceCreate = createResourceObject();

        String response = mvc.perform(MockMvcRequestBuilders.post("/resourceInventoryManagement/v4/resource")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( resourceCreate ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( resourceRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_RESOURCES + 1 );
        return response;
    }


    private ResourceCreate createResourceObject() {
        ResourceCreate resourceCreate = new ResourceCreate();
        resourceCreate.setName("Test Resource");
        resourceCreate.setCategory("Experimentation");
        resourceCreate.setDescription("A Test Resource");
        resourceCreate.setStartOperatingDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        resourceCreate.setEndOperatingDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );

        ResourceSpecificationRef aServiceSpecificationRef = new ResourceSpecificationRef();

        resourceCreate.setResourceSpecification( aServiceSpecificationRef );

        return resourceCreate;
    }
}
