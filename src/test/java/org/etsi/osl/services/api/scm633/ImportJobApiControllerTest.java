package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.scm633.api.ImportJobApiController;
import org.etsi.osl.tmf.scm633.model.ExportJobCreate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public class ImportJobApiControllerTest extends BaseIT  {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateImportJob() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testExportJob.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String exportJobString = IOUtils.toString(in, "UTF-8");
        ExportJobCreate exportJobCreate = JsonUtils.toJsonObj(exportJobString, ExportJobCreate.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/importJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( exportJobCreate ) ))
                .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/importJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( exportJobCreate ) ))
                .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteImportJob() throws Exception {

        mvc.perform(MockMvcRequestBuilders.delete("/serviceCatalogManagement/v4/importJob/testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListImportJob() throws Exception {

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/importJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/importJob")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveImportJob() throws Exception {

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/importJob?testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/importJob/testId")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501));
    }
}