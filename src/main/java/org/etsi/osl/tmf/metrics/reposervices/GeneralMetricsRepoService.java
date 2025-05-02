package org.etsi.osl.tmf.metrics.reposervices;

import org.etsi.osl.tmf.pm632.repo.IndividualRepository;
import org.etsi.osl.tmf.rcm634.repo.ResourceSpecificationRepository;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.repo.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneralMetricsRepoService {

    @Autowired
    IndividualRepository individualRepository;

    @Autowired
    ResourceSpecificationRepository resourceSpecificationRepository;

    @Autowired
    CategoriesRepository categoriesRepository;

    public int countRegisteredIndividuals() {
        return individualRepository.countAll();
    }

    public int countPublishedServiceSpecifications() {
        List<ServiceCategory> serviceCategories = categoriesRepository.findByOrderByName();
        int count = 0;

        for (ServiceCategory serviceCategory : serviceCategories) {
            count += serviceCategory.getServiceCandidateObj().size() + serviceCategory.getServiceCandidateRefs().size();
        }
        return count;
    }

    public int countRegisteredResourceSpecifications() {
        return resourceSpecificationRepository.countLogical() + resourceSpecificationRepository.countPhysical();
    }
}
