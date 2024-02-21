package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.scm633.model.*;
import org.etsi.osl.tmf.JsonUtils;

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
public class ExportJobApiController633Test {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateExportJob() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testExportJob.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String exportJobString = IOUtils.toString(in, "UTF-8");
        ExportJobCreate exportJobCreate = JsonUtils.toJsonObj(exportJobString, ExportJobCreate.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/exportJob")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content( JsonUtils.toJson( exportJobCreate ) ))
        .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/exportJob")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content( JsonUtils.toJson( exportJobCreate ) ))
        .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteExportJob() throws Exception {

        mvc.perform(MockMvcRequestBuilders.delete("/serviceCatalogManagement/v4/exportJob/testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListExportJob() throws Exception {

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/exportJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/exportJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveExportJob() throws Exception {

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/exportJob?testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/exportJob/testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }
}