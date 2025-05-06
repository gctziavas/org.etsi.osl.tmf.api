package org.etsi.osl.tmf.pcm620.api;

import java.util.ArrayList;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreate;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationUpdate;
import org.etsi.osl.tmf.pcm620.reposervices.ProductOfferingRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductSpecificationRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
//@RefreshScope
@Component
public class ProductSpecificationApiRouteBuilder extends RouteBuilder {

  private static final transient Log logger = LogFactory.getLog(ProductSpecificationApiRouteBuilder.class.getName());

  @Value("${CATALOG_GET_PRODUCTSPEC_BY_ID}")
  private String CATALOG_GET_PRODUCTSPEC_BY_ID = "";
  

  @Value("${CATALOG_ADD_PRODUCTSPEC}")
  private String CATALOG_ADD_PRODUCTSPEC = "";
  

  @Value("${CATALOG_UPD_PRODUCTSPEC}")
  private String CATALOG_UPD_PRODUCTSPEC = "";
  
  @Value("${CATALOG_UPDADD_PRODUCTSPEC}")
  private String CATALOG_UPDADD_PRODUCTSPEC = "";
  

  @Value("${CATALOG_GET_PRODUCTOFFERING_BY_ID}")
  private String CATALOG_GET_PRODUCTOFFERING_BY_ID = "";


  @Value("${CATALOG_SEARCH_PRODUCTOFFERINGS}")
  private String CATALOG_SEARCH_PRODUCTOFFERINGS = "";
  
  
  
  @Autowired
  ProductSpecificationRepoService productSpecificationRepoService;

  @Autowired
  ProductOfferingRepoService productOfferingRepoService;
  

  @Override
  public void configure() throws Exception {
    from( CATALOG_GET_PRODUCTSPEC_BY_ID )
    .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTSPEC_BY_ID + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .bean( productSpecificationRepoService, "findByUuidEager")
    .marshal().json( JsonLibrary.Jackson, String.class)
    .convertBodyTo( String.class );

    
    from( CATALOG_ADD_PRODUCTSPEC )
    .log(LoggingLevel.INFO, log, CATALOG_ADD_PRODUCTSPEC + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .unmarshal().json( JsonLibrary.Jackson, ProductSpecificationCreate .class, true)
    .bean( productSpecificationRepoService, "addProductSpecification(${body})")
    .marshal().json( JsonLibrary.Jackson)
    .convertBodyTo( String.class );
            
    from( CATALOG_UPD_PRODUCTSPEC )
    .log(LoggingLevel.INFO, log, CATALOG_UPD_PRODUCTSPEC + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .unmarshal().json( JsonLibrary.Jackson, ProductSpecificationUpdate.class, true)
    .bean( productSpecificationRepoService, "updateProductSpecification(${header.serviceSpecId},  ${body} )")
    .marshal().json( JsonLibrary.Jackson)
    .convertBodyTo( String.class );

    from( CATALOG_UPDADD_PRODUCTSPEC )
    .log(LoggingLevel.INFO, log, CATALOG_UPD_PRODUCTSPEC + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .unmarshal().json( JsonLibrary.Jackson, ProductSpecificationUpdate.class, true)
    .bean( productSpecificationRepoService, "updateOrAddProductSpecification(${header.serviceSpecId}, ${header.forceId}, ${body} )")
    .marshal().json( JsonLibrary.Jackson)
    .convertBodyTo( String.class );
    

    from( CATALOG_GET_PRODUCTOFFERING_BY_ID )
    .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTOFFERING_BY_ID + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .bean( productOfferingRepoService, "findByUuidEager")
    .marshal().json( JsonLibrary.Jackson, String.class)
    .convertBodyTo( String.class );
    


    from( CATALOG_SEARCH_PRODUCTOFFERINGS )
    .log(LoggingLevel.INFO, log, CATALOG_SEARCH_PRODUCTOFFERINGS + " message received!")
    .to("log:DEBUG?showBody=true&showHeaders=true")
    .unmarshal().json( JsonLibrary.Jackson, ArrayList.class, true)
    .bean( productOfferingRepoService, "searchProductOfferings( ${body} )");
    
    
  }
}
