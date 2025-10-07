package org.etsi.osl.services.api.ri639;


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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.services.api.ResourceInventoryIntegrationTest;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.Any;
import org.etsi.osl.tmf.common.model.UserPartRoleType;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.common.model.service.ResourceRef;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.rcm634.model.LogicalResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.PhysicalResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.PhysicalResourceSpecificationUpdate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecification;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationCreate;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationRef;
import org.etsi.osl.tmf.rcm634.model.ResourceSpecificationUpdate;
import org.etsi.osl.tmf.ri639.model.Characteristic;
import org.etsi.osl.tmf.ri639.model.LogicalResource;
import org.etsi.osl.tmf.ri639.model.Resource;
import org.etsi.osl.tmf.ri639.model.ResourceCreate;
import org.etsi.osl.tmf.ri639.model.ResourceRelationship;
import org.etsi.osl.tmf.ri639.model.ResourceStatusType;
import org.etsi.osl.tmf.ri639.reposervices.ResourceRepoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public class ResourceRepoServiceTest  extends BaseIT{

    private static final transient Log logger = LogFactory.getLog( ResourceInventoryIntegrationTest.class.getName());

    @Autowired
    private MockMvc mvc;

    @Autowired
    ResourceRepoService resourceRepoService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAll() throws Exception {

        // Test with not null fields and params
        String fields = "resourceVersion, usageState";
        Map<String, String> params = new HashMap<>();
        params.put("category", "Non existing category");

        List resourceList = resourceRepoService.findAll(fields, params);
        assertThat(resourceList.size()).isEqualTo(resourceRepoService.findAll().size());

    }


//    // This test causes "HikariPool-1 - Connection is not available, request timed out after 30000ms" error for other tests
//    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
//    @Test
//    public void testUpdateResource() throws Exception {

        // Test with non-existing resource
//        String nonExistingResourceId = "nonExistingId";
//        ResourceUpdate ru = new ResourceUpdate();
//
//        Resource nullResource = resourceRepoService.updateResource(nonExistingResourceId, ru, false);
//        assertThat(nullResource).isEqualTo(null);

        // Test with existing resource
//        String response = createResource();
//
//        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
//        String id = responsesResource.getId();
//
//        ResourceUpdate resourceUpdate = new ResourceUpdate();
//        resourceUpdate.setAtType("New atType");
//        resourceUpdate.setName("New name");
//        resourceUpdate.setCategory("New category");
//        resourceUpdate.setDescription("New description");
//        resourceUpdate.setStartOperatingDate(OffsetDateTime.now(ZoneOffset.UTC ).toString());
//        resourceUpdate.setUsageState(responsesResource.getUsageState());
//        resourceUpdate.setResourceStatus(ResourceStatusType.ALARM);
//        resourceUpdate.setResourceVersion("New version");
//        resourceUpdate.setAdministrativeState(responsesResource.getAdministrativeState());
//        resourceUpdate.setResourceSpecification(responsesResource.getResourceSpecification());
//        resourceUpdate.setPlace(new RelatedPlaceRefOrValue());
//
//        RelatedParty relatedParty = new RelatedParty();
//        relatedParty.setName("Related Party");
//        List<RelatedParty> relatedPartyList = new ArrayList<>();
//        relatedPartyList.add(relatedParty);
//
//        resourceUpdate.setRelatedParty(relatedPartyList);
//
//        Resource updatedResource = resourceRepoService.updateResource(id, resourceUpdate, false);
//
//        assertThat(updatedResource.getName()).isEqualTo("New name");
//        assertThat(updatedResource.getCategory()).isEqualTo("New category");
//        assertThat(updatedResource.getDescription()).isEqualTo("New description");
//        assertThat(updatedResource.getResourceVersion()).isEqualTo("New version");
//        assertThat(updatedResource.getResourceStatus()).isEqualTo(ResourceStatusType.ALARM);
//    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAllActiveResourcesToTerminate() throws Exception {

        String response = createResource();

        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
        String id = responsesResource.getId();

        List<String> resourcesList = resourceRepoService.findAllActiveResourcesToTerminate();
        assertThat(resourcesList).contains(id);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAllActiveAndReservedResourcesOfPartners() throws Exception {

        String response = createResource();

        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
        String id = responsesResource.getId();

        List<String> resourcesList = resourceRepoService.findAllActiveAndReservedResourcesOfPartners();
        assertThat(resourcesList).contains(id);
    }

//    // This test causes "HikariPool-1 - Connection is not available, request timed out after 30000ms" error for other tests
//    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
//    @Test
//    public void testAddOrUpdateResourceByNameCategoryVersion() throws Exception {
//
//        String response = createResource();
//
//        Resource responsesResource = JsonUtils.toJsonObj(response,  LogicalResource.class);
//        String name = responsesResource.getName();
//        String category = responsesResource.getCategory();
//        String version = responsesResource.getResourceVersion();
//
//        ResourceCreate aResource = new ResourceCreate();
//        aResource.setName("A new name");
//        aResource.setCategory("A new category");
//
//        Resource response2 = resourceRepoService.addOrUpdateResourceByNameCategoryVersion(name, category, version, aResource);
//        assertThat(response2.getName()).isEqualTo("A new name");
//        assertThat(response2.getCategory()).isEqualTo("A new category");
//    }


    private String createResource() throws Exception{

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
        aResource.setResourceStatus(ResourceStatusType.AVAILABLE);



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
        //create a first resoruce that will be added to the next one as ref
        String responseResource = mvc.perform(MockMvcRequestBuilders.post("/resourceInventoryManagement/v4/resource")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( aResource ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        logger.info("responseResource = " + responseResource);
        Resource responseRsc = JsonUtils.toJsonObj( responseResource,  LogicalResource.class);


        aResource.setName("aNew Resource");

        ResourceRelationship rri = new ResourceRelationship();
        rri.setRelationshipType("PARENT");
        ResourceRef rrref = new ResourceRef();
        rrref.setName( responseRsc.getName() );
        rrref.setId( responseRsc.getId() );
        rri.setResource( rrref );
        aResource.addResourceRelationshipItem( rri  );

        logger.info("aService JSON = " + JsonUtils.toJsonString( aResource ));

        responseResource = mvc.perform(MockMvcRequestBuilders.post("/resourceInventoryManagement/v4/resource")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( aResource ) ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        logger.info("responseResource = " + responseResource);
        responseRsc = JsonUtils.toJsonObj( responseResource,  LogicalResource.class);


        logger.info("testService = " + JsonUtils.toJsonString( responseRsc ));


        assertThat( responseRsc.getCategory()  ).isEqualTo( "Experimentation" );
        assertThat( responseRsc.getDescription()  ).isEqualTo( "Experimentation Descr" );
        assertThat( responseRsc.getStartOperatingDate() ).isNotNull();
        assertThat( responseRsc.getEndOperatingDate() ).isNotNull();
        assertThat( responseRsc.getResourceCharacteristic().size()  ).isEqualTo( 1 );
        assertThat( responseRsc.getResourceCharacteristicByName( "externalPartnerServiceId" ) ).isNotNull();
        assertThat( responseRsc.getResourceCharacteristicByName( "externalPartnerServiceId" ).getValue().getValue()  ).isEqualTo( "NONE" )  ;
        assertThat( responseRsc.getResourceSpecification().getId()  ).isNotNull();
        assertThat( responseRsc.getResourceSpecification().getName()  ).isNotNull();
        assertThat( responseRsc.getResourceRelationship().size()  ).isEqualTo( 1 );


        assertThat( responseRsc.getNote().size()  ).isEqualTo( 2 );

        boolean userPartyRoleexists = false;
        for (RelatedParty r : responseRsc.getRelatedParty()) {
            if ( r.getName().equals( "osadmin" ) && r.getRole().equals( UserPartRoleType.REQUESTER.toString() )) {
                userPartyRoleexists = true;
            }
        }

        assertThat(userPartyRoleexists  ).isTrue() ;


        return responseResource;
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
