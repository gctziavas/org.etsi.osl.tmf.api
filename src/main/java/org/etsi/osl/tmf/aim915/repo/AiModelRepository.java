package org.etsi.osl.tmf.aim915.repo;

import org.etsi.osl.tmf.aim915.model.AiModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiModelRepository extends CrudRepository<AiModel, Long>, PagingAndSortingRepository<AiModel, Long> {
    Optional<AiModel> findByUuid(String uuid);
}
