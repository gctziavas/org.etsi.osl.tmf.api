package org.etsi.osl.services.api.po622;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.etsi.osl.tmf.JsonUtils;
import org.etsi.osl.tmf.OpenAPISpringBoot;
import org.etsi.osl.tmf.common.model.service.Note;
import org.etsi.osl.tmf.pcm620.model.ProductOffering;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreate;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingRef;
import org.etsi.osl.tmf.pcm620.model.ProductSpecification;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreate;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationRef;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCatalogRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCategoryRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingPriceRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationRepoService;
import org.etsi.osl.tmf.po622.model.OrderItemActionType;
import org.etsi.osl.tmf.po622.model.ProductOrder;
import org.etsi.osl.tmf.po622.model.ProductOrderCreate;
import org.etsi.osl.tmf.po622.model.ProductOrderItem;
import org.etsi.osl.tmf.po622.model.ProductOrderItemStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderStateType;
import org.etsi.osl.tmf.po622.model.ProductOrderUpdate;
import org.etsi.osl.tmf.po622.reposervices.ProductOrderRepoService;
import org.etsi.osl.tmf.prm669.model.RelatedParty;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import jakarta.validation.Valid;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = OpenAPISpringBoot.class)
@AutoConfigureTestDatabase //this automatically uses h2
@AutoConfigureMockMvc
@ActiveProfiles("testing")
public class ProductOrderRepoServiceTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  ProductOrderRepoService productOrderRepoService;


  @Autowired
  ProductCatalogRepoService catalogRepoService;

  @Autowired
  ProductCategoryRepoService categRepoService;

  @Autowired
  ProductOfferingRepoService productOfferingRepoService;


  @Autowired
  ProductOfferingPriceRepoService productOfferingPriceRepoService;


  @Autowired
  ProductSpecificationRepoService productSpecificationRepoService;


  @Autowired
  private WebApplicationContext context;
  


  @Autowired
  TransactionTemplate txTemplate;

  @Before
  public void setup() throws Exception {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }



  @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
  @Test
  public void testFindAllParams() throws Exception {

    String response = createProductOrder();

    ProductOrder responsesProductOrder = JsonUtils.toJsonObj(response, ProductOrder.class);
    String soId = responsesProductOrder.getId();
    String state = responsesProductOrder.getState().toString();

    // Test with not null params
    Map<String, String> params = new HashMap<>();
    params.put("state", state);

    List<ProductOrder> productOrderList = productOrderRepoService.findAllParams(params);

    boolean idExists = false;
    for (ProductOrder po : productOrderList) {
      if (po.getId().equals(soId)) {
        idExists = true;
      }
    }
    assertThat(idExists).isTrue();

    // Test with null params
    Map<String, String> paramsEmpty = new HashMap<>();
    List<ProductOrder> productOrderListEmptyParams =
        productOrderRepoService.findAllParams(paramsEmpty);

    boolean idExistsEmptyParams = false;
    for (ProductOrder so : productOrderListEmptyParams) {
      if (so.getId().equals(soId)) {
        idExistsEmptyParams = true;
      }
    }
    assertThat(idExistsEmptyParams).isTrue();
    assertThat(productOrderListEmptyParams.size())
        .isEqualTo(productOrderRepoService.findAll().size());
  }


  @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
  @Test
  public void testFindAllParamsJsonOrderIDs() throws Exception {

    String response = createProductOrder();
    String response2 = createProductOrder();

    ProductOrder responsesProductOrder = JsonUtils.toJsonObj(response, ProductOrder.class);
    String soId = responsesProductOrder.getId();

    ProductOrder responsesProductOrder2 = JsonUtils.toJsonObj(response2, ProductOrder.class);
    String soId2 = responsesProductOrder2.getId();

    String state = responsesProductOrder.getState().toString();
    Map<String, String> params = new HashMap<>();
    params.put("state", state);

    String soIds = productOrderRepoService.findAllParamsJsonOrderIDs(params);
    assertThat(soIds).contains(soId);
    assertThat(soIds).contains(soId2);
  }


  @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
  @Test
  public void testUpdateProductOrder() throws Exception {


    assertThat(productOrderRepoService.findAll().size()).isEqualTo(0);
    
    String response = txTemplate.execute(status -> {
      try {
        return createProductOrder();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    });
    
    
    ProductOrder responsesProductOrder = JsonUtils.toJsonObj(response, ProductOrder.class);
    String poId = responsesProductOrder.getId();
    assertThat(productOrderRepoService.findAll().size()).isEqualTo(1);



    ProductOrder responseSOUpd =  txTemplate.execute(status -> {
      
      ProductOrderUpdate prodOrderUpd = new ProductOrderUpdate();
      prodOrderUpd.setExpectedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
      Note en = new Note();
      en.text("test note2");
      prodOrderUpd.addNoteItem(en);
      
      //ProductOrderItem its = responsesProductOrder.getProductOrderItem().stream().findFirst().get();
      ProductOrderItem its = new ProductOrderItem();
      prodOrderUpd.addProductOrderItemItem( its );
      
      prodOrderUpd.getProductOrderItem().get(0).setState(ProductOrderItemStateType.INPROGRESS);
      prodOrderUpd.setState(ProductOrderStateType.COMPLETED);
      prodOrderUpd.setCategory("New Test Category");
      prodOrderUpd.setDescription("New Test Description");
      prodOrderUpd.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
      prodOrderUpd.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
      //return productOrderRepoService.updateProductOrder(poId, prodOrderUpd);
      
      byte[] req;
      try {
        req = JsonUtils.toJson(prodOrderUpd);
        String responseP = mvc
          .perform(MockMvcRequestBuilders.patch("/productOrderingManagement/v4/productOrder/" + poId)
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON).content(req))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        System.out.println(" ====================> " + responseP);
        ProductOrder responsePO = JsonUtils.toJsonObj( responseP, ProductOrder.class);
        return responsePO;
        
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }


      
      return null;
      
      
      
    });
    
    int i = txTemplate.execute(status -> {
      return productOrderRepoService.findAll().size();
    });
    
    
    assertThat(responseSOUpd.getState().toString()).isEqualTo("COMPLETED");
    assertThat(responseSOUpd.getDescription()).isEqualTo("New Test Description");
    
    assertThat(productOrderRepoService.findAll().size()).isEqualTo(1);
    
    assertThat(responseSOUpd.getNote().size()).isEqualTo(3);
    assertThat(responseSOUpd.getRelatedParty().size()).isEqualTo(1);
    assertThat(responseSOUpd.getProductOrderItem().size()).isEqualTo(2);

    ProductOrderUpdate prodOrderUpd = new ProductOrderUpdate();
    Note en = new Note();
    en.text("test note3");
    prodOrderUpd.addNoteItem(en);
    prodOrderUpd.addRelatedPartyItem(new RelatedParty());
    responseSOUpd = productOrderRepoService.updateProductOrder(poId, prodOrderUpd);
    assertThat(productOrderRepoService.findAll().size()).isEqualTo(1);
    assertThat(responseSOUpd.getNote().size()).isEqualTo(4);
    assertThat(responseSOUpd.getRelatedParty().size()).isEqualTo(2);


  }


  @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
  @Test
  public void testGetProductOrderEagerAsString() throws Exception {

    String response = createProductOrder();
    ProductOrder responsesProductOrder = JsonUtils.toJsonObj(response, ProductOrder.class);
    String soId = responsesProductOrder.getId();

    String eager = productOrderRepoService.getProductOrderEagerAsString(soId);
    ProductOrder eagerProductOrder = JsonUtils.toJsonObj(eager, ProductOrder.class);
    assertThat(eagerProductOrder.getDescription()).isEqualTo("A Test Product Order");
    assertThat(eagerProductOrder.getCategory()).isEqualTo("Test Category");
    assertThat(eagerProductOrder.getId()).isEqualTo(soId);
  }



  @WithMockUser(username = "osadmin", roles = {"ADMIN", "USER"})
  @Test
  public void testGetImageProductOrderItemRelationshipGraph() throws Exception {

    String response = createProductOrder();
    ProductOrder responsesProductOrder = JsonUtils.toJsonObj(response, ProductOrder.class);
    String soId = responsesProductOrder.getId();
    Set<ProductOrderItem> productOrderItemSet = responsesProductOrder.getProductOrderItem();

    for (ProductOrderItem soi : productOrderItemSet) {
      String responseGraph =
          productOrderRepoService.getImageProductOrderItemRelationshipGraph(soId, soi.getId());
      assertThat(responseGraph).isNotNull();
    }
  }


  private String createProductOrder() throws Exception {

    int currSize = productOrderRepoService.findAll().size();

    File sspec = new File("src/test/resources/testProductSpec.json");
    InputStream in = new FileInputStream(sspec);
    String sspectext = IOUtils.toString(in, "UTF-8");

    ProductSpecificationCreate psc =
        JsonUtils.toJsonObj(sspectext, ProductSpecificationCreate.class);
    psc.setVersion("1.1");
    ProductSpecification responseProdSpec = createProductSpec(psc);


    assertThat(productSpecificationRepoService.findAll().size()).isEqualTo(currSize + 1);



    ProductSpecificationRef prodSpecRef = new ProductSpecificationRef();
    prodSpecRef.setId(responseProdSpec.getId());
    @Valid
    ProductOfferingCreate pefCre = new ProductOfferingCreate();
    pefCre.productSpecification(prodSpecRef);


    ProductOffering pOffer = productOfferingRepoService.addProductOffering(pefCre);

    ProductOfferingRef prodOffRef = new ProductOfferingRef();
    prodOffRef.setId(pOffer.getId());



    ProductOrderCreate productOrder = new ProductOrderCreate();
    productOrder.setCategory("Test Category");
    productOrder.setDescription("A Test Product Order");
    productOrder.setRequestedStartDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
    productOrder.setRequestedCompletionDate(OffsetDateTime.now(ZoneOffset.UTC).toString());



    ProductOrderItem poi = new ProductOrderItem();
    poi.action(OrderItemActionType.ADD).productOffering(prodOffRef);

    productOrder.getProductOrderItem().add(poi);


    byte[] req = JsonUtils.toJson(productOrder);
    String response = mvc
        .perform(MockMvcRequestBuilders.post("/productOrderingManagement/v4/productOrder")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(req))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


    System.out.println(" ====================> " + response);
    ProductOrder responsePO = JsonUtils.toJsonObj(response, ProductOrder.class);

    assertThat(productOrderRepoService.findAll().size()).isEqualTo(currSize + 1);
    assertThat(responsePO.getId()).isNotNull();
    assertThat(responsePO.getCategory()).isEqualTo("Test Category");
    assertThat(responsePO.getDescription()).isEqualTo("A Test Product Order");
    assertThat(responsePO.getRelatedParty().size()).isEqualTo(1);

    assertThat(responsePO.getRelatedParty().stream().findFirst().get().getName())
        .isEqualTo("osadmin");

    return response;

  }


  private ProductSpecification createProductSpec(ProductSpecificationCreate prodSpecificationCreate)
      throws Exception {

    String response = mvc
        .perform(MockMvcRequestBuilders.post("/productCatalogManagement/v4/productSpecification")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(JsonUtils.toJson(prodSpecificationCreate)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    ProductSpecification responsesSpec = JsonUtils.toJsonObj(response, ProductSpecification.class);

    return responsesSpec;
  }
}
