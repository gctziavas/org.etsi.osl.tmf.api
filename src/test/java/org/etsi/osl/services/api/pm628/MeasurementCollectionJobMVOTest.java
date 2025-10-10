package org.etsi.osl.services.api.pm628;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobMVO;
import org.etsi.osl.tmf.pm628.model.ReportingPeriod;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

class MeasurementCollectionJobMVOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testToString() {
        MeasurementCollectionJobMVO job = new MeasurementCollectionJobMVO("MeasurementCollectionJob")
                .outputFormat("JSON")
                .jobOnDemand(true);
        job.setExecutionState(ExecutionStateType.ACKNOWLEDGED);

        String str = job.toString();
        assertTrue(str.contains("outputFormat: JSON"));
        assertTrue(str.contains("jobOnDemand: true"));
        assertTrue(str.contains("executionState: acknowledged"));
        assertTrue(str.contains("class MeasurementCollectionJobMVO"));
    }

    @Test
    void testSerializationDeserialization() throws Exception {
        MeasurementCollectionJobMVO job = new MeasurementCollectionJobMVO("MeasurementCollectionJob")
                .outputFormat("JSON")
                .jobOnDemand(false);

        // Set a nested object for better coverage
        job.setReportingPeriod(ReportingPeriod.R_1H);
        job.setExecutionState(ExecutionStateType.ACKNOWLEDGED);

        String json = objectMapper.writeValueAsString(job);
        MeasurementCollectionJobMVO deserialized = objectMapper.readValue(json, MeasurementCollectionJobMVO.class);

        assertEquals(job, deserialized);
        assertEquals("JSON", deserialized.getOutputFormat());
        assertFalse(deserialized.getJobOnDemand());
        assertNotNull(deserialized.getReportingPeriod());
        assertEquals(ExecutionStateType.ACKNOWLEDGED, deserialized.getExecutionState());
    }
}