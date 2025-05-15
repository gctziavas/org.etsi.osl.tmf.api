package org.etsi.osl.tmf.metrics.reposervices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.etsi.osl.tmf.ri639.repo.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ResourceMetricsRepoService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ResourceRepository resourceRepository;

    public int countTotalResources(ResourceStatusType state) {
        if (state == null) {
            return resourceRepository.countAll();
        } else {
            return resourceRepository.countByResourceStatus(state);
        }
    }

    public Map<String, Integer> getResourcesGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
        if (starttime.plusDays(31).isBefore(endtime)) {
            starttime = endtime.minusDays(31);
        }

        List<Object[]> rawResults = resourceRepository.groupByStateBetweenDates(starttime, endtime);

        return rawResults.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
    }

}
