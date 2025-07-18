package org.etsi.osl.tmf.metrics.api;

import org.etsi.osl.tmf.metrics.PublishedServiceSpecifications;
import org.etsi.osl.tmf.metrics.RegisteredIndividuals;
import org.etsi.osl.tmf.metrics.RegisteredResourceSpecifications;
import org.etsi.osl.tmf.metrics.reposervices.GeneralMetricsRepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GeneralMetricsApiController implements GeneralMetricsApi {

    private static final Logger log = LoggerFactory.getLogger(GeneralMetricsApiController.class);

    private final GeneralMetricsRepoService generalMetricsRepoService;

    @Autowired
    public GeneralMetricsApiController(GeneralMetricsRepoService generalMetricsRepoService) {
        this.generalMetricsRepoService = generalMetricsRepoService;
    }

    @Override
    public ResponseEntity<RegisteredIndividuals> getRegisteredIndividuals() {
        try {
            int totalIndividuals = generalMetricsRepoService.countRegisteredIndividuals();
            RegisteredIndividuals response = new RegisteredIndividuals(totalIndividuals);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total registered individuals. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<PublishedServiceSpecifications> getPublishedServiceSpecifications() {
        try {
            int totalSpecifications = generalMetricsRepoService.countPublishedServiceSpecifications();
            PublishedServiceSpecifications response = new PublishedServiceSpecifications(totalSpecifications);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total published service specifications. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<RegisteredResourceSpecifications> getRegisteredResourceSpecifications() {
        try {
            int totalResourceSpecifications = generalMetricsRepoService.countRegisteredResourceSpecifications();
            RegisteredResourceSpecifications response = new RegisteredResourceSpecifications(totalResourceSpecifications);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total registered resource specifications. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
