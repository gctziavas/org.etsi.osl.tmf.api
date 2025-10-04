package org.etsi.osl.services.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.ServiceSpecificationRef;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.metrics.api.ServiceMetricsApiController;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.sim638.model.Service;
import org.etsi.osl.tmf.sim638.model.ServiceCreate;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.jayway.jsonpath.JsonPath;

public class ServiceMetricsApiControllerTest  extends BaseIT {

    private MockMvc mvc;

    @Autowired
    ServiceRepoService serviceRepoService;

    @Autowired
    private WebApplicationContext context;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeAll
    public void setup() throws Exception {
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
    public void testCountTotalServices() throws Exception {
        createService(ServiceStateType.ACTIVE);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalServices" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalServices = JsonPath.read(response, "$.totalServices");


        assertThat(totalServices).isEqualTo(serviceRepoService.findAll().size());
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCountTotalServicesWithState() throws Exception {
        createService(ServiceStateType.ACTIVE);
        createService(ServiceStateType.INACTIVE);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalServices" )
                        .param("state", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalServices = JsonPath.read(response, "$.totalServices");


        List<Service> servicesList = serviceRepoService.findAll();
        int activeServices = (int) servicesList.stream().filter(service -> service.getState() == ServiceStateType.ACTIVE).count();

        assertThat(totalServices).isEqualTo(activeServices);
        assertThat(activeServices).isEqualTo(2);
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
    @Test
    public void testGetServicesGroupedByState() throws Exception {
        String startTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        createService(ServiceStateType.ACTIVE);
        createService(ServiceStateType.ACTIVE);
        createService(ServiceStateType.ACTIVE);
        createService(ServiceStateType.INACTIVE);
        createService(ServiceStateType.INACTIVE);
        createService(ServiceStateType.TERMINATED);

        String endTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/servicesGroupByState")
                        .param("starttime", startTime)
                        .param("endtime", endTime)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> groupByState = JsonPath.read(response, "$.services.aggregations.groupByState");

        // Create a map from key -> count
        Map<String, Integer> stateCounts = groupByState.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.get("key"),
                        entry -> (Integer) entry.get("count")
                ));

        assertThat(stateCounts.get("ACTIVE")).isEqualTo(3);
        assertThat(stateCounts.get("INACTIVE")).isEqualTo(2);
        assertThat(stateCounts.get("TERMINATED")).isEqualTo(1);
        assertThat(stateCounts.get("FEASIBILITYCHECKED")).isEqualTo(0);
        assertThat(stateCounts.get("RESERVED")).isEqualTo(0);
        assertThat(stateCounts.get("DESIGNED")).isEqualTo(0);
    }



    @Transactional
    void createService(ServiceStateType state ) throws Exception {
        int servicesCount = serviceRepoService.findAll().size();

        File sspec = new File( "src/test/resources/testServiceSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");

        ServiceSpecificationCreate sspeccr1 = JsonUtils.toJsonObj( sspectext,  ServiceSpecificationCreate.class);
        sspeccr1.setName("Spec1");
        ServiceSpecification responsesSpec = createServiceSpec(sspeccr1);

        ServiceCreate aService = new ServiceCreate();
        aService.setName("aNew Service");
        aService.setCategory("Test Category");
        aService.setDescription("A Test Service");
        aService.setStartDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aService.setEndDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aService.setState(state);

        Characteristic serviceCharacteristicItem = new Characteristic();

        serviceCharacteristicItem.setName( "ConfigStatus" );
        serviceCharacteristicItem.setValue( new Any("NONE"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        ServiceSpecificationRef aServiceSpecificationRef = new ServiceSpecificationRef();
        aServiceSpecificationRef.setId(responsesSpec.getId() );
        aServiceSpecificationRef.setName(responsesSpec.getName());

        aService.setServiceSpecificationRef(aServiceSpecificationRef );

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceInventory/v4/service")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( aService ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Service responseService = JsonUtils.toJsonObj(response, Service.class);

        assertThat( serviceRepoService.findAll().size() ).isEqualTo( servicesCount + 1 );
        assertThat(responseService.getCategory()).isEqualTo("Test Category");
        assertThat(responseService.getDescription()).isEqualTo("A Test Service");

    }

    private ServiceSpecification createServiceSpec(ServiceSpecificationCreate sspeccr1) throws Exception{
        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( sspeccr1 ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonUtils.toJsonObj(response,  ServiceSpecification.class);
    }
}
