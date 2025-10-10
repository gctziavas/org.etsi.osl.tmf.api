package org.etsi.osl.services.api.rcm634;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import org.etsi.osl.tmf.rcm634.model.ExportJob;
import org.etsi.osl.tmf.rcm634.model.ExportJobStateChangeEvent;
import org.etsi.osl.tmf.rcm634.model.ExportJobStateChangeEventPayload;
import org.junit.jupiter.api.Test;

public class ExportJobStateChangeEventTest {

    @Test
    void testExportJobStateChangeEvent() {
        ExportJobStateChangeEvent event = new ExportJobStateChangeEvent();
        String id = "testId";
        String href = "testHref";
        String eventId = "testEventId";
        OffsetDateTime eventTime = OffsetDateTime.now();
        String eventType = "testEventType";
        String correlationId = "testCorrelationId";
        String domain = "testDomain";
        String title = "testTitle";
        String description = "testDescription";
        String priority = "testPriority";
        OffsetDateTime timeOccurred = OffsetDateTime.now();
        ExportJobStateChangeEventPayload eventPayload = new ExportJobStateChangeEventPayload();

        event.id(id);
        event.href(href);
        event.eventId(eventId);
        event.eventTime(eventTime);
        event.eventType(eventType);
        event.correlationId(correlationId);
        event.domain(domain);
        event.title(title);
        event.description(description);
        event.priority(priority);
        event.timeOcurred(timeOccurred);
        event.event(eventPayload);

        assertEquals(id, event.getId());
        assertEquals(href, event.getHref());
        assertEquals(eventId, event.getEventId());
        assertEquals(eventTime, event.getEventTime());
        assertEquals(eventType, event.getEventType());
        assertEquals(correlationId, event.getCorrelationId());
        assertEquals(domain, event.getDomain());
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(priority, event.getPriority());
        assertEquals(timeOccurred, event.getTimeOcurred());
        assertEquals(eventPayload, event.getEvent());
    }

    @Test
    void testEquals() {
        ExportJobStateChangeEvent event1 = new ExportJobStateChangeEvent();
        event1.setId("testId");
        event1.setHref("testHref");
        event1.setEventId("testEventId");
        event1.setEventTime(OffsetDateTime.now());
        event1.setEventType("testEventType");
        event1.setCorrelationId("testCorrelationId");
        event1.setDomain("testDomain");
        event1.setTitle("testTitle");
        event1.setDescription("testDescription");
        event1.setPriority("testPriority");
        event1.setTimeOcurred(OffsetDateTime.now());
        event1.setEvent(new ExportJobStateChangeEventPayload());

        ExportJobStateChangeEvent event2 = new ExportJobStateChangeEvent();
        event2.setId("testId");
        event2.setHref("testHref");
        event2.setEventId("testEventId");
        event2.setEventTime(event1.getEventTime());
        event2.setEventType("testEventType");
        event2.setCorrelationId("testCorrelationId");
        event2.setDomain("testDomain");
        event2.setTitle("testTitle");
        event2.setDescription("testDescription");
        event2.setPriority("testPriority");
        event2.setTimeOcurred(event1.getTimeOcurred());
        event2.setEvent(new ExportJobStateChangeEventPayload());

        assertTrue(event1.equals(event2));

        event1.setId("differentId");

        assertFalse(event1.equals(event2));
    }

    @Test
    void testHashCode() {
        ExportJobStateChangeEvent event1 = new ExportJobStateChangeEvent();
        event1.setId("testId");
        event1.setHref("testHref");
        event1.setEventId("testEventId");
        event1.setEventTime(OffsetDateTime.now());
        event1.setEventType("testEventType");
        event1.setCorrelationId("testCorrelationId");
        event1.setDomain("testDomain");
        event1.setTitle("testTitle");
        event1.setDescription("testDescription");
        event1.setPriority("testPriority");
        event1.setTimeOcurred(OffsetDateTime.now());
        event1.setEvent(new ExportJobStateChangeEventPayload());

        ExportJobStateChangeEvent event2 = new ExportJobStateChangeEvent();
        event2.setId("testId");
        event2.setHref("testHref");
        event2.setEventId("testEventId");
        event2.setEventTime(event1.getEventTime());
        event2.setEventType("testEventType");
        event2.setCorrelationId("testCorrelationId");
        event2.setDomain("testDomain");
        event2.setTitle("testTitle");
        event2.setDescription("testDescription");
        event2.setPriority("testPriority");
        event2.setTimeOcurred(event1.getTimeOcurred());
        event2.setEvent(new ExportJobStateChangeEventPayload());

        assertEquals(event1.hashCode(), event2.hashCode());

        event1.setId("differentId");

        assertNotEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void testToString() {
        ExportJobStateChangeEvent event = new ExportJobStateChangeEvent();
        event.setId("testId");
        event.setHref("testHref");
        event.setEventId("testEventId");
        event.setEventTime(OffsetDateTime.parse("2024-01-19T17:02:03.289504717+02:00"));
        event.setEventType("testEventType");
        event.setCorrelationId("testCorrelationId");
        event.setDomain("testDomain");
        event.setTitle("testTitle");
        event.setDescription("testDescription");
        event.setPriority("testPriority");
        event.setTimeOcurred(OffsetDateTime.parse("2024-01-19T17:02:03.289527665+02:00"));
        event.setEvent(new ExportJobStateChangeEventPayload());

        String expectedString = "class ExportJobStateChangeEvent {\n" +
                "    id: testId\n" +
                "    href: testHref\n" +
                "    eventId: testEventId\n" +
                "    eventTime: 2024-01-19T17:02:03.289504717+02:00\n" +
                "    eventType: testEventType\n" +
                "    correlationId: testCorrelationId\n" +
                "    domain: testDomain\n" +
                "    title: testTitle\n" +
                "    description: testDescription\n" +
                "    priority: testPriority\n" +
                "    timeOcurred: 2024-01-19T17:02:03.289527665+02:00\n" +
                "    event: class ExportJobStateChangeEventPayload {\n" +
                "        exportJob: null\n" +
                "    }\n" +
                "}";

        assertEquals(expectedString, event.toString());
    }

    @Test
    void testToIndentedString() throws Exception {
        ExportJobStateChangeEvent event = new ExportJobStateChangeEvent();

        Method method = ExportJobStateChangeEvent.class.getDeclaredMethod("toIndentedString", Object.class);
        method.setAccessible(true);

        String input = "Hello\nWorld";
        String expectedOutput = "Hello\n    World";

        String output = (String) method.invoke(event, input);

        assertEquals(expectedOutput, output);
    }

    @Test
    void testExportJob() {
        ExportJobStateChangeEventPayload payload = new ExportJobStateChangeEventPayload();
        ExportJob job = new ExportJob();
        job.setId("testId");
        payload.setExportJob(job);
        assertEquals(job, payload.getExportJob());

        ExportJob newJob = new ExportJob();
        newJob.setId("newTestId");
        ExportJobStateChangeEventPayload newPayload = payload.exportJob(newJob);
        assertEquals(payload, newPayload);
        assertEquals(newJob, payload.getExportJob());
    }
}