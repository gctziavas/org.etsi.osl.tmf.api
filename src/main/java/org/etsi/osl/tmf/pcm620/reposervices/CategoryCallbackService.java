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

import org.etsi.osl.tmf.pcm620.model.CategoryCreateEvent;
import org.etsi.osl.tmf.pcm620.model.CategoryDeleteEvent;
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
public class CategoryCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryCallbackService.class);

    @Autowired
    private EventSubscriptionRepoService eventSubscriptionRepoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send category create event to all registered callback URLs
     * @param categoryCreateEvent The category create event to send
     */
    public void sendCategoryCreateCallback(CategoryCreateEvent categoryCreateEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "categoryCreateEvent")) {
                sendCategoryCreateEventToCallback(subscription.getCallback(), categoryCreateEvent);
            }
        }
    }

    /**
     * Send category delete event to all registered callback URLs
     * @param categoryDeleteEvent The category delete event to send
     */
    public void sendCategoryDeleteCallback(CategoryDeleteEvent categoryDeleteEvent) {
        List<EventSubscription> subscriptions = eventSubscriptionRepoService.findAll();
        
        for (EventSubscription subscription : subscriptions) {
            if (shouldNotifySubscription(subscription, "categoryDeleteEvent")) {
                sendCategoryDeleteEventToCallback(subscription.getCallback(), categoryDeleteEvent);
            }
        }
    }

    /**
     * Send category create event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The category create event
     */
    private void sendCategoryCreateEventToCallback(String callbackUrl, CategoryCreateEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/categoryCreateEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CategoryCreateEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent category create event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send category create event to callback URL: {}", callbackUrl, e);
        }
    }

    /**
     * Send category delete event to a specific callback URL
     * @param callbackUrl The callback URL to send to
     * @param event The category delete event
     */
    private void sendCategoryDeleteEventToCallback(String callbackUrl, CategoryDeleteEvent event) {
        try {
            String url = buildCallbackUrl(callbackUrl, "/listener/categoryDeleteEvent");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CategoryDeleteEvent> entity = new HttpEntity<>(event, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            logger.info("Successfully sent category delete event to callback URL: {} - Response: {}", 
                url, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Failed to send category delete event to callback URL: {}", callbackUrl, e);
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
        return query.contains("category") || 
               query.contains(eventType.toLowerCase()) ||
               query.contains("category.create") ||
               query.contains("category.delete");
    }
}