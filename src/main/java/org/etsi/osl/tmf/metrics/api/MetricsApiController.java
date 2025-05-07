package org.etsi.osl.tmf.metrics.api;

import org.etsi.osl.tmf.metrics.reposervices.MetricsRepoService;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MetricsApiController implements MetricsApi {

    private static final Logger log = LoggerFactory.getLogger(MetricsApiController.class);
    private final MetricsRepoService resourceMetricsRepoService;

    @Autowired
    public MetricsApiController(MetricsRepoService resourceMetricsRepoService) {
        this.resourceMetricsRepoService = resourceMetricsRepoService;
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getTotalResources(ResourceStatusType state) {
        try {
            int totalResources = resourceMetricsRepoService.countTotalResources(state);
            Map<String, Integer> response = new HashMap<>();
            response.put("totalResources", totalResources);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total resources. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getResourcesGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
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

            // Build groupByState list
            List<Map<String, Object>> groupByStateList = fullStateMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("key", entry.getKey());
                        map.put("count", entry.getValue());
                        return map;
                    })
                    .toList();


            // Wrap in response structure
            Map<String, Object> aggregations = Map.of("groupByState", groupByStateList);
            Map<String, Object> services = Map.of(
                    "total", fullStateMap.values().stream().mapToInt(Integer::intValue).sum(),
                    "aggregations", aggregations
            );

            Map<String, Object> response = Map.of("resources", services);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve resources grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
