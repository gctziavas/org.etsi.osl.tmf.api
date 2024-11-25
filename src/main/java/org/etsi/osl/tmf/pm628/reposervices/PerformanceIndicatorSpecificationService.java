package org.etsi.osl.tmf.pm628.reposervices;

import org.etsi.osl.tmf.pm628.model.*;
import org.etsi.osl.tmf.pm628.repo.PerformanceIndicatorSpecificationRepository;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PerformanceIndicatorSpecificationService {
    private static final Logger log = LoggerFactory.getLogger(MeasurementCollectionJobService.class);

    private final PerformanceIndicatorSpecificationRepository performanceIndicatorSpecificationRepository;

    @Autowired
    public PerformanceIndicatorSpecificationService(PerformanceIndicatorSpecificationRepository performanceIndicatorSpecificationRepository) {
        this.performanceIndicatorSpecificationRepository = performanceIndicatorSpecificationRepository;
    }

    public List<PerformanceIndicatorSpecification> findAllPerformanceIndicatorSpecifications(){
        log.debug("PerformanceIndicatorSpecificationService: LIST");
        return (List<PerformanceIndicatorSpecification>) performanceIndicatorSpecificationRepository.findAll();
    }

    public PerformanceIndicatorSpecification findPerformanceIndicatorSpecificationByUuid(String uuid){
        log.debug("PerformanceIndicatorSpecification FIND BY UUID");
        Optional<PerformanceIndicatorSpecification> performanceIndicatorSpecification = performanceIndicatorSpecificationRepository.findByUuid(uuid);
        return performanceIndicatorSpecification.orElse(null);
    }

    public PerformanceIndicatorSpecification createPerformanceIndicatorSpecification(PerformanceIndicatorSpecificationFVO performanceIndicatorSpecificationFVO){
        log.debug("PerformanceIndicatorSpecification CREATE: {}", performanceIndicatorSpecificationFVO);

        PerformanceIndicatorSpecificationMapper mapper = Mappers.getMapper(PerformanceIndicatorSpecificationMapper.class);
        PerformanceIndicatorSpecification pis = mapper.createPerformanceIndicatorSpecification(performanceIndicatorSpecificationFVO);

        return performanceIndicatorSpecificationRepository.save(pis);
    }

    public PerformanceIndicatorSpecification updatePerformanceIndicatorSpecification(String uuid, PerformanceIndicatorSpecificationMVO performanceIndicatorSpecificationMVO){
        log.debug("PerformanceIndicatorSpecification UPDATE with UUID: {}", uuid);
        PerformanceIndicatorSpecification performanceIndicatorSpecification = performanceIndicatorSpecificationRepository.findByUuid(uuid).
                orElseThrow(() -> new IllegalArgumentException("No PerformanceIndicatorSpecification with UUID: " + uuid));

        PerformanceIndicatorSpecificationMapper mapper = Mappers.getMapper(PerformanceIndicatorSpecificationMapper.class);
        performanceIndicatorSpecification = mapper.updatePerformanceIndicatorSpecification(performanceIndicatorSpecificationMVO, performanceIndicatorSpecification);

        performanceIndicatorSpecificationRepository.save(performanceIndicatorSpecification);
        return performanceIndicatorSpecification;
    }

    public Void deletePerformanceIndicatorSpecification(String uuid){
        log.debug("PerformanceIndicatorSpecification DELETE with UUID:{}", uuid);
        PerformanceIndicatorSpecification performanceIndicatorSpecification = performanceIndicatorSpecificationRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("No PerformanceIndicatorSpecification with UUID: " + uuid));
        performanceIndicatorSpecificationRepository.delete(performanceIndicatorSpecification);

        return null;
    }
}
