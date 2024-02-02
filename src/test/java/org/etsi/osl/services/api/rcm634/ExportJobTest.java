package org.etsi.osl.services.api.rcm634;

import org.etsi.osl.tmf.rcm634.model.ExportJob;
import org.etsi.osl.tmf.rcm634.model.JobStateType;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ExportJobTest {

    @Test
    void testExportJob() {
        ExportJob job = new ExportJob();
        String id = "testId";
        String query = "testQuery";
        OffsetDateTime completionDate = OffsetDateTime.now();
        OffsetDateTime creationDate = OffsetDateTime.now();
        String contentType = "testContentType";
        String errorLog = "testErrorLog";
        String path = "testPath";
        String url = "testUrl";

        job.id(id);
        job.query(query);
        job.setCompletionDate(completionDate);
        job.setCreationDate(creationDate);
        job.setContentType(contentType);
        job.setErrorLog(errorLog);
        job.setPath(path);
        job.setUrl(url);

        assertEquals(id, job.getId());
        assertEquals(query, job.getQuery());
        assertEquals(completionDate, job.getCompletionDate());
        assertEquals(creationDate, job.getCreationDate());
        assertEquals(contentType, job.getContentType());
        assertEquals(errorLog, job.getErrorLog());
        assertEquals(path, job.getPath());
        assertEquals(url, job.getUrl());

        job = new ExportJob();
        job.setId(id);
        job.setQuery(query);
        job.setCompletionDate(completionDate);
        job.setCreationDate(creationDate);
        job.setContentType(contentType);
        job.setErrorLog(errorLog);
        job.setPath(path);
        job.setUrl(url);

        assertEquals(id, job.getId());
        assertEquals(query, job.getQuery());
        assertEquals(completionDate, job.getCompletionDate());
        assertEquals(creationDate, job.getCreationDate());
        assertEquals(contentType, job.getContentType());
        assertEquals(errorLog, job.getErrorLog());
        assertEquals(path, job.getPath());
        assertEquals(url, job.getUrl());
    }

    @Test
    void testEquals() {
        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";
        OffsetDateTime completionDate = OffsetDateTime.now();
        OffsetDateTime creationDate = OffsetDateTime.now();
        String contentType = "testContentType";
        String errorLog = "testErrorLog";
        String path = "testPath";
        String url = "testUrl";

        ExportJob job1 = new ExportJob();
        job1.setId(id);
        job1.setQuery(query);
        job1.setCompletionDate(completionDate);
        job1.setCreationDate(creationDate);
        job1.setContentType(contentType);
        job1.setErrorLog(errorLog);
        job1.setPath(path);
        job1.setUrl(url);

        ExportJob job2 = new ExportJob();
        job2.setId(id);
        job2.setQuery(query);
        job2.setCompletionDate(completionDate);
        job2.setCreationDate(creationDate);
        job2.setContentType(contentType);
        job2.setErrorLog(errorLog);
        job2.setPath(path);
        job2.setUrl(url);

        assertTrue(job1.equals(job2));

        job1.id("differentId");

        assertFalse(job1.equals(job2));
    }

    @Test
    void testHashCode() {
        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";
        OffsetDateTime completionDate = OffsetDateTime.now();
        OffsetDateTime creationDate = OffsetDateTime.now();
        String contentType = "testContentType";
        String errorLog = "testErrorLog";
        String path = "testPath";
        String url = "testUrl";

        ExportJob job1 = new ExportJob();
        job1.setId(id);
        job1.setQuery(query);
        job1.setCompletionDate(completionDate);
        job1.setCreationDate(creationDate);
        job1.setContentType(contentType);
        job1.setErrorLog(errorLog);
        job1.setPath(path);
        job1.setUrl(url);

        ExportJob job2 = new ExportJob();
        job2.setId(id);
        job2.setQuery(query);
        job2.setCompletionDate(completionDate);
        job2.setCreationDate(creationDate);
        job2.setContentType(contentType);
        job2.setErrorLog(errorLog);
        job2.setPath(path);
        job2.setUrl(url);

        assertEquals(job1.hashCode(), job2.hashCode());

        job1.id("differentId");

        assertNotEquals(job1.hashCode(), job2.hashCode());
    }

    @Test
    void testToString() {
        ExportJob job = new ExportJob();

        String id = "testId";
        String callback = "testCallback";
        String query = "testQuery";
        OffsetDateTime completionDate = OffsetDateTime.parse("2024-01-19T16:35:07.730526064+02:00");
        OffsetDateTime creationDate = OffsetDateTime.parse("2024-01-19T16:35:07.731493435+02:00");
        String contentType = "testContentType";
        String errorLog = "testErrorLog";
        String path = "testPath";
        String url = "testUrl";

        job.id(id);
        job.query(query);
        job.setCompletionDate(completionDate);
        job.setCreationDate(creationDate);
        job.setContentType(contentType);
        job.setErrorLog(errorLog);
        job.setPath(path);
        job.setUrl(url);

        String expectedString = "class ExportJob {\n" +
                "    id: testId\n" +
                "    href: null\n" +
                "    completionDate: 2024-01-19T16:35:07.730526064+02:00\n" +
                "    contentType: testContentType\n" +
                "    creationDate: 2024-01-19T16:35:07.731493435+02:00\n" +
                "    errorLog: testErrorLog\n" +
                "    path: testPath\n" +
                "    query: testQuery\n" +
                "    url: testUrl\n" +
                "    status: null\n" +
                "    _atBaseType: null\n" +
                "    _atSchemaLocation: null\n" +
                "    _atType: null\n" +
                "}";

        assertEquals(expectedString, job.toString());
    }

    @Test
    void testHref() {
        ExportJob job = new ExportJob();
        String href = "testHref";
        job.setHref(href);
        assertEquals(href, job.getHref());

        ExportJob newJob = job.href("newTestHref");
        assertEquals(job, newJob);
    }

    @Test
    void testCompletionDate() {
        ExportJob job = new ExportJob();
        OffsetDateTime completionDate = OffsetDateTime.now();
        job.setCompletionDate(completionDate);
        assertEquals(completionDate, job.getCompletionDate());

        ExportJob newJob = job.completionDate(OffsetDateTime.now());
        assertEquals(job, newJob);
    }

    @Test
    void testContentType() {
        ExportJob job = new ExportJob();
        String contentType = "testContentType";
        job.setContentType(contentType);
        assertEquals(contentType, job.getContentType());

        ExportJob newJob = job.contentType("newTestContentType");
        assertEquals(job, newJob);
    }

    @Test
    void testCreationDate() {
        ExportJob job = new ExportJob();
        OffsetDateTime creationDate = OffsetDateTime.now();
        job.setCreationDate(creationDate);
        assertEquals(creationDate, job.getCreationDate());
    }

    @Test
    void testStatus() {
        ExportJob job = new ExportJob();
        JobStateType status = JobStateType.RUNNING;
        job.setStatus(status);
        assertEquals(status, job.getStatus());

        ExportJob newJob = job.status(JobStateType.FAILED);
        assertEquals(job, newJob);
    }

    @Test
    void testAtBaseType() {
        ExportJob job = new ExportJob();
        String baseType = "testBaseType";
        job.setAtBaseType(baseType);
        assertEquals(baseType, job.getAtBaseType());

        ExportJob newJob = job._atBaseType("newTestBaseType");
        assertEquals(job, newJob);
    }

    @Test
    void testAtSchemaLocation() {
        ExportJob job = new ExportJob();
        String schemaLocation = "testSchemaLocation";
        job.setAtSchemaLocation(schemaLocation);
        assertEquals(schemaLocation, job.getAtSchemaLocation());

        ExportJob newJob = job._atSchemaLocation("newTestSchemaLocation");
        assertEquals(job, newJob);
    }

    @Test
    void testAtType() {
        ExportJob job = new ExportJob();
        String type = "testType";
        job.setAtType(type);
        assertEquals(type, job.getAtType());

        ExportJob newJob = job._atType("newTestType");
        assertEquals(job, newJob);
    }

}