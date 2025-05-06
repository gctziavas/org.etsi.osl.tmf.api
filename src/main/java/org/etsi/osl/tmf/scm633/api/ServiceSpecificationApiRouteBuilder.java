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
package org.etsi.osl.tmf.scm633.api;

import java.io.IOException;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.tmf.scm633.model.ServiceSpecification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreate;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationUpdate;
import org.etsi.osl.tmf.scm633.reposervices.ServiceSpecificationRepoService;
import org.etsi.osl.tmf.sim638.model.ServiceCreate;
import org.etsi.osl.tmf.sim638.model.ServiceUpdate;
import org.etsi.osl.tmf.so641.api.ServiceOrderApiRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
//@RefreshScope
@Component
public class ServiceSpecificationApiRouteBuilder extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog(ServiceSpecificationApiRouteBuilder.class.getName());

	@Value("${CATALOG_GET_SERVICESPEC_BY_ID}")
	private String CATALOG_GET_SERVICESPEC_BY_ID = "";
	

	@Value("${CATALOG_ADD_SERVICESPEC}")
	private String CATALOG_ADD_SERVICESPEC = "";
	

	@Value("${CATALOG_UPD_SERVICESPEC}")
	private String CATALOG_UPD_SERVICESPEC = "";
	
	@Value("${CATALOG_UPDADD_SERVICESPEC}")
	private String CATALOG_UPDADD_SERVICESPEC = "";
	
	@Value("${NFV_CATALOG_GET_NSD_BY_ID}")
	private String NFV_CATALOG_GET_NSD_BY_ID = "";
	
	@Value("${CATALOG_UPD_EXTERNAL_SERVICESPEC}")
	private String CATALOG_UPD_EXTERNAL_SERVICESPEC = "";


    @Value("${CATALOG_SEARCH_SERVICESPECREFS}")
    private String CATALOG_SEARCH_SERVICESPECREFS = "";


	    
	    
	@Autowired
	ServiceSpecificationRepoService serviceSpecificationRepoService;
	
	
	@Override
	public void configure() throws Exception {
		
		from( CATALOG_GET_SERVICESPEC_BY_ID )
		.log(LoggingLevel.INFO, log, CATALOG_GET_SERVICESPEC_BY_ID + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.bean( serviceSpecificationRepoService, "findByUuidEager")
		.marshal().json( JsonLibrary.Jackson, String.class)
		.convertBodyTo( String.class );
		
		from( CATALOG_UPD_EXTERNAL_SERVICESPEC )
		.log(LoggingLevel.INFO, log, CATALOG_UPD_EXTERNAL_SERVICESPEC + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.unmarshal()
		.json(JsonLibrary.Jackson, ServiceSpecification.class, true)		
		.bean( serviceSpecificationRepoService, "updateExternalServiceSpec(${header.servicespecid}, ${header.orgid}, ${body})")
		.marshal().json( JsonLibrary.Jackson, String.class)
		.convertBodyTo( String.class );
		
		from( CATALOG_ADD_SERVICESPEC )
		.log(LoggingLevel.INFO, log, CATALOG_ADD_SERVICESPEC + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.unmarshal().json( JsonLibrary.Jackson, ServiceSpecificationCreate .class, true)
		.bean( serviceSpecificationRepoService, "addServiceSpecification(${body})")
		.marshal().json( JsonLibrary.Jackson)
		.convertBodyTo( String.class );
				
		from( CATALOG_UPD_SERVICESPEC )
		.log(LoggingLevel.INFO, log, CATALOG_UPD_SERVICESPEC + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.unmarshal().json( JsonLibrary.Jackson, ServiceSpecificationUpdate.class, true)
		.bean( serviceSpecificationRepoService, "updateServiceSpecification(${header.serviceSpecId},  ${body} )")
		.marshal().json( JsonLibrary.Jackson)
		.convertBodyTo( String.class );

		from( CATALOG_UPDADD_SERVICESPEC )
		.log(LoggingLevel.INFO, log, CATALOG_UPD_SERVICESPEC + " message received!")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.unmarshal().json( JsonLibrary.Jackson, ServiceSpecificationUpdate.class, true)
		.bean( serviceSpecificationRepoService, "updateOrAddServiceSpecification(${header.serviceSpecId}, ${header.forceId}, ${body} )")
		.marshal().json( JsonLibrary.Jackson)
		.convertBodyTo( String.class );
		


        from( CATALOG_SEARCH_SERVICESPECREFS )
        .log(LoggingLevel.INFO, log, CATALOG_SEARCH_SERVICESPECREFS + " message received!")
        .to("log:DEBUG?showBody=true&showHeaders=true")
        .unmarshal().json( JsonLibrary.Jackson, ArrayList.class, true)
        .bean( serviceSpecificationRepoService, "searchServiceSpecRefs( ${body} )");
        
        
	}

	
	

	
	static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue( content, valueType);
    }
	
}
