package org.etsi.osl.services.api.so641;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.service.Place;
import org.etsi.osl.tmf.common.model.service.ServiceSpecificationRef;
import org.etsi.osl.tmf.pm632.model.ContactMedium;
import org.etsi.osl.tmf.pm632.model.Individual;
import org.etsi.osl.tmf.pm632.model.IndividualCreate;
import org.etsi.osl.tmf.pm632.model.MediumCharacteristic;
import org.etsi.osl.tmf.pm632.reposervices.IndividualRepoService;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.etsi.osl.tmf.so641.model.ServiceOrderCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrderItem;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.etsi.osl.tmf.so641.model.ServiceOrderUpdate;
import org.etsi.osl.tmf.so641.model.ServiceRestriction;
import org.etsi.osl.tmf.so641.reposervices.ServiceOrderRepoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceOrderApiControllerTest  extends BaseIT {

    private static final int FIXED_BOOTSTRAPS_SPECS = 0;

    private MockMvc mvc;

	@PersistenceContext
	private EntityManager entityManager;

    @Autowired
    ServiceOrderRepoService serviceOrderRepoService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;
    

    @Autowired
    IndividualRepoService individualRepoService;

    @BeforeAll
    public void setup(WebApplicationContext context) throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

	@AfterEach
	public void tearDown() {
		if (entityManager != null) {
			entityManager.clear();
		}
	}


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateServiceOrder() throws Exception {
      

      assertThat( individualRepoService.findAll().size() ).isEqualTo( 0 );
      
      
      String response = mvc.perform(MockMvcRequestBuilders.get("/party/v4/individual/myuser")
          .with( SecurityMockMvcRequestPostProcessors.csrf())
          .contentType(MediaType.APPLICATION_JSON))             
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))      
          .andExpect(status().isOk())
          .andReturn().getResponse().getContentAsString();
  

        assertThat( individualRepoService.findAll().size() ).isEqualTo( 1 );
  

        Individual responseIndv = JsonUtils.toJsonObj(response,  Individual.class);

        assertThat( responseIndv.getId() ).isNotNull() ;
  
        response = createServiceOrder();

        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        assertThat( responsesServiceOrder.getDescription() ).isEqualTo( "A Test Service Order" );
        assertThat( responsesServiceOrder.getRelatedParty().size() ) .isEqualTo( 1 );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getName() ) .isEqualTo( "osadmin" );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getId() ) .isEqualTo( responseIndv.getId() );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getRole() ) .isEqualTo( "REQUESTER" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testCreateServiceOrderWithNonExistingServiceSpecification() throws Exception {

        createServiceOrderWithNonExistingServiceSpecification();
        assertThat( serviceOrderRepoService.findAll().size() ).isEqualTo( 1 );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testDeleteServiceOrder() throws Exception {

        String response = createServiceOrder();

        assertThat( serviceOrderRepoService.findAll().size() ).isEqualTo( 2 );
        
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String id = responsesServiceOrder.getId();

        mvc.perform(MockMvcRequestBuilders.delete("/serviceOrdering/v4/serviceOrder/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        assertThat( serviceOrderRepoService.findAll().size() ).isEqualTo( 1 );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testListServiceOrder() throws Exception {

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceOrdering/v4/serviceOrder")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        List<ServiceOrder> serviceOrderList= objectMapper.readValue(response2, new TypeReference<List<ServiceOrder>>() {});
        assertThat(serviceOrderList.size()).isEqualTo(serviceOrderRepoService.findAll().size());
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testPatchServiceOrder() throws Exception {

        String response = createServiceOrder();
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();

        ServiceOrderUpdate servOrderUpd = new ServiceOrderUpdate();
        servOrderUpd.setState(ServiceOrderStateType.COMPLETED);
        servOrderUpd.setCategory("New Test Category");
        servOrderUpd.setDescription("New Test Description");

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceOrdering/v4/serviceOrder/" + soId)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( servOrderUpd ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ServiceOrder responsesServiceOrder2 = JsonUtils.toJsonObj(response2,  ServiceOrder.class);
        assertThat(responsesServiceOrder2.getState().toString()).isEqualTo("COMPLETED");
        assertThat(responsesServiceOrder2.getDescription()).isEqualTo("New Test Description");
        assertThat(responsesServiceOrder2.getCategory()).isEqualTo("New Test Category");
        assertThat( responsesServiceOrder.getRelatedParty().size() ) .isEqualTo( 1 );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getName() ) .isEqualTo( "osadmin" );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getId() ) .isEqualTo( individualRepoService.findByUsername("osadmin").getId() );
        assertThat( responsesServiceOrder.getRelatedParty().stream().findFirst().get().getRole() ) .isEqualTo( "REQUESTER" );
    }
    
    

    @WithMockUser(username="osadminmodifier", roles = {"ADMIN","USER"})
    @Test
    public void testPatchServiceOrderModifier() throws Exception {

        String response = createServiceOrder();
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();

        ServiceOrderUpdate servOrderUpd = new ServiceOrderUpdate();
        servOrderUpd.setState(ServiceOrderStateType.COMPLETED);
        servOrderUpd.setCategory("New Test Category1");
        servOrderUpd.setDescription("New Test Description1");
        List<RelatedParty> rpl = new ArrayList<>();
        RelatedParty rp = new RelatedParty();
        rp.setId("12345");
        rpl.add( rp);
        servOrderUpd.setRelatedParty(rpl);

        String response2 = mvc.perform(MockMvcRequestBuilders.patch("/serviceOrdering/v4/serviceOrder/" + soId)
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( JsonUtils.toJson( servOrderUpd ) ))
                .andExpect(status().isOk() )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ServiceOrder responsesServiceOrder2 = JsonUtils.toJsonObj(response2,  ServiceOrder.class);
        assertThat(responsesServiceOrder2.getState().toString()).isEqualTo("COMPLETED");
        assertThat(responsesServiceOrder2.getDescription()).isEqualTo("New Test Description1");
        assertThat(responsesServiceOrder2.getCategory()).isEqualTo("New Test Category1");
        assertThat( responsesServiceOrder2.getRelatedParty().size() ) .isEqualTo( 2 );
    }



    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testRetrieveServiceOrder() throws Exception {

        String response = createServiceOrder();

        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String id = responsesServiceOrder.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceOrdering/v4/serviceOrder/" + id )
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk() )
                .andReturn().getResponse().getContentAsString();

        ServiceOrder responsesServiceOrder2 = JsonUtils.toJsonObj(response2,  ServiceOrder.class);
        assertThat( responsesServiceOrder2.getDescription() ).isEqualTo( "A Test Service Order" );
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetImageServiceOrderItemRelationshipGraph() throws Exception {

        // Create a Service Order
        String response = createServiceOrder();
        ServiceOrder responsesOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String orderId = responsesOrder.getId();

        String soiId = "";
        Set<ServiceOrderItem> serviceOrderItemSet =  responsesOrder.getOrderItem();

        // Find the first element if present
        Optional<ServiceOrderItem> optionalFirstServiceOrderItem = serviceOrderItemSet.stream().findFirst();

        if (optionalFirstServiceOrderItem.isPresent()) {
            soiId = optionalFirstServiceOrderItem.get().getId();
        }

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceOrdering/v4/serviceOrder/" + orderId + "/item/" + soiId + "/relationship_graph")
                )
                .andExpect(status().is(302) )
                .andReturn().getResponse().getRedirectedUrl();

        assertThat( response2 ).contains("/blockdiag/svg/");
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetImageServiceOrderNotesGraph() throws Exception {

        // Create a Service Order
        String response = createServiceOrder();
        ServiceOrder responsesOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String orderId = responsesOrder.getId();

        String response2 = mvc.perform(MockMvcRequestBuilders.get("/serviceOrdering/v4/serviceOrder/" + orderId + "/notes_graph")
                )
                .andExpect(status().is(302) )
                .andReturn().getResponse().getRedirectedUrl();

        assertThat( response2 ).contains("/actdiag/svg/");
    }


    private String createServiceOrder() throws Exception {


        File sspec = new File("src/test/resources/testServiceSpec.json");
        InputStream in = new FileInputStream(sspec);
        String sspectext = IOUtils.toString(in, "UTF-8");

        ServiceSpecificationCreate sspeccr = JsonUtils.toJsonObj(sspectext, ServiceSpecificationCreate.class);
        sspeccr.setName("Spec1");
        ServiceSpecification responsesSpec = createServiceSpec( sspeccr);

        ServiceOrderCreate serviceOrder = new ServiceOrderCreate();
        serviceOrder.setCategory("Test Category");
        serviceOrder.setDescription("A Test Service Order");
        serviceOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        serviceOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
 
        ServiceOrderItem soi = new ServiceOrderItem();
        serviceOrder.getOrderItem().add(soi);
        soi.setState(ServiceOrderStateType.ACKNOWLEDGED);

        ServiceRestriction serviceRestriction = new ServiceRestriction();
        ServiceSpecificationRef aServiceSpecificationRef = new ServiceSpecificationRef();
        aServiceSpecificationRef.setId(responsesSpec.getId());
        aServiceSpecificationRef.setName(responsesSpec.getName());

        serviceRestriction.setServiceSpecification(aServiceSpecificationRef);
        serviceRestriction.setName("aServiceRestriction");
        
        Place pi = new Place();
        pi.setName("palcename");
        pi.setRole("local");
        serviceRestriction.addPlaceItem( pi  );
        Place pi2 = new Place();
        pi2.setName("palcename2");
        pi2.setRole("local");
        serviceRestriction.addPlaceItem( pi2  );
        soi.setService(serviceRestriction);
         
        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceOrdering/v4/serviceOrder")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceOrder)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ServiceOrder responseSO = JsonUtils.toJsonObj(response, ServiceOrder.class);

        assertThat(responseSO.getCategory()).isEqualTo("Test Category");
        assertThat(responseSO.getDescription()).isEqualTo("A Test Service Order");
        assertThat(responseSO.getOrderItem().size() ).isEqualTo( 1 );
        assertThat(responseSO.getOrderItem().stream().findFirst().get().getService() ).isNotNull();
        assertThat(responseSO.getOrderItem().stream().findFirst().get().getService().getPlace().size() ).isEqualTo(2);

        return response;

    }


    private ServiceSpecification createServiceSpec( ServiceSpecificationCreate serviceSpecificationCreate) throws Exception {

        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceCatalogManagement/v4/serviceSpecification")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceSpecificationCreate)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceSpecification responsesSpec = JsonUtils.toJsonObj(response, ServiceSpecification.class);

        return responsesSpec;
    }


    private void createServiceOrderWithNonExistingServiceSpecification() throws Exception {


        ServiceOrderCreate serviceOrder = new ServiceOrderCreate();
        serviceOrder.setCategory("Test Category");
        serviceOrder.setDescription("A Test Service Order");
        serviceOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        serviceOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());

        ServiceOrderItem soi = new ServiceOrderItem();
        serviceOrder.getOrderItem().add(soi);
        soi.setState(ServiceOrderStateType.ACKNOWLEDGED);

        ServiceRestriction serviceRestriction = new ServiceRestriction();
        ServiceSpecificationRef aServiceSpecificationRef = new ServiceSpecificationRef();
        aServiceSpecificationRef.setId("A random non-existing Id");
        aServiceSpecificationRef.setName("A random non-existing name");

        serviceRestriction.setServiceSpecification(aServiceSpecificationRef);
        serviceRestriction.setName("aServiceRestriction");
        soi.setService(serviceRestriction);

        mvc
            .perform(MockMvcRequestBuilders.post("/serviceOrdering/v4/serviceOrder")
                .with( SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceOrder)))
        .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
    }
}