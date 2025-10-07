package org.etsi.osl.services.api.so641;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.services.api.BaseIT;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.common.model.service.ServiceSpecificationRef;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrder;
import org.etsi.osl.tmf.so641.model.ServiceOrderCreate;
import org.etsi.osl.tmf.so641.model.ServiceOrderItem;
import org.etsi.osl.tmf.so641.model.ServiceOrderRelationship;
import org.etsi.osl.tmf.so641.model.ServiceOrderStateType;
import org.etsi.osl.tmf.so641.model.ServiceOrderUpdate;
import org.etsi.osl.tmf.so641.model.ServiceRestriction;
import org.etsi.osl.tmf.so641.reposervices.ServiceOrderRepoService;
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


public class ServiceOrderRepoServiceTest  extends BaseIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    ServiceOrderRepoService serviceOrderRepoService;

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
    public void testFindAllParams() throws Exception {

        String response = createServiceOrder();

        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();
        String state = responsesServiceOrder.getState().toString();

        // Test with not null params
        Map<String, String> params = new HashMap<>();
        params.put("state", state);

        List<ServiceOrder> serviceOrderList = serviceOrderRepoService.findAllParams(params);

        boolean idExists = false;
        for (ServiceOrder so : serviceOrderList) {
            if ( so.getId().equals( soId ) ) {
                idExists= true;
            }
        }
        assertThat( idExists ).isTrue();

        // Test with null params
        Map<String, String> paramsEmpty = new HashMap<>();
        List<ServiceOrder> serviceOrderListEmptyParams = serviceOrderRepoService.findAllParams(paramsEmpty);

        boolean idExistsEmptyParams = false;
        for (ServiceOrder so : serviceOrderListEmptyParams) {
            if ( so.getId().equals( soId ) ) {
                idExistsEmptyParams= true;
            }
        }
        assertThat( idExistsEmptyParams ).isTrue();
        assertThat(serviceOrderListEmptyParams.size()).isEqualTo(serviceOrderRepoService.findAll().size());
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testFindAllParamsJsonOrderIDs() throws Exception {

        String response = createServiceOrder();
        String response2 = createServiceOrder();

        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();

        ServiceOrder responsesServiceOrder2 = JsonUtils.toJsonObj(response2,  ServiceOrder.class);
        String soId2 = responsesServiceOrder2.getId();

        String state = responsesServiceOrder.getState().toString();
        Map<String, String> params = new HashMap<>();
        params.put("state", state);

        String soIds = serviceOrderRepoService.findAllParamsJsonOrderIDs(params);
        assertThat(soIds).contains(soId);
        assertThat(soIds).contains(soId2);
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testUpdateServiceOrder() throws Exception {

        String response = createServiceOrder();
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();

        ServiceOrderUpdate servOrderUpd = new ServiceOrderUpdate();
        servOrderUpd.setExpectedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        Note en = new Note();
        en.text("test note2");
        servOrderUpd.addNoteItem(en);
        servOrderUpd.addOrderItemItem((new ArrayList<>(responsesServiceOrder.getOrderItem())).get(0));
        servOrderUpd.getOrderItem().get(0).setState(ServiceOrderStateType.INPROGRESS);
        servOrderUpd.setState(ServiceOrderStateType.COMPLETED);
        servOrderUpd.setCategory("New Test Category");
        servOrderUpd.setDescription("New Test Description");
        servOrderUpd.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        servOrderUpd.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
        servOrderUpd.setStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());

        List<ServiceOrderRelationship> serviceOrderRelationshipList = new ArrayList<>();
        serviceOrderRelationshipList.add(new ServiceOrderRelationship());
        servOrderUpd.setOrderRelationship(serviceOrderRelationshipList);

        ServiceOrder responseSOUpd = serviceOrderRepoService.updateServiceOrder(soId, servOrderUpd);
        assertThat(responseSOUpd.getState().toString()).isEqualTo("COMPLETED");
        assertThat(responseSOUpd.getDescription()).isEqualTo("New Test Description");
    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetServiceOrderEagerAsString() throws Exception {

        String response = createServiceOrder();
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();

        String eager = serviceOrderRepoService.getServiceOrderEagerAsString(soId);
        ServiceOrder eagerServiceOrder = JsonUtils.toJsonObj(eager,  ServiceOrder.class);
        assertThat(eagerServiceOrder.getDescription()).isEqualTo("A Test Service Order");
        assertThat(eagerServiceOrder.getCategory()).isEqualTo("Test Category");
        assertThat(eagerServiceOrder.getId()).isEqualTo(soId);
    }


//    // This test causes "HikariPool-1 - Connection is not available, request timed out after 30000ms" error for other tests
//    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
//    @Test
//    public void testAddServiceOrderReturnEager() throws Exception {
//
//        ServiceOrderCreate serviceOrder = new ServiceOrderCreate();
//        serviceOrder.setCategory("Test Category");
//        serviceOrder.setDescription("A Test Service Order");
//        serviceOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
//        serviceOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
//
//        Note noteItem = new Note();
//        noteItem.text("Test note");
//        serviceOrder.addNoteItem(noteItem);
//
//        ServiceOrderItem soi = new ServiceOrderItem();
//        serviceOrder.getOrderItem().add(soi);
//        soi.setState(ServiceOrderStateType.ACKNOWLEDGED);
//
//
//        String eager = serviceOrderRepoService.addServiceOrderReturnEager(serviceOrder);
//        ServiceOrder eagerServiceOrder = JsonUtils.toJsonObj(eager,  ServiceOrder.class);
//        assertThat(eagerServiceOrder.getDescription()).isEqualTo("A Test Service Order");
//        assertThat(eagerServiceOrder.getCategory()).isEqualTo("Test Category");
//    }


    @WithMockUser(username="osadmin", roles = {"ADMIN","USER"})
    @Test
    public void testGetImageServiceOrderItemRelationshipGraph() throws Exception {

        String response = createServiceOrder();
        ServiceOrder responsesServiceOrder = JsonUtils.toJsonObj(response,  ServiceOrder.class);
        String soId = responsesServiceOrder.getId();
        Set<ServiceOrderItem> serviceOrderItemSet = responsesServiceOrder.getOrderItem();

        for (ServiceOrderItem soi: serviceOrderItemSet) {
            String responseGraph = serviceOrderRepoService.getImageServiceOrderItemRelationshipGraph(soId, soi.getId());
            assertThat( responseGraph ).isNotNull();
        }
    }


    private String createServiceOrder() throws Exception {

        int currSize = serviceOrderRepoService.findAll().size();

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
        soi.setService(serviceRestriction);

        String response = mvc
                .perform(MockMvcRequestBuilders.post("/serviceOrdering/v4/serviceOrder")
                        .with( SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(serviceOrder)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ServiceOrder responseSO = JsonUtils.toJsonObj(response, ServiceOrder.class);

        assertThat( serviceOrderRepoService.findAll().size() ).isEqualTo( currSize + 1 );
        assertThat(responseSO.getCategory()).isEqualTo("Test Category");
        assertThat(responseSO.getDescription()).isEqualTo("A Test Service Order");

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
}
