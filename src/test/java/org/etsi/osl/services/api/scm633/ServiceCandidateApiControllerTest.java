package org.etsi.osl.services.api.scm633;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;


import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.model.*;
import org.etsi.osl.tmf.JsonUtils;

import org.etsi.osl.tmf.scm633.reposervices.CandidateRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
@ActiveProfiles("testing")
public class ServiceCandidateApiControllerTest {

    private static final int FIXED_BOOTSTRAPS_SPECS = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    CandidateRepoService candidateRepoService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateServiceCandidate() throws Exception {

        String response = createServiceCandidate();

        ServiceCandidate responsesServiceCandidate = JsonUtils.toJsonObj(response,  ServiceCandidate.class);
        assertThat( responsesServiceCandidate.getDescription() ).isEqualTo( "A Test Service Candidate" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteServiceCandidate() throws Exception {

        String response = createServiceCandidate();

        ServiceCandidate responsesServiceCandidate = JsonUtils.toJsonObj(response,  ServiceCandidate.class);
        String id = responsesServiceCandidate.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/serviceCatalogManagement/v4/serviceCandidate/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        assertThat( candidateRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListServiceCandidate() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceCandidate")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<ServiceCandidate> serviceCandidateList= objectMapper.readValue(response, new TypeReference<List<ServiceCandidate>>() {});

        assertThat(serviceCandidateList.size()).isEqualTo(candidateRepoService.findAll().size());

        String id = candidateRepoService.findAll().get(0).getId();

        boolean idExists = false;
        for (ServiceCandidate sc : serviceCandidateList) {
            if ( sc.getId().equals( id ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testPatchServiceCandidate() throws Exception {

        String response = createServiceCandidate();

        ServiceCandidate responsesServiceCandidate = JsonUtils.toJsonObj(response,  ServiceCandidate.class);
        String id = responsesServiceCandidate.getId();

        JSONObject obj = JsonUtils.toJsonObj(response, JSONObject.class);
        obj.remove("uuid");
        obj.remove("id");
        obj.remove("lastUpdate");
        response = JsonUtils.toJsonString(obj);

        ServiceCandidateUpdate serviceCandidateUpdate = JsonUtils.toJsonObj(response,  ServiceCandidateUpdate.class);
        serviceCandidateUpdate.setDescription("Test Service Candidate new description");

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceCatalogManagement/v4/serviceCandidate/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateUpdate ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("description", is("Test Service Candidate new description")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ServiceCandidate responsesServiceCandidate2 = JsonUtils.toJsonObj(response2,  ServiceCandidate.class);
        assertThat( responsesServiceCandidate2.getDescription() ).isEqualTo( "Test Service Candidate new description" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveServiceCandidate() throws Exception {

        String response = createServiceCandidate();

        ServiceCandidate responsesServiceCandidate = JsonUtils.toJsonObj(response,  ServiceCandidate.class);
        String id = responsesServiceCandidate.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceCandidate/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        ServiceCandidate responsesServiceCandidate2 = JsonUtils.toJsonObj(response2,  ServiceCandidate.class);
        assertThat( responsesServiceCandidate2.getDescription() ).isEqualTo( "A Test Service Candidate" );
    }

    private String createServiceCandidate() throws Exception{
        File resourceSpecFile = new File("src/test/resources/testServiceCandidate.txt");
        InputStream in = new FileInputStream(resourceSpecFile);
        String serviceCandidateString = IOUtils.toString(in, "UTF-8");
        ServiceCandidateCreate serviceCandidate = JsonUtils.toJsonObj(serviceCandidateString, ServiceCandidateCreate.class);

        assertThat( candidateRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS);

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceCandidate")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidate ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("description", is("A Test Service Candidate")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat( candidateRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_SPECS + 1 );
        return response;
    }
}
