package org.etsi.osl.tmf.metrics.api;

import org.etsi.osl.tmf.metrics.*;
import org.etsi.osl.tmf.metrics.reposervices.MetricsRepoService;
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
public class MetricsApiController implements MetricsApi {

    private static final Logger log = LoggerFactory.getLogger(MetricsApiController.class);
    private final MetricsRepoService serviceOrderMetricsRepoService;

    @Autowired
    public MetricsApiController(MetricsRepoService serviceOrderMetricsRepoService) {
        this.serviceOrderMetricsRepoService = serviceOrderMetricsRepoService;
    }

    @Override
    public ResponseEntity<TotalServiceOrders> getTotalServiceOrders(ServiceOrderStateType state) {
        try {
            int totalServiceOrders = serviceOrderMetricsRepoService.countTotalServiceOrders(state);
            TotalServiceOrders response = new TotalServiceOrders(totalServiceOrders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total service orders. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ActiveServiceOrders> getTotalActiveServiceOrders() {
        try {
            int totalActiveServiceOrders = serviceOrderMetricsRepoService.countTotalActiveServiceOrders();
            ActiveServiceOrders response = new ActiveServiceOrders(totalActiveServiceOrders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total active service orders. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ServiceOrdersGroupByDay> getServiceOrdersGroupedByDay(OffsetDateTime starttime, OffsetDateTime endtime) {
        try {
            Map<String, Integer> orderDatesGroupedByDate = serviceOrderMetricsRepoService.getServiceOrdersGroupedByDay(starttime, endtime);

            // Fill missing days with count 0
            Map<String, Integer> fullDayMap = new LinkedHashMap<>();
            OffsetDateTime cursor = starttime.truncatedTo(ChronoUnit.DAYS);
            OffsetDateTime endDay = endtime.truncatedTo(ChronoUnit.DAYS);
            while (!cursor.isAfter(endDay)) {
                String key = cursor.toInstant().toString();
                fullDayMap.put(key, orderDatesGroupedByDate.getOrDefault(key, 0));
                cursor = cursor.plusDays(1);
            }

            // Convert to model list
            List<GroupByItem> groupByDayList = fullDayMap.entrySet().stream()
                    .map(entry -> new GroupByItem(entry.getKey(), entry.getValue()))
                    .toList();

            GroupByDayAggregations aggregations = new GroupByDayAggregations(groupByDayList);
            int total = fullDayMap.values().stream().mapToInt(Integer::intValue).sum();
            ServiceOrdersDay wrapper = new ServiceOrdersDay(total, aggregations);
            ServiceOrdersGroupByDay response = new ServiceOrdersGroupByDay(wrapper);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve services grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ServiceOrdersGroupByState> getServiceOrdersGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
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

            // Create aggregation items
            List<GroupByItem> groupByStateList = fullStateMap.entrySet().stream()
                    .map(entry -> new GroupByItem(entry.getKey(), entry.getValue()))
                    .toList();

            // Build response structure using models
            GroupByStateAggregations aggregations = new GroupByStateAggregations(groupByStateList);
            int total = fullStateMap.values().stream().mapToInt(Integer::intValue).sum();
            ServiceOrders services = new ServiceOrders(total, aggregations);
            ServiceOrdersGroupByState response = new ServiceOrdersGroupByState(services);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Couldn't retrieve services grouped by state. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
