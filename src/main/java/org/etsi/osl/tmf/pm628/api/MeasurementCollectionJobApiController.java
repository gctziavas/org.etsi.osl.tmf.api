package org.etsi.osl.tmf.pm628.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJob;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobFVO;
import org.etsi.osl.tmf.pm628.model.MeasurementCollectionJobMVO;
import org.etsi.osl.tmf.pm628.reposervices.MeasurementCollectionJobService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-05-15T07:30:16.936523289Z[Etc/UTC]", comments = "Generator version: 7.6.0-SNAPSHOT")
@Controller
@RequestMapping("/monitoring/v5")
public class MeasurementCollectionJobApiController implements MeasurementCollectionJobApi {

    private static final Logger log = LoggerFactory.getLogger(MeasurementCollectionJobApiController.class);

    private final NativeWebRequest request;

    @Autowired
    MeasurementCollectionJobService measurementCollectionJobService;

    @Autowired
    public MeasurementCollectionJobApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<MeasurementCollectionJob> createMeasurementCollectionJob(
            @Parameter(name = "MeasurementCollectionJobFVO", description = "The MeasurementCollectionJob to be created", required = true) @Valid @RequestBody MeasurementCollectionJobFVO measurementCollectionJobFVO,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<MeasurementCollectionJob>(measurementCollectionJobService.createMeasurementCollectionJob(measurementCollectionJobFVO), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<MeasurementCollectionJob>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<Void> deleteMeasurementCollectionJob(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id
    ) {
        try {
            return new ResponseEntity<Void>(measurementCollectionJobService.deleteMeasurementCollectionJob(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<List<MeasurementCollectionJob>> listMeasurementCollectionJob(
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields,
            @Parameter(name = "allParams", description = "Filters to be applied in the query", in = ParameterIn.QUERY) @Valid @RequestParam(value = "allParams", required = false) Map<String, String> allParams,
            @Parameter(name = "offset", description = "Requested index for start of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "offset", required = false) Integer offset,
            @Parameter(name = "limit", description = "Requested number of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "limit", required = false) Integer limit
    ) {
        try {
            if (allParams == null)
                allParams = new HashMap<>();

            return new ResponseEntity<List<MeasurementCollectionJob>>(measurementCollectionJobService.findAll(fields, allParams), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<List<MeasurementCollectionJob>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<MeasurementCollectionJob> patchMeasurementCollectionJob(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "MeasurementCollectionJobMVO", description = "The MeasurementCollectionJob to be patched", required = true) @Valid @RequestBody MeasurementCollectionJobMVO measurementCollectionJobMVO,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<MeasurementCollectionJob>(measurementCollectionJobService.updateMeasurementCollectionJob(id, measurementCollectionJobMVO), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<MeasurementCollectionJob>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<MeasurementCollectionJob> retrieveMeasurementCollectionJob(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<MeasurementCollectionJob>(measurementCollectionJobService.findMeasurementCollectionJobByUuid(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<MeasurementCollectionJob>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
