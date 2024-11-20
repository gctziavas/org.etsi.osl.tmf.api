package org.etsi.osl.services.api.scm633;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.*;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.common.model.Attachment;
import org.etsi.osl.tmf.rcm634.model.*;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.etsi.osl.tmf.scm633.model.*;
import org.etsi.osl.tmf.JsonUtils;

import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.etsi.osl.tmf.stm653.model.ServiceTestSpecification;
import org.etsi.osl.tmf.stm653.model.ServiceTestSpecificationCreate;
import org.etsi.osl.tmf.stm653.reposervices.ServiceTestSpecificationRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("testing")
public class ServiceSpecificationApiControllerTest {

    private static final int FIXED_BOOTSTRAPS_SPECS = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    ServiceTestSpecificationRepoService aServiceTestSpecRpoService;

    @Autowired
    ResourceSpecificationRepoService resourceSpecificationRepoService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ServiceSpecificationRepoService specRepoService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testDeleteServiceSpecification() throws Exception {

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS );
        String response = createServiceSpecification();

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String id = responsesSpec.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/serviceCatalogManagement/v4/serviceSpecification/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testListServiceSpecification() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification")

                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<ServiceSpecification> serviceSpecificationList = objectMapper.readValue(response,  new TypeReference<List<ServiceSpecification>>() {});

        assertThat(serviceSpecificationList.size()).isEqualTo(specRepoService.findAll().size());
        String id = specRepoService.findAll().get(0).getId();

