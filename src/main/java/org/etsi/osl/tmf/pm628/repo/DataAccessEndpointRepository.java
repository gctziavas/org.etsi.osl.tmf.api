package org.etsi.osl.tmf.pm628.repo;

import org.etsi.osl.tmf.pm628.model.DataAccessEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataAccessEndpointRepository extends CrudRepository<DataAccessEndpoint, Long>, PagingAndSortingRepository<DataAccessEndpoint, Long> {
    Optional<DataAccessEndpoint> findByUuid(String uuid);
}
