package org.etsi.osl.services.api.pm628;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.io.FileInputStream;
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
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobFVO;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobMVO;
import org.etsi.osl.tmf.pm628.model.ReportingPeriod;
import org.etsi.osl.tmf.pm628.model.ResourceStatusType;
import org.etsi.osl.tmf.pm628.reposervices.MeasurementCollectionJobService;
import org.etsi.osl.tmf.ri639.model.ResourceAdministrativeStateType;
import org.etsi.osl.tmf.ri639.model.ResourceOperationalStateType;
import org.etsi.osl.tmf.ri639.model.ResourceUsageStateType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

public class MeasurementCollectionJobServiceTest  extends BaseIT {

    private static final int FIXED_BOOTSTRAPS_JOBS = 0;

    @Autowired
    MeasurementCollectionJobService measurementCollectionJobService;

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testFindAllMeasurementCollectionJobs() throws Exception {
        createMeasurementCollectionJob();

        List<MeasurementCollectionJob> mcjList = measurementCollectionJobService.findAllMeasurementCollectionJobs();

        assertThat(mcjList.size()).isEqualTo(3);
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testFindAllByExecutionState() throws Exception {
        createMeasurementCollectionJob();

        List<MeasurementCollectionJob> mcjList = measurementCollectionJobService.findAllByExecutionState(ExecutionStateType.ACKNOWLEDGED);

        assertThat(mcjList.size()).isEqualTo(2);
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testFindMeasurementCollectionJobByUuid() throws Exception {
        MeasurementCollectionJob mcj = createMeasurementCollectionJob();
        String id = mcj.getUuid();

        MeasurementCollectionJob mcj2 = measurementCollectionJobService.findMeasurementCollectionJobByUuid(id);

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
        MeasurementCollectionJob mcj = createMeasurementCollectionJob();

        assertThat(mcj.getConsumingApplicationId()).isEqualTo("4000");
        assertThat(mcj.getJobId()).isEqualTo("400");
        assertThat(mcj.getAdminState()).isEqualTo(AdministrativeState.LOCKED);
        assertThat(mcj.getExecutionState()).isEqualTo(ExecutionStateType.ACKNOWLEDGED);
        assertThat(mcj.getGranularity()).isEqualTo(Granularity.fromValue("g_1mn"));
        assertThat(mcj.getReportingPeriod()).isEqualTo(ReportingPeriod.fromValue("r_1mn"));
    }

    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
    @Test
    public void testUpdateMeasurementCollectionJob() throws Exception {
        MeasurementCollectionJob mcj = createMeasurementCollectionJob();
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

        MeasurementCollectionJob updatedMcj = measurementCollectionJobService.updateMeasurementCollectionJob(id, mcjMVO);

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
        MeasurementCollectionJob mcj = createMeasurementCollectionJob();
        String id = mcj.getUuid();

        measurementCollectionJobService.deleteMeasurementCollectionJob(id);

        assertThat(measurementCollectionJobService.findAllMeasurementCollectionJobs().size()).isEqualTo(1);
    }



    private MeasurementCollectionJob createMeasurementCollectionJob() throws Exception {

        File fvo = new File("src/test/resources/testMeasurementCollectionJobFVO.json");
        InputStream in = new FileInputStream(fvo);
        String mcjFVOText = IOUtils.toString(in, "UTF-8");

        MeasurementCollectionJobFVO mcjFVO = JsonUtils.toJsonObj(mcjFVOText, MeasurementCollectionJobFVO.class);

        MeasurementCollectionJob response = measurementCollectionJobService.createMeasurementCollectionJob(mcjFVO);


        return response;
    }

    private MeasurementCollectionJob createNewMeasurementCollectionJobWithExecutionState(int previousNumberOfMcjs, ExecutionStateType executionStateType) throws Exception {
        assertThat(measurementCollectionJobService.findAllMeasurementCollectionJobs().size()).isEqualTo(FIXED_BOOTSTRAPS_JOBS + previousNumberOfMcjs);

        File fvo = new File("src/test/resources/testMeasurementCollectionJobFVO.json");
        InputStream in = new FileInputStream(fvo);
        String mcjFVOText = IOUtils.toString(in, "UTF-8");

        MeasurementCollectionJobFVO mcjFVO = JsonUtils.toJsonObj(mcjFVOText, MeasurementCollectionJobFVO.class);
        mcjFVO.setExecutionState(executionStateType);

        MeasurementCollectionJob response = measurementCollectionJobService.createMeasurementCollectionJob(mcjFVO);

        assertThat(measurementCollectionJobService.findAllMeasurementCollectionJobs().size()).isEqualTo(FIXED_BOOTSTRAPS_JOBS + previousNumberOfMcjs + 1);

        return response;
    }

//    @WithMockUser(username="osadmin", roles = {"USER","ADMIN"})
//    @Test
//    public void testFindPendingOrInProgressMeasurementCollectionJobs() throws Exception {
//        MeasurementCollectionJob pendingMcj = createNewMeasurementCollectionJobWithExecutionState(measurementCollectionJobService.findAllMeasurementCollectionJobs().size(), ExecutionStateType.PENDING);
//
//        int currentNumOfMcjs = measurementCollectionJobService.findAllMeasurementCollectionJobs().size();
//        MeasurementCollectionJob inProgressMcj = createNewMeasurementCollectionJobWithExecutionState(currentNumOfMcjs, ExecutionStateType.INPROGRESS);
//
//
//        List<MeasurementCollectionJob> ackMcjList = measurementCollectionJobService.findAllByExecutionState(ExecutionStateType.ACKNOWLEDGED);
//
//        List<MeasurementCollectionJob> mcjList = measurementCollectionJobService.findPendingOrInProgressMeasurementCollectionJobs();
//
//
//        assertThat(mcjList.size()).isEqualTo(FIXED_BOOTSTRAPS_JOBS + 2);
//    }
}
