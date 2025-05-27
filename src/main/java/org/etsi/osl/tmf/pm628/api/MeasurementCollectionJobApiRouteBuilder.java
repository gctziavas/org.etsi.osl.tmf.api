package org.etsi.osl.tmf.pm628.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobFVO;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobMVO;
import org.etsi.osl.tmf.pm628.reposervices.MeasurementCollectionJobService;
import org.etsi.osl.tmf.pm628.api.MeasurementCollectionJobApiRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
@Component
public class MeasurementCollectionJobApiRouteBuilder extends RouteBuilder {

    private static final transient Log logger = LogFactory.getLog(MeasurementCollectionJobApiRouteBuilder.class.getName());

    @Value("${PM_MEASUREMENT_COLLECTION_JOBS_GET}")
    private String PM_GET_MEASUREMENT_COLLECTION_JOBS;

    @Value("${PM_MEASUREMENT_COLLECTION_GET_JOB_BY_ID}")
    private String PM_MEASUREMENT_COLLECTION_GET_JOB_BY_ID;

    @Value("${PM_MEASUREMENT_COLLECTION_JOB_ADD}")
    private String PM_ADD_MEASUREMENT_COLLECTION_JOB;

    @Value("${PM_MEASUREMENT_COLLECTION_JOB_CREATED}")
    private String PM_CREATED_MEASUREMENT_COLLECTION_JOB;

    @Value("${PM_MEASUREMENT_COLLECTION_JOB_UPDATE}")
    private String PM_UPDATE_MEASUREMENT_COLLECTION_JOB;

    @Value("${PM_MEASUREMENT_COLLECTION_JOB_GET_INPROGRESS_OR_PENDING}")
    private String PM_MEASUREMENT_COLLECTION_JOB_GET_INPROGRESS_OR_PENDING;

    @Autowired
    private ProducerTemplate template;

    @Autowired
    MeasurementCollectionJobService measurementCollectionJobService;

    @Autowired
    private CentralLogger centralLogger;

    @Override
    public void configure() throws Exception {
        from(PM_GET_MEASUREMENT_COLLECTION_JOBS)
                .log(LoggingLevel.INFO, log, PM_GET_MEASUREMENT_COLLECTION_JOBS + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(measurementCollectionJobService, "findAllMeasurementCollectionJobs")
                .convertBodyTo(String.class);


        from(PM_MEASUREMENT_COLLECTION_GET_JOB_BY_ID)
                .log(LoggingLevel.INFO, log, PM_MEASUREMENT_COLLECTION_GET_JOB_BY_ID + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(measurementCollectionJobService, "findMeasurementCollectionJobByUuidEagerAsString")
                .convertBodyTo(String.class);

        from(PM_ADD_MEASUREMENT_COLLECTION_JOB)
                .log(LoggingLevel.INFO, log, PM_ADD_MEASUREMENT_COLLECTION_JOB + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .unmarshal()
                .json(JsonLibrary.Jackson, MeasurementCollectionJobFVO.class, true)
                .bean(measurementCollectionJobService, "createMeasurementCollectionJob(${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(PM_UPDATE_MEASUREMENT_COLLECTION_JOB)
                .log(LoggingLevel.INFO, log, PM_UPDATE_MEASUREMENT_COLLECTION_JOB + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true").unmarshal()
                .json(JsonLibrary.Jackson, MeasurementCollectionJobMVO.class, true)
                .bean(measurementCollectionJobService, "updateMeasurementCollectionJob(${header.mcjid}, ${body})")
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class);

        from(PM_MEASUREMENT_COLLECTION_JOB_GET_INPROGRESS_OR_PENDING)
                .log(LoggingLevel.INFO, log, PM_MEASUREMENT_COLLECTION_JOB_GET_INPROGRESS_OR_PENDING + " message received!")
                .to("log:DEBUG?showBody=true&showHeaders=true")
                .bean(measurementCollectionJobService, "findPendingOrInProgressMeasurementCollectionJobsAsJson")
                .process(exchange -> {
                    Object body = exchange.getIn().getBody();
                    if (!(body instanceof String)) {
                        throw new IllegalArgumentException("Unexpected body type: " + body.getClass());
                    }
                    // Body remains as a String
                    exchange.getIn().setBody(body);
                });
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
