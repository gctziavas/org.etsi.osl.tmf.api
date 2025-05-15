package org.etsi.osl.tmf.metrics.reposervices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.etsi.osl.tmf.common.model.service.ServiceStateType;
import org.etsi.osl.tmf.sim638.repo.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceMetricsRepoService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServiceRepository serviceRepo;

    public int countTotalServices(ServiceStateType state) {
        if (state == null) {
            return serviceRepo.countAll();
        } else {
            return serviceRepo.countByState(state);
        }
    }

    public Map<String, Integer> getServicesGroupedByState(OffsetDateTime starttime, OffsetDateTime endtime) {
        if (starttime.plusDays(31).isBefore(endtime)) {
            starttime = endtime.minusDays(31);
        }

        List<Object[]> rawResults = serviceRepo.groupByStateBetweenDates(starttime, endtime);

        return rawResults.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
    }

}
