package org.etsi.osl.tmf.metrics.reposervices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.etsi.osl.tmf.so641.repo.ServiceOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceOrderMetricsRepoService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServiceOrderRepository serviceOrderRepository;

    public int countTotalServiceOrders(ServiceOrderStateType state) {
        if (state == null) {
            return serviceOrderRepository.countAll();
        } else {
            return serviceOrderRepository.countByState(state);
        }
    }

    public int countTotalActiveServiceOrders() {
        OffsetDateTime currentDate = OffsetDateTime.now();
        List<ServiceOrderStateType> activeStates = List.of(
                ServiceOrderStateType.INPROGRESS,
                ServiceOrderStateType.COMPLETED
        );

        return serviceOrderRepository.countAllActive(currentDate, activeStates);
    }

    public Map<String, Integer> getServiceOrdersGroupedByDay(OffsetDateTime starttime, OffsetDateTime endtime) {
        if (starttime.plusDays(31).isBefore(endtime)) {
            starttime = endtime.minusDays(31);
        }

        List<OffsetDateTime> orderDates = serviceOrderRepository.getOrderDatesBetweenDates(starttime, endtime);

        // First group by day with count as Long
        Map<String, Long> grouped = orderDates.stream()
                .map(dt -> dt.truncatedTo(ChronoUnit.DAYS))  // Remove time portion
                .collect(Collectors.groupingBy(
                        dt -> dt.toInstant().toString(),      // Format as ISO string (Z)
                        Collectors.counting()
                ));

        // Convert Map<String, Long> to Map<String, Integer>
        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().intValue()
                ));
    }

    public Map<String, Integer> getServiceOrdersGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
        if (starttime.plusDays(31).isBefore(endtime)) {
            starttime = endtime.minusDays(31);
        }

        List<Object[]> rawResults = serviceOrderRepository.groupByStateBetweenDates(starttime, endtime);

        return rawResults.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
    }

}
