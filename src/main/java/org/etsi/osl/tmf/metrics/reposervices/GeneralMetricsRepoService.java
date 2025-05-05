package org.etsi.osl.tmf.metrics.reposervices;

import org.etsi.osl.tmf.pm632.repo.IndividualRepository;
import org.etsi.osl.tmf.rcm634.repo.ResourceSpecificationRepository;
import org.etsi.osl.tmf.scm633.model.ServiceCandidate;
import org.etsi.osl.tmf.scm633.model.ServiceCategory;
import org.etsi.osl.tmf.scm633.repo.CandidateRepository;
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

    @Autowired
    CandidateRepository candidateRepository;

    public int countRegisteredIndividuals() {
        return individualRepository.countAll();
    }

    public int countPublishedServiceSpecifications() {

        List<ServiceCandidate> serviceCandidates = candidateRepository.findAll();
        int count = 0;

        for (ServiceCandidate serviceCandidate : serviceCandidates) {
            System.out.println("ServiceCandidate Category: " + serviceCandidate.getCategoryObj());
            if (serviceCandidate.getCategory() != null) {
                count += 1;
            }
        }
        return count;
    }

    public int countRegisteredResourceSpecifications() {
        return resourceSpecificationRepository.countLogical() + resourceSpecificationRepository.countPhysical();
    }
}
