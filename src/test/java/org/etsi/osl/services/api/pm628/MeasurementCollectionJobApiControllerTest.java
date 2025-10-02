package org.etsi.osl.services.api.pm628;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.pm628.model.AdministrativeState;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpoint;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpointMVO;
import org.etsi.osl.tmf.pm628.model.DataFilterAttributeStringArray;
import org.etsi.osl.tmf.pm628.model.DataFilterMapItemMVO;
import org.etsi.osl.tmf.pm628.model.DataFilterMapMVO;
import org.etsi.osl.tmf.pm628.model.DataFilterTemplateMVO;
import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.Granularity;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobCreateEvent;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobCreateEventPayload;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobFVO;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobMVO;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobRef;
import org.etsi.osl.tmf.pm628.model.ReportingPeriod;
import org.etsi.osl.tmf.pm628.model.ResourceStatusType;
import org.etsi.osl.tmf.pm628.reposervices.MeasurementCollectionJobService;
import org.etsi.osl.tmf.ri639.model.ResourceAdministrativeStateType;
import org.etsi.osl.tmf.ri639.model.ResourceOperationalStateType;
import org.etsi.osl.tmf.ri639.model.ResourceUsageStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class MeasurementCollectionJobApiControllerTest extends BaseIT {

    private static final int FIXED_BOOTSTRAPS_JOBS = 0;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    MeasurementCollectionJobService measurementCollectionJobService;

    @BeforeEach
    public void setup() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).
                apply(springSecurity(springSecurityFilterChain)).build();

    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testFindAllMeasurementCollectionJobs() throws Exception {
        String response = mvc
                .perform(MockMvcRequestBuilders.get("/monitoring/v5/measurementCollectionJob")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<MeasurementCollectionJob> mcjList = objectMapper.readValue(response, new TypeReference<List<MeasurementCollectionJob>>() {});
        assertThat(mcjList.size()).isEqualTo(0);
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testFindMeasurementCollectionJobByUuid() throws Exception {
        String response = createMeasurementCollectionJob();
        MeasurementCollectionJob mcj = JsonUtils.toJsonObj(response, MeasurementCollectionJob.class);
        String id = mcj.getUuid();

        String response2 = mvc
                .perform(MockMvcRequestBuilders.get("/monitoring/v5/measurementCollectionJob/" + id)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        MeasurementCollectionJob mcj2 = JsonUtils.toJsonObj(response2, MeasurementCollectionJob.class);

        assertThat(mcj2.getConsumingApplicationId()).isEqualTo(mcj.getConsumingApplicationId());
        assertThat(mcj2.getJobId()).isEqualTo(mcj.getJobId());
        assertThat(mcj2.getAdminState()).isEqualTo(mcj.getAdminState());
        assertThat(mcj2.getExecutionState()).isEqualTo(mcj.getExecutionState());
        assertThat(mcj2.getGranularity()).isEqualTo(mcj.getGranularity());
        assertThat(mcj2.getReportingPeriod()).isEqualTo(mcj.getReportingPeriod());
    }

    
    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testCreateMeasurementCollectionJob() throws Exception {
        String response = createMeasurementCollectionJob();
        MeasurementCollectionJob mcj = JsonUtils.toJsonObj(response, MeasurementCollectionJob.class);

        assertThat(mcj.getConsumingApplicationId()).isEqualTo("4000");
        assertThat(mcj.getJobId()).isEqualTo("400");
        assertThat(mcj.getAdminState()).isEqualTo(AdministrativeState.LOCKED);
        assertThat(mcj.getExecutionState()).isEqualTo(ExecutionStateType.ACKNOWLEDGED);
        assertThat(mcj.getGranularity()).isEqualTo(Granularity.fromValue("g_1mn"));
        assertThat(mcj.getReportingPeriod()).isEqualTo(ReportingPeriod.fromValue("r_1mn"));
        
        
     // Create the event
        MeasurementCollectionJobRef ref = new MeasurementCollectionJobRef();
        ref.setId(mcj.getUuid());
        ref.setHref(mcj.getHref());
        ref.setName("MeasurementCollectionJob");
        
        MeasurementCollectionJobCreateEventPayload payload = new MeasurementCollectionJobCreateEventPayload();
        payload.setMeasurementCollectionJob(ref);

        MeasurementCollectionJobCreateEvent event = new MeasurementCollectionJobCreateEvent();
        event.setTitle("MeasurementCollectionJob created");
        event.setDescription("MeasurementCollectionJob with UUID: " + mcj.getUuid() + " has been created");
        event.setEvent(payload);

        String apayload = toJsonString(event);
        
        MeasurementCollectionJobCreateEvent eventresponse = toJsonObj (apayload, MeasurementCollectionJobCreateEvent.class);
        assertThat(eventresponse.getEvent().getMeasurementCollectionJob()).isNotNull();
    }
    

    static String toJsonString(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }

    static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue( content, valueType);
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testUpdateMeasurementCollectionJob() throws Exception {
        String response = createMeasurementCollectionJob();
        MeasurementCollectionJob mcj = JsonUtils.toJsonObj(response, MeasurementCollectionJob.class);
        String id = mcj.getUuid();

        MeasurementCollectionJobMVO mcjMVO = new MeasurementCollectionJobMVO();
        mcjMVO.setOutputFormat("Test Output Format");
        mcjMVO.setGranularity(Granularity.G_5MN);
        mcjMVO.setReportingPeriod(ReportingPeriod.R_1H);

        List<DataAccessEndpointMVO> daeList =new ArrayList<>();
        DataAccessEndpointMVO daeMVO = new DataAccessEndpointMVO();
        daeMVO.setUri(URI.create("https://test.com"));
        daeMVO.setApiType("Test API type");
        daeMVO.setValue("Test value");
        daeMVO.setCategory("Test category");
        daeMVO.setDescription("Test description");
        daeMVO.setName("Test name");
        daeMVO.setEndOperatingDate(OffsetDateTime.now());
        daeMVO.setAdministrativeState(ResourceAdministrativeStateType.LOCKED);
        daeMVO.setOperationalState(ResourceOperationalStateType.ENABLE);
        daeMVO.setResourceStatus(ResourceStatusType.AVAILABLE);
        daeMVO.setUsageState(ResourceUsageStateType.IDLE);

        DataFilterMapMVO dfmMVO = new DataFilterMapMVO();
        List<DataFilterMapItemMVO> mappings = new ArrayList<>();

        DataFilterMapItemMVO dfmiMVO = new DataFilterMapItemMVO();

        // Set filterTemplate value for dfmiMVO
        DataFilterTemplateMVO dftMVO = new DataFilterTemplateMVO();
        dftMVO.setName("Test DataFilterTemplate");
        dftMVO.setDescription("A Test DataFilterTemplate");
        dfmiMVO.setFilterTemplate(dftMVO);

        // Set stringArray value for dfmiMVO
        DataFilterAttributeStringArray dfasa = new DataFilterAttributeStringArray();
        List<String> list = new ArrayList<>();
        list.add("Test DataFilterAttributeString 1");
        list.add("Test DataFilterAttributeString 2");
        dfasa.setValue(list);
        dfmiMVO.setStringArray(dfasa);

        mappings.add(dfmiMVO);
        dfmMVO.setMappings(mappings);
        daeMVO.setUriQueryFilter(dfmMVO);
        daeList.add(daeMVO);
        mcjMVO.setDataAccessEndpoint(daeList);

        String response2 = mvc
                .perform(MockMvcRequestBuilders.patch("/monitoring/v5/measurementCollectionJob/" + id)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(mcjMVO)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        MeasurementCollectionJob updatedMcj = JsonUtils.toJsonObj(response2, MeasurementCollectionJob.class);

        assertThat(updatedMcj.getOutputFormat()).isEqualTo("Test Output Format");
        assertThat(updatedMcj.getGranularity()).isEqualTo(Granularity.G_5MN);
        assertThat(updatedMcj.getReportingPeriod()).isEqualTo(ReportingPeriod.R_1H);

        List<DataAccessEndpoint> dataAccessEndpointList = updatedMcj.getDataAccessEndpoint();
        DataAccessEndpoint updatedDae = dataAccessEndpointList.get(0);
        assertThat(updatedDae.getUri()).isEqualTo(URI.create("https://test.com"));
        assertThat(updatedDae.getApiType()).isEqualTo("Test API type");
        assertThat(updatedDae.getValue()).isEqualTo("Test value");
        assertThat(updatedDae.getCategory()).isEqualTo("Test category");
        assertThat(updatedDae.getDescription()).isEqualTo("Test description");
        assertThat(updatedDae.getName()).isEqualTo("Test name");
        assertThat(updatedDae.getEndOperatingDate()).isEqualTo(daeMVO.getEndOperatingDate());
        assertThat(updatedDae.getAdministrativeState()).isEqualTo(ResourceAdministrativeStateType.LOCKED);
        assertThat(updatedDae.getOperationalState()).isEqualTo(ResourceOperationalStateType.ENABLE);
        assertThat(updatedDae.getResourceStatus()).isEqualTo(ResourceStatusType.AVAILABLE);
        assertThat(updatedDae.getUsageState()).isEqualTo(ResourceUsageStateType.IDLE);
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testDeleteMeasurementCollectionJob() throws Exception {
        String response = createMeasurementCollectionJob();
        MeasurementCollectionJob mcj = JsonUtils.toJsonObj(response, MeasurementCollectionJob.class);
        String id = mcj.getUuid();
        

        assertThat(measurementCollectionJobService.findAllMeasurementCollectionJobs().size()).isEqualTo(1);

        mvc
            .perform(MockMvcRequestBuilders.delete("/monitoring/v5/measurementCollectionJob/" + id)
                    .with( SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(measurementCollectionJobService.findAllMeasurementCollectionJobs().size()).isEqualTo(0);
    }



    private String createMeasurementCollectionJob() throws Exception {

        File fvo = new File("src/test/resources/testMeasurementCollectionJobFVO.json");
        InputStream in = new FileInputStream(fvo);
        String mcjFVOText = IOUtils.toString(in, "UTF-8");

        MeasurementCollectionJobFVO mcjFVO = JsonUtils.toJsonObj(mcjFVOText, MeasurementCollectionJobFVO.class);

        String response = mvc
                .perform(MockMvcRequestBuilders.post("/monitoring/v5/measurementCollectionJob")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(mcjFVO)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


        return response;
    }
}
