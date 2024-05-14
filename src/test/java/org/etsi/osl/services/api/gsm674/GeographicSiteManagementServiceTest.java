package org.etsi.osl.services.api.gsm674;
import org.etsi.osl.tmf.gsm674.model.GeographicSite;
import org.etsi.osl.tmf.gsm674.repo.GeographicSiteManagementRepository;
import org.etsi.osl.tmf.gsm674.reposervices.GeographicSiteManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@AutoConfigureMockMvc
@ActiveProfiles("testing")
class GeographicSiteManagementServiceTest {
    @InjectMocks
    private GeographicSiteManagementService service;

    @Mock
    private GeographicSiteManagementRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindAllGeographicSites() {
        // Mock data
        List<GeographicSite> sites = List.of(new GeographicSite(), new GeographicSite());
        when(repository.findAll()).thenReturn(sites);

        List<GeographicSite> result = service.findAllGeographicSites();

        assertEquals(sites, result);
    }

    @Test
    void testFindGeographicSiteByUUID() {
        // Mock data
        String uuid = "123";
        GeographicSite site = new GeographicSite();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(site));

        GeographicSite result = service.findGeographicSiteByUUID(uuid);

        assertEquals(site, result);
    }

    @Test
    void testCreateGeographicSite() {
        // Mock data
        GeographicSite site = new GeographicSite();
        when(repository.save(any())).thenReturn(site);

        GeographicSite result = service.createGeographicSite(site);

        assertEquals(site, result);
    }

    @Test
    void testUpdateGeographicSite() {
        // Mock data
        String id = "123";
        GeographicSite existingSite = new GeographicSite();
        GeographicSite newSite = new GeographicSite();
        when(repository.findByUuid(id)).thenReturn(Optional.of(existingSite));
        when(repository.save(any())).thenReturn(existingSite);

        GeographicSite result = service.updateGeographicSite(id, newSite);

        assertNotNull(result);
        assertEquals(existingSite, result);
        // Add additional assertions for updated fields if needed
    }

    @Test
    void testDeleteGeographicSiteById() {
        // Mock data
        String id = "123";
        GeographicSite existingSite = new GeographicSite();
        when(repository.findByUuid(id)).thenReturn(Optional.of(existingSite));

        Void result = service.deleteGeographicSiteById(id);

        assertNull(result);
        verify(repository, times(1)).delete(existingSite);
    }
}
