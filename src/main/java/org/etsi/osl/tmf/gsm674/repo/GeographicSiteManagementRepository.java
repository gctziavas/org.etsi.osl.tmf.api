package org.etsi.osl.tmf.gsm674.repo;

import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeographicSiteManagementRepository extends CrudRepository<GeographicSite, Long>, PagingAndSortingRepository<GeographicSite, Long> {
    Optional<GeographicSite> findByUuid(String id);
}
