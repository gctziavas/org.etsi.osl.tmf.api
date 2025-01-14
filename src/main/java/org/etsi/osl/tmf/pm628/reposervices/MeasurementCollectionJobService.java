package org.etsi.osl.tmf.pm628.reposervices;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.query.Query;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.pm628.api.MeasurementCollectionJobApiRouteBuilderEvents;
import org.etsi.osl.tmf.pm628.model.*;
import org.etsi.osl.tmf.pm628.repo.MeasurementCollectionJobRepository;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

@Service
@Transactional
public class MeasurementCollectionJobService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementCollectionJobService.class);

    @Autowired
    private final MeasurementCollectionJobRepository measurementCollectionJobRepository;

    @Autowired
    MeasurementCollectionJobApiRouteBuilderEvents routeBuilderEvents;

    private SessionFactory sessionFactory;

    @Autowired
    public MeasurementCollectionJobService(MeasurementCollectionJobRepository measurementCollectionJobRepository, EntityManagerFactory factory) {
        this.measurementCollectionJobRepository = measurementCollectionJobRepository;

        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }
    
    public List<MeasurementCollectionJob> findAllMeasurementCollectionJobs(){
        log.debug("MeasurementCollectionJobService: LIST");
        return (List<MeasurementCollectionJob>) measurementCollectionJobRepository.findAll();
    }

    public List<MeasurementCollectionJob> findAllByExecutionState(ExecutionStateType executionState) {
        log.debug("find by state:" + executionState );
        return (List<MeasurementCollectionJob>) this.measurementCollectionJobRepository.findByExecutionState(executionState);
    }

    @Transactional
    public List findAll(@Valid String fields, Map<String, String> allParams) throws UnsupportedEncodingException {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append("mcj.uuid as uuid, ")
                .append("mcj.creationTime as creationTime, ")
                .append("mcj.reportingPeriod as reportingPeriod, ")
                .append("mcj.granularity as granularity, ")
                .append("mcj.consumingApplicationId as consumingApplicationId, ")
                .append("mcj.producingApplicationId as producingApplicationId, ")
                .append("mcj.executionState as executionState, ")
                .append("mcj.jobId as jobId, ")
                // DataAccessEndpoint attributes
                .append("dae.uuid as dataAccessEndpoint_uuid, ")
                .append("dae.uri as dataAccessEndpoint_uri, ")
                .append("dae.apiType as dataAccessEndpoint_apiType, ")
                .append("dae.resourceStatus as dataAccessEndpoint_resourceStatus, ")
                .append("dae.operationalState as dataAccessEndpoint_operationalState, ")
                    // DataAccessEndpoint uriQueryFilter mappings attributes
                    .append("map.filterTemplate.name as dae_uriqf_mapping_name, ")
                    .append("map.filterTemplate.description as dae_uriqf_mapping_description, ")
                    .append("map.stringArray.value as dae_uriqf_mapping_value, ")
                    .append("map.filterTemplate.id as dae_uriqf_mapping_id, ")
                    // DataAccessEndpoint resourceOrderItem attributes
                    .append("roi.resourceOrderHref as dae_resOrdItem_resourceOrderHref, ")
                    .append("roi.resourceOrderId as dae_resOrdItem_resourceOrderId, ")
                    .append("roi.itemAction as dae_resOrdItem_itemAction, ")
                    .append("roi.itemId as dae_resOrdItem_itemId, ")
                    .append("roi.role as dae_resOrdItem_role, ")
                // ScheduleDefinition attributes
                .append("sd.scheduleDefinitionStartTime as scheduleDefinition_startTime, ")
                .append("sd.scheduleDefinitionEndTime as scheduleDefinition_endTime, ")
                .append("sd.recurringFrequency as scheduleDefinition_recurringFrequency, ")
                .append("sd.excludedDate as scheduleDefinition_excludedDate, ")
                .append("sd.scheduleDefinitionHourRange as scheduleDefinition_hourRange, ")
                .append("sd.monthlyScheduleDayOfMonthDefinition as scheduleDefinition_monthlyScheduleDayOfMonthDefinition, ")
                .append("sd.dateScheduleDefintion as scheduleDefinition_dateScheduleDefintion, ")
                .append("wsd.dates as scheduleDefinition_weeklyScheduledDefinition_Dates, ")
                .append("sd.monthlyScheduleDayOfWeekDefinition.recurringDaySequence as scheduleDefinition_monthlyScheduleDayOfWeekDefinition_recurringDaySequence, ")
                .append("domr.dates as scheduleDefinition_monthlyScheduleDayOfWeekDefinition_dayOfMonthRecurrence_dates ");

            if (fields != null && !fields.isEmpty()) {
                String[] fieldArray = fields.split(",");
                for (String field : fieldArray) {
                    sql.append(", mcj.").append(field.trim()).append(" as ").append(field.trim());
                }
            }

            sql.append(" FROM PM628_MCJob mcj ")
                    .append("LEFT JOIN mcj.dataAccessEndpoint dae ")
                    .append("LEFT JOIN dae.uriQueryFilter.mappings map ")
                    .append("LEFT JOIN dae.resourceOrderItem roi ")
                    .append("LEFT JOIN mcj.scheduleDefinition sd ")
                    .append("LEFT JOIN sd.weeklyScheduledDefinition wsd ")
                    .append("LEFT JOIN sd.monthlyScheduleDayOfWeekDefinition.dayOfMonthRecurrence domr ");

            if (!allParams.isEmpty()) {
                sql.append(" WHERE ");
                for (String paramName : allParams.keySet()) {
                    sql.append("mcj.").append(paramName).append(" LIKE :").append(paramName).append(" AND ");
                }
                sql.setLength(sql.length() - 5);  // Remove the last " AND "
            }

            sql.append(" ORDER BY mcj.creationTime DESC");

            Query query = session.createQuery(sql.toString());
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                query.setParameter(entry.getKey(), "%" + URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8.toString()) + "%");
            }

            List<Object[]> queryResult = query.list();
            Map<String, Map<String, Object>> mcJobMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> daeMap = new LinkedHashMap<>();
            Map<String, Map<String, Object>> sdMap = new LinkedHashMap<>();

            for (Object[] row : queryResult) {
                String mcjUuid = (String) row[0];

                // If this mcjUuid hasn't been seen, create a new entry for it
                mcJobMap.computeIfAbsent(mcjUuid, k -> {
                    Map<String, Object> mcJobData = new LinkedHashMap<>();
                    mcJobData.put("uuid", row[0]);
                    mcJobData.put("creationTime", row[1]);
                    mcJobData.put("reportingPeriod", row[2]);
                    mcJobData.put("granularity", row[3]);
                    mcJobData.put("consumingApplicationId", row[4]);
                    mcJobData.put("producingApplicationId", row[5]);
                    mcJobData.put("executionState", row[6]);
                    mcJobData.put("jobId", row[7]);
                    mcJobData.put("dataAccessEndpoint", new ArrayList<Map<String, Object>>());
                    mcJobData.put("scheduleDefinition", new ArrayList<Map<String, Object>>());

                    if (fields != null && !fields.isEmpty()) {
                        String[] fieldArray = fields.split(",");
                        for (int i = 0; i < fieldArray.length; i++) {
                            mcJobData.put(fieldArray[i].trim(), row[9 + i]);
                        }
                    }
                    return mcJobData;
                });

                String daeUuid = (String) row[8];

                // Check if DataAccessEndpoint fields are null; if so, skip adding it
                if (daeUuid != null) {
                    Map<String, Object> daeData = daeMap.computeIfAbsent(daeUuid, k -> {
                        Map<String, Object> newDaeData = new LinkedHashMap<>();
                        newDaeData.put("uuid", row[8]);
                        newDaeData.put("uri", row[9]);
                        newDaeData.put("apiType", row[10]);
                        newDaeData.put("resourceStatus", row[11]);
                        newDaeData.put("operationalState", row[12]);
                        newDaeData.put("uriQueryFilter", new LinkedHashMap<String, Object>());
                        newDaeData.put("resourceOrderItem", new ArrayList<Map<String, Object>>());
                        ((List<Map<String, Object>>) mcJobMap.get(mcjUuid).get("dataAccessEndpoint")).add(newDaeData);
                        return newDaeData;
                    });

                    // Create uriQueryFilter map and add mappings
                    Map<String, Object> uriQueryFilter = (Map<String, Object>) daeData.get("uriQueryFilter");
                    Set<Map<String, Object>> mappingsSet = (Set<Map<String, Object>>) uriQueryFilter.computeIfAbsent("mappings", k -> new LinkedHashSet<>());

                    // Check if mapping fields are null; if so, skip adding it
                    if (row[13] != null || row[14] != null || row[15] != null || row[16] != null) {
                        Map<String, Object> mappingData = new LinkedHashMap<>();
                        mappingData.put("name", row[13]);
                        mappingData.put("description", row[14]);
                        mappingData.put("value", row[15]);
                        mappingData.put("id", row[16]);
                        mappingsSet.add(mappingData);
                    }

                    // Retrieve resourceOrderItem and add to DAE
                    List<Map<String, Object>> roiList = (List<Map<String, Object>>) daeData.get("resourceOrderItem");

                    // Check if resource order item fields are null; if so, skip adding it
                    if (row[17] != null || row[18] != null || row[19] != null || row[20] != null || row[21] != null) {
                        Map<String, Object> roiData = new LinkedHashMap<>();
                        roiData.put("resourceOrderHref", row[17]);
                        roiData.put("resourceOrderId", row[18]);
                        roiData.put("itemAction", row[19]);
                        roiData.put("itemId", row[20]);
                        roiData.put("role", row[21]);
                        roiList.add(roiData);
                    }
                }

                // Check if ScheduleDefinition fields are null; if so, skip adding it
                if (row[22] != null || row[23] != null || row[24] != null || row[25] != null || row[26] != null || row[27] != null || row[28] != null || row[29] != null | row[30] != null) {
                    String sdUuid = mcjUuid + "_" + row[22] + "_" + row[23]; // Unique key for schedule definition

                    Map<String, Object> sdData = sdMap.computeIfAbsent(sdUuid, k -> {
                        Map<String, Object> newSdData = new LinkedHashMap<>();
                        newSdData.put("scheduleDefinitionStartTime", row[22]);
                        newSdData.put("scheduleDefinitionEndTime", row[23]);
                        newSdData.put("recurringFrequency", row[24]);
                        newSdData.put("excludedDate", new LinkedHashSet<>());
                        newSdData.put("scheduleDefinitionHourRange", row[26]);
                        newSdData.put("monthlyScheduleDayOfMonthDefinition", new LinkedHashSet<>());
                        newSdData.put("dateScheduleDefintion", new LinkedHashSet<>());
                        newSdData.put("weeklyScheduledDefinition", new LinkedHashMap<>());
                        newSdData.put("monthlyScheduleDayOfWeekDefinition", new LinkedHashMap<>());
                        ((List<Map<String, Object>>) mcJobMap.get(mcjUuid).get("scheduleDefinition")).add(newSdData);
                        return newSdData;
                    });

                    // Add excludedDate
                    if (row[25] != null) {
                        ((Set<Object>) sdData.get("excludedDate")).add(row[25]);
                    }

                    // Add monthlyScheduleDayOfMonthDefinition
                    if (row[27] != null) {
                        ((Set<Object>) sdData.get("monthlyScheduleDayOfMonthDefinition")).add(row[27]);
                    }

                    // Add dateScheduleDefintion
                    if (row[28] != null) {
                        ((Set<Object>) sdData.get("dateScheduleDefintion")).add(row[28]);
                    }

                    // Add weeklyScheduledDefinition
                    Map<String, Object> weeklyScheduledDefinition = (Map<String, Object>) sdData.get("weeklyScheduledDefinition");
                    if (row[29] != null) {
                        Set<Map<String, Object>> dayOfWeekRecurrence = (Set<Map<String, Object>>) weeklyScheduledDefinition.computeIfAbsent("dayOfWeekRecurrence", k -> new LinkedHashSet<>());
                        Map<String, Object> wsdData = new LinkedHashMap<>();
                        wsdData.put("dates", row[29]);
                        dayOfWeekRecurrence.add(wsdData);
                    }

                    // Add monthlyScheduleDayOfWeekDefinition
                    Map<String, Object> monthlyScheduleDayOfWeekDefinition = (Map<String, Object>) sdData.get("monthlyScheduleDayOfWeekDefinition");
                    if (row[30] != null) {
                        monthlyScheduleDayOfWeekDefinition.put("recurringDaySequence", row[30]);
                    }
                    if (row[31] != null) {
                        Set<Map<String, Object>> dayOfMonthRecurrence = (Set<Map<String, Object>>) monthlyScheduleDayOfWeekDefinition.computeIfAbsent("dayOfMonthRecurrence", k -> new LinkedHashSet<>());
                        Map<String, Object> domrData = new LinkedHashMap<>();
                        domrData.put("dates", row[31]);
                        dayOfMonthRecurrence.add(domrData);
                    }
                }
            }
            resultList.addAll(mcJobMap.values());
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("Error executing query", e);
        } finally {
            tx.commit();
            session.close();
        }

        return resultList;
    }

    @Transactional  
    public String findMeasurementCollectionJobByUuidEagerAsString(String uuid) throws JsonProcessingException{

      MeasurementCollectionJob mcj = findMeasurementCollectionJobByUuidEager(uuid);
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new Hibernate5JakartaModule());
      String res = mapper.writeValueAsString(mcj);
      log.debug("=====> MCJObjectMapper {}", res);
      return res;

    }
    

    @Transactional
    private MeasurementCollectionJob findMeasurementCollectionJobByUuidEager(String id) {
      if (id == null || id.equals("")) {
        return null;
      }
      MeasurementCollectionJob s = null;
      try (Session session = sessionFactory.openSession()) {
        Transaction tx = session.beginTransaction();
        s = (MeasurementCollectionJob) session.get(MeasurementCollectionJob.class, id);
        if (s == null) {
          log.debug("=====> findMeasurementCollectionJobByUuidEager last resort");
          return this.findMeasurementCollectionJobByUuid(id);// last resort
        }

        Hibernate.initialize(s.getDataAccessEndpoint());
        Hibernate.initialize(s.getFileTransferData());
        Hibernate.initialize(s.getPerformanceIndicatorGroupSpecification());
        Hibernate.initialize(s.getPerformanceIndicatorSpecification());
        Hibernate.initialize(s.getScheduleDefinition());
        Hibernate.initialize(s.getTrackingRecord());

        tx.commit();
      } catch (Exception e) {
        e.printStackTrace();
      }

      return s;
    }

    @Transactional
    public MeasurementCollectionJob findMeasurementCollectionJobByUuid(String uuid){
        log.debug("MeasurementCollectionJob FIND BY UUID");
        Optional<MeasurementCollectionJob> measurementCollectionJob = measurementCollectionJobRepository.findByUuid(uuid);
        return measurementCollectionJob.orElse(null);
    }

    @Transactional
    public MeasurementCollectionJob createMeasurementCollectionJob(MeasurementCollectionJobFVO measurementCollectionJobFVO){
        log.debug("MeasurementCollectionJob CREATE: {}", measurementCollectionJobFVO);

        MeasurementCollectionJobMapper mapper = Mappers.getMapper(MeasurementCollectionJobMapper.class);
        MeasurementCollectionJob mcj = mapper.createMeasurementCollectionJob(measurementCollectionJobFVO);
        mcj.setCreationTime( OffsetDateTime.now() );
        mcj.setLastModifiedTime( OffsetDateTime.now());
        mcj = this.measurementCollectionJobRepository.saveAndFlush(mcj);
        raiseMCJCreateNotification(mcj);

        return mcj;
    }

    @Transactional
    public MeasurementCollectionJob updateMeasurementCollectionJob(String uuid, @Valid MeasurementCollectionJobMVO measurementCollectionJobUpdate){
        log.debug("MeasurementCollectionJob UPDATE with UUID: {}", uuid);

        MeasurementCollectionJob measurementCollectionJob = measurementCollectionJobRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No MeasurementCollectionJob with UUID: " + uuid));

        ExecutionStateType originalExecutionState = measurementCollectionJob.getExecutionState();
        boolean executionStateChanged = false;

        MeasurementCollectionJobMapper mapper = Mappers.getMapper(MeasurementCollectionJobMapper.class);
        measurementCollectionJob = mapper.updateMeasurementCollectionJob(measurementCollectionJobUpdate, measurementCollectionJob);

        measurementCollectionJob.setLastModifiedTime( OffsetDateTime.now());
        measurementCollectionJob = this.measurementCollectionJobRepository.save(measurementCollectionJob);

        
        // This may be unnecessary since MeasurementCollectionJobMVO doesn't have the executionState attribute
        if ( originalExecutionState!=null) {
          executionStateChanged = !originalExecutionState.equals(measurementCollectionJob.getExecutionState());          
        }

        if (executionStateChanged) {
            raiseMCJExecutionStateChangeNotification(measurementCollectionJob);
        } else {
            raiseMCJAttributeValueChangeNotification(measurementCollectionJob);
        }

        return measurementCollectionJob;
    }

    public Void deleteMeasurementCollectionJob(String uuid){
        log.debug("MeasurementCollectionJob DELETE with UUID:{}", uuid);
        MeasurementCollectionJob measurementCollectionJob = measurementCollectionJobRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("No MeasurementCollectionJob with UUID: " + uuid));
        measurementCollectionJobRepository.delete(measurementCollectionJob);
        raiseMCJDeleteNotification(measurementCollectionJob);

        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void raiseMCJCreateNotification(MeasurementCollectionJob mcj){
        // Create the event payload
        MeasurementCollectionJobRef ref = new MeasurementCollectionJobRef();
        ref.setId(mcj.getUuid());
        ref.setHref(mcj.getHref());
        ref.setName("MeasurementCollectionJob");
        

        MeasurementCollectionJobCreateEventPayload payload = new MeasurementCollectionJobCreateEventPayload();
        payload.setMeasurementCollectionJob(ref);

        // Create the event
        MeasurementCollectionJobCreateEvent event = new MeasurementCollectionJobCreateEvent();
        event.setTitle("MeasurementCollectionJob created");
        event.setDescription("MeasurementCollectionJob with UUID: " + mcj.getUuid() + " has been created");
        event.setEvent(payload);

        routeBuilderEvents.publishEvent(event, mcj.getUuid());
    }

    @Transactional
    private void raiseMCJAttributeValueChangeNotification(MeasurementCollectionJob mcj){

        // Create the event payload
        MeasurementCollectionJobAttributeValueChangeEventPayload payload = new MeasurementCollectionJobAttributeValueChangeEventPayload();
        payload.setMeasurementCollectionJob(mcj);

        // Create the event
        MeasurementCollectionJobAttributeValueChangeEvent event = new MeasurementCollectionJobAttributeValueChangeEvent();
        event.setTitle("MeasurementCollectionJob attribute value changed");
        event.setDescription("MeasurementCollectionJob with UUID: " + mcj.getUuid() + " has been updated");
        event.setEvent(payload);

        routeBuilderEvents.publishEvent(event, mcj.getUuid());
    }

    @Transactional
    private void raiseMCJExecutionStateChangeNotification(MeasurementCollectionJob mcj){

        // Create the event payload
        MeasurementCollectionJobExecutionStateChangeEventPayload payload = new MeasurementCollectionJobExecutionStateChangeEventPayload();
        payload.setMeasurementCollectionJob(mcj);

        // Create the event
        MeasurementCollectionJobExecutionStateChangeEvent event = new MeasurementCollectionJobExecutionStateChangeEvent();
        event.setTitle("MeasurementCollectionJob execution state changed");
        event.setDescription("MeasurementCollectionJob with UUID: " + mcj.getUuid() + " execution state has been updated");
        event.setEvent(payload);

        routeBuilderEvents.publishEvent(event, mcj.getUuid());
    }

    @Transactional
    private void raiseMCJDeleteNotification(MeasurementCollectionJob mcj){

        // Create the event payload
        MeasurementCollectionJobDeleteEventPayload payload = new MeasurementCollectionJobDeleteEventPayload();
        payload.setMeasurementCollectionJob(mcj);

        // Create the event
        MeasurementCollectionJobDeleteEvent event = new MeasurementCollectionJobDeleteEvent();
        event.setTitle("MeasurementCollectionJob deleted");
        event.setDescription("MeasurementCollectionJob with UUID: " + mcj.getUuid() + " execution state has been deleted");
        event.setEvent(payload);

        routeBuilderEvents.publishEvent(event, mcj.getUuid());
    }
}
