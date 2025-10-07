package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import org.etsi.osl.tmf.rcm634.model.ExportJobCreate;
import org.etsi.osl.tmf.rcm634.model.JobStateType;
import org.junit.jupiter.api.Test;

public class ExportJobCreateTest {

    @Test
    void testExportJobCreate() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String query = "testQuery";
        OffsetDateTime completionDate = OffsetDateTime.now();
        OffsetDateTime creationDate = OffsetDateTime.now();
        String contentType = "testContentType";
        String errorLog = "testErrorLog";
        String path = "testPath";
        String url = "testUrl";

        jobCreate.query(query);
        jobCreate.setCompletionDate(completionDate);
        jobCreate.setCreationDate(creationDate);
        jobCreate.setContentType(contentType);
        jobCreate.setErrorLog(errorLog);
        jobCreate.setPath(path);
        jobCreate.setUrl(url);

        assertEquals(query, jobCreate.getQuery());
        assertEquals(completionDate, jobCreate.getCompletionDate());
        assertEquals(creationDate, jobCreate.getCreationDate());
        assertEquals(contentType, jobCreate.getContentType());
        assertEquals(errorLog, jobCreate.getErrorLog());
        assertEquals(path, jobCreate.getPath());
        assertEquals(url, jobCreate.getUrl());

        jobCreate = new ExportJobCreate();
        jobCreate.setQuery(query);
        jobCreate.setCompletionDate(completionDate);
        jobCreate.setCreationDate(creationDate);
        jobCreate.setContentType(contentType);
        jobCreate.setErrorLog(errorLog);
        jobCreate.setPath(path);
        jobCreate.setUrl(url);

        assertEquals(query, jobCreate.getQuery());
        assertEquals(completionDate, jobCreate.getCompletionDate());
        assertEquals(creationDate, jobCreate.getCreationDate());
        assertEquals(contentType, jobCreate.getContentType());
        assertEquals(errorLog, jobCreate.getErrorLog());
        assertEquals(path, jobCreate.getPath());
        assertEquals(url, jobCreate.getUrl());
    }

    @Test
    void testStatus() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        JobStateType status = JobStateType.RUNNING;
        jobCreate.setStatus(status);
        assertEquals(status, jobCreate.getStatus());

        ExportJobCreate newJobCreate = jobCreate.status(JobStateType.FAILED);
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testAtBaseType() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String baseType = "testBaseType";
        jobCreate.setAtBaseType(baseType);
        assertEquals(baseType, jobCreate.getAtBaseType());

        ExportJobCreate newJobCreate = jobCreate._atBaseType("newTestBaseType");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testAtSchemaLocation() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String schemaLocation = "testSchemaLocation";
        jobCreate.setAtSchemaLocation(schemaLocation);
        assertEquals(schemaLocation, jobCreate.getAtSchemaLocation());

        ExportJobCreate newJobCreate = jobCreate._atSchemaLocation("newTestSchemaLocation");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testAtType() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String type = "testType";
        jobCreate.setAtType(type);
        assertEquals(type, jobCreate.getAtType());

        ExportJobCreate newJobCreate = jobCreate._atType("newTestType");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testErrorLog() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String errorLog = "testErrorLog";
        jobCreate.setErrorLog(errorLog);
        assertEquals(errorLog, jobCreate.getErrorLog());

        ExportJobCreate newJobCreate = jobCreate.errorLog("newTestErrorLog");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testPath() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String path = "testPath";
        jobCreate.setPath(path);
        assertEquals(path, jobCreate.getPath());

        ExportJobCreate newJobCreate = jobCreate.path("newTestPath");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testUrl() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        String url = "testUrl";
        jobCreate.setUrl(url);
        assertEquals(url, jobCreate.getUrl());

        ExportJobCreate newJobCreate = jobCreate.url("newTestUrl");
        assertEquals(jobCreate, newJobCreate);
    }

    @Test
    void testEquals() {
        ExportJobCreate jobCreate1 = new ExportJobCreate();
        jobCreate1.setQuery("testQuery");
        jobCreate1.setCompletionDate(OffsetDateTime.now());
        jobCreate1.setCreationDate(OffsetDateTime.now());
        jobCreate1.setContentType("testContentType");
        jobCreate1.setErrorLog("testErrorLog");
        jobCreate1.setPath("testPath");
        jobCreate1.setUrl("testUrl");

        ExportJobCreate jobCreate2 = new ExportJobCreate();
        jobCreate2.setQuery("testQuery");
        jobCreate2.setCompletionDate(jobCreate1.getCompletionDate());
        jobCreate2.setCreationDate(jobCreate1.getCreationDate());
        jobCreate2.setContentType("testContentType");
        jobCreate2.setErrorLog("testErrorLog");
        jobCreate2.setPath("testPath");
        jobCreate2.setUrl("testUrl");

        assertTrue(jobCreate1.equals(jobCreate2));

        jobCreate1.setQuery("differentQuery");

        assertFalse(jobCreate1.equals(jobCreate2));
    }

    @Test
    void testHashCode() {
        ExportJobCreate jobCreate1 = new ExportJobCreate();
        jobCreate1.setQuery("testQuery");
        jobCreate1.setCompletionDate(OffsetDateTime.now());
        jobCreate1.setCreationDate(OffsetDateTime.now());
        jobCreate1.setContentType("testContentType");
        jobCreate1.setErrorLog("testErrorLog");
        jobCreate1.setPath("testPath");
        jobCreate1.setUrl("testUrl");

        ExportJobCreate jobCreate2 = new ExportJobCreate();
        jobCreate2.setQuery("testQuery");
        jobCreate2.setCompletionDate(jobCreate1.getCompletionDate());
        jobCreate2.setCreationDate(jobCreate1.getCreationDate());
        jobCreate2.setContentType("testContentType");
        jobCreate2.setErrorLog("testErrorLog");
        jobCreate2.setPath("testPath");
        jobCreate2.setUrl("testUrl");

        assertEquals(jobCreate1.hashCode(), jobCreate2.hashCode());

        jobCreate1.setQuery("differentQuery");

        assertNotEquals(jobCreate1.hashCode(), jobCreate2.hashCode());
    }

    @Test
    void testToString() {
        ExportJobCreate jobCreate = new ExportJobCreate();
        jobCreate.setQuery("testQuery");
        jobCreate.setCompletionDate(OffsetDateTime.parse("2024-01-19T17:02:03.289504717+02:00"));
        jobCreate.setCreationDate(OffsetDateTime.parse("2024-01-19T17:02:03.289527665+02:00"));
        jobCreate.setContentType("testContentType");
        jobCreate.setErrorLog("testErrorLog");
        jobCreate.setPath("testPath");
        jobCreate.setUrl("testUrl");

        String expectedString = "class ExportJobCreate {\n" +
                "    completionDate: 2024-01-19T17:02:03.289504717+02:00\n" +
                "    contentType: testContentType\n" +
                "    creationDate: 2024-01-19T17:02:03.289527665+02:00\n" +
                "    errorLog: testErrorLog\n" +
                "    path: testPath\n" +
                "    query: testQuery\n" +
                "    url: testUrl\n" +
                "    status: null\n" +
                "    _atBaseType: null\n" +
                "    _atSchemaLocation: null\n" +
                "    _atType: null\n" +
                "}";

        assertEquals(expectedString, jobCreate.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        ExportJobCreate jobCreate = new ExportJobCreate();

        Method method = ExportJobCreate.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(jobCreate, input);

        assertEquals(expectedOutput, output);
    }
}