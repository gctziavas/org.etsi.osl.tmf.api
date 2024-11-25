package org.etsi.osl.tmf.pm628.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CLevel;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.pm628.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Configuration
//@RefreshScope
@Component
public class MeasurementCollectionJobApiRouteBuilderEvents extends RouteBuilder{

    private static final transient Log logger = LogFactory.getLog(MeasurementCollectionJobApiRouteBuilderEvents.class.getName());


    @Value("${EVENT_MEASUREMENT_COLLECTION_JOB_CREATE}")
    private String EVENT_MEASUREMENT_COLLECTION_JOB_CREATE="";

    @Value("${EVENT_MEASUREMENT_COLLECTION_JOB_DELETE}")
    private String EVENT_MEASUREMENT_COLLECTION_JOB_DELETE="";

    @Value("${EVENT_MEASUREMENT_COLLECTION_JOB_ATTRIBUTE_VALUE_CHANGED}")
    private String EVENT_MEASUREMENT_COLLECTION_JOB_ATTRIBUTE_VALUE_CHANGED="";

    @Value("${EVENT_MEASUREMENT_COLLECTION_JOB_EXECUTION_STATE_CHANGED}")
    private String EVENT_MEASUREMENT_COLLECTION_JOB_EXECUTION_STATE_CHANGED="";

    @Value("${spring.application.name}")
    private String compname;

    @Autowired
    private ProducerTemplate template;


    @Autowired
    private CentralLogger centralLogger;

    @Override
    public void configure() throws Exception {

    }

    @Transactional    public void publishEvent(final Event event, final String objId) {
        event.setEventType(event.getClass().getName());
        logger.info("will send Event for type " + event.getEventType());

        try{
            String msgtopic = "";

            if (event instanceof MeasurementCollectionJobCreateEvent) {
                msgtopic = EVENT_MEASUREMENT_COLLECTION_JOB_CREATE;
            } else if (event instanceof MeasurementCollectionJobDeleteEvent) {
                msgtopic = EVENT_MEASUREMENT_COLLECTION_JOB_DELETE;
            } else if (event instanceof MeasurementCollectionJobAttributeValueChangeEvent) {
                msgtopic = EVENT_MEASUREMENT_COLLECTION_JOB_ATTRIBUTE_VALUE_CHANGED;
            } else if (event instanceof MeasurementCollectionJobExecutionStateChangeEvent) {
                msgtopic = EVENT_MEASUREMENT_COLLECTION_JOB_EXECUTION_STATE_CHANGED;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("eventid", event.getEventId() );
            map.put("objId", objId ); //measurementcollectionjob id

            String apayload = toJsonString(event);
            template.sendBodyAndHeaders(msgtopic, apayload , map);

            centralLogger.log( CLevel.INFO, apayload, compname );
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Cannot send Event . " + e.getMessage()  );
        }
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
}


