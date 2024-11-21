package org.etsi.osl.services.api.scm633;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.model.*;

import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase //this automatically uses h2
@ActiveProfiles("testing")

public class ServiceCategoryApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    CategoryRepoService categoryRepoService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListServiceCategory() throws Exception {

        String response = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceCategory")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<ServiceCategory> serviceCategoryList = objectMapper.readValue(response, new TypeReference<List<ServiceCategory>>() {});

        assertThat(serviceCategoryList.size()).isEqualTo(categoryRepoService.findAll().size());
        String id = categoryRepoService.findAll().get(0).getId();

        boolean idExists = false;
        for (ServiceCategory ss : serviceCategoryList) {
            if ( ss.getId().equals( id ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();
    }
}
