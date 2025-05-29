package org.etsi.osl.services.api.pm628;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.ManagementJobMVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagementJobMVOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testToStringShowsExecutionState() {
        ManagementJobMVO job = new ManagementJobMVO("ManagementJob");
        job.setExecutionState(ExecutionStateType.ACKNOWLEDGED);

        String str = job.toString();
        // executionState is not included in the default toString, so this will fail unless you add it to the toString method
        assertTrue(str.contains("executionState: acknowledged"), "Default toString does not show executionState");
    }

    @Test
    void testSerializationDeserialization() throws Exception {
        ManagementJobMVO job = new ManagementJobMVO("ManagementJob");
        job.setExecutionState(ExecutionStateType.ACKNOWLEDGED);

        String json = objectMapper.writeValueAsString(job);
        ManagementJobMVO deserialized = objectMapper.readValue(json, ManagementJobMVO.class);

        assertEquals(job, deserialized);
        assertEquals(ExecutionStateType.ACKNOWLEDGED, deserialized.getExecutionState());
    }
}