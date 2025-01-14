package org.etsi.osl.tmf.pm628.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.pm628.model.DataAccessEndpoint;
import org.etsi.osl.tmf.pm628.reposervices.DataAccessEndpointService;
import org.etsi.osl.tmf.pm628.api.DataAccessEndpointApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Generated;
import java.util.List;
import java.util.Optional;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-05-15T07:30:16.936523289Z[Etc/UTC]", comments = "Generator version: 7.6.0-SNAPSHOT")
@Controller
@RequestMapping("/performance/v5")
public class DataAccessEndpointApiController implements DataAccessEndpointApi {

    private static final Logger log = LoggerFactory.getLogger(DataAccessEndpointApiController.class);

    private final NativeWebRequest request;

    @Autowired
    DataAccessEndpointService dataAccessEndpointService;

    @Autowired
    public DataAccessEndpointApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<List<DataAccessEndpoint>> listDataAccessEndpoint(
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields,
            @Parameter(name = "offset", description = "Requested index for start of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "offset", required = false) Integer offset,
            @Parameter(name = "limit", description = "Requested number of resources to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "limit", required = false) Integer limit
    ) {
        try {
            return new ResponseEntity<List<DataAccessEndpoint>>(dataAccessEndpointService.findAllDataAccessEndpoints(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<List<DataAccessEndpoint>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public ResponseEntity<DataAccessEndpoint> retrieveDataAccessEndpoint(
            @Parameter(name = "id", description = "Identifier of the Resource", required = true, in = ParameterIn.PATH) @PathVariable("id") String id,
            @Parameter(name = "fields", description = "Comma-separated properties to be provided in response", in = ParameterIn.QUERY) @Valid @RequestParam(value = "fields", required = false) String fields
    ) {
        try {
            return new ResponseEntity<DataAccessEndpoint>(dataAccessEndpointService.findDataAccessEndpointByUuid(id), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<DataAccessEndpoint>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}