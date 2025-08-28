package org.etsi.osl.tmf.aim915.repo;

import org.etsi.osl.tmf.aim915.model.AiModelSpecification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiModelSpecificationRepository extends CrudRepository<AiModelSpecification, Long>, PagingAndSortingRepository<AiModelSpecification, Long> {
    Optional<AiModelSpecification> findByUuid(String uuid);
}
