package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.ExportJob;
import org.etsi.osl.tmf.rcm634.model.ExportJobCreateEventPayload;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

public class ExportJobCreateEventPayloadTest {

    @Test
    void testExportJobCreateEventPayload() {
        ExportJobCreateEventPayload payload = new ExportJobCreateEventPayload();
        ExportJob job = new ExportJob();
        job.setId("testId");

        payload.exportJob(job);
        assertEquals(job, payload.getExportJob());

        payload = new ExportJobCreateEventPayload();
        payload.setExportJob(job);
        assertEquals(job, payload.getExportJob());
    }

    @Test
    void testEquals() {
        ExportJobCreateEventPayload payload1 = new ExportJobCreateEventPayload();
        ExportJob job = new ExportJob();
        job.setId("testId");
        payload1.setExportJob(job);

        ExportJobCreateEventPayload payload2 = new ExportJobCreateEventPayload();
        payload2.setExportJob(job);

        assertTrue(payload1.equals(payload2));

        payload1.setExportJob(new ExportJob());

        assertFalse(payload1.equals(payload2));
    }

    @Test
    void testHashCode() {
        ExportJobCreateEventPayload payload1 = new ExportJobCreateEventPayload();
        ExportJob job = new ExportJob();
        job.setId("testId");
        payload1.setExportJob(job);

        ExportJobCreateEventPayload payload2 = new ExportJobCreateEventPayload();
        payload2.setExportJob(job);

        assertEquals(payload1.hashCode(), payload2.hashCode());

        payload1.setExportJob(new ExportJob());

        assertNotEquals(payload1.hashCode(), payload2.hashCode());
    }

    @Test
    void testToString() {
        ExportJobCreateEventPayload payload = new ExportJobCreateEventPayload();
        ExportJob job = new ExportJob();
        job.setId("testId");
        payload.setExportJob(job);

        String expectedString = "class ExportJobCreateEventPayload {\n" +
                "    exportJob: class ExportJob {\n" +
                "        id: testId\n" +
                "        href: null\n" +
                "        completionDate: null\n" +
                "        contentType: null\n" +
                "        creationDate: null\n" +
                "        errorLog: null\n" +
                "        path: null\n" +
                "        query: null\n" +
                "        url: null\n" +
                "        status: null\n" +
                "        _atBaseType: null\n" +
                "        _atSchemaLocation: null\n" +
                "        _atType: null\n" +
                "    }\n" +
                "}";

        assertEquals(expectedString, payload.toString());
    }
}