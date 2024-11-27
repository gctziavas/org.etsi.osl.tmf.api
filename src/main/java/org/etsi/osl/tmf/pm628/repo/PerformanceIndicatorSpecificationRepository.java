package org.etsi.osl.tmf.pm628.repo;

import org.etsi.osl.tmf.pm628.model.PerformanceIndicatorSpecification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerformanceIndicatorSpecificationRepository extends CrudRepository<PerformanceIndicatorSpecification, Long>, PagingAndSortingRepository<PerformanceIndicatorSpecification, Long> {
    Optional<PerformanceIndicatorSpecification> findByUuid(String uuid);
}
