package org.etsi.osl.tmf.metrics.api;

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
public class GerenalMetricsApiController implements GeneralMetricsApi {

    private static final Logger log = LoggerFactory.getLogger(GerenalMetricsApiController.class);

    private final GeneralMetricsRepoService generalMetricsRepoService;

    @Autowired
    public GerenalMetricsApiController(GeneralMetricsRepoService generalMetricsRepoService) {
        this.generalMetricsRepoService = generalMetricsRepoService;
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getRegisteredIndividuals() {
        try {
            int totalIndividuals = generalMetricsRepoService.countRegisteredIndividuals();
            Map<String, Integer> response = new HashMap<>();
            response.put("registeredIndividuals", totalIndividuals);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total registered individuals. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getPublishedServiceSpecifications() {
        try {
            int totalSpecifications = generalMetricsRepoService.countPublishedServiceSpecifications();
            Map<String, Integer> response = new HashMap<>();
            response.put("publishedServiceSpecifications", totalSpecifications);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total published service specifications. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getRegisteredResourceSpecifications() {
        try {
            int totalResourceSpecifications = generalMetricsRepoService.countRegisteredResourceSpecifications();
            Map<String, Integer> response = new HashMap<>();
            response.put("registeredResourceSpecifications", totalResourceSpecifications);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't retrieve total registered resource specifications. ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
