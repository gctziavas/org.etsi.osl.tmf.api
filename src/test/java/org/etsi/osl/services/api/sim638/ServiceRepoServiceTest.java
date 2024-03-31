package org.etsi.osl.services.api.sim638;

import org.apache.commons.io.IOUtils;
import org.etsi.osl.model.nfv.DeploymentDescriptor;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.AttachmentRefOrValue;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.common.model.service.*;
import org.etsi.osl.tmf.common.model.service.Characteristic;
import org.etsi.osl.tmf.common.model.service.Place;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.rcm634.model.LogicalResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.LogicalResourceSpecificationCreate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRef;
import org.etsi.osl.tmf.ri639.model.*;
import org.etsi.osl.tmf.ri639.reposervices.ResourceRepoService;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.sim638.model.Service;
import org.etsi.osl.tmf.sim638.model.ServiceActionQueueItem;
import org.etsi.osl.tmf.sim638.model.ServiceCreate;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.etsi.osl.tmf.sim638.service.ServiceRepoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = OpenAPISpringBoot.class)
//@AutoConfigureTestDatabase //this automatically uses h2
@AutoConfigureMockMvc
@ActiveProfiles("testing")
//@TestPropertySource(
//		  locations = "classpath:application-testing.yml")
public class ServiceRepoServiceTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    ServiceRepoService serviceRepoService;

    @Autowired
    ResourceRepoService resourceRepoService;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAll() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        List<Service> serviceList = serviceRepoService.findAll("Test Party", UserPartRoleType.REQUESTER);

        boolean idExists = false;
        for (Service s : serviceList) {
            if ( s.getId().equals( id ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();
    }


//    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
//    @Test
//    public void testUpdateService() throws Exception {
//        String response = createService();
//        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
//        String id = responsesService.getId();
//
//        ServiceUpdate serviceUpdate = createServiceUpdateObject();
//        serviceRepoService.updateService(id, serviceUpdate, true, null, null);
//
//        Service updatedService = serviceRepoService.findByUuid(id);
//        assertThat(updatedService.getType()).isEqualTo("Updated type");
//        assertThat(updatedService.getName()).isEqualTo("Updated name");
//        assertThat(updatedService.getServiceType()).isEqualTo("Updated Service Type");
//    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteServiceActionQueueItemByUuid() throws Exception {
        ServiceActionQueueItem saqi = new ServiceActionQueueItem();
        ServiceActionQueueItem saqiResponse = serviceRepoService.addServiceActionQueueItem(saqi);
        String uuid = saqiResponse.getUuid();

        serviceRepoService.deleteServiceActionQueueItemByUuid(uuid);
        List<ServiceActionQueueItem> saqiList = serviceRepoService.findAllServiceActionQueueItems();

        boolean idExists = false;
        for (ServiceActionQueueItem s : saqiList) {
            if ( s.getUuid().equals( uuid ) ) {
                idExists= true;
                break;
            }
        }
        assertThat( idExists ).isFalse();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAllActiveServicesToTerminate() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        List<String> serviceList = serviceRepoService.findAllActiveServicesToTerminate();

        boolean idExists = false;
        for (String serviceId : serviceList) {
            if (serviceId.equals(id)) {
                idExists = true;
                break;
            }
        }
        assertThat( idExists ).isTrue();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAllActiveAndReservedServicesOfPartners() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        List<String> serviceList = serviceRepoService.findAllActiveAndReservedServicesOfPartners();

        boolean idExists = false;
        for (String serviceId : serviceList) {
            if (serviceId.equals(id)) {
                idExists = true;
                break;
            }
        }
        assertThat( idExists ).isTrue();
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindDeploymentRequestID() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();

        List<Service> serviceList = serviceRepoService.findDeploymentRequestID("1234567890");

        boolean idExists = false;
        for (Service s : serviceList) {
            if ( s.getId().equals( id ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();
    }


//    // org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection [HikariPool-1 - Connection is not available, request timed out after 30000ms.]
//    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
//    @Test
//    public void testNfvCatalogNSResourceChanged() throws Exception {
//        String response = createService();
//        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
//        String id = responsesService.getId();
//
//        DeploymentDescriptor dd = new DeploymentDescriptor();
//        dd.setId(1234567890);
//
//        serviceRepoService.nfvCatalogNSResourceChanged(dd);
//
//        Service updatedService = serviceRepoService.findByUuid(id);
//        Set<Note> noteList = updatedService.getNote();
//
//        boolean expectedNoteExists = false;
//        for (Note n : noteList) {
//            if ( n.getText().equals("NS Resource LCM Changed") && n.getAuthor().equals("SIM638-API")) {
//                expectedNoteExists= true;
//                break;
//            }
//        }
//        assertThat( expectedNoteExists ).isTrue();
//    }


//    //  org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection [HikariPool-1 - Connection is not available, request timed out after 30000ms.]
    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testResourceStateChangedEvent() throws Exception {
        String response = createService();
        Service responsesService = JsonUtils.toJsonObj(response,  Service.class);
        String id = responsesService.getId();
        Set<ResourceRef> resourceRefSet = responsesService.getSupportingResource();
        List<ResourceRef> resourceRefList = new ArrayList<>(resourceRefSet);

        assertThat(resourceRefList.size()).isEqualTo(1);
        ResourceRef firstResourceRef = resourceRefList.get(0);

        Resource resource = resourceRepoService.findByUuid(firstResourceRef.getId());

        ResourceStateChangeNotification resourceCreateNotification = new ResourceStateChangeNotification();
        ResourceStateChangeEvent event = new ResourceStateChangeEvent();
        event.getEvent().setResource(resource);
        resourceCreateNotification.setEvent(event);

        serviceRepoService.resourceStateChangedEvent(resourceCreateNotification);
        Service updatedService = serviceRepoService.findByUuid(id);

        Set<Note> noteSet = updatedService.getNote();
        List<Note> noteList = new ArrayList<>(noteSet);

        boolean expectedNoteExists = false;
        for (Note n : noteList) {
            if ( n.getText().contains("State Changed with status:") && n.getAuthor().equals("SIM638-API")) {
                expectedNoteExists= true;
                break;
            }
        }
        assertThat( expectedNoteExists ).isTrue();
    }


    private String createService() throws Exception {
        int servicesCount = serviceRepoService.findAll().size();

        File sspec = new File( "src/test/resources/testServiceSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");

        ServiceSpecificationCreate sspeccr1 = JsonUtils.toJsonObj( sspectext,  ServiceSpecificationCreate.class);
        sspeccr1.setName("Spec1");
        ServiceSpecification responsesSpec1 = createServiceSpec(sspectext, sspeccr1);

        //service 2 is an RFS
        ServiceSpecificationCreate sspeccr2 = JsonUtils.toJsonObj( sspectext,  ServiceSpecificationCreate.class);
        sspeccr2.setName("Spec2");
        ResourceSpecificationRef resourceSpecificationItem = new ResourceSpecificationRef();
        resourceSpecificationItem.setId("resourceid");
        resourceSpecificationItem.setName("resourceName");
        sspeccr2.addResourceSpecificationItem(resourceSpecificationItem);
        ServiceSpecification responsesSpec2 = createServiceSpec(sspectext, sspeccr2);

        // Add them as bundle
        ServiceSpecificationCreate sspeccr3 = JsonUtils.toJsonObj( sspectext,  ServiceSpecificationCreate.class);
        sspeccr3.setName("BundleExampleSpec");
        sspeccr3.isBundle(true);
        sspeccr3.addServiceSpecRelationshipWith( responsesSpec1 );
        sspeccr3.addServiceSpecRelationshipWith( responsesSpec2 );
        ServiceSpecification responsesSpec3 = createServiceSpec(sspectext, sspeccr3);

        ServiceCreate aService = new ServiceCreate();
        aService.setName("aNew Service");
        aService.setCategory("Test Category");
        aService.setDescription("A Test Service");
        aService.setStartDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aService.setEndDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aService.setState(ServiceStateType.ACTIVE);

        Note noteItem = new Note();
        noteItem.text("test note");
        aService.addNoteItem(noteItem);

        Characteristic serviceCharacteristicItem = new Characteristic();

        serviceCharacteristicItem.setName( "ConfigStatus" );
        serviceCharacteristicItem.setValue( new Any("NONE"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        serviceCharacteristicItem = new Characteristic();
        serviceCharacteristicItem.setName( "NSLCM" );
        serviceCharacteristicItem.setValue( new Any("nslcm_test"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        serviceCharacteristicItem = new Characteristic();
        serviceCharacteristicItem.setName( "NSR" );
        serviceCharacteristicItem.setValue( new Any("nsr_test"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        serviceCharacteristicItem = new Characteristic();
        serviceCharacteristicItem.setName( "externalPartnerServiceId" );
        serviceCharacteristicItem.setValue( new Any("ext_test"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        serviceCharacteristicItem = new Characteristic();
        serviceCharacteristicItem.setName( "DeploymentRequestID" );
        serviceCharacteristicItem.setValue( new Any("1234567890"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        serviceCharacteristicItem = new Characteristic();
        serviceCharacteristicItem.setName( "long_string" );
        serviceCharacteristicItem.setValue( new Any("12345"));
        aService.addServiceCharacteristicItem(serviceCharacteristicItem);

        ServiceSpecificationRef aServiceSpecificationRef = new ServiceSpecificationRef();
        aServiceSpecificationRef.setId(responsesSpec3.getId() );
        aServiceSpecificationRef.setName(responsesSpec3.getName());

        aService.setServiceSpecificationRef(aServiceSpecificationRef );

        List<RelatedParty> rpl = new ArrayList<>();
        RelatedParty rp = new RelatedParty();
        rp.setName("Test Party");
        rpl.add(rp);
        aService.setRelatedParty(rpl);

        ResourceCreate rc = new ResourceCreate();
        rc.setName("Test Resource");
        rc.setCategory("Test Resource Category");
        LogicalResourceSpecification lrs = createLogicalResourceSpec();
        ResourceSpecificationRef rsRef = new ResourceSpecificationRef();
        rsRef.setId(lrs.getId());
        rsRef.setName(lrs.getName());
        rc.setResourceSpecification(rsRef);
        Resource resourceResponse = resourceRepoService.addResource(rc);
        List<ResourceRef> rrl = new ArrayList<>();
        ResourceRef rRef = new ResourceRef();
        rRef.setId(resourceResponse.getId());
        rRef.setName(resourceResponse.getName());
        rrl.add(rRef);

        aService.setSupportingResource(rrl);

        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceInventory/v4/service")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( aService ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Service responseService = JsonUtils.toJsonObj(response, Service.class);

        assertThat( serviceRepoService.findAll().size() ).isEqualTo( servicesCount + 1 );
        assertThat(responseService.getCategory()).isEqualTo("Test Category");
        assertThat(responseService.getDescription()).isEqualTo("A Test Service");

        return response;
    }


    private ServiceSpecification createServiceSpec(String sspectext, ServiceSpecificationCreate sspeccr1) throws Exception{
        String response = mvc.perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( sspeccr1 ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response,  ServiceSpecification.class);
        return responsesSpec;
    }


    private LogicalResourceSpecification createLogicalResourceSpec() throws Exception{
        File sspec = new File( "src/test/resources/testResourceSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");
        LogicalResourceSpecificationCreate sspeccr = JsonUtils.toJsonObj( sspectext,  LogicalResourceSpecificationCreate.class);

        AttachmentRefOrValue attachmentItem = new AttachmentRefOrValue();
        attachmentItem.setId( "a-ref-id" );
        attachmentItem.setDescription("an attachment");
        attachmentItem.setUrl("a url");
        attachmentItem.setName("aname");
        sspeccr.addAttachmentItem(attachmentItem);
        String response = mvc.perform(MockMvcRequestBuilders.post("/resourceCatalogManagement/v4/resourceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( sspeccr ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Resource Spec")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LogicalResourceSpecification responsesSpec = JsonUtils.toJsonObj(response,  LogicalResourceSpecification.class);
        return responsesSpec;
    }


    private ServiceUpdate createServiceUpdateObject() throws Exception{
        ServiceUpdate serviceUpdate = new ServiceUpdate();
        serviceUpdate.setType("Updated type");
        serviceUpdate.setName("Updated name");
        serviceUpdate.setCategory("Updated category");
        serviceUpdate.setDescription("Updated description");
        serviceUpdate.setStartDate(OffsetDateTime.now(ZoneOffset.UTC ).toString());
        serviceUpdate.hasStarted(true);
        serviceUpdate.isServiceEnabled(true);
        serviceUpdate.isStateful(true);
        serviceUpdate.setServiceDate(OffsetDateTime.now(ZoneOffset.UTC ).toString());
        serviceUpdate.setServiceType("Updated Service Type");
        serviceUpdate.setStartMode("Updated Start Mode");
        serviceUpdate.setState(ServiceStateType.FEASIBILITYCHECKED);

        List<Place> placeList = new ArrayList<>();
        Place place = new Place();
        place.setName("Updated place");
        place.setType("Updated type");
        placeList.add(place);
        serviceUpdate.setPlace(placeList);

        List<RelatedParty> partyList = new ArrayList<>();
        RelatedParty party = new RelatedParty();
        party.setName("Updated party");
        party.setType("Updated type");
        partyList.add(party);
        serviceUpdate.setRelatedParty(partyList);

        ResourceCreate rc = new ResourceCreate();
        rc.setName("Updated Resource");
        rc.setCategory("Updated Resource Category");
        LogicalResourceSpecification lrs = createLogicalResourceSpec();
        ResourceSpecificationRef rsRef = new ResourceSpecificationRef();
        rsRef.setId(lrs.getId());
        rsRef.setName(lrs.getName());
        rc.setResourceSpecification(rsRef);
        Resource resourceResponse = resourceRepoService.addResource(rc);
        List<ResourceRef> rrl = new ArrayList<>();
        ResourceRef rRef = new ResourceRef();
        rRef.setId(resourceResponse.getId());
        rRef.setName(resourceResponse.getName());
        rrl.add(rRef);

        serviceUpdate.setSupportingResource(rrl);

        return serviceUpdate;
    }
}
