package org.etsi.osl.tmf.pm628.repo;

import java.util.Optional;
import org.etsi.osl.tmf.pm628.model.ExecutionStateType;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementCollectionJobRepository extends JpaRepository <MeasurementCollectionJob, Long> {
    Optional<MeasurementCollectionJob> findByUuid(String uuid);

    Iterable<MeasurementCollectionJob> findByExecutionState(ExecutionStateType executionState);
}
