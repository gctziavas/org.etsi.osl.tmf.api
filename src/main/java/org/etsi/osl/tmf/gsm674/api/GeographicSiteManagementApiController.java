package org.etsi.osl.tmf.gsm674.api;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.etsi.osl.tmf.gsm674.reposervices.GeographicSiteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/geographicSiteManagement/v4/")
public class GeographicSiteManagementApiController implements GeographicSiteManagementApi{

    private static final String COULD_NOT_SERIALIZE="Couldn't serialize response for content type application/json";
    private final GeographicSiteManagementService geographicSiteManagementService;

    @Autowired
    public GeographicSiteManagementApiController(GeographicSiteManagementService geographicSiteManagementService) {
        this.geographicSiteManagementService = geographicSiteManagementService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<List<GeographicSite>> retrieveGeographicSite() {


        try {
            return new ResponseEntity<>(geographicSiteManagementService.findAllGeographicSites(), HttpStatus.OK);

        } catch (Exception e) {
            log.error(COULD_NOT_SERIALIZE, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<GeographicSite> createGeographicSite(
            @Parameter(description = "The geographic site to be created", required = true) @Valid @RequestBody GeographicSite geographicSite
    ) {

        try {

            GeographicSite c = geographicSiteManagementService.createGeographicSite(geographicSite);

            return new ResponseEntity<>(c, HttpStatus.OK);


        } catch (Exception e) {
            log.error(COULD_NOT_SERIALIZE, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<Void> deleteGeographicSite(
            @Parameter(description = "Identifier of the geographic site", required = true) @PathVariable("id") String id) {

        try {
            return new ResponseEntity<>(geographicSiteManagementService.deleteGeographicSiteById(id), HttpStatus.OK);
        }catch (Exception e) {
            log.error(COULD_NOT_SERIALIZE, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PreAuthorize("hasAnyAuthority('ROLE_USER')" )
    @Override
    public ResponseEntity<GeographicSite> patchGeographicalSite(
            @Parameter(description = "Identifier of the ServiceOrder", required = true) @PathVariable("id") String id,
            @Parameter(description = "The ServiceOrder to be updated", required = true) @Valid @RequestBody GeographicSite geographicSite) {
        GeographicSite c = geographicSiteManagementService.updateGeographicSite(id, geographicSite);

        return new ResponseEntity<>(c, HttpStatus.OK);
    }
}