        boolean idExists = false;
        for (ServiceSpecification ss : serviceSpecificationList) {
            if ( ss.getId().equals( id ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testPatchServiceSpecification() throws Exception {

        String response = createServiceSpecification();
        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1);

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String id = responsesSpec.getId();

        JSONObject obj = JsonUtils.toJsonObj(response, JSONObject.class);
        obj.remove("uuid");
        obj.remove("id");
        obj.remove("lastUpdate");
        response = JsonUtils.toJsonString(obj);

        ServiceSpecificationUpdate ServiceSpecUpdate = JsonUtils.toJsonObj(response,  ServiceSpecificationUpdate.class);
        ServiceSpecUpdate.setName( "Test Spec new name" );
        ServiceSpecUpdate.setVersion("2.x");

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceCatalogManagement/v4/serviceSpecification/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( ServiceSpecUpdate ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Spec new name")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ServiceSpecification responsesServiceSpec2 = JsonUtils.toJsonObj(response2,  ServiceSpecification.class);
        assertThat( responsesServiceSpec2.getName() ).isEqualTo( "Test Spec new name" );
        assertThat( responsesServiceSpec2.getVersion() ).isEqualTo( "2.x" );
        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testRetrieveServiceSpecification() throws Exception {

        String response = createServiceSpecification();

        ServiceSpecification responsesServiceSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String id = responsesServiceSpec.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        ServiceSpecification responsesServiceSpec2 = JsonUtils.toJsonObj(response2,  ServiceSpecification.class);
        assertThat( responsesServiceSpec2.getName() ).isEqualTo( "Test Spec" );
        assertThat( responsesServiceSpec2.getVersion() ).isEqualTo( "1.8.0" );
    }



    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testGetAttachment() throws Exception {

        // Create a Service Specification
        String response = createServiceSpecification();
        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String specId = responsesSpec.getId();

        // Test method for non-existing logo attachment
        String attId = "logo";
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + specId + "/attachment/" + attId)
                .with( SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound() )
        .andReturn().getResponse().getContentAsString();

        // Test method for non-existing non-logo attachment
        attId = "random";
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + specId + "/attachment/" + attId)
                .with( SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound() )
        .andReturn().getResponse().getContentAsString();


        // Test method for existing attachment

        // Add a new attachment to the Service Specification
        String response2 = createAttachmentAndAddToServiceSpecification(specId);
        Attachment attachment = JsonUtils.toJsonObj(response2,  Attachment.class);
        attId = attachment.getId();
        String attName = attachment.getName();
        String attMimeType = attachment.getMimeType();

        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + specId + "/attachment/" + attId)
                        )
                .andExpect(content().contentTypeCompatibleWith(MediaType.ALL_VALUE))
                .andExpect(status().isOk() )
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + attName))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, attMimeType))
                .andReturn().getResponse().getContentAsString();
    }



    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testGetAttachmentWithFilename() throws Exception {

        // Create a Service Specification
        String response = createServiceSpecification();
        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String specId = responsesSpec.getId();

        String response2 = createAttachmentAndAddToServiceSpecification(specId);
        Attachment attachment = JsonUtils.toJsonObj(response2,  Attachment.class);
        String attId = attachment.getId();
        String attName = attachment.getName();
        String attMimeType = attachment.getMimeType();

        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + specId + "/attachment/" + attId + "/" + attName)
                )
                .andExpect(content().contentTypeCompatibleWith(MediaType.ALL_VALUE))
                .andExpect(status().isOk() )
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + attName))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, attMimeType))
                .andReturn().getResponse().getContentAsString();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testRetrieveServiceSpecificationDescriptor() throws Exception {

        // Test a non-existing spec
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + "fakeId" + "/sd")
                        )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
    }


    @WithMockUser(username = "osadmin", roles = { "ADMIN","USER" })
    //@Test
    public void testSpecFromTestSpec() throws Exception {

        // Creeate a Test Spec
        File sspec = new File( "src/test/resources/testServiceTestSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");
        ServiceTestSpecificationCreate spec = JsonUtils.toJsonObj( sspectext,  ServiceTestSpecificationCreate.class);

        int originalSize = aServiceTestSpecRpoService.findAll().size();

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceTestManagement/v4/serviceTestSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( spec ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("A test name")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        assertThat( aServiceTestSpecRpoService.findAll().size() ).isEqualTo( originalSize + 1 );
        ServiceTestSpecification sts = JsonUtils.toJsonObj(response,  ServiceTestSpecification.class);
        assertThat(sts.getName()).isEqualTo("A test name");
        String stsId = sts.getId();

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS);

        // Create a Service Spec from the Test Spec
        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/specFromTestSpec/" + stsId)
                        .with( SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1 );

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response2,  ServiceSpecification.class);
        assertThat( responsesSpec.getName() ).isEqualTo( "A test name" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    //@Test
    public void testGetImageSpecificationRelationshipGraph() throws Exception {

        // Create a Service Specification
        String response = createServiceSpecification();
        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        String specId = responsesSpec.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/" + specId + "/relationship_graph")
                )
                .andExpect(status().is(302) )
                .andReturn().getResponse().getRedirectedUrl();

        assertThat( response2 ).contains("/blockdiag/svg/");
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN"})
    //@Test
    public void testSpecFromResourceSpec() throws Exception {

        File rspec = new File( "src/test/resources/testResourceSpec.json" );
        InputStream in = new FileInputStream( rspec );
        String rspectext = IOUtils.toString(in, "UTF-8");
        ResourceSpecificationCreate rspeccr = JsonUtils.toJsonObj( rspectext,  ResourceSpecificationCreate.class);

        int originalSize = resourceSpecificationRepoService.findAll().size();

        String responseSpec = mvc.perform(MockMvcRequestBuilders.post( "/resourceCatalogManagement/v4/resourceSpecification"  )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( rspeccr ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( resourceSpecificationRepoService.findAll().size() ).isEqualTo( originalSize + 1 );
        ResourceSpecification responsesSpec1 = JsonUtils.toJsonObj(responseSpec,  PhysicalResourceSpecification.class);
        assertThat(responsesSpec1.getName()).isEqualTo("Test Resource Spec");
        String rSpecId = responsesSpec1.getId();
        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS);

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceSpecification/specFromResourceSpec/" + rSpecId)
                        .with( SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1 );

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response2,  ServiceSpecification.class);
        assertThat( responsesSpec.getName() ).isEqualTo( "Test Resource Spec" );
    }


    private String createServiceSpecification() throws Exception{
        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS );

        File sspec = new File( "src/test/resources/testServiceSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");
        ServiceSpecificationCreate serviceSpecificationCreate = JsonUtils.toJsonObj( sspectext,  ServiceSpecificationCreate.class);

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationCreate ) ))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( specRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1 );

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        assertThat( responsesSpec.getName() ).isEqualTo( "Test Spec" );

        return response;
    }

    private String createAttachmentAndAddToServiceSpecification(String serviceSpecId) throws Exception {

        Attachment att = new Attachment();

        File gz = new File( "src/test/resources/cirros_vnf.tar.gz" );
        InputStream ing = new FileInputStream( gz );
        MockMultipartFile prodFile = new MockMultipartFile("afile", "cirros_vnf.tar.gz", "application/x-gzip", IOUtils.toByteArray(ing));

        String response = mvc.perform(MockMvcRequestBuilders
                        .multipart("/serviceCatalogManagement/v4/serviceSpecification/" + serviceSpecId + "/attachment" )
                        .file(prodFile)
                        .param("attachment", JsonUtils.toJsonString(att))
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("cirros_vnf.tar.gz")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Attachment attachment = JsonUtils.toJsonObj( response,  Attachment.class);

        assertThat(attachment.getName()).isEqualTo("cirros_vnf.tar.gz");
        assertThat(attachment.getUrl()).contains(serviceSpecId);

        return response;
    }
}
