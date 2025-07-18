package org.etsi.osl.tmf.metrics.api;

import org.etsi.osl.tmf.metrics.*;
import org.etsi.osl.tmf.metrics.reposervices.ResourceMetricsRepoService;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResourceMetricsApiController implements ResourceMetricsApi {

    private static final Logger log = LoggerFactory.getLogger(ResourceMetricsApiController.class);
    private final ResourceMetricsRepoService resourceMetricsRepoService;

    @Autowired
    public ResourceMetricsApiController(ResourceMetricsRepoService resourceMetricsRepoService) {
        this.resourceMetricsRepoService = resourceMetricsRepoService;
    }

    @Override
    public ResponseEntity<TotalResources> getTotalResources(ResourceStatusType state) {
        try {
            int totalResources = resourceMetricsRepoService.countTotalResources(state);
            TotalResources response = new TotalResources(totalResources);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total resources. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ResourcesGroupByState> getResourcesGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
        try {
            Map<String, Integer> resourcesByState = resourceMetricsRepoService.getResourcesGroupedByState(starttime, endtime);

            // Initialize with all possible states and 0. Ensures that all states are represented, even if not present in the data.
            Map<String, Integer> fullStateMap = new LinkedHashMap<>();
            for (ResourceStatusType state : ResourceStatusType.values()) {
                fullStateMap.put(state.name(), 0); // default to 0
            }

            // Overwrite counts with actual data
            resourcesByState.forEach((key, value) -> {
                fullStateMap.put(key.toUpperCase(), value); // normalize case just in case
            });

            // Create aggregation items
            List<ResourcesGroupByStateItem> groupByStateList = fullStateMap.entrySet().stream()
                    .map(entry -> new ResourcesGroupByStateItem(ResourceStatusType.valueOf(entry.getKey()), entry.getValue()))
                    .toList();

            // Build response structure using models
            ResourcesGroupByStateAggregations aggregations = new ResourcesGroupByStateAggregations(groupByStateList);
            int total = fullStateMap.values().stream().mapToInt(Integer::intValue).sum();
            Resources services = new Resources(total, aggregations);
            ResourcesGroupByState response = new ResourcesGroupByState(services);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve resources grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
