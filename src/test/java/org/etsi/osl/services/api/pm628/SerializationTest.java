package org.etsi.osl.services.api.pm628;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationTest {

    @Test
    public void testJsonArraySerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a list of objects to serialize
        MeasurementCollectionJob job1 = new MeasurementCollectionJob("JobType1").outputFormat("JSON");
        job1.setType("MeasurementCollectionJob");
        MeasurementCollectionJob job2 = new MeasurementCollectionJob("JobType2").outputFormat("JSON");
        job2.setType("MeasurementCollectionJob");
        String job1Str = objectMapper.writeValueAsString(job1);
        System.out.println(job1Str);
//        List<MeasurementCollectionJob> jobs = List.of(job1, job2);
        List<MeasurementCollectionJob> jobs = new ArrayList<>();
        jobs.add(job1);
        jobs.add(job2);

        // Serialize the list to JSON
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        String jsonArray = objectMapper.writeValueAsString(jobs);
        System.out.println(jsonArray);

        // Deserialize the JSON back to a list
        var deserializedJobs = objectMapper.readValue(
                jsonArray,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MeasurementCollectionJob.class)
        );

        // Assert the deserialized list matches the original
        assertEquals(jobs.size(), ((List<MeasurementCollectionJob>) deserializedJobs).size());
        assertEquals(jobs.get(0).getOutputFormat(), ((List<MeasurementCollectionJob>) deserializedJobs).get(0).getOutputFormat());
        assertEquals(jobs.get(1).getOutputFormat(), ((List<MeasurementCollectionJob>) deserializedJobs).get(1).getOutputFormat());
    }
}