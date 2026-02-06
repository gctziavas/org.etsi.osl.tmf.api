/*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 - 2021 openslice.io
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
package org.etsi.osl.tmf.pcm620.reposervices;

import java.util.List;

import org.etsi.osl.tmf.pcm620.model.CatalogCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CatalogDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.EventSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CatalogCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogCallbackService.class);

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send catalog create event to all registered callback URLs
     * @param catalogCreateEvent The catalog create event to send
     */
    public void sendCatalogCreateCallback(CatalogCreateEvent catalogCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "catalogCreateEvent")) {
                sendCatalogCreateEventToCallback(subscription.getCallback(), catalogCreateEvent);
            }
        }
    }

    /**
     * Send catalog delete event to all registered callback URLs
     * @param catalogDeleteEvent The catalog delete event to send
     */
    public void sendCatalogDeleteCallback(CatalogDeleteEvent catalogDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "catalogDeleteEvent")) {
                sendCatalogDeleteEventToCallback(subscription.getCallback(), catalogDeleteEvent);
            }
        }
    }

    /**
     * Send catalog create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The catalog create event
     */
    private void sendCatalogCreateEventToCallback(String callbackUrl, CatalogCreateEvent event) {
      
        String url = buildCallbackUrl(callbackUrl, "/listener/catalogCreateEvent");
        try {
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatalogCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            if (response!=null) {
              logger.info("Successfully sent catalog create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());   
            } else {
              logger.error("category delete event to callback URL: {} - Response: IS NULL", 
                  url);
            }
            
        } catch (Exception e) {
            logger.error("Failed to send catalog create event to callback URL: {}", url, e);
        }
    }

    /**
     * Send catalog delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The catalog delete event
     */
    private void sendCatalogDeleteEventToCallback(String callbackUrl, CatalogDeleteEvent event) {
        String url = buildCallbackUrl(callbackUrl, "/listener/catalogDeleteEvent");
        try {
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CatalogDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            

            if (response!=null) {
              logger.info("Successfully sent catalog delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());        
            } else {
              logger.error("catalog delete event to callback URL: {} - Response: IS NULL", 
                  url);
            }
            
            
        } catch (Exception e) {
            logger.error("Failed to send catalog delete event to callback URL: {}", url, e);
        }
    }

    /**
     * Build the full callback URL with the listener endpoint
     * @param baseUrl The base callback URL
     * @param listenerPath The listener path to append
     * @return The complete callback URL
     */
    private String buildCallbackUrl(String baseUrl, String listenerPath) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + listenerPath;
        } else {
            return baseUrl + listenerPath;
        }
    }

    /**
     * Check if a subscription should be notified for a specific event type
     * @param subscription The event subscription
     * @param eventType The event type to check
     * @return true if the subscription should be notified
     */
    private boolean shouldNotifySubscription(EventSubscription subscription, String eventType) {
        // If no query is specified, notify all events
        if (subscription.getQuery() == null || subscription.getQuery().trim().isEmpty()) {
            return true;
        }
        
        // Check if the query contains the event type
        String query = subscription.getQuery().toLowerCase();
        return query.contains("catalog") || 
               query.contains(eventType.toLowerCase()) ||
               query.contains("catalog.create") ||
               query.contains("catalog.delete");
    }
}