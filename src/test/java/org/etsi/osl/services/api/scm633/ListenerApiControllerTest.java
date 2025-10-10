package org.etsi.osl.services.api.scm633;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCandidateDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogBatchNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public class ListenerApiControllerTest   extends BaseIT {

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
    public void testListenToServiceCandidateChangeNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String changeNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCandidateChangeNotification serviceCandidateChangeNotification = JsonUtils.toJsonObj(changeNotificationString, ServiceCandidateChangeNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateChangeNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateChangeNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCandidateCreateNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String createNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCandidateCreateNotification serviceCandidateCreateNotification = JsonUtils.toJsonObj(createNotificationString, ServiceCandidateCreateNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateCreateNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateCreateNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCandidateDeleteNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String deleteNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCandidateDeleteNotification serviceCandidateDeleteNotification = JsonUtils.toJsonObj(deleteNotificationString, ServiceCandidateDeleteNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateDeleteNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCandidateDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCandidateDeleteNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCatalogBatchNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String batchNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCatalogBatchNotification serviceCatalogBatchNotification = JsonUtils.toJsonObj(batchNotificationString, ServiceCatalogBatchNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogBatchNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogBatchNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogBatchNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogBatchNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCatalogChangeNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String changeNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCatalogChangeNotification serviceCatalogChangeNotification = JsonUtils.toJsonObj(changeNotificationString, ServiceCatalogChangeNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogChangeNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogChangeNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCatalogCreateNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String createNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCatalogCreateNotification serviceCatalogCreateNotification = JsonUtils.toJsonObj(createNotificationString, ServiceCatalogCreateNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogCreateNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogCreateNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCatalogDeleteNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String deleteNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCatalogDeleteNotification serviceCatalogDeleteNotification = JsonUtils.toJsonObj(deleteNotificationString, ServiceCatalogDeleteNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogDeleteNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCatalogDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCatalogDeleteNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCategoryChangeNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String changeNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCategoryChangeNotification serviceCategoryChangeNotification = JsonUtils.toJsonObj(changeNotificationString, ServiceCategoryChangeNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryChangeNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryChangeNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCategoryCreateNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String createNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCategoryCreateNotification serviceCategoryCreateNotification = JsonUtils.toJsonObj(createNotificationString, ServiceCategoryCreateNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryCreateNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryCreateNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceCategoryDeleteNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String deleteNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceCategoryDeleteNotification serviceCategoryDeleteNotification = JsonUtils.toJsonObj(deleteNotificationString, ServiceCategoryDeleteNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryDeleteNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceCategoryDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceCategoryDeleteNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceSpecificationChangeNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String changeNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceSpecificationChangeNotification serviceSpecificationChangeNotification = JsonUtils.toJsonObj(changeNotificationString, ServiceSpecificationChangeNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationChangeNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationChangeNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationChangeNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceSpecificationCreateNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String createNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceSpecificationCreateNotification serviceSpecificationCreateNotification = JsonUtils.toJsonObj(createNotificationString, ServiceSpecificationCreateNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationCreateNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationCreateNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationCreateNotification ) ))
                .andExpect(status().is(200));
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListenToServiceSpecificationDeleteNotification() throws Exception {

        File resourceSpecFile = new File("src/test/resources/testServiceCandidateChangeNotification.json");
        InputStream in = new FileInputStream(resourceSpecFile);
        String deleteNotificationString = IOUtils.toString(in, "UTF-8");
        ServiceSpecificationDeleteNotification serviceSpecificationDeleteNotification = JsonUtils.toJsonObj(deleteNotificationString, ServiceSpecificationDeleteNotification.class);

        // Test when providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationDeleteNotification ) ))
                .andExpect(status().is(200));

        // Test when not providing an "Accept" request header
        mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/listener/serviceSpecificationDeleteNotification")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( serviceSpecificationDeleteNotification ) ))
                .andExpect(status().is(200));
    }
}