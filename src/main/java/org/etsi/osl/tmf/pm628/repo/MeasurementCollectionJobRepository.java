package org.etsi.osl.tmf.pm628.repo;

import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeasurementCollectionJobRepository extends CrudRepository<MeasurementCollectionJob, Long>, PagingAndSortingRepository<MeasurementCollectionJob, Long> {
    Optional<MeasurementCollectionJob> findByUuid(String uuid);

    Iterable<MeasurementCollectionJob> findByExecutionState(ExecutionStateType executionState);
}
