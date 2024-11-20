/**
 * @Author: Eduardo Santos
 * @Date:   2024-05-30 12:52:02
 * @Last Modified by:   Eduardo Santos
 * @Last Modified time: 2024-05-31 13:30:14
 */

package org.etsi.osl.services.reposervices.scm633;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.etsi.osl.model.nfv.NetworkServiceDescriptor;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecification;
import org.etsi.osl.tmf.rcm634.reposervices.ResourceSpecificationRepoService;
import org.etsi.osl.tmf.scm633.api.ServiceSpecificationApiRouteBuilderNSD;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristic;
import org.etsi.osl.tmf.scm633.model.ServiceSpecCharacteristicValue;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

@RunWith(SpringRunner.class)
@ActiveProfiles("testing")
@Transactional
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.MOCK , classes = OpenAPISpringBoot.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class ServiceSpecificationRepoServiceTest {

    @Autowired
    private ServiceSpecificationRepoService serviceSpecificationRepoService;

    @MockBean
    private ServiceSpecificationApiRouteBuilderNSD serviceSpecificationApiRouteBuilderNSD;

    @Mock
    private static ResourceSpecificationRepoService resourceSpecRepoService;

    private static NetworkServiceDescriptor nsd;

    private static ResourceSpecification resourceNSD;

    @BeforeClass
    public static void setupBeforeClass() {
        // Load NSD from JSON file to NetworkServiceDescriptor.class
        try {
            ObjectMapper mapper = new ObjectMapper();

            nsd = mapper.readValue(
                    new File(
                            "src/test/resources/reposervices/scm633/nsd.json"),
                    NetworkServiceDescriptor.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Assert that the NSD was properly loaded
        assertNotNull(nsd);

        // Mock resourceNSD
        resourceNSD = mock(ResourceSpecification.class);
        resourceNSD.setName(nsd.getName());
        resourceNSD.setVersion(nsd.getVersion());
        resourceNSD.setDescription(nsd.getShortDescription());
    }

    @Before
    public void setupBefore() {
        when(serviceSpecificationApiRouteBuilderNSD.retrieveNSD(anyString())).thenReturn(nsd);
        when(resourceSpecRepoService.addResourceSpec(any())).thenReturn(resourceNSD);
    }


    /**
     * Tests that specFromNSDID returns a non-null result when retrieveNSD returns a non-null NetworkServiceDescriptor.
     * 
     * This test verifies that the method retrieveNSD correctly returns a non-null NetworkServiceDescriptor
     * and that the specFromNSDID method does not return null in such a case.
     */
    @Test
    public void testSpecFromNSDIDReturnsNonNullWhenRetrieveNSDReturnsNonNull() {
        // Setup
        String id = "testId";
        
        NetworkServiceDescriptor mockNsd = mock(NetworkServiceDescriptor.class);
        when(serviceSpecificationApiRouteBuilderNSD.retrieveNSD(id)).thenReturn(mockNsd);

        NetworkServiceDescriptor result = serviceSpecificationApiRouteBuilderNSD.retrieveNSD(id);

        // Assertion
        assertNotNull("The result should not be null when NSD is not null", result);
    }


    /**
     * Tests that retrieveNSD returns null when the method is mocked to return null.
     * 
     * This test verifies that the method retrieveNSD correctly returns null when it is expected to.
     */
    @Test
    public void testRetrieveNSDReturnsNull() {
        // When retrieveNSD return null
        String id = "SomeId";
        when(serviceSpecificationApiRouteBuilderNSD.retrieveNSD(id)).thenReturn(null);

        // Act
        NetworkServiceDescriptor result = serviceSpecificationApiRouteBuilderNSD.retrieveNSD(id);

        // Assert
        assertNull(result);
    }


    /**
     * Tests the basic properties of the ServiceSpecification.
     * 
     * This test verifies that the ServiceSpecification object created from the NSD ID
     * has the correct name, version, and description. It also checks that the correct number
     * of ServiceSpecification objects are created.
     */
    @Test
    public void testServiceSpecificationBaseProperties() {
        String testId = "validId";

        // Invoke the SUT
        List<ServiceSpecification> result = serviceSpecificationRepoService.specFromNSDID(testId);

        // Evaluate the obtained results
        // Assert that two service specifications are created
        assertEquals(1, result.size());

        ServiceSpecification serviceSpec = result.get(0);

        // Assert that the name, version, and description are the correct ones
        assertEquals("tutorial_ns@osm14", serviceSpec.getName());
        assertEquals("1.0", serviceSpec.getVersion());
        assertEquals("tutorial_ns", serviceSpec.getDescription());
    }


    /**
     * Tests the characteristics of the ServiceSpecification objects.
     * 
     * This test verifies that the method specFromNSDID correctly creates a list of 
     * ServiceSpecification objects from a given NSD ID and that these objects have the expected 
     * characteristics. It checks that the list contains exactly one ServiceSpecification object and 
     * that this object has the expected characteristics, including the correct name, value, and alias 
     * for each characteristic.
     */
    @Test
    public void testServiceSpecificationCharacteristics() {
        String testId = "validId";

        // Invoke the SUT
        List<ServiceSpecification> result = serviceSpecificationRepoService.specFromNSDID(testId);

        // Evaluate the obtained results
        // Assert that two service specifications are created
        assertEquals(1, result.size());
        
        ServiceSpecification serviceSpec = result.get(0);

        // Evaluate each Service Specification Characteristic
        // Get all Service Specification Characteristics 
        Set<ServiceSpecCharacteristic> serviceSpecCharacteristics = serviceSpec.getServiceSpecCharacteristic();

        // Cast Service Specification Characteristics Set to ArrayList
        List<ServiceSpecCharacteristic> serviceSpecCharacteristicsList = new ArrayList<>(serviceSpecCharacteristics);

        // Order Service Specification Characteristics list by characterists' name
        Collections.sort(serviceSpecCharacteristicsList, Comparator.comparing(ServiceSpecCharacteristic::getName));

        // Assert that the Service Specification Characterists are the ones desired
        assertEquals(27, serviceSpec.getServiceSpecCharacteristic().size());

        /********************* Assert Service Specification Characteristic APPLY_CONFIG *********************/
        
        ServiceSpecCharacteristic serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(0);
        @Valid Set<ServiceSpecCharacteristicValue> serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        Any v = serviceSpecCharacteristicValues.iterator().next().getValue();
        String value = v.getValue();
        String alias = v.getAlias();

        assertEquals("APPLY_CONFIG", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic ConfigStatus *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(1);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("ConfigStatus", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic ConstituentVnfrIps *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(2);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("ConstituentVnfrIps", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic DeploymentRequestID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(3);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("DeploymentRequestID", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic InstanceId *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(4);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("InstanceId", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic MANOproviderID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(5);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("MANOproviderID", serviceSpecCharacteristic.getName());
        assertEquals("1", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic MANOproviderName *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(6);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("MANOproviderName", serviceSpecCharacteristic.getName());
        assertEquals("osm14", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic MemberVNFIndex_vnf1 *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(7);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();
        
        assertEquals("MemberVNFIndex_vnf1", serviceSpecCharacteristic.getName());
        assertEquals("vnf1", value);
        assertEquals("tutorial_vnf", alias);

        /********************* Assert Service Specification Characteristic MemberVNFIndex_vnf2 *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(8);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("MemberVNFIndex_vnf2", serviceSpecCharacteristic.getName());
        assertEquals("vnf2", value);
        assertEquals("tutorial_vnf", alias);

        /********************* Assert Service Specification Characteristic NSDID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(9);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("NSDID", serviceSpecCharacteristic.getName());
        assertEquals("4", value);
        assertEquals("id", alias);

        /********************* Assert Service Specification Characteristic NSLCM *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(10);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("NSLCM", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic NSR *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(11);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("NSR", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OSM_CONFIG *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(12);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OSM_CONFIG", serviceSpecCharacteristic.getName());
        assertEquals("{\"nsdId\":\"9dc24900-e63e-4c0e-b686-ee2ef124c5c2\", \"vimAccountId\":\"479356bf-72ff-4dfd-8483-5c23f48dd0bc\"}", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OSM_NSDCATALOGID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(13);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OSM_NSDCATALOGID", serviceSpecCharacteristic.getName());
        assertEquals("9dc24900-e63e-4c0e-b686-ee2ef124c5c2", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OnBoardDescriptorID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(14);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OnBoardDescriptorID", serviceSpecCharacteristic.getName());
        assertEquals("4", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OnBoardDescriptorUUID *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(15);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OnBoardDescriptorUUID", serviceSpecCharacteristic.getName());
        assertEquals("c271dcf1-d823-4458-99dc-7fd80ba93a06", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OnBoardingStatus *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(16);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OnBoardingStatus", serviceSpecCharacteristic.getName());
        assertEquals("ONBOARDED", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic OperationalStatus *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(17);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("OperationalStatus", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic PackageLocation *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(18);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("PackageLocation", serviceSpecCharacteristic.getName());
        assertEquals("http://10.255.28.246/osapi/packages/da63085b-12f9-48a7-b4bb-e7039b128bc3/tutorial_ns.tar.gz", value);
        assertEquals("PackageLocation", alias);

        /********************* Assert Service Specification Characteristic PackagingFormat *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(19);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("PackagingFormat", serviceSpecCharacteristic.getName());
        assertEquals("OSMvTHIRTEEN", value);
        assertEquals("PackagingFormat", alias);

        /********************* Assert Service Specification Characteristic Primitives *********************/
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(20);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("PrimitivesList", serviceSpecCharacteristic.getName());
//        assertEquals("{\"vnfs\":[{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vnf1\",\"vdu-configs\":[{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vdu1\"},{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vdu2\"}]},{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vnf2\",\"vdu-configs\":[{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vdu1\"},{\"config-primitive\":[{\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}],\"name\":\"run-touch-command\"}],\"id\":\"vdu2\"}]}]}", value);
        assertEquals("{\"vnfs\":[{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vnf1\",\"vdu-configs\":[{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vdu1\"},{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vdu2\"}]},{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vnf2\",\"vdu-configs\":[{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vdu1\"},{\"config-primitive\":[{\"name\":\"run-touch-command\",\"parameter\":[{\"name\":\"filename\",\"data-type\":\"STRING\"}]}],\"id\":\"vdu2\"}]}]}", value);
        assertEquals("", alias);


        /********************* Assert Service Specification Characteristic SSHKEY *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(21);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("SSHKEY", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic SSPEC_GRAPH_NOTATION *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(22);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("SSPEC_GRAPH_NOTATION", serviceSpecCharacteristic.getName());
        //assertEquals("blockdiag {default_textcolor = white;\r\ndefault_fontsize = 12;\r\n\r\n\"null\" -> \"7690ef08-e94f-4c9f-923d-e8a3ad125908\";\r\n\"7690ef08-e94f-4c9f-923d-e8a3ad125908\" [ label = \"tutorial_ns\", shape = roundedbox, color = \"#e28743\"]; \"null\" [ label = \"tutorial_ns@osm14\", color = \"#2596be\"]; }", value);
        assertEquals("SSPEC_GRAPH_NOTATION", alias);

        /********************* Assert Service Specification Characteristic Status *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(23);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("Status", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic VNFINDEXREF_INFO_vnf1 *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(24);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("VNFINDEXREF_INFO_vnf1", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic VNFINDEXREF_INFO_vnf2 *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(25);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("VNFINDEXREF_INFO_vnf2", serviceSpecCharacteristic.getName());
        assertEquals("", value);
        assertEquals("", alias);

        /********************* Assert Service Specification Characteristic Vendor *********************/
        
        serviceSpecCharacteristic = serviceSpecCharacteristicsList.get(26);
        serviceSpecCharacteristicValues = serviceSpecCharacteristic.getServiceSpecCharacteristicValue();
        v = serviceSpecCharacteristicValues.iterator().next().getValue();
        value = v.getValue();
        alias = v.getAlias();

        assertEquals("Vendor", serviceSpecCharacteristic.getName());
        assertEquals(null, value);
        assertEquals("Vendor", alias);
        
    }

}
