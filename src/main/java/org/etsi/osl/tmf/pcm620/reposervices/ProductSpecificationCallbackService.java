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

import org.etsi.osl.tmf.pcm620.model.ProductSpecificationCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductSpecificationDeleteEvent;
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
public class ProductSpecificationCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(ProductSpecificationCallbackService.class);

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send product specification create event to all registered callback URLs
     * @param productSpecificationCreateEvent The product specification create event to send
     */
    public void sendProductSpecificationCreateCallback(ProductSpecificationCreateEvent productSpecificationCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productSpecificationCreateEvent")) {
                sendProductSpecificationCreateEventToCallback(subscription.getCallback(), productSpecificationCreateEvent);
            }
        }
    }

    /**
     * Send product specification delete event to all registered callback URLs
     * @param productSpecificationDeleteEvent The product specification delete event to send
     */
    public void sendProductSpecificationDeleteCallback(ProductSpecificationDeleteEvent productSpecificationDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productSpecificationDeleteEvent")) {
                sendProductSpecificationDeleteEventToCallback(subscription.getCallback(), productSpecificationDeleteEvent);
            }
        }
    }

    /**
     * Send product specification create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product specification create event
     */
    private void sendProductSpecificationCreateEventToCallback(String callbackUrl, ProductSpecificationCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productSpecificationCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductSpecificationCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            if (response!=null) {
            logger.info("Successfully sent product specification create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            } else {
              logger.error("product create event to callback URL: {} - Response: IS NULL", 
                  url);
            }
            
        } catch (Exception e) {
            logger.error("Failed to send product specification create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product specification delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product specification delete event
     */
    private void sendProductSpecificationDeleteEventToCallback(String callbackUrl, ProductSpecificationDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productSpecificationDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductSpecificationDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

            if (response!=null) {
            logger.info("Successfully sent product specification delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            } else {
              logger.error("product delete event to callback URL: {} - Response: IS NULL", 
                  url);
            }
            
        } catch (Exception e) {
            logger.error("Failed to send product specification delete event to callback URL: {}", callbackUrl, e);
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
        return query.contains("productspecification") || 
               query.contains(eventType.toLowerCase()) ||
               query.contains("productspecification.create") ||
               query.contains("productspecification.delete");
    }
}