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

import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceCreateEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceDeleteEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceAttributeValueChangeEvent;
import org.etsi.osl.tmf.pcm620.model.ProductOfferingPriceStateChangeEvent;
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
public class ProductOfferingPriceCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(ProductOfferingPriceCallbackService.class);

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send product offering price create event to all registered callback URLs
     * @param productOfferingPriceCreateEvent The product offering price create event to send
     */
    public void sendProductOfferingPriceCreateCallback(ProductOfferingPriceCreateEvent productOfferingPriceCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingPriceCreateEvent")) {
                sendProductOfferingPriceCreateEventToCallback(subscription.getCallback(), productOfferingPriceCreateEvent);
            }
        }
    }

    /**
     * Send product offering price delete event to all registered callback URLs
     * @param productOfferingPriceDeleteEvent The product offering price delete event to send
     */
    public void sendProductOfferingPriceDeleteCallback(ProductOfferingPriceDeleteEvent productOfferingPriceDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingPriceDeleteEvent")) {
                sendProductOfferingPriceDeleteEventToCallback(subscription.getCallback(), productOfferingPriceDeleteEvent);
            }
        }
    }

    /**
     * Send product offering price attribute value change event to all registered callback URLs
     * @param productOfferingPriceAttributeValueChangeEvent The product offering price attribute value change event to send
     */
    public void sendProductOfferingPriceAttributeValueChangeCallback(ProductOfferingPriceAttributeValueChangeEvent productOfferingPriceAttributeValueChangeEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingPriceAttributeValueChangeEvent")) {
                sendProductOfferingPriceAttributeValueChangeEventToCallback(subscription.getCallback(), productOfferingPriceAttributeValueChangeEvent);
            }
        }
    }

    /**
     * Send product offering price state change event to all registered callback URLs
     * @param productOfferingPriceStateChangeEvent The product offering price state change event to send
     */
    public void sendProductOfferingPriceStateChangeCallback(ProductOfferingPriceStateChangeEvent productOfferingPriceStateChangeEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "productOfferingPriceStateChangeEvent")) {
                sendProductOfferingPriceStateChangeEventToCallback(subscription.getCallback(), productOfferingPriceStateChangeEvent);
            }
        }
    }

    /**
     * Send product offering price create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering price create event
     */
    private void sendProductOfferingPriceCreateEventToCallback(String callbackUrl, ProductOfferingPriceCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingPriceCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingPriceCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering price create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering price create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering price delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering price delete event
     */
    private void sendProductOfferingPriceDeleteEventToCallback(String callbackUrl, ProductOfferingPriceDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingPriceDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingPriceDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering price delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering price delete event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering price attribute value change event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering price attribute value change event
     */
    private void sendProductOfferingPriceAttributeValueChangeEventToCallback(String callbackUrl, ProductOfferingPriceAttributeValueChangeEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingPriceAttributeValueChangeEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingPriceAttributeValueChangeEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering price attribute value change event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering price attribute value change event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send product offering price state change event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The product offering price state change event
     */
    private void sendProductOfferingPriceStateChangeEventToCallback(String callbackUrl, ProductOfferingPriceStateChangeEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/productOfferingPriceStateChangeEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProductOfferingPriceStateChangeEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent product offering price state change event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send product offering price state change event to callback URL: {}", callbackUrl, e);
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
        return query.contains("productofferingprice") || 
               query.contains(eventType.toLowerCase()) ||
               query.contains("productofferingprice.create") ||
               query.contains("productofferingprice.delete") ||
               query.contains("productofferingprice.attributevaluechange") ||
               query.contains("productofferingprice.statechange");
    }
}