package org.etsi.osl.services.api.scm633;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.etsi.osl.tmf.scm633.api.ServiceCatalogApiRouteBuilderEvents;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.etsi.osl.tmf.scm633.reposervices.ServiceCatalogCallbackService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
public class ServiceSpecificationNotificationServiceTest {

    @Mock
    private ServiceCatalogApiRouteBuilderEvents eventPublisher;
    
    @Mock
    private ServiceCatalogCallbackService serviceCatalogCallbackService;

    @InjectMocks
    private ServiceSpecificationNotificationService serviceSpecificationNotificationService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishServiceSpecificationCreateNotification() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-123");
        serviceSpecification.setName("Test Service Specification");
        serviceSpecification.setDescription("A test service specification for notifications");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationCreateNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationCreateNotification.class), eq("test-spec-123"));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationCreateCallback(any());
    }

    @Test
    public void testPublishServiceSpecificationDeleteNotification() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-456");
        serviceSpecification.setName("Test Service Specification to Delete");
        serviceSpecification.setDescription("A test service specification for deletion notifications");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationDeleteNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationDeleteNotification.class), eq("test-spec-456"));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationDeleteCallback(any());
    }

    @Test
    public void testPublishServiceSpecificationChangeNotification() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setUuid("test-spec-789");
        serviceSpecification.setName("Test Service Specification to Change");
        serviceSpecification.setDescription("A test service specification for change notifications");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationChangeNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationChangeNotification.class), eq("test-spec-789"));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationChangeCallback(any());
    }

    @Test
    public void testPublishServiceSpecificationCreateNotificationWithNullUuid() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setName("Test Service Specification with Null UUID");
        serviceSpecification.setDescription("A test service specification with null UUID");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationCreateNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationCreateNotification.class), eq(null));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationCreateCallback(any());
    }

    @Test
    public void testPublishServiceSpecificationDeleteNotificationWithNullUuid() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setName("Test Service Specification with Null UUID");
        serviceSpecification.setDescription("A test service specification with null UUID");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationDeleteNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationDeleteNotification.class), eq(null));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationDeleteCallback(any());
    }

    @Test
    public void testPublishServiceSpecificationChangeNotificationWithNullUuid() {
        // Arrange
        ServiceSpecification serviceSpecification = new ServiceSpecification();
        serviceSpecification.setName("Test Service Specification with Null UUID");
        serviceSpecification.setDescription("A test service specification with null UUID");

        // Act
        serviceSpecificationNotificationService.publishServiceSpecificationChangeNotification(serviceSpecification);

        // Assert
        verify(eventPublisher, times(1)).publishEvent(any(ServiceSpecificationChangeNotification.class), eq(null));
        verify(serviceCatalogCallbackService, times(1)).sendServiceSpecificationChangeCallback(any());
    }
}