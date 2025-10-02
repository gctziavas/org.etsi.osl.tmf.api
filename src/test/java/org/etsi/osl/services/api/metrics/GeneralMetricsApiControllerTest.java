package org.etsi.osl.services.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pm632.model.IndividualCreate;
import org.etsi.osl.tmf.pm632.reposervices.IndividualRepoService;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.reposervices.CandidateRepoService;
import org.etsi.osl.tmf.scm633.reposervices.CatalogRepoService;
import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.jayway.jsonpath.JsonPath;

public class GeneralMetricsApiControllerTest extends BaseIT {

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

    @Autowired
    CatalogRepoService catalogRepoService;

    @Autowired
    CategoryRepoService categoryRepoService;


    @BeforeEach
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

        int count = 0;
        Set<ServiceCategory> serviceCategories = new HashSet<>();
        List<ServiceCatalog> serviceCatalogs = catalogRepoService.findAll();

        for (ServiceCatalog serviceCatalog : serviceCatalogs) {
            serviceCategories.addAll(serviceCatalog.getCategoryObj());
        }

        for (ServiceCategory serviceCategory : serviceCategories) {
            count += serviceCategory.getServiceCandidateObj().size() + serviceCategory.getServiceCandidateRefs().size();
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
