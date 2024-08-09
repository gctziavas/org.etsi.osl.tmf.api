package org.etsi.osl.tmf.po622.api;

import java.util.Map;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.po622.model.ProductOrderCreate;
import org.etsi.osl.tmf.po622.model.ProductOrderUpdate;
import org.etsi.osl.tmf.po622.reposervices.ProductOrderRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
//@RefreshScope
@Component
public class ProductOrderApiRouteBuilder  extends RouteBuilder {

  private static final transient Log logger = LogFactory.getLog(ProductOrderApiRouteBuilder.class.getName());
  
  @Value("${CATALOG_GET_PRODUCTORDER_BY_ID}")
  private String CATALOG_GET_PRODUCTORDER_BY_ID = "";

  @Value("${CATALOG_GET_PRODUCTORDERS}")
  private String CATALOG_GET_PRODUCTORDERS = "";

  @Value("${CATALOG_GET_INITIAL_PRODUCTORDERS_IDS}")
  private String CATALOG_GET_INITIAL_PRODUCTORDERS_IDS = "";

  @Value("${CATALOG_GET_PRODUCTORDER_IDS_BY_STATE}")
  private String CATALOG_GET_PRODUCTORDER_IDS_BY_STATE = "";

  @Value("${CATALOG_UPD_PRODUCTORDER_BY_ID}")
  private String CATALOG_UPD_PRODUCTORDER_BY_ID = "";

  @Value("${CATALOG_ADD_PRODUCTORDER}")
  private String CATALOG_ADD_PRODUCTORDER = "";  


  @Value("${GET_USER_BY_USERNAME}")
  private String GET_USER_BY_USERNAME = "";

  @Value("${spring.application.name}")
  private String compname;
  
  @Autowired
  private ProducerTemplate template;

  @Autowired
  ProductOrderRepoService productOrderRepoService;

  @Autowired
  private CentralLogger centralLogger;
  
  
  @Override
  public void configure() throws Exception {
    from(CATALOG_GET_PRODUCTORDERS).log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTORDERS + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .bean(productOrderRepoService, "findAllParamsJsonOrderIDs").convertBodyTo(String.class);


from(CATALOG_GET_INITIAL_PRODUCTORDERS_IDS)
    .log(LoggingLevel.INFO, log, CATALOG_GET_INITIAL_PRODUCTORDERS_IDS + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true").setBody(constant("{\"state\":\"INITIAL\"}")).unmarshal()
    .json(JsonLibrary.Jackson, Map.class, true).bean(productOrderRepoService, "findAllParamsJsonOrderIDs")
    .convertBodyTo(String.class);

from(CATALOG_GET_PRODUCTORDER_IDS_BY_STATE)
    .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTORDER_IDS_BY_STATE + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true").setBody(simple("{\"state\":\"${header.orderstate}\"}"))
    .unmarshal().json(JsonLibrary.Jackson, Map.class, true)
    .bean(productOrderRepoService, "findAllParamsJsonOrderIDs").convertBodyTo(String.class);

from(CATALOG_GET_PRODUCTORDER_BY_ID)
    .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTORDER_BY_ID + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .bean(productOrderRepoService, "getProductOrderEagerAsString").convertBodyTo(String.class);

from(CATALOG_UPD_PRODUCTORDER_BY_ID)
    .log(LoggingLevel.INFO, log, CATALOG_UPD_PRODUCTORDER_BY_ID + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true").unmarshal()
    .json(JsonLibrary.Jackson, ProductOrderUpdate.class, true)
    .bean(productOrderRepoService, "updateProductOrder(${header.orderid}, ${body})");


from(CATALOG_ADD_PRODUCTORDER)
    .log(LoggingLevel.INFO, log, CATALOG_ADD_PRODUCTORDER + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true").unmarshal()
    .json(JsonLibrary.Jackson, ProductOrderCreate.class, true)
    .bean(productOrderRepoService, "addProductOrderReturnEager(${body})")
    .convertBodyTo(String.class); //creates back a response
  }
  
  
}
