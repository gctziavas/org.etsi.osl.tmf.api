package org.etsi.osl.services.api.scm633;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.rcm634.model.ResourceCatalog;
import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiController;
import org.etsi.osl.tmf.scm633.model.ServiceCatalog;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreate;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogUpdate;
import org.etsi.osl.tmf.scm633.reposervices.CatalogRepoService;
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
import net.minidev.json.JSONObject;

public class ServiceCatalogApiControllerTest  extends BaseIT {

    private static final int FIXED_BOOTSTRAPS_CATALOGS = 1;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    CatalogRepoService catalogRepoService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteServiceCatalog() throws Exception {

        String response = createServiceCatalog();

        ResourceCatalog responsesCatalog = JsonUtils.toJsonObj(response,  ResourceCatalog.class);
        assertThat( responsesCatalog.getName() ).isEqualTo( "Test Catalog" );

        String id = responsesCatalog.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/serviceCatalogManagement/v4/serviceCatalog/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        assertThat( catalogRepoService.findAll().size() ).isEqualTo( FIXED_BOOTSTRAPS_CATALOGS );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListServiceCatalog() throws Exception {

        createServiceCatalog();

        String response = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceCatalog")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<ServiceCatalog> serviceCatalogList = JsonUtils.toJsonObj(response,  ArrayList.class);

        assertThat(serviceCatalogList.size()).isEqualTo(FIXED_BOOTSTRAPS_CATALOGS + 1);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testPatchServiceCatalog() throws Exception {

        String response = createServiceCatalog();

        ServiceCatalog responsesServiceCatalog = JsonUtils.toJsonObj(response,  ServiceCatalog.class);
        String id = responsesServiceCatalog.getId();

        JSONObject obj = JsonUtils.toJsonObj(response, JSONObject.class);
        obj.remove("uuid");
        obj.remove("id");
        obj.remove("lastUpdate");
        response = JsonUtils.toJsonString(obj);

        ServiceCatalogUpdate ServiceCatalogUpdate = JsonUtils.toJsonObj(response,  ServiceCatalogUpdate.class);
        ServiceCatalogUpdate.setName( "Test Service Catalog new name" );
        ServiceCatalogUpdate.setVersion("2.x");

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceCatalogManagement/v4/serviceCatalog/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( ServiceCatalogUpdate ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Service Catalog new name")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ServiceCatalog responsesServiceCatalog2 = JsonUtils.toJsonObj(response2,  ServiceCatalog.class);
        assertThat( responsesServiceCatalog2.getName() ).isEqualTo( "Test Service Catalog new name" );
        assertThat( responsesServiceCatalog2.getVersion() ).isEqualTo( "2.x" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveServiceCatalog() throws Exception {

        String response = createServiceCatalog();

        ServiceCatalog responsesServiceCatalog = JsonUtils.toJsonObj(response,  ServiceCatalog.class);
        String id = responsesServiceCatalog.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceCatalogManagement/v4/serviceCatalog/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        ServiceCatalog responsesServiceCatalog2 = JsonUtils.toJsonObj(response2,  ServiceCatalog.class);
        assertThat( responsesServiceCatalog2.getName() ).isEqualTo( "Test Catalog" );
        assertThat( responsesServiceCatalog2.getVersion() ).isEqualTo( "1.8" );
    }

    private String createServiceCatalog() throws Exception{

        File scatalog = new File( "src/test/resources/testResourceCatalog.txt" );
        InputStream in = new FileInputStream( scatalog );
        String resvxf = IOUtils.toString(in, "UTF-8");

        ServiceCatalogCreate scc = JsonUtils.toJsonObj( resvxf,  ServiceCatalogCreate.class);

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceCatalog")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( scc ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Catalog")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        return response;
    }
}
