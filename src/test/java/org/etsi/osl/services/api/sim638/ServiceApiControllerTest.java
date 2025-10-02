package org.etsi.osl.services.api.sim638;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.service.ServiceSpecificationRef;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.sim638.api.ServiceApiController;
import org.etsi.osl.tmf.sim638.model.Service;
import org.etsi.osl.tmf.sim638.model.ServiceCreate;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import org.etsi.osl.tmf.so641.model.ServiceOrderCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrderItem;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.etsi.osl.tmf.so641.model.ServiceRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ServiceApiControllerTest  extends BaseIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    ServiceRepoService serviceRepoService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceApiController mockServiceApiController;

    private ServiceRepoService mockServiceRepoService;

    @BeforeEach
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Mocks
        mockServiceRepoService = mock(ServiceRepoService.class);
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        mockServiceApiController = new ServiceApiController(mockObjectMapper, null);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test01DeleteService() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/serviceInventory/v4/service/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(501) );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test02ListService() throws Exception {
        createService();
        String response = mvc.perform(MockMvcRequestBuilders.get("/serviceInventory/v4/service" )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<Service> serviceList = objectMapper.readValue(response, new TypeReference<List<Service>>() {});
        assertThat(serviceList.size()).isEqualTo( 0 );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test03RetrieveService() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceInventory/v4/service/" + id)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        Service responsesService2 = JsonUtils.toJsonObj(response2, Service.class);

        assertThat(responsesService2.getId()).isEqualTo(responsesService.getId());
        assertThat(responsesService2.getCategory()).isEqualTo(responsesService.getCategory());
        assertThat(responsesService2.getName()).isEqualTo(responsesService.getName());
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test04CreateServiceHandleException() {
        ServiceCreate serviceCreate = new ServiceCreate();
        serviceCreate.setName("Test name");

        when(mockServiceRepoService.addService(any()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<Service> response = mockServiceApiController.createService(mock(Principal.class), serviceCreate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test05ListServiceHandleException(){
        when(mockServiceRepoService.findAll())
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<List<Service>> response = mockServiceApiController.listService(mock(Principal.class), null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void test06RetrieveServiceHandleException(){
        when(mockServiceRepoService.findByUuid(any()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<Service> response = mockServiceApiController.retrieveService(mock(Principal.class), "test id", "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private String createService() throws Exception {
        int servicesCount = serviceRepoService.findAll().size();

        File sspec = new File("src/test/resources/testServiceSpec.json");
        InputStream in = new FileInputStream(sspec);
        String sspectext = IOUtils.toString(in, "UTF-8");

        ServiceSpecificationCreate sspeccr = JsonUtils.toJsonObj(sspectext, ServiceSpecificationCreate.class);
        sspeccr.setName("Spec1");
        ServiceSpecification responsesSpec = createServiceSpec( sspeccr);

        ServiceOrderCreate serviceOrder = new ServiceOrderCreate();
        serviceOrder.setCategory("Test Category");
        serviceOrder.setDescription("A Test Service");
        serviceOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        serviceOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());

        ServiceOrderItem soi = new ServiceOrderItem();
        serviceOrder.getOrderItem().add(soi);
        soi.setState(ServiceOrderStateType.ACKNOWLEDGED);

        ServiceRestriction serviceRestriction = new ServiceRestriction();
        ServiceSpecificationRef aServiceSpecificationRef = new ServiceSpecificationRef();
        aServiceSpecificationRef.setId(responsesSpec.getId());
        aServiceSpecificationRef.setName(responsesSpec.getName());

        serviceRestriction.setServiceSpecification(aServiceSpecificationRef);
        serviceRestriction.setName("aServiceRestriction");
        soi.setService(serviceRestriction);

        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceInventory/v4/service")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceOrder)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Service responseService = JsonUtils.toJsonObj(response, Service.class);

        assertThat( serviceRepoService.findAll().size() ).isEqualTo( servicesCount + 1 );
        assertThat(responseService.getCategory()).isEqualTo("Test Category");
        assertThat(responseService.getDescription()).isEqualTo("A Test Service");

        return response;
    }


    private ServiceSpecification createServiceSpec( ServiceSpecificationCreate serviceSpecificationCreate) throws Exception {
        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceSpecificationCreate)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response, ServiceSpecification.class);

        return responsesSpec;
    }
}
