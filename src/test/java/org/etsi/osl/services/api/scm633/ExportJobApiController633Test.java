package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.scm633.api.ExportJobApiController633;
import org.etsi.osl.tmf.scm633.model.ExportJobCreate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public class ExportJobApiController633Test  extends BaseIT {

    private MockMvc mvc;

	@PersistenceContext
	private EntityManager entityManager;

    @Autowired
    private WebApplicationContext context;

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