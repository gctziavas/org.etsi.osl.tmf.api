package org.etsi.osl.tmf.metrics.reposervices;

import org.etsi.osl.tmf.pm632.repo.IndividualRepository;
import org.etsi.osl.tmf.rcm634.repo.ResourceSpecificationRepository;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.repo.CandidateRepository;
import org.etsi.osl.tmf.scm633.repo.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GeneralMetricsRepoService {

    @Autowired
    IndividualRepository individualRepository;

    @Autowired
    ResourceSpecificationRepository resourceSpecificationRepository;

    @Autowired
    CategoriesRepository categoriesRepository;

    @Autowired
    CandidateRepository candidateRepository;

    // Cached values for performance optimization
    private static Integer cachedPublishedServiceSpecCount = null;
    private static OffsetDateTime lastRetrieved = null;
    private static final Duration CACHE_DURATION = Duration.ofMinutes(15);

    public int countRegisteredIndividuals() {
        return individualRepository.countAll();
    }

    public int countPublishedServiceSpecifications() {
        OffsetDateTime now = OffsetDateTime.now();

        if (cachedPublishedServiceSpecCount == null || lastRetrieved == null ||
                Duration.between(lastRetrieved, now).compareTo(CACHE_DURATION) > 0) {

            int count = 0;
            List<ServiceCategory> serviceCategories = categoriesRepository.findByOrderByName();

            for (ServiceCategory serviceCategory : serviceCategories) {
                count += serviceCategory.getServiceCandidateObj().size()
                        + serviceCategory.getServiceCandidateRefs().size();
            }

            cachedPublishedServiceSpecCount = count;
            lastRetrieved = now;
        }
        return cachedPublishedServiceSpecCount;
    }

    public int countRegisteredResourceSpecifications() {
        return resourceSpecificationRepository.countLogical() + resourceSpecificationRepository.countPhysical();
    }
}
