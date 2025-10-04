package org.etsi.osl.services.api.pcm620;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.pcm620.model.Catalog;
import org.etsi.osl.tmf.pcm620.model.CatalogCreate;
import org.etsi.osl.tmf.pcm620.reposervices.CatalogNotificationService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCatalogRepoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class CatalogNotificationIntegrationTest extends BaseIT {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductCatalogRepoService productCatalogRepoService;

    @SpyBean
    private CatalogNotificationService catalogNotificationService;

    @PersistenceContext
    private EntityManager entityManager;

    private AutoCloseable mocks;

    @BeforeAll
    public void setupOnce() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeAll
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (entityManager != null) {
            entityManager.clear();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCatalogCreateNotificationFlow() throws Exception {
        // Arrange
        CatalogCreate catalogCreate = new CatalogCreate();
        catalogCreate.setName("Test Notification Catalog");
        catalogCreate.setDescription("A catalog to test notifications");
        catalogCreate.setVersion("1.0");

        // Act - Create catalog through repository service
        Catalog createdCatalog = productCatalogRepoService.addCatalog(catalogCreate);

        // Assert - Verify notification was published
        verify(catalogNotificationService, timeout(1000)).publishCatalogCreateNotification(any(Catalog.class));
        
        // Verify catalog was created
        assert createdCatalog != null;
        assert createdCatalog.getName().equals("Test Notification Catalog");
        assert createdCatalog.getUuid() != null;
    }

    @Test
    @WithMockUser(username = "osadmin", roles = {"ADMIN"})
    public void testCatalogDeleteNotificationFlow() throws Exception {
        // Arrange - First create a catalog
        CatalogCreate catalogCreate = new CatalogCreate();
        catalogCreate.setName("Test Delete Notification Catalog");
        catalogCreate.setDescription("A catalog to test delete notifications");
        catalogCreate.setVersion("1.0");
        
        Catalog createdCatalog = productCatalogRepoService.addCatalog(catalogCreate);
        String catalogId = createdCatalog.getUuid();

        // Act - Delete the catalog
        productCatalogRepoService.deleteById(catalogId);

        // Assert - Verify both create and delete notifications were published
        verify(catalogNotificationService, timeout(1000)).publishCatalogCreateNotification(any(Catalog.class));
        verify(catalogNotificationService, timeout(1000)).publishCatalogDeleteNotification(any(Catalog.class));
    }

    @Test
    public void testDirectCatalogOperations() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setName("Direct Test Catalog");
        catalog.setDescription("Direct catalog for testing");

        // Act - Add catalog directly
        Catalog savedCatalog = productCatalogRepoService.addCatalog(catalog);
        
        // Assert - Verify notification was called
        verify(catalogNotificationService, timeout(1000)).publishCatalogCreateNotification(any(Catalog.class));
        
        assert savedCatalog != null;
        assert savedCatalog.getName().equals("Direct Test Catalog");
    }
}