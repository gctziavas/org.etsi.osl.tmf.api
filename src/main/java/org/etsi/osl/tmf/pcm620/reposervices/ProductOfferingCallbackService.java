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

import org.etsi.osl.tmf.pcm620.model.ProductOfferingCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingStateChangeEvent;
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
public class ProductOfferingCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(ProductOfferingCallbackService.class);

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send product offering create event to all registered callback URLs
     * @param productOfferingCreateEvent The product offering create event to send
     */
    public void sendProductOfferingCreateCallback(ProductOfferingCreateEvent productOfferingCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingCreateEvent")) {
                sendProductOfferingCreateEventToCallback(subscription.getCallback(), productOfferingCreateEvent);
            }
        }
    }

    /**
     * Send product offering delete event to all registered callback URLs
     * @param productOfferingDeleteEvent The product offering delete event to send
     */
    public void sendProductOfferingDeleteCallback(ProductOfferingDeleteEvent productOfferingDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingDeleteEvent")) {
                sendProductOfferingDeleteEventToCallback(subscription.getCallback(), productOfferingDeleteEvent);
            }
        }
    }

    /**
     * Send product offering attribute value change event to all registered callback URLs
     * @param productOfferingAttributeValueChangeEvent The product offering attribute value change event to send
     */
    public void sendProductOfferingAttributeValueChangeCallback(ProductOfferingAttributeValueChangeEvent productOfferingAttributeValueChangeEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingAttributeValueChangeEvent")) {
                sendProductOfferingAttributeValueChangeEventToCallback(subscription.getCallback(), productOfferingAttributeValueChangeEvent);
            }
        }
    }

    /**
     * Send product offering state change event to all registered callback URLs
     * @param productOfferingStateChangeEvent The product offering state change event to send
     */
    public void sendProductOfferingStateChangeCallback(ProductOfferingStateChangeEvent productOfferingStateChangeEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingStateChangeEvent")) {
                sendProductOfferingStateChangeEventToCallback(subscription.getCallback(), productOfferingStateChangeEvent);
            }
        }
    }

    /**
     * Send product offering create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering create event
     */
    private void sendProductOfferingCreateEventToCallback(String callbackUrl, ProductOfferingCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering delete event
     */
    private void sendProductOfferingDeleteEventToCallback(String callbackUrl, ProductOfferingDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering delete event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering attribute value change event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering attribute value change event
     */
    private void sendProductOfferingAttributeValueChangeEventToCallback(String callbackUrl, ProductOfferingAttributeValueChangeEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingAttributeValueChangeEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingAttributeValueChangeEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering attribute value change event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering attribute value change event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering state change event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering state change event
     */
    private void sendProductOfferingStateChangeEventToCallback(String callbackUrl, ProductOfferingStateChangeEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingStateChangeEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingStateChangeEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering state change event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering state change event to callback URL: {}", callbackUrl, e);
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
        return query.contains("productoffering") || 
               query.contains(eventType.toLowerCase()) ||
               query.contains("productoffering.create") ||
               query.contains("productoffering.delete") ||
               query.contains("productoffering.attributevaluechange") ||
               query.contains("productoffering.statechange");
    }
}