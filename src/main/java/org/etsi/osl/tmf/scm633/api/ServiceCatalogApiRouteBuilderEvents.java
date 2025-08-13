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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CLevel;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCatalogDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceCategoryDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationCreateNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationDeleteNotification;
import org.etsi.osl.tmf.scm633.model.ServiceSpecificationChangeNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceCatalogApiRouteBuilderEvents extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog(ServiceCatalogApiRouteBuilderEvents.class.getName());

	@Value("${EVENT_SERVICE_CATALOG_CREATE}")
	private String EVENT_SERVICE_CATALOG_CREATE = "direct:EVENT_SERVICE_CATALOG_CREATE";
	
	@Value("${EVENT_SERVICE_CATALOG_DELETE}")
	private String EVENT_SERVICE_CATALOG_DELETE = "direct:EVENT_SERVICE_CATALOG_DELETE";

	@Value("${EVENT_SERVICE_CATEGORY_CREATE}")
	private String EVENT_SERVICE_CATEGORY_CREATE = "direct:EVENT_SERVICE_CATEGORY_CREATE";
	
	@Value("${EVENT_SERVICE_CATEGORY_DELETE}")
	private String EVENT_SERVICE_CATEGORY_DELETE = "direct:EVENT_SERVICE_CATEGORY_DELETE";

	@Value("${EVENT_SERVICE_SPECIFICATION_CREATE}")
	private String EVENT_SERVICE_SPECIFICATION_CREATE = "direct:EVENT_SERVICE_SPECIFICATION_CREATE";
	
	@Value("${EVENT_SERVICE_SPECIFICATION_DELETE}")
	private String EVENT_SERVICE_SPECIFICATION_DELETE = "direct:EVENT_SERVICE_SPECIFICATION_DELETE";

	@Value("${EVENT_SERVICE_SPECIFICATION_CHANGE}")
	private String EVENT_SERVICE_SPECIFICATION_CHANGE = "direct:EVENT_SERVICE_SPECIFICATION_CHANGE";

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ProducerTemplate template;


    @Autowired
    private CentralLogger centralLogger;
    
	@Override
	public void configure() throws Exception {
		
	}

	public void publishEvent(Object notification, String objId) {

		try {
			String msgtopic = "";
			
			if (notification instanceof ServiceCatalogCreateNotification) {
				 msgtopic = EVENT_SERVICE_CATALOG_CREATE;
			} else if (notification instanceof ServiceCatalogDeleteNotification) {
				 msgtopic = EVENT_SERVICE_CATALOG_DELETE;
			} else if (notification instanceof ServiceCategoryCreateNotification) {
				 msgtopic = EVENT_SERVICE_CATEGORY_CREATE;
			} else if (notification instanceof ServiceCategoryDeleteNotification) {
				 msgtopic = EVENT_SERVICE_CATEGORY_DELETE;
			} else if (notification instanceof ServiceSpecificationCreateNotification) {
				 msgtopic = EVENT_SERVICE_SPECIFICATION_CREATE;
			} else if (notification instanceof ServiceSpecificationDeleteNotification) {
				 msgtopic = EVENT_SERVICE_SPECIFICATION_DELETE;
			} else if (notification instanceof ServiceSpecificationChangeNotification) {
				 msgtopic = EVENT_SERVICE_SPECIFICATION_CHANGE;
			}
			
			Map<String, Object> map = new HashMap<>();
			String eventId = null;
			
			if (notification instanceof ServiceCatalogCreateNotification) {
				eventId = ((ServiceCatalogCreateNotification) notification).getEventId();
			} else if (notification instanceof ServiceCatalogDeleteNotification) {
				eventId = ((ServiceCatalogDeleteNotification) notification).getEventId();
			} else if (notification instanceof ServiceCategoryCreateNotification) {
				eventId = ((ServiceCategoryCreateNotification) notification).getEventId();
			} else if (notification instanceof ServiceCategoryDeleteNotification) {
				eventId = ((ServiceCategoryDeleteNotification) notification).getEventId();
			} else if (notification instanceof ServiceSpecificationCreateNotification) {
				eventId = ((ServiceSpecificationCreateNotification) notification).getEventId();
			} else if (notification instanceof ServiceSpecificationDeleteNotification) {
				eventId = ((ServiceSpecificationDeleteNotification) notification).getEventId();
			} else if (notification instanceof ServiceSpecificationChangeNotification) {
				eventId = ((ServiceSpecificationChangeNotification) notification).getEventId();
			}
			
			map.put("eventid", eventId);
			map.put("objId", objId);
			
			String apayload = toJsonString(notification);
			map.put("event", apayload);
			
			template.sendBodyAndHeaders(msgtopic, apayload, map);

			String msgtxt = "EVENT " + notification.getClass().getName() + " sent";
			logger.info(msgtxt);
			centralLogger.log(CLevel.INFO, msgtxt, compname());

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot send Event . " + e.getMessage());
		}
	}

	static String toJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.writeValueAsString(object);
	}

	static <T> T toJsonObj(String content, Class<T> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(content, valueType);
    }
	
	private String compname() {
		return "ServiceCatalogApiRouteBuilderEvents";
	}
}