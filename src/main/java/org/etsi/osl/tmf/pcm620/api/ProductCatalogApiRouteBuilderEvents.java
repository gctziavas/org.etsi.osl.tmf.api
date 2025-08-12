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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etsi.osl.centrallog.client.CLevel;
import org.etsi.osl.centrallog.client.CentralLogger;
import org.etsi.osl.tmf.common.model.Notification;
import org.etsi.osl.tmf.pcm620.model.CatalogCreateNotification;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Component
public class ProductCatalogApiRouteBuilderEvents extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog(ProductCatalogApiRouteBuilderEvents.class.getName());

	@Value("${EVENT_PRODUCT_CATALOG_CREATE}")
	private String EVENT_CATALOG_CREATE = "direct:EVENT_CATALOG_CREATE";
	
	@Value("${EVENT_PRODUCT_CATALOG_DELETE}")
	private String EVENT_CATALOG_DELETE = "direct:EVENT_CATALOG_DELETE";

	@Value("${spring.application.name}")
	private String compname;
	
	@Autowired
	private ProducerTemplate template;

	@Autowired
	private CentralLogger centralLogger;

	@Override
	public void configure() throws Exception {
		// Configure routes for catalog events
	}

	/**
	 * Publish notification events for catalog operations
	 * @param n The notification to publish
	 * @param objId The catalog object ID
	 */
    @Transactional
	public void publishEvent(final Notification n, final String objId) {
		n.setEventType(n.getClass().getName());
		logger.info("will send Event for type " + n.getEventType());
		try {
			String msgtopic = "";
			
			if (n instanceof CatalogCreateNotification) {
				 msgtopic = EVENT_CATALOG_CREATE;
			} else if (n instanceof CatalogDeleteNotification) {
				 msgtopic = EVENT_CATALOG_DELETE;				
			}
			
			Map<String, Object> map = new HashMap<>();
			map.put("eventid", n.getEventId());
			map.put("objId", objId);
			
			String apayload = toJsonString(n);
			template.sendBodyAndHeaders(msgtopic, apayload, map);
			
			centralLogger.log(CLevel.INFO, apayload, compname);	

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot send Event . " + e.getMessage());
		}
	}

	static String toJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.writeValueAsString(object);
	}

	static <T> T toJsonObj(String content, Class<T> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(content, valueType);
    }
}