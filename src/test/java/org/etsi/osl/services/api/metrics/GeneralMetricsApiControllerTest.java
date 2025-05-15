package org.etsi.osl.services.api.metrics;

import com.jayway.jsonpath.JsonPath;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.pm632.model.IndividualCreate;
import org.etsi.osl.tmf.pm632.reposervices.IndividualRepoService;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.etsi.osl.tmf.scm633.model.ServiceCandidate;
import org.etsi.osl.tmf.scm633.reposervices.CandidateRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = OpenAPISpringBoot.class
)
//@AutoConfigureTestDatabase //this automatically uses h2
@AutoConfigureMockMvc
@ActiveProfiles("testing")
//@TestPropertySource(
//		  locations = "classpath:application-testing.yml")

public class MetricsApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    IndividualRepoService individualRepoService;

    @Autowired
    ResourceSpecificationRepoService resourceSpecificationRepoService;

    @Autowired
    CandidateRepoService candidateRepoService;


    @Before
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetRegisteredIndividuals() throws Exception {

        IndividualCreate individualCreate = new IndividualCreate();
        individualCreate.setFullName("John Doe");
        individualRepoService.addIndividual(individualCreate);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/registeredIndividuals" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalIndividuals = JsonPath.read(response, "$.registeredIndividuals");

        assertThat(totalIndividuals).isEqualTo(individualRepoService.findAll().size());
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetPublishedServiceSpecifications() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/publishedServiceSpecifications" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalSpecs = JsonPath.read(response, "$.publishedServiceSpecifications");

        List<ServiceCandidate> serviceCandidates = candidateRepoService.findAll();
        int count = 0;

        for (ServiceCandidate serviceCandidate : serviceCandidates) {
            if (serviceCandidate.getCategory() != null) {
                count += 1;
            }
        }

        assertThat(totalSpecs).isEqualTo(count);
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetRegisteredResourceSpecifications() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/registeredResourceSpecifications" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalSpecs = JsonPath.read(response, "$.registeredResourceSpecifications");

        assertThat(totalSpecs).isEqualTo(resourceSpecificationRepoService.findAll().size());
    }
}
