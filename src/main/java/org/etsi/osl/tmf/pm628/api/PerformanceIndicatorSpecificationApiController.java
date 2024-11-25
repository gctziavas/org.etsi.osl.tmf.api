package org.etsi.osl.tmf.pm628.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.etsi.osl.tmf.pm628.model.PerformanceIndicatorSpecification;
import org.etsi.osl.tmf.pm628.model.PerformanceIndicatorSpecificationFVO;
import org.etsi.osl.tmf.pm628.model.PerformanceIndicatorSpecificationMVO;
import org.etsi.osl.tmf.pm628.reposervices.PerformanceIndicatorSpecificationService;
import org.etsi.osl.tmf.pm628.api.PerformanceIndicatorSpecificationApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Generated;
import java.util.List;
import java.util.Optional;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-05-15T07:30:16.936523289Z[Etc/UTC]", comments = "Generator version: 7.6.0-SNAPSHOT")
@Controller
@RequestMapping("/performance/v5")
public class PerformanceIndicatorSpecificationApiController implements PerformanceIndicatorSpecificationApi {

    private static final Logger log = LoggerFactory.getLogger(PerformanceIndicatorSpecificationApiController.class);

    private final NativeWebRequest request;

    @Autowired
    PerformanceIndicatorSpecificationService performanceIndicatorSpecificationService;

    @Autowired
    public PerformanceIndicatorSpecificationApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<PerformanceIndicatorSpecification> createPerformanceIndicatorSpecification(
            @Parameter(name = "PerformanceIndicatorSpecificationFVO", description = "The PerformanceIndicatorSpecification to be created", required = true) @Valid @RequestBody PerformanceIndicatorSpecificationFVO performanceIndicatorSpecificationFVO,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<PerformanceIndicatorSpecification>(performanceIndicatorSpecificationService.createPerformanceIndicatorSpecification(performanceIndicatorSpecificationFVO), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<PerformanceIndicatorSpecification>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<Void> deletePerformanceIndicatorSpecification(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id
    ) {
        try {
            return new ResponseEntity<Void>(performanceIndicatorSpecificationService.deletePerformanceIndicatorSpecification(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<List<PerformanceIndicatorSpecification>> listPerformanceIndicatorSpecification(
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields,
            @Parameter(name = "offset", description = "Requested index for start of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "offset", required = false) Integer offset,
            @Parameter(name = "limit", description = "Requested number of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "limit", required = false) Integer limit
    ) {
        try {
            // #TODO: Add parameters to findAllPerformanceIndicatorSpecifications
            return new ResponseEntity<List<PerformanceIndicatorSpecification>>(performanceIndicatorSpecificationService.findAllPerformanceIndicatorSpecifications(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<List<PerformanceIndicatorSpecification>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<PerformanceIndicatorSpecification> patchPerformanceIndicatorSpecification(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "PerformanceIndicatorSpecificationMVO", description = "The PerformanceIndicatorSpecification to be patched", required = true) @Valid @RequestBody PerformanceIndicatorSpecificationMVO performanceIndicatorSpecificationMVO,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<PerformanceIndicatorSpecification>(performanceIndicatorSpecificationService.updatePerformanceIndicatorSpecification(id, performanceIndicatorSpecificationMVO), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<PerformanceIndicatorSpecification>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<PerformanceIndicatorSpecification> retrievePerformanceIndicatorSpecification(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            // #TODO: Add parameters to findPerformanceIndicatorSpecificationByUuid
            return new ResponseEntity<PerformanceIndicatorSpecification>(performanceIndicatorSpecificationService.findPerformanceIndicatorSpecificationByUuid(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<PerformanceIndicatorSpecification>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
