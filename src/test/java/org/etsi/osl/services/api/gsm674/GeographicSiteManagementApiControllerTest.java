package org.etsi.osl.services.api.gsm674;

import org.etsi.osl.tmf.gsm674.api.GeographicSiteManagementApiController;
import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.etsi.osl.tmf.gsm674.reposervices.GeographicSiteManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@ActiveProfiles("testing")
class GeographicSiteManagementApiControllerTest {

    @InjectMocks
    private GeographicSiteManagementApiController controller;

    @Mock
    private GeographicSiteManagementService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRetrieveGeographicSite() {
        List<GeographicSite> sites = new ArrayList<>();
        // Add test data to sites list
        when(service.findAllGeographicSites()).thenReturn(sites);

        ResponseEntity<List<GeographicSite>> response = controller.retrieveGeographicSite();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sites, response.getBody());
    }

    @Test
    void throwExceptionTestWhenRetrieveGeographicSites(){
        when(service.findAllGeographicSites()).thenThrow(RuntimeException.class);
        ResponseEntity<List<GeographicSite>> response = controller.retrieveGeographicSite();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }
    @Test
    void testCreateGeographicSite() {
        GeographicSite site = new GeographicSite();

        when(service.createGeographicSite(any())).thenReturn(site);

        ResponseEntity<GeographicSite> response = controller.createGeographicSite(site);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(site, response.getBody());
    }

    @Test
    void testExceptionWhenCreateGeographicSite(){
        GeographicSite site = new GeographicSite();

        when(service.createGeographicSite(any())).thenThrow(RuntimeException.class);

        ResponseEntity<GeographicSite> response = controller.createGeographicSite(site);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testDeleteGeographicSite() {

        doNothing().when(service).deleteGeographicSiteById(anyString());
        ResponseEntity<Void> response = controller.deleteGeographicSite("siteId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteGeographicSiteException() {

        doThrow(RuntimeException.class).when(service).deleteGeographicSiteById(anyString());
        ResponseEntity<Void> response = controller.deleteGeographicSite("siteId");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testPatchGeographicalSite() {
        String siteId = "siteId";
        GeographicSite updatedSite = new GeographicSite();
        // Set up mock service behavior
        when(service.updateGeographicSite(anyString(), any())).thenReturn(updatedSite);

        ResponseEntity<GeographicSite> response = controller.patchGeographicalSite(siteId, updatedSite);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSite, response.getBody());
    }

    @Test
    void testPatchGeographicalSiteException() {
        String siteId = "siteId";
        GeographicSite updatedSite = new GeographicSite();
        // Set up mock service behavior
        when(service.updateGeographicSite(anyString(), any())).thenThrow(RuntimeException.class);

        ResponseEntity<GeographicSite> response = controller.patchGeographicalSite(siteId, updatedSite);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
