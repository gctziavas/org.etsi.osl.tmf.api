package org.etsi.osl.services.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.metrics.api.ResourceMetricsApiController;
import org.etsi.osl.tmf.rcm634.model.LogicalResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.PhysicalResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.PhysicalResourceSpecificationUpdate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCreate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRef;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationUpdate;
import org.etsi.osl.tmf.ri639.model.Characteristic;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.etsi.osl.tmf.ri639.reposervices.ResourceRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.jayway.jsonpath.JsonPath;

public class ResourceMetricsApiControllerTest  extends BaseIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    ResourceRepoService resourceRepoService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCountTotalResources() throws Exception {
        createResource(ResourceStatusType.AVAILABLE);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalResources" )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalResources = JsonPath.read(response, "$.totalResources");


        assertThat(totalResources).isEqualTo(resourceRepoService.findAll().size());
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCountTotalServicesWithState() throws Exception {
        createResource(ResourceStatusType.AVAILABLE);
        createResource(ResourceStatusType.STANDBY);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/totalResources" )
                        .param("state", "STANDBY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        int totalResources = JsonPath.read(response, "$.totalResources");


        List<Resource> resourcesList = resourceRepoService.findAll();
        int standByResources = (int) resourcesList.stream().filter(resource -> resource.getResourceStatus() == ResourceStatusType.STANDBY).count();

        assertThat(totalResources).isEqualTo(standByResources);
        assertThat(totalResources).isEqualTo(1);
    }

    @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
    @Test
    public void testGetServicesGroupedByState() throws Exception {
        String startTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        createResource(ResourceStatusType.AVAILABLE);
        createResource(ResourceStatusType.AVAILABLE);
        createResource(ResourceStatusType.AVAILABLE);
        createResource(ResourceStatusType.RESERVED);
        createResource(ResourceStatusType.RESERVED);
        createResource(ResourceStatusType.STANDBY);

        String endTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String response = mvc.perform(MockMvcRequestBuilders.get("/metrics/resourcesGroupByState")
                        .param("starttime", startTime)
                        .param("endtime", endTime)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> groupByState = JsonPath.read(response, "$.resources.aggregations.groupByState");

        // Create a map from key -> count
        Map<String, Integer> stateCounts = groupByState.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.get("key"),
                        entry -> (Integer) entry.get("count")
                ));

        assertThat(stateCounts.get("AVAILABLE")).isEqualTo(3);
        assertThat(stateCounts.get("RESERVED")).isEqualTo(2);
        assertThat(stateCounts.get("STANDBY")).isEqualTo(1);
        assertThat(stateCounts.get("ALARM")).isEqualTo(0);
        assertThat(stateCounts.get("UNKNOWN")).isEqualTo(0);
        assertThat(stateCounts.get("SUSPENDED")).isEqualTo(0);
    }

    private void createResource(ResourceStatusType statusType) throws Exception{

        /**
         * first add 2 specs
         */

        File sspec = new File( "src/test/resources/testResourceSpec.json" );
        InputStream in = new FileInputStream( sspec );
        String sspectext = IOUtils.toString(in, "UTF-8");


        ResourceSpecificationCreate sspeccr1 = JsonUtils.toJsonObj( sspectext,  ResourceSpecificationCreate.class);
        sspeccr1.setName("Spec1");
        ResourceSpecification responsesSpec1 = createResourceSpec( sspeccr1);

        //res 2 is an RFS
        ResourceSpecificationCreate sspeccr2 = JsonUtils.toJsonObj( sspectext,  ResourceSpecificationCreate.class);
        sspeccr2.setName("Spec2");

        sspeccr2.addResourceSpecificationRelationshipWith( responsesSpec1 );
        LogicalResourceSpecification responsesSpec2 = (LogicalResourceSpecification) createResourceSpec( sspeccr2 );
        /**
         * add them as bundle
         */

        ResourceSpecificationCreate sspeccr3 = JsonUtils.toJsonObj( sspectext,  ResourceSpecificationCreate.class);
        sspeccr3.setName("BundleExampleSpec");
        sspeccr3.isBundle(true);
        sspeccr3.addResourceSpecificationRelationshipWith( responsesSpec1 );
        sspeccr3.addResourceSpecificationRelationshipWith( responsesSpec2 );
        ResourceSpecification responsesSpec3 = createResourceSpec( sspeccr3);

        ResourceCreate aResource = new ResourceCreate();
        aResource.setName("aNew Resource parent");
        aResource.setCategory("Experimentation");
        aResource.setDescription("Experimentation Descr");
        aResource.setStartOperatingDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aResource.setEndOperatingDate( OffsetDateTime.now(ZoneOffset.UTC ).toString() );
        aResource.setResourceStatus(statusType);



        Note noteItem = new Note();
        noteItem.text("test note");
        aResource.addNoteItem(noteItem);

        Characteristic resCharacteristicItem = new Characteristic();

        resCharacteristicItem.setName( "externalPartnerServiceId" );
        resCharacteristicItem.setValue( new Any("NONE"));
        aResource.addResourceCharacteristicItem(resCharacteristicItem);

        ResourceSpecificationRef aServiceSpecificationRef = new ResourceSpecificationRef();
        aServiceSpecificationRef.setId(responsesSpec3.getId() );
        aServiceSpecificationRef.setName(responsesSpec3.getName());

        aResource.setResourceSpecification( aServiceSpecificationRef );

        resourceRepoService.addResource(aResource);
    }


    private ResourceSpecification createResourceSpec(ResourceSpecificationUpdate sspeccr1) throws Exception{

        URI url = new URI("/resourceCatalogManagement/v4/resourceSpecification");
        if (sspeccr1 instanceof PhysicalResourceSpecificationUpdate ) {
            url = new URI("/resourceCatalogManagement/v4/resourceSpecification");
        }
        String responseSpec = mvc.perform(MockMvcRequestBuilders.post( url  )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( sspeccr1 ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ResourceSpecification responsesSpec1;
        if (sspeccr1 instanceof PhysicalResourceSpecificationUpdate ) {
            responsesSpec1 = JsonUtils.toJsonObj(responseSpec,  PhysicalResourceSpecification.class);
        }else {
            responsesSpec1 = JsonUtils.toJsonObj(responseSpec,  LogicalResourceSpecification.class);
        }

        return responsesSpec1;
    }
}
