/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 - 2020 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.etsi.osl.tmf.pcm620.api;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCatalogRepoService;
import org.etsi.osl.tmf.pcm620.reposervices.ProductCategoryRepoService;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.etsi.osl.tmf.scm633.reposervices.CatalogRepoService;
import org.etsi.osl.tmf.scm633.reposervices.CategoryRepoService;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
//@RefreshScope
@Component
public class ProductCatalogApiRouteBuilder extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog(ProductCatalogApiRouteBuilder.class.getName());

	

    @Value("${CATALOG_GET_PRODUCTCATALOGS}")
    private String CATALOG_GET_PRODUCTCATALOGS = "";    

    @Value("${CATALOG_GET_PRODUCTCATALOG_BY_ID}")
    private String CATALOG_GET_PRODUCTCATALOG_BY_ID = "";
    
    @Value("${CATALOG_GET_PRODUCTCATALOG_BY_NAME}")
    private String CATALOG_GET_PRODUCTCATALOG_BY_NAME = "";
    
    @Value("${CATALOG_GET_PRODUCTCATEGORIES}")
    private String CATALOG_GET_PRODUCTCATEGORIES = "";
    
    @Value("${CATALOG_GET_PRODUCTCATEGORY_BY_ID}")
    private String CATALOG_GET_PRODUCTCATEGORY_BY_ID = "";
	

    @Value("${CATALOG_GET_PRODUCTOFFERINGS_BYCATEGORY_ID}")
    private String CATALOG_GET_PRODUCTOFFERINGS_BYCATEGORY_ID = "";
	
  
	@Autowired
	ProductCatalogRepoService catalogRepoService;
	

    @Autowired
    ProductCategoryRepoService categoryRepoService;
	
	
	@Override
	public void configure() throws Exception {
		
		from( CATALOG_GET_PRODUCTCATALOG_BY_ID )
		.log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTCATALOG_BY_ID + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.bean( catalogRepoService, "findByUuidEager(${header.catalogId})");
		
		from( CATALOG_GET_PRODUCTCATALOGS )
		.log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTCATALOGS + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")	
		.bean( catalogRepoService, "findAllEager()");
		
	    from( CATALOG_GET_PRODUCTCATALOG_BY_NAME )
	    .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTCATALOG_BY_NAME + " message received!")
	    .to("log:DEBUG?showBody=true&showHeaders=true")
        .bean( catalogRepoService, "findByNameEager(${header.catalogName})")
	    .marshal().json( JsonLibrary.Jackson, String.class)
	    .convertBodyTo( String.class );
	      
	    
        from( CATALOG_GET_PRODUCTCATEGORIES )
        .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTCATEGORIES + " message received!")
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .bean( catalogRepoService, "findAllCategoriesByCatalogName(${header.catalogName})");
          
        
        from( CATALOG_GET_PRODUCTCATEGORY_BY_ID )
        .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTCATEGORY_BY_ID + " message received!")
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .bean( categoryRepoService, "findByIdEager(${header.catalogId})");
        
        
        from( CATALOG_GET_PRODUCTOFFERINGS_BYCATEGORY_ID )
        .log(LoggingLevel.INFO, log, CATALOG_GET_PRODUCTOFFERINGS_BYCATEGORY_ID + " message received!")
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .bean( categoryRepoService, "findAllProductOfferingsByCategId(${header.categoryId})");
	      
	}

	
	

	
	static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue( content, valueType);
    }
	
}
