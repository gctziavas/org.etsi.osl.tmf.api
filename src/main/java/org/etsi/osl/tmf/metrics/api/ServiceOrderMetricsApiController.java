package org.etsi.osl.tmf.metrics.api;

import org.etsi.osl.tmf.metrics.reposervices.ServiceOrderMetricsRepoService;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ServiceOrderMetricsApiController implements ServiceOrderMetricsApi{

    private static final Logger log = LoggerFactory.getLogger(ServiceOrderMetricsApiController.class);
    private final ServiceOrderMetricsRepoService serviceOrderMetricsRepoService;

    @Autowired
    public ServiceOrderMetricsApiController(ServiceOrderMetricsRepoService serviceOrderMetricsRepoService) {
        this.serviceOrderMetricsRepoService = serviceOrderMetricsRepoService;
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getTotalServiceOrders(ServiceOrderStateType state) {
        try {
            int totalServiceOrders = serviceOrderMetricsRepoService.countTotalServiceOrders(state);
            Map<String, Integer> response = new HashMap<>();
            response.put("totalServiceOrders", totalServiceOrders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total service orders. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getTotalActiveServiceOrders() {
        try {
            int totalActiveServiceOrders = serviceOrderMetricsRepoService.countTotalActiveServiceOrders();
            Map<String, Integer> response = new HashMap<>();
            response.put("activeServiceOrders", totalActiveServiceOrders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total active service orders. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getServiceOrdersGroupedByDay(OffsetDateTime starttime, OffsetDateTime endtime) {
        try {
            Map<String, Long> orderDatesGroupedByDate = serviceOrderMetricsRepoService.getServiceOrdersGroupedByDay(starttime, endtime);

            // Fill missing days with count 0
            Map<String, Long> fullDayMap = new LinkedHashMap<>();
            OffsetDateTime cursor = starttime.truncatedTo(ChronoUnit.DAYS);
            OffsetDateTime endDay = endtime.truncatedTo(ChronoUnit.DAYS);
            while (!cursor.isAfter(endDay)) {
                String key = cursor.toInstant().toString();
                fullDayMap.put(key, orderDatesGroupedByDate.getOrDefault(key, 0L));
                cursor = cursor.plusDays(1);
            }

            List<Map<String, Object>> groupByDayList = fullDayMap.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("key", entry.getKey());
                        dayMap.put("count", entry.getValue());
                        return dayMap;
                    })
                    .toList();

            Map<String, Object> aggregations = Map.of("groupByDay", groupByDayList);
            Map<String, Object> serviceOrders = Map.of(
                    "total", fullDayMap.values().stream().mapToLong(Long::longValue).sum(),
                    "aggregations", aggregations
            );

            Map<String, Object> response = Map.of("serviceOrders", serviceOrders);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve services grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getServiceOrdersGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
        try {
            Map<String, Integer> servicesByState = serviceOrderMetricsRepoService.getServiceOrdersGroupedByState(starttime, endtime);

            // Initialize with all possible states and 0. Ensures that all states are represented, even if not present in the data.
            Map<String, Integer> fullStateMap = new LinkedHashMap<>();
            for (ServiceOrderStateType state : ServiceOrderStateType.values()) {
                fullStateMap.put(state.name(), 0);
            }

            // Overwrite counts with actual data
            servicesByState.forEach((key, value) -> {
                fullStateMap.put(key.toUpperCase(), value);
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

            Map<String, Object> response = Map.of("serviceOrders", services);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve services grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
